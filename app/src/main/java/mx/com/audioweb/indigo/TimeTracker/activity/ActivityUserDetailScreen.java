// Copyright (c) 2014, VoiceVault Inc.

// All rights reserved.

// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:

//  * Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.

//  * Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.

//  * Neither the name of VoiceVault nor the names of its contributors
//    may be used to endorse or promote products derived from this
//    software without specific prior written permission.

//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL VOICEVAULT BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
// OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
// LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
// OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package mx.com.audioweb.indigo.TimeTracker.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mx.com.audioweb.indigo.R;
import mx.com.audioweb.indigo.TimeTracker.api.CONFIG;
import mx.com.audioweb.indigo.TimeTracker.api.GpsTracker;
import mx.com.audioweb.indigo.TimeTracker.api.MainService;
import mx.com.audioweb.indigo.TimeTracker.api.RestService;
import mx.com.audioweb.indigo.TimeTracker.api.RestartServiceReceiver;

public class ActivityUserDetailScreen extends Activity {

    final int RQS_GooglePlayServices = 1;
    protected boolean doubleBackToExitPressedOnce = false;
    TextView userName, name, time, cLocation, phone, email;
    Button showMap, showAuthenticate;
    GpsTracker gpsTracker;
    List<Address> currentLocation;
    Context mContext;
    JSONObject jObject;
    ProgressDialog progress;
    SharedPreferences myPrefs;
    JSONArray jArray, jLocArray;
    JSONObject json;
    String webURLUser, webURLLocation, authVoiceID = "", date_time, current_time, addressLine, getUserName;
    double stringLatitude, stringLongitude, latitude, longitude;
    AsyncTask<String, String, JSONObject> getUserDetailTask;
    AsyncTask<String, String, JSONObject> getLocationDetailTask;
    AsyncTask<String, String, String> getLocPlayServiceTask;
    Intent servintent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(ActivityUserDetailScreen.this, "a4c94239");
        mContext = ActivityUserDetailScreen.this;
        servintent = new Intent(mContext, MainService.class);
        setContentView(R.layout.user_detail_screen);
        getActionBar().setDisplayShowCustomEnabled(true);
        startService(servintent);

		/*if(isMyServiceRunning(MainService.class)){
            Log.e("UserDetailScreen", "Service is already running");

            startService(servintent);

		}else{
			startService(new Intent(mContext , MainService.class));
			Log.e("UserDetailScreen", "Service started first time");
		}*/

