package mx.com.audioweb.indigo.TimeTracker.api;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import mx.com.audioweb.indigo.R;
import mx.com.audioweb.indigo.TimeTracker.Shared_notifications;
import mx.com.audioweb.indigo.TimeTracker.activity.TimeTracking_Activity;
import mx.com.audioweb.indigo.TimeTracker.task.SalesCurrentLocationTask;

//import com.ecosmob.salestracker.activity.ActivityVoiceAuthenticationScreen;

public class MainService extends Service {

    private static final String TAG = null;
    AsyncTask<String, String, JSONObject> getShiftNotiDetailTask;
    Context mContext;
    int TIEMPO_ALERTA = 8;
    String webURLShift, getUserName, startTime = "", endTime = "", checkAPIDate = "", currentAPIDate = "";
    JSONObject json;
    SharedPreferences myPrefs;
    JSONArray jArray;
    Calendar shiftStart, shiftEnd;
    int showNotification = 0, NotificationCount, dayTime;
    Timer shiftTimer;
    Calendar mCalander = Calendar.getInstance();
    Long randomRepeatTime;
    NotificationManager mNotificationManager;
    Shared_notifications session;
    Editor edit;
    String webURLSetLocation;
    GpsTracker gpsTracker;
    long time;
    double stringLatitude, stringLongitude;
    boolean isVoiceAuthenticated, serviceStarted = true, dailyService = true, callService = false, firstLogin = true;
    SimpleDateFormat apiFormatter = new SimpleDateFormat("dd/MM");

    Calendar tomorrowCalendar = Calendar.getInstance();
    //boolean tryExceed = false;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        mContext = getApplicationContext();
        BugSenseHandler.initAndStartSession(mContext, "a4c94239");
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        myPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        edit = myPrefs.edit();
        Log.e(TAG, "Service created at " + mCalander.getTime());
        gpsTracker = new GpsTracker(mContext);
        json = new JSONObject();
        getUserName = myPrefs.getString("User Name", "");
        webURLShift = CONFIG.SERVER_URL + "salesmen/notification/" + getUserName;
        mCalander.set(Calendar.HOUR_OF_DAY, 6);
        time = mCalander.getTimeInMillis();
        edit.putInt("tryAgainCount", 0);
        edit.commit();
        final Random randomTime = new Random();

        randomRepeatTime = (long) (8 + randomTime.nextInt(7));

        getShiftTime();
        Log.e("Start PRUEBA", "--------------------------- START -----------------------------");

        startNotification();


