package mx.com.audioweb.indigo.TimeTracker.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mx.com.audioweb.indigo.R;
import mx.com.audioweb.indigo.TimeTracker.api.CONFIG;
import mx.com.audioweb.indigo.TimeTracker.api.GpsTracker;
import mx.com.audioweb.indigo.TimeTracker.api.MapBuilder;
import mx.com.audioweb.indigo.TimeTracker.api.RestService;

@SuppressLint("SetJavaScriptEnabled")
public class MapWebviewActivity extends Activity {

    private final String filename = "MySampleFile.html";
    WebView myBrowser;
    SharedPreferences myPrefs;
    String getUserName, map = " ", webURL, addressLIne, addressline;
    double stringLatitude, stringLongitude;
    GpsTracker gpsTracker;
    File myInternalFile;
    ProgressDialog progress;
    boolean loadingFinished = true, redirect = false;
    JSONArray jArray;
    JSONObject json;
    TextView info;
    List<LatLng> allPoints;

    AsyncTask<String, String, String> getLocationDetailTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(MapWebviewActivity.this, "a4c94239");
        setContentView(R.layout.mapwebview);
        myBrowser = (WebView) findViewById(R.id.mybrowser);
        myPrefs = PreferenceManager.getDefaultSharedPreferences(MapWebviewActivity.this);
        getUserName = myPrefs.getString("User Name", "");

        if (!CONFIG.isNetworkAvailable(MapWebviewActivity.this)) {
            Toast.makeText(MapWebviewActivity.this, "Check your internet connection",
                    Toast.LENGTH_LONG).show();
        }
        WebSettings settings = myBrowser.getSettings();

        json = new JSONObject();
        gpsTracker = new GpsTracker(this);

        webURL = CONFIG.SERVER_URL + "location/" + getUserName;
        Log.e("URI", webURL);

        progress = ProgressDialog.show(MapWebviewActivity.this, "Please Wait", "Refreshing");
        progress.setCancelable(true);
        myInternalFile = new File(filename);
        //File sdcard = Environment.getExternalStorageDirectory().getAbsoluteFile();


        myBrowser.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progress.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (progress.isShowing()) {
                    progress.dismiss();
                }
            }
        });

        setCurrentLocation();
        //load saved file

        myBrowser.getSettings().setJavaScriptEnabled(true);
        myBrowser.getSettings().setBuiltInZoomControls(true);
        myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        settings.getJavaScriptEnabled();
        settings.getBuiltInZoomControls();
        settings.setBuiltInZoomControls(true);
        getUserName = myPrefs.getString("User Name", "");

    }


    public void setCurrentLocation() {
        getLocationDetailTask = new AsyncTask<String, String, String>() {
            HttpResponse result;
            JSONObject json = new JSONObject();

            @Override
            protected String doInBackground(String... params) {
                // TODO Auto-generated method stub
                try {
                    result = RestService.doGet(webURL);
                    json = RestService.JSONFormResponse(result);
                    try {
                        jArray = new JSONArray();
                        jArray = json.getJSONArray("data");
                        allPoints = new ArrayList<LatLng>();
                        Log.e("Get Location Detail", jArray.toString());
                        List<Address> allLocation = null;
                        for (int i = 0; i < jArray.length(); i++) {

                            JSONObject jObject = jArray.getJSONObject(i);
                            final Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.ENGLISH);
                            try {
                                if ((jObject.getDouble("latitude") != 0) && (jObject.getDouble("longitude") != 0)) {

                                    stringLatitude = jObject.getDouble("latitude");
                                    stringLongitude = jObject.getDouble("longitude");
                                }
                                if ((stringLatitude != 0) && (stringLongitude != 0)) {
                                    // Storing each json item in variable
                                    allLocation = new ArrayList<Address>();
                                    addressline = CONFIG.getCurrentLocationViaJSON(stringLatitude, stringLongitude);
                                    ;
                                    Log.e("All LOCATION", addressline);
                                    allPoints.add(new LatLng(stringLatitude, stringLongitude));
                                    Log.e("All point size", "" + allPoints.size());
                                    Log.e("LOCATION", "" + i);
                                }
                                map += createMarker(stringLatitude, stringLongitude, addressline, i);

                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        try {
                            //Write data for web view and store in internal storage

                            //+ createNavigationPath(allPoints);
                            FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
                            //fos = new FileOutputStream(myInternalFile);
                            fos.write((MapBuilder.map_content + map + createNavigationPath(allPoints) + MapBuilder.map_content2).getBytes());
                            fos.close();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.e("HTML FILE", MapBuilder.map_content + map + createNavigationPath(allPoints) + MapBuilder.map_content2);
                        Log.e("PAth", getFilesDir().toString());
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    return null;
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String json) {
                int code = result.getStatusLine().getStatusCode();
                String address = "";
//				Log.e("Get Current Location Detail",json.toString());
                if (code == 200) {
                    Log.e("Map view", "successfully done");
                    myBrowser.loadUrl("file:///" + getFilesDir() + "/MySampleFile.html");
//					
                } else {
                    Log.e("Map view", "server error");
                }
            }
        };
        try {
            getLocationDetailTask.execute().get();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String createMarker(double lati, double longi, String addressline2, int i) {

        String marker1 = "var myLatlng" + i + " = new google.maps.LatLng(" + lati + ", " + longi + "); "
                + "var mapOptions" + i + " = {zoom: 11,center: myLatlng" + i + ",mapTypeId: google.maps.MapTypeId.ROADMAP  };";

        if (i == 0) {
            marker1 += "map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions" + 0 + ");";
        }

        String marker2 = "var contentString" + 0 + "= '<div id=" + "\"content\"" + ">'+"
                + "'<div id=" + "\"siteNotice\"" + ">'+'</div>'+'<h4 id=" + "\"firstHeading\"" + " class=" + "\"firstHeading\"" + ">" + addressline2 + "</h4>'+ '</div>';"
                + "var infowindow" + i + " = new google.maps.InfoWindow({content: contentString" + 0 + "});"
                + "var marker" + i + " = new google.maps.Marker({position: myLatlng" + i + ",map: map,animation: google.maps.Animation.DROP});"
                + "google.maps.event.addListener(marker" + i + ", 'click', function() {infowindow" + i + ".open(map,marker" + i + ");});\n";

        String marker3 =
                "google.maps.event.addListener(marker, 'click', function() {infowindow.open(map,marker);});";

        return marker1 + marker2;
    }

    public String createNavigationPath(List<LatLng> allPoints) {
        String path = "var allPoints = [";
        for (int j = 0; j < allPoints.size(); j++) {
            if (j == (allPoints.size() - 1)) {

                path += "new google.maps.LatLng(" + allPoints.get(j).latitude + "," + allPoints.get(j).longitude + ")";
            } else {
                path += "new google.maps.LatLng(" + allPoints.get(j).latitude + "," + allPoints.get(j).longitude + "),";
            }
        }
        path += " ]; var flightPath = new google.maps.Polyline"
                + "({path: allPoints,geodesic: true,strokeColor: '#FF0000',strokeOpacity: 1.0,strokeWeight: 2});flightPath.setMap(map);";
        return path;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //	setCurrentLocation();
    }
}

