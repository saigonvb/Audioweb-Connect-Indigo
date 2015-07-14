package mx.com.audioweb.indigo.Notifications;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import mx.com.audioweb.indigo.Home;
import mx.com.audioweb.indigo.R;
import mx.com.audioweb.indigo.SharedPreferencesExecutor;

/**
 * Created by Juan Acosta on 12/10/2014.
 */
public class PusherService extends Service implements ConnectionEventListener, ChannelEventListener {
    public static final ScheduledExecutorService connectionAttemptsWorker = Executors.newSingleThreadScheduledExecutor();
    public static final int NOTIFY_ME_ID = 1337;
    public static int failedConnectionAttempts = 0;
    public static int MAX_RETRIES = 10;
    public static String PUBLIC_CHANNEL_NAME;
    public static Pusher pusher;
    public static Channel publicChannel;
    public static int numMessages = 0;
    public static ConnectionState targetState = ConnectionState.DISCONNECTED;
    public static String[] events = new String[101];
    public Context context;
    private SharedPreferencesExecutor<Group> shaEx;
    private ArrayList<Group> groups;
    private NotificationManager mNotificationManager;

    public static void achieveExpectedConnectionState() {
        //ConnectionState currentState = pusher.getConnection().getState();
        ConnectionState currentState = ConnectionState.DISCONNECTED;
        if (currentState == targetState) {
            // do nothing, we're there.
            failedConnectionAttempts = 0;
        } else if (targetState == ConnectionState.CONNECTED &&
                failedConnectionAttempts == MAX_RETRIES) {
            targetState = ConnectionState.DISCONNECTED;
            log("failed to connect after " + failedConnectionAttempts + " attempts. Reconnection attempts stopped.");
        } else if (currentState == ConnectionState.DISCONNECTED &&
                targetState == ConnectionState.CONNECTED) {
            Runnable task = new Runnable() {
                public void run() {
                    pusher.connect();
                    //Intent i = new Intent (getApplicationContext(),PusherService.class);
                    //startService(i);
                }
            };
            log("Connecting in " + failedConnectionAttempts + " seconds");
            connectionAttemptsWorker.schedule(task, (failedConnectionAttempts), TimeUnit.SECONDS);
            ++failedConnectionAttempts;
        } else if (currentState == ConnectionState.CONNECTED &&
                targetState == ConnectionState.DISCONNECTED) {
            pusher.disconnect();
        } else {
            // transitional state
        }
    }

    public static void log(String msg) {
        Log.e("--", msg);
    }