        return START_STICKY;
    }

    public void getShiftTime() {
        shiftTimer = new Timer();

        shiftTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                final Random randomTime = new Random();

                randomRepeatTime = (long) (8 + randomTime.nextInt(7));
                //randomRepeatTime = (long) ( TIEMPO_ALERTA + randomTime.nextInt(7));
                callService = myPrefs.getBoolean("Call_Service", false);
                dayTime = mCalander.get(Calendar.HOUR_OF_DAY);


                if (callService) {
                    if (CONFIG.isNetworkAvailable(mContext)) {
                        String resultTask = getShiftTimeTask();
                        Log.e("Value if resultTask", resultTask);
                        if (!resultTask.isEmpty()) {
                            edit.putBoolean("Call_Service", false);
                            edit.commit();
                            serviceStarted = false;
                            tomorrowCalendar.add(Calendar.DAY_OF_YEAR, 1);
                            Date tomorrow = tomorrowCalendar.getTime();
                            checkAPIDate = apiFormatter.format(tomorrow);
                        }
                        Log.e("Shift Start Time", "my time" + startTime);
                        Log.e("Notification", "" + showNotification);

                    } else {
                        Log.e("No internet", "you do not have internet connectivity");
                    }
                }
                currentAPIDate = apiFormatter.format(new Date());
                Log.e("Current Date & checkedDate", "" + currentAPIDate + "==" + checkAPIDate);

                if (!dailyService && checkAPIDate.equalsIgnoreCase(currentAPIDate)) {
                    Log.e("Value of dailyService", "" + dailyService);
                    String resultTask = getShiftTimeTask();
                    if (!resultTask.isEmpty()) {
                        dailyService = true;
                        serviceStarted = false;
                        NotificationCount = 0;
                        tomorrowCalendar.add(Calendar.DAY_OF_YEAR, 1);
                        Date tomorrow = tomorrowCalendar.getTime();
                        checkAPIDate = apiFormatter.format(tomorrow);
                    }
                } else if (serviceStarted) {
                    String resultTask = getShiftTimeTask();
                    if (!resultTask.isEmpty()) {
                        serviceStarted = false;
                        NotificationCount = 0;
                        tomorrowCalendar.add(Calendar.DAY_OF_YEAR, 1);
                        Date tomorrow = tomorrowCalendar.getTime();
                        checkAPIDate = apiFormatter.format(tomorrow);
                    }
                }
            }
        }, 0l, 1000 * 60);
    }

    public void startNotification() {
        final Timer startNotiTimer = new Timer();
        NotificationCount = 0;
        session = new Shared_notifications(mContext);
        //Log.e("SESION CHECK", String.valueOf(session.checkNotification()));

        Log.e("RepeatTime", "startNotification : " + randomRepeatTime);
        startNotiTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Log.e("Start notification", "Time" + 1000 * 60 * 2);

                SimpleDateFormat sdf = new SimpleDateFormat("kk:mm", Locale.US);
                Date inTime = null, outTime = null, nowTime = new Date();
                SimpleDateFormat sdf2 = new SimpleDateFormat("kk:mm:ss", Locale.US);

                startTime = myPrefs.getString("startTime", "");
                endTime = myPrefs.getString("endTime", "");
                showNotification = myPrefs.getInt("showNotification", 0);
                //tryExceed = myPrefs.getBoolean("tryExceed", false);

                if (!startTime.isEmpty() && !endTime.isEmpty() && showNotification == 1) {
                    try {
                        inTime = sdf.parse(startTime);
                        outTime = sdf.parse(endTime);
                        nowTime = sdf.parse(sdf2.format(nowTime));
                        Log.e("current Time", "" + nowTime);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if ((nowTime.compareTo(inTime) == 1 || nowTime.compareTo(inTime) == 0) &&
                            (outTime.compareTo(nowTime) == 1 || outTime.compareTo(nowTime) == 0)) {
                        Log.e("NotificationCount", "" + NotificationCount);
                        isVoiceAuthenticated = myPrefs.getBoolean("isVoiceAuthenticated", false);

                        Log.e("isShowNoti", "" + isVoiceAuthenticated);
                        if (NotificationCount == 0) {

                            edit.putBoolean("isVoiceAuthenticated", false);
                            edit.commit();
                            if (session.checkNotification()) {
                                createNotification();
                            }
                            Log.e(TAG, "Main Notification");

                        } else if (!isVoiceAuthenticated && NotificationCount < 3) {//&& !tryExceed
                            if (session.checkNotification()) {
                                createNotification();
                            }

                        } else if (NotificationCount == 3 && !isVoiceAuthenticated) { //&& !tryExceed

                            NotificationCount = 0;
                            mNotificationManager.cancel(999);

                            try {
                                if (gpsTracker.canGetLocation()) {
                                    try {
                                        webURLSetLocation = CONFIG.SERVER_URL + "location";
                                        stringLatitude = gpsTracker.getLatitude();
                                        stringLongitude = gpsTracker.getLongitude();
                                        json.put("username", getUserName);
                                        json.put("latitude", stringLatitude);
                                        json.put("longitude", stringLongitude);
                                        json.put("is_auth", 0);
                                        Log.e("json data", json.toString());

                                        Handler handler = new Handler(Looper.getMainLooper());
                                        handler.post(new Runnable() {
                                            public void run() {
                                                try {
                                                    if (!CONFIG.isNetworkAvailable(mContext)) {
                                                        Log.e("isNetworkAvailable", "Check your internet connection");
                                                    } else {
                                                        SalesCurrentLocationTask login = new SalesCurrentLocationTask(mContext, json);
                                                        login.execute(webURLSetLocation);
                                                    }
                                                } catch (Exception e) {
                                                    // TODO Auto-generated catch block
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                        edit.putBoolean("isVoiceAuthenticated", true);
                                        edit.commit();
                                    } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                } else {
                                    gpsTracker.showSettingsAlert();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (NotificationCount < 3) {
                            NotificationCount++;
                        }
                        isVoiceAuthenticated = myPrefs.getBoolean("isVoiceAuthenticated", false);
                        if (isVoiceAuthenticated) {//|| tryExceed
                            startNotiTimer.cancel();
                            NotificationCount = 0;
                            repeatNotification();
                            edit.putBoolean("tryExceed", false);
                            edit.commit();
                        }
                    } else if (nowTime.compareTo(outTime) == 0 || nowTime.compareTo(outTime) == 1) {
                        dailyService = false;
                        startNotiTimer.cancel();
                        NotificationCount = 0;
                        Log.e("Service status", "stopSelf()");
                    }
                }
            }
        }, randomRepeatTime * 60 * 1000, 1000 * 60 * 2);

    }

    public void repeatNotification() {
        Timer repeatNotiTimer = new Timer();
        Log.e("RepeatTime", "repeatNotification : " + randomRepeatTime);
        repeatNotiTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                startNotification();
            }
        }, randomRepeatTime * 60 * 1000);
    }

    public void createNotification() {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Time Tracker")
                        .setContentText(getText(R.string.service_started).toString()).setLights(0xff00ff00, 300, 1000);
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(mContext, TimeTracking_Activity.class);
        //Intent resultIntent = new Intent(mContext, MyActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack for the Intent (but not the Intent itself)
        //stackBuilder.addParentStack(ActivityVoiceAuthenticationScreen.class);
        stackBuilder.addParentStack(TimeTracking_Activity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(999, mBuilder.build());
    }

    public synchronized String getShiftTimeTask() {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                try {
                    getShiftNotiDetailTask = new AsyncTask<String, String, JSONObject>() {
                        HttpResponse result;

                        @Override
                        protected JSONObject doInBackground(String... params) {
                            // TODO Auto-generated method stub
                            try {
                                result = RestService.doGet(webURLShift);
                                return RestService.JSONFormResponse(result);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(JSONObject json) {
                            int code = result.getStatusLine().getStatusCode();
                            Log.e("Get Noti Detail", json.toString());
                            Log.e("code", "" + code);
                            if (code == 200) {

                                try {
                                    jArray = new JSONArray();
                                    jArray = json.getJSONArray("data");

                                    for (int i = 0; i < jArray.length(); i++) {
                                        JSONObject jObject = jArray.getJSONObject(i);
                                        // Storing each json item in variable
                                        showNotification = jObject.getInt("show_notification");
                                        edit.putInt("showNotification", showNotification);
                                        Log.e("show notification", "" + showNotification);

                                        startTime = jObject.getString("shift_start_time");
                                        edit.putString("startTime", startTime);
                                        Log.e("start time", startTime);

                                        endTime = jObject.getString("shift_end_time");
                                        edit.putString("endTime", endTime);
                                        Log.e("end time", endTime);
                                        edit.commit();
                                    }
                                } catch (Exception e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }
                            }
                        }
                    };
                    if (!CONFIG.isNetworkAvailable(mContext)) {
                        Log.e("isNetworkAvailable", "Check your internet connection");
                    } else {
                        getShiftNotiDetailTask.execute().get();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        return startTime + ":" + endTime;
    }

    public String getCurrentTime() {
        Date now = new Date();
        SimpleDateFormat df = new SimpleDateFormat("kk:mm", Locale.US);
        return df.format(now);
        //	return now.getHours() + ":" + now.getMinutes() ;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        //Restart service here if it closed
        sendBroadcast(new Intent("RestartService"));
    }
}
