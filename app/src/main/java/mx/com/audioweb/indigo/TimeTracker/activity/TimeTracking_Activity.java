package mx.com.audioweb.indigo.TimeTracker.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

import org.json.JSONObject;

import mx.com.audioweb.indigo.R;
import mx.com.audioweb.indigo.TimeTracker.Shared_notifications;
import mx.com.audioweb.indigo.TimeTracker.api.CONFIG;
import mx.com.audioweb.indigo.TimeTracker.api.GpsTracker;
import mx.com.audioweb.indigo.TimeTracker.task.SalesCurrentLocationTask;
import mx.com.audioweb.indigo.TimeTracker.task.VoiceAuthenticationTask;

/**
 * Created by Juan Acosta on 10/14/2014.
 */

public class TimeTracking_Activity extends Activity {

    private static Context mContext;
    /**
     * Instance of our library
     */

    protected boolean doubleBackToExitPressedOnce = false;
    NotificationManager mNotificationManager;
    SharedPreferences myPrefs;
    JSONObject json;
    int result;
    boolean isRegistered, isAuth;
    SharedPreferences.Editor edit;
    GpsTracker gpsTracker;
    CheckBox notificacion;
    double stringLatitude, stringLongitude;
    SharedPreferences preferences;
    Shared_notifications session;
    private String authVoiceID, getUserName, webURL, voice_id;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(TimeTracking_Activity.this, "a4c94239");
        setContentView(R.layout.activity_timetracker);
        mContext = TimeTracking_Activity.this;
        myPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        notificacion = (CheckBox) findViewById(R.id.checknotification);


        if (myPrefs.getBoolean("isVoiceAuthenticated", false)) {
            Intent mainIntent = new Intent(mContext, ActivityUserDetailScreen.class);
            startActivity(mainIntent);
            finish();
        }

        gpsTracker = new GpsTracker(mContext);
        if (!gpsTracker.canGetLocation()) {
            gpsTracker.showSettingsAlert();

        }
        getUserName = myPrefs.getString("User Name", "");
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancel(999);
        json = new JSONObject();
        voice_id = myPrefs.getString("Voice_Id", "");
        webURL = CONFIG.SERVER_URL + "salesmen/authentication";
        edit = myPrefs.edit();
        isRegistered = myPrefs.getBoolean("isVoiceRegistered", false);
        session = new Shared_notifications(mContext);

        //if(isRegistered){
        findViewById(R.id.auth_button).setEnabled(true);
        Button login = (Button) findViewById(R.id.auth_button);
        login.setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_button));
    }

    /**
     * Get the result from the registration
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 999) {
            if (resultCode == RESULT_OK) {

                try {
                    json.put("username", getUserName);
                    json.put("voice_auth_id", voice_id);

                    Log.e("Logon json", json.toString());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                VoiceAuthenticationTask authtask = new VoiceAuthenticationTask(mContext, json);
                try {
                    if (!CONFIG.isNetworkAvailable(mContext)) {
                        Toast.makeText(mContext, "Check your internet connection", Toast.LENGTH_LONG).show();
                    } else {
                        result = authtask.execute(webURL).get();
                        Log.e("Result", "" + result);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (result == 200) {
                    isAuth = true;
                    edit.putBoolean("is_auhtentication", true);
                    edit.commit();
                }
                finish();
            }
        }
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.checknotification:
                session.RestartNotification();
                if (checked) {
                    session.createNotification();

                } else {
                    session.clearNotification();

                }
                // Remove the meat
                break;

        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        this.onCreate(null);

    }

    /**
     * Start the login process
     *
     * @param v button
     */
    public void startAuthenticateClick(View v) {
        if (!CONFIG.isNetworkAvailable(mContext)) {
            Toast.makeText(getApplicationContext(), "Check your internet connection", Toast.LENGTH_LONG).show();
        } else {

            if (gpsTracker.canGetLocation()) {
                try {
                    stringLatitude = gpsTracker.getLatitude();
                    stringLongitude = gpsTracker.getLongitude();


                    if (stringLatitude != 0 && stringLongitude != 0) {
                        json.put("username", getUserName);
                        json.put("latitude", stringLatitude);
                        json.put("longitude", stringLongitude);
                        json.put("is_auth", 1);
                        Log.e("json data", json.toString());
                        SalesCurrentLocationTask login = new SalesCurrentLocationTask(mContext, json);
                        login.execute(CONFIG.SERVER_URL + "location");

                        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences.Editor edit = preferences.edit();
                        edit.putBoolean("isVoiceAuthenticated", true);
                        edit.putInt("tryAgainCount", 0);
                        //edit.putBoolean("tryExceed", true);

                        edit.commit();
                        Intent returnIntent = new Intent();
                        setResult(RESULT_OK, returnIntent);
                        startActivity(new Intent(mContext, ActivityUserDetailScreen.class));
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