    public static String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, "");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        super.onCreate();
        //PUBLIC_CHANNEL_NAME = getDefaults("channel",Home.channel_context);
        PUBLIC_CHANNEL_NAME = "Audioweb";
        //new ChannelAcTask(getApplicationContext()).execute(getDefaults("User Id", Home.mContext));
        //Log.e("CHANNEL -->", PUBLIC_CHANNEL_NAME);
        PusherOptions options = new PusherOptions().setEncrypted(true);
        pusher = new Pusher("9218b58ee0e88650b113", options);
        pusher.getConnection().bind(ConnectionState.ALL, this);
        //Toast.makeText(this, "INICIO", Toast.LENGTH_LONG).show();
        targetState = ConnectionState.CONNECTED;
        achieveExpectedConnectionState();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "INICIO SERVICE", Toast.LENGTH_LONG).show();
        final NotificationManager mgr =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Log.d("CANAL--------------------->", PUBLIC_CHANNEL_NAME);
        // if(PUBLIC_CHANNEL_NAME.equals(null)){
        publicChannel = pusher.subscribe("Audioweb");
        // }
        //else{
        //publicChannel = pusher.subscribe(PUBLIC_CHANNEL_NAME);
        //}


        listenToOtherEvent();
        /*Runnable task = new Runnable() {
            public void run() {
                publicChannel.bind("0", new SubscriptionEventListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onEvent(String channel, String event, String data) {
                        JSONObject jsonObject = null;
                        //System.out.println("Received event with data: " + data);
                        Log.e("Received event with data: ", data);
                        try {
                            jsonObject = new JSONObject(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (jsonObject != null) {

                            String mensj = "";
                            String titulo = "";
                            String id = "";
                            try {
                                mensj = jsonObject.getString("message");
                                titulo = jsonObject.getString("title");
                                id = jsonObject.getString("time");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Notification_Info ci = new Notification_Info();
                            ci.name = titulo;
                            ci.surname = mensj;
                            ci.email = id;

                            Notification_List.result.add(ci);
                           /* Notification note = new Notification(R.drawable.ic_launcher,
                                    titulo,
                                    System.currentTimeMillis());
                            note.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
                            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            NotificationCompat.Builder mBuilder;
                            mBuilder = new NotificationCompat.Builder(getApplicationContext());
                            mBuilder.setContentTitle(titulo);
                            mBuilder.setAutoCancel(true);
                            mBuilder.setContentText(mensj);
                            mBuilder.setTicker("Nuevo Mensaje");
                            mBuilder.setPriority(10);
                            mBuilder.setSmallIcon(R.drawable.ic_launcher);
                            mBuilder.setNumber(++numMessages);
                            mBuilder.setSound(alarmSound);
                            mBuilder.setVibrate(new long[]{500, 500, 500, 500, 500});
                            //LED
                            mBuilder.setLights(Color.RED, 3000, 3000);

                            NotificationCompat.InboxStyle inboxStyle =
                                    new NotificationCompat.InboxStyle();

                            events[numMessages] = new String(mensj);


                            // Sets a title for the Inbox style big view
                            inboxStyle.setBigContentTitle(getApplicationInfo().loadLabel(getPackageManager()).toString());
                            // Moves events into the big view
                            for (int i = 0; i < events.length; i++) {

                                inboxStyle.addLine(events[i]);
                            }
                            mBuilder.setStyle(inboxStyle);

                            Intent resultIntent = new Intent(getApplicationContext(), Notification_List.class);

                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                            //stackBuilder.addParentStack(NotificationView.class);

                         /* Adds the Intent that starts the Activity to the top of the stack
                            stackBuilder.addNextIntent(resultIntent);
                            PendingIntent resultPendingIntent =
                                    stackBuilder.getPendingIntent(
                                            0,
                                            PendingIntent.FLAG_UPDATE_CURRENT

                                    );

                            mBuilder.setContentIntent(resultPendingIntent);

                            mNotificationManager =
                                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                            /* notificationID allows you to update the notification later on.
                            mNotificationManager.notify(NOTIFY_ME_ID, mBuilder.build());
                            // This pending intent will open after notification click
                            /*PendingIntent i = PendingIntent.getActivity(getBaseContext(), 0,
                                    new Intent(getBaseContext(),MainActivity.class),
                                    0);

                            note.setLatestEventInfo(getBaseContext(), titulo,
                                    mensj, i);

                            //After uncomment this line you will see number of notification arrived
                            note.number=2;
                            mgr.notify(NOTIFY_ME_ID, note);
                        }
                    }

                });

            }
        };*/
        //connectionAttemptsWorker.schedule(task, (failedConnectionAttempts), TimeUnit.SECONDS);
        return super.onStartCommand(intent, flags, startId);
    }

    public void listenToOtherEvent() {
        shaEx = new SharedPreferencesExecutor<Group>(getApplicationContext());
        groups = shaEx.retreiveAll(Group.class);
        Log.e("Groups -->", String.valueOf(groups));
        publicChannel.bind("new-groups", this);
        for (Group group : groups) {
            publicChannel.bind(group.getGroup_id(), this);
            Log.e("LD", group.getGroup_name());
        }

    }

    public void newEvent(String group) {
        publicChannel.bind(group, this);
    }

    @Override
    public void onDestroy() {
        //Toast.makeText(this, "FIN", Toast.LENGTH_LONG).show();
        super.onDestroy();
    }

    @Override
    public void onSubscriptionSucceeded(String channelName) {
        String msg = String.format("Subscription succeeded for [%s]", channelName);
        log(msg);
    }

    @Override
    public void onConnectionStateChange(ConnectionStateChange connectionStateChange) {
        String msg = String.format("Connection state changed from [%s] to [%s]",
                connectionStateChange.getPreviousState(), connectionStateChange.getCurrentState());
        log(msg);
        achieveExpectedConnectionState();

    }

    @Override
    public void onError(String message, String code, Exception e) {
        String msg = String.format("Connection error: [%s] [%s] [%s]", message, code, e);
        log(msg);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onEvent(String channel, String event, String data) {
        JSONObject jsonObject = null;
        Log.e("EVENTO", event);
        //System.out.println("Received event with data: " + data);
        Log.e("Received event with data: ", data);
        try {
            jsonObject = new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonObject != null) {
            if (jsonObject.has("usuarios")) {
                Log.d("Entro", "YES");
                try{
                    String usuarios = jsonObject.getString("usuarios");
                    String[] parts = usuarios.split(",");
                    //Log.d("USER_ID",User_info.USER_ID);
                    for (int i = 0; i < parts.length; i++) {
                        Log.d("NUMERO",parts[i]);
                        if (parts[i].equals("12")) {
                            publicChannel.bind(jsonObject.getString("grupo"), this);
                            //newEvent(jsonObject.getString("grupo"));
                            Log.e("Se agrego a un nuevo Grupo", "JEJETL");
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {

                String mensj = "";
                String titulo = "";
                String id = "";
                try{
                    mensj = jsonObject.getString("message");
                    titulo = jsonObject.getString("title");
                    id = jsonObject.getString("time");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Notification_Info ci = new Notification_Info();
                ci.name = titulo;
                ci.surname = mensj;
                ci.email = id;

                Notification_List.result.add(ci);
                           /* Notification note = new Notification(R.drawable.ic_launcher,
                                    titulo,
                                    System.currentTimeMillis());
                            note.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;*/
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder mBuilder;
                mBuilder = new NotificationCompat.Builder(getApplicationContext());
                mBuilder.setContentTitle(titulo);
                mBuilder.setAutoCancel(true);
                mBuilder.setContentText(mensj);
                mBuilder.setTicker("Nuevo Mensaje");
                mBuilder.setPriority(10);
                mBuilder.setSmallIcon(R.drawable.ic_launcher);
                mBuilder.setNumber(++numMessages);
                mBuilder.setSound(alarmSound);
                mBuilder.setVibrate(new long[]{500, 500, 500, 500, 500});
                //LED
                mBuilder.setLights(Color.RED, 3000, 3000);

                NotificationCompat.InboxStyle inboxStyle =
                        new NotificationCompat.InboxStyle();

                events[numMessages] = new String(mensj);


                // Sets a title for the Inbox style big view
                inboxStyle.setBigContentTitle(getApplicationInfo().loadLabel(getPackageManager()).toString());
                // Moves events into the big view
                for (int i = 0; i < events.length; i++) {

                    inboxStyle.addLine(events[i]);
                }
                mBuilder.setStyle(inboxStyle);

                Intent resultIntent = new Intent(getApplicationContext(), Home.class);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                //stackBuilder.addParentStack(NotificationView.class);

                         /* Adds the Intent that starts the Activity to the top of the stack */
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT

                        );

                mBuilder.setContentIntent(resultPendingIntent);

                mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                            /* notificationID allows you to update the notification later on. */
                mNotificationManager.notify(NOTIFY_ME_ID, mBuilder.build());
                // This pending intent will open after notification click
                            /*PendingIntent i = PendingIntent.getActivity(getBaseContext(), 0,
                                    new Intent(getBaseContext(),MainActivity.class),
                                    0);

                            note.setLatestEventInfo(getBaseContext(), titulo,
                                    mensj, i);

                            //After uncomment this line you will see number of notification arrived
                            note.number=2;
                            mgr.notify(NOTIFY_ME_ID, note);*/
            }
        }

    }

    public String getChannel(String uid) throws JSONException {
        BufferedReader bufferedReader = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost request = new HttpPost("http://66.226.72.48/connect/webServices/getChannel.php");
        List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("userId", uid));
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
            request.setEntity(entity);
            HttpResponse response = httpClient.execute(request);
            bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            bufferedReader.close();
            Log.e("JSON-->>", stringBuffer.toString());
            JSONArray jsonArray = new JSONArray(stringBuffer.toString());
            String string = stringBuffer.toString();
            if (!string.equals("null")) {

                String canal = jsonArray.getJSONObject(0).getString("smen_channel");
                Log.e("CHANNELLLL --->>>", jsonArray.getJSONObject(0).getString("smen_channel"));
               /* ArrayList<Group> groups = new ArrayList<Group>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    groups.add(gson.fromJson(jsonArray.getJSONObject(i).toString(), Group.class));
                    Log.e("JSONOBJ", jsonArray.getJSONObject(i).toString());
                }
                return groups;*/
                return canal;
            } else {
                return null;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    class ChannelAcTask extends AsyncTask<String, Void, Boolean> {
        private Context context;
        private String canal;

        public ChannelAcTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String usr_id = params[0];

            try {

                Log.i("ID->>", usr_id);
                canal = getChannel(usr_id);
                PUBLIC_CHANNEL_NAME = canal;


            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("Contactos", "Error cargando contactos");
            } catch (Exception e) {
                Log.e("Contactos", "Error inesperado");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("ContactosTask", "Entro onPostExecute");


        }

    }
}

