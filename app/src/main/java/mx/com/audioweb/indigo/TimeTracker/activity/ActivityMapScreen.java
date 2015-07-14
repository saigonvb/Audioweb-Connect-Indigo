package mx.com.audioweb.indigo.TimeTracker.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mx.com.audioweb.indigo.R;
import mx.com.audioweb.indigo.TimeTracker.api.CONFIG;
import mx.com.audioweb.indigo.TimeTracker.api.GpsTracker;
import mx.com.audioweb.indigo.TimeTracker.api.RestService;

public class ActivityMapScreen extends Activity {

    GpsTracker gpsTracker;
    Context mContext;
    String addressLine, getUserName, webURL;
    double stringLatitude, stringLongitude;
    List<LatLng> allPoints;
    LatLng points;
    SharedPreferences myPrefs;
    JSONArray jArray;
    JSONObject json;
    TextView info;
    double lat, lng;
    ProgressDialog dialog;
    AsyncTask<String, String, JSONObject> getLocationDetailTask;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(ActivityMapScreen.this, "a4c94239");
        setContentView(R.layout.map_screen);
        mContext = ActivityMapScreen.this;
        allPoints = new ArrayList<LatLng>();
        gpsTracker = new GpsTracker(mContext);
        myPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        getUserName = myPrefs.getString("User Name", "");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        dialog = new ProgressDialog(mContext);

        jArray = new JSONArray();
        json = new JSONObject();

        webURL = CONFIG.SERVER_URL + "location/" + getUserName;
        if (!CONFIG.isNetworkAvailable(mContext)) {
            Toast.makeText(mContext, "Check your internet connection", Toast.LENGTH_LONG).show();
        }
        try {
            initilizeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initilizeMap() {
        if (gpsTracker.canGetLocation()) {
            setCurrentLocation();
        } else {
            gpsTracker.showSettingsAlert();
        }
    }

    private void drawMarker(double latitude, double longitude, final String address) {
        MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).snippet(address);

        // ROSE color icon
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_user));
        // adding marker
        googleMap.addMarker(marker);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(12).build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        gpsTracker = new GpsTracker(this);
        setCurrentLocation();
    }

    public void setCurrentLocation() {

        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        googleMap.setMyLocationEnabled(true);
        getLocationDetailTask = new AsyncTask<String, String, JSONObject>() {
            HttpResponse result;

            @Override
            protected void onPreExecute() {
                dialog = ProgressDialog.show(mContext, "Map is loading,", "Please wait...", true);
            }

            @Override
            protected JSONObject doInBackground(String... params) {
                // TODO Auto-generated method stub
                try {
                    result = RestService.doGet(webURL);
                    return RestService.JSONFormResponse(result);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject json) {
                List<Address> allLocation;
                allLocation = new ArrayList<Address>();
                int code = result.getStatusLine().getStatusCode();
                //	Log.e("Get Current Location Detail Detail",json.toString());
                if (code == 200) {
                    //dialog.dismiss();
                    try {
                        jArray = new JSONArray();
                        jArray = json.getJSONArray("data");
                        //Log.e("Get Location Detail",jArray.toString());

                        for (int i = 0; i < jArray.length(); i++) {
                            Log.e("length", "" + jArray.length());

                            JSONObject jObject = jArray.getJSONObject(i);
                            final Geocoder geocoder = new Geocoder(mContext, Locale.ENGLISH);
                            try {
                                if ((jObject.getDouble("latitude") != 0) && (jObject.getDouble("longitude") != 0)) {

                                    stringLatitude = jObject.getDouble("latitude");
                                    stringLongitude = jObject.getDouble("longitude");
                                }
                                if ((stringLatitude != 0) && (stringLongitude != 0)) {
                                    // Storing each json item in variable

                                    allLocation = geocoder.getFromLocation(stringLatitude, stringLongitude, 1);
                                    Log.e("All LOCATION", allLocation.toString());
                                    Log.e("LOCATION", "" + i);
                                    allPoints.add(new LatLng(stringLatitude, stringLongitude));
                                    Log.e("All Points", allPoints.toString());
                                } else {
                                    Log.e("LatLng", "" + stringLatitude + "," + stringLongitude);
                                }
                                Polyline route = googleMap.addPolyline(new PolylineOptions()
                                        .width(5)
                                        .geodesic(true)
                                        .color(Color.RED));
                                //route.setPoints(allPoints);
                                List<LatLng> points = new ArrayList<LatLng>();
                                points = route.getPoints();
                                for (int j = 0; j < i + 1; j++) {

                                    LatLng lastPoint = new LatLng(allPoints.get(j).latitude, allPoints.get(j).longitude);
                                    Log.e("Last Points", lastPoint.toString());
                                    points.add(lastPoint);
                                    route.setPoints(points);
                                }
                                Log.e("MARK LOCATION", allLocation.toString());
                                if (allLocation.size() > 0) {
                                    getMarker(allLocation);
                                } else {
                                    allLocation.add(null);
                                    Log.e("There is no data availabale in all location", "" + allLocation);
                                }

                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else if (code == 404) {
                    //dialog.dismiss();
                    Toast.makeText(mContext, "There is no data to display on map", Toast.LENGTH_LONG).show();
                } else {
                    dialog.dismiss();
                }
            }
        };
        if (!CONFIG.isNetworkAvailable(mContext)) {
            Toast.makeText(mContext,
                    "Check your internet connection", Toast.LENGTH_LONG)
                    .show();
        } else {
            getLocationDetailTask.execute();
        }
    }

    public String getMarker(List<Address> allLocation) {
        Address address = allLocation.get(0);
        addressLine = address.getAddressLine(0) + "\n"
                + address.getAddressLine(1) + "\n"
                + address.getAddressLine(2);
        drawMarker(stringLatitude, stringLongitude, addressLine);
        return addressLine;
    }

    class MyInfoWindowAdapter implements InfoWindowAdapter {

        public final View myContentsView;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.marker, null);
        }

        @Override
        public View getInfoContents(Marker arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            View v = getLayoutInflater().inflate(R.layout.marker, null);

            info = (TextView) v.findViewById(R.id.info);
            info.setText(marker.getSnippet());
            return v;
        }
    }
}