        gpsTracker = new GpsTracker(mContext);
        json = new JSONObject();
        myPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        getUserName = myPrefs.getString("User Name", "");
        authVoiceID = myPrefs.getString("CLAIMANT_ID", "");
        if (!CONFIG.isNetworkAvailable(mContext)) {
            Toast.makeText(mContext,
                    "Check your internet connection", Toast.LENGTH_LONG)
                    .show();
        }
        progress = ProgressDialog.show(mContext, "Please Wait", "Refreshing");
        progress.setCanceledOnTouchOutside(true);
        currentLocation = new ArrayList<Address>();
        userName = (TextView) findViewById(R.id.txtlargeuser);
        name = (TextView) findViewById(R.id.txtlargename);
        time = (TextView) findViewById(R.id.txtlargetime);
        cLocation = (TextView) findViewById(R.id.txtlargeloc);
        phone = (TextView) findViewById(R.id.txtlargephone);
        email = (TextView) findViewById(R.id.txtlargeemail);
        webURLUser = CONFIG.SERVER_URL + "salesmen/" + getUserName;
        showMap = (Button) findViewById(R.id.btnmap);
        showMap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!CONFIG.isNetworkAvailable(mContext)) {
                    Toast.makeText(mContext,
                            "Check your internet connection", Toast.LENGTH_LONG).show();
                } else {
                    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
					/*if (resultCode == ConnectionResult.SUCCESS){
						//if installed then map view opened	                
						Intent typeIntent = new Intent(mContext, ActivityMapScreen.class);
						startActivity(typeIntent); 
					} */
                    //else  {
                    Intent typeIntent = new Intent(mContext, MapWebviewActivity.class);
                    //Intent typeIntent = new Intent(mContext, ActivityMapScreen.class);
                    startActivity(typeIntent);
                    //}
                }
            }
        });
        current_time = getCurrentTime();
        scheduleAlarm();
        if (gpsTracker.canGetLocation()) {
            setCurrentLocation();
            getUserDetail();
        } else {
            gpsTracker.showSettingsAlert();
        }

        showAuthenticate = (Button) findViewById(R.id.authenticateBtn);
        showAuthenticate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Editor edit = myPrefs.edit();
                edit.putBoolean("isVoiceAuthenticated", false);
                edit.commit();
                startActivity(new Intent(ActivityUserDetailScreen.this, TimeTracking_Activity.class));
                finish();
            }
        });
    }

    private void scheduleAlarm() {
        // TODO Auto-generated method stub
        Calendar calendar = Calendar.getInstance();
        //set Hour of day to 12 so alarm will triggered everyday on 12 PM
        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long totaltime = calendar.getTimeInMillis();
        Intent intent = new Intent(mContext, RestartServiceReceiver.class);
        PendingIntent pintent = PendingIntent.getService(mContext, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, totaltime, AlarmManager.INTERVAL_DAY, pintent);
        //Toast.makeText(ActivityUserDetailScreen.this, "Service started", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        gpsTracker = new GpsTracker(mContext);
        setCurrentLocation();
        getUserDetail();
    }

    public void setCurrentLocation() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (resultCode == ConnectionResult.SUCCESS) {
            if (gpsTracker.canGetLocation()) {
                stringLatitude = gpsTracker.getLatitude();
                stringLongitude = gpsTracker.getLongitude();
                final Geocoder geocoder = new Geocoder(mContext, Locale.ENGLISH);
                try {
                    if (!CONFIG.isNetworkAvailable(mContext)) {
                        Toast.makeText(mContext,
                                "Check your internet connection", Toast.LENGTH_LONG)
                                .show();
                    } else {
                        currentLocation = geocoder.getFromLocation(stringLatitude, stringLongitude, 1);
                        if ((currentLocation != null) && (currentLocation.size() > 0)) {
                            Address address = currentLocation.get(0);
                            addressLine = address.getAddressLine(0) + "\n"
                                    + address.getAddressLine(1) + "\n"
                                    + address.getAddressLine(2);
                            String currentTime = getCurrentTime();
                            cLocation.setText(addressLine);
                            time.setText(currentTime);
                        }
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else {
            if (gpsTracker.canGetLocation()) {

                //	webURLLocation = CONFIG.SERVER_URL + "location/" + getUserName;
                getLocPlayServiceTask = new AsyncTask<String, String, String>() {
                    HttpResponse result;
                    //JSONObject json = new JSONObject();

                    @Override
                    protected String doInBackground(String... params) {
                        // TODO Auto-generated method stub
                        try {
                            stringLatitude = gpsTracker.getLatitude();
                            stringLongitude = gpsTracker.getLongitude();

                            return addressLine = CONFIG.getCurrentLocationViaJSON(stringLatitude, stringLongitude);

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(String json) {
                        Log.e("Json Play", "" + json);
                        //						int code = result.getStatusLine().getStatusCode();
                        if (!json.isEmpty()) {
                            try {
                                String currentTime = getCurrentTime();
                                cLocation.setText(addressLine);
                                time.setText(currentTime);
                            } catch (Exception e) {
                                // TODO: handle exception
                                e.printStackTrace();
                                //Toast.makeText(getApplicationContext(), "You Don't Internet Connectivity", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                };
                if (!CONFIG.isNetworkAvailable(mContext)) {
                    Toast.makeText(mContext,
                            "Check your internet connection", Toast.LENGTH_LONG)
                            .show();
                } else {
                    getLocPlayServiceTask.execute();
                }
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
	/*@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				mContext);

		// set title
		alertDialogBuilder.setTitle("Exit");

		// set dialog message
		alertDialogBuilder
		.setMessage("Do you really want to exit?")
		.setCancelable(false)
		.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked, close
				// current activity
				moveTaskToBack(true);
			}
		})
		.setNegativeButton("No",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked, just close
				// the dialog box and do nothing
				dialog.cancel();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}	*/

    public void getUserDetail() {
        getUserDetailTask = new AsyncTask<String, String, JSONObject>() {
            HttpResponse result;

            @Override
            protected void onPreExecute() {
                // TODO Auto-generated method stub
                super.onPreExecute();
                progress.show();
            }

            @Override
            protected JSONObject doInBackground(String... params) {
                // TODO Auto-generated method stub
                try {
                    result = RestService.doGet(webURLUser);
                    return RestService.JSONFormResponse(result);
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject json) {
                int code = 0;
                try {
                    code = result.getStatusLine().getStatusCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Log.e("Get User Detail",json.toString());
                if (code == 200) {

                    try {
                        jArray = new JSONArray();

                        jArray = json.getJSONArray("data");

                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject jObject = jArray.getJSONObject(i);
                            // Storing each json item in variable
                            userName.setText(jObject.getString("username"));
                            name.setText(jObject.getString("first_name") + " " + (jObject.getString("last_name")));
                            phone.setText(jObject.getString("smen_ph_no"));
                            email.setText(jObject.getString("smen_email_id"));

                        }
                        progress.dismiss();
                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                }
            }
        };
        if (!CONFIG.isNetworkAvailable(mContext)) {
            Toast.makeText(mContext,
                    "Check your internet connection", Toast.LENGTH_LONG)
                    .show();
        } else {
            getUserDetailTask.execute();
        }
    }

    public String getCurrentTime() {
        Date now = new Date();

        SimpleDateFormat df = new SimpleDateFormat("kk:mm:ss", Locale.US);
        return df.format(now);
        //	return now.getHours() + ":" + now.getMinutes() ;

    }
}
