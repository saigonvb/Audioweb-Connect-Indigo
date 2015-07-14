package mx.com.audioweb.indigo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.json.JSONException;

import java.util.ArrayList;

import mx.com.audioweb.indigo.AudioConference.AudioConferencia;
import mx.com.audioweb.indigo.Chat.Chat.UserList;
import mx.com.audioweb.indigo.Citas.Cita;
import mx.com.audioweb.indigo.Citas.Citas_list;
import mx.com.audioweb.indigo.LiveStreaming.LiveStreaming;
import mx.com.audioweb.indigo.Notifications.Group;
import mx.com.audioweb.indigo.Notifications.Notification_List;
import mx.com.audioweb.indigo.Notifications.PusherService;
import mx.com.audioweb.indigo.TimeTracker.activity.TimeTracking_Activity;


public class Home extends Activity implements View.OnClickListener {

    private static final String TAG = "TAG";
    public static Context mContext;
    public ImageButton TT, LS, C, AC, NOT, CT;
    public AlertDialog.Builder alertDialogBuilder;
    public AlertDialog alertDialog;
    public String userName;
    public String uid;
    private static final String DATEF = "yyyy-MM-dd HH:mm:ss";
    private Gson gson = new GsonBuilder().setDateFormat(DATEF).create();
    SharedPreferences myPrefs;
    SharedPreferences.Editor edit;
    private ArrayList<User_info> userinfos;
    private ArrayList<Group> groups;

    public static void setDefaults(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        final SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final String user = (mSharedPreference.getString("userName", "Default_Value"));
        String pass = (mSharedPreference.getString("passWord", "Default_Value"));
        Log.e("DATOS-->", user + " -> " + pass);
        Log.e("USERLIST", String.valueOf(UserList.user));
        if (UserList.user == null) {
            ParseUser.logInInBackground(user, pass, new LogInCallback() {

                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    //dialog.dismiss();
                    if (parseUser != null) {
                        Log.e("Ya se Registro-->", getString(R.string.title_activity_login) + " ");
                        UserList.user = parseUser;
                        userName = UserList.user.getString("Name");
                        Log.e("USER", UserList.user.getUsername());
                        Log.e("USERNAME-->", UserList.user.getString("Name"));

                        if (userName.equals("0")) {
                            showDialog(0);
                        }


                    } else {
                        Log.e("ERROR REGISTRO-->", getString(R.string.err_login) + " " + e.getMessage());
                        //Utils.showDialog(Login.this, getString(R.string.err_login) + " " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        } else {
            userName = UserList.user.getString("Name");
            if (userName.equals("0")) {
                showDialog(0);
            }
        }

        PusherService.numMessages = 0;
        PusherService.events = new String[101];
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //final SharedPreferences.Editor edit = myPrefs.edit();


        mContext = Home.this;
        myPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        edit = myPrefs.edit();
        Bundle extras = getIntent().getExtras();
        String userName = "0";
        if (extras != null) {
            userName = extras.getString("uid");
            Log.e("EXTRAS", userName);
            edit.putString("User Id", userName);
            edit.commit();
            // and get whatever type user account id is
        }
        //new GroupAcTask(getApplicationContext(), groups).execute(myPrefs.getString("User Id", ""));


        AC = (ImageButton) findViewById(R.id.imageButton);
        TT = (ImageButton) findViewById(R.id.imageButton2);
        LS = (ImageButton) findViewById(R.id.imageButton4);
        C = (ImageButton) findViewById(R.id.imageButton5);
        NOT = (ImageButton) findViewById(R.id.imageButton6);
        CT = (ImageButton) findViewById(R.id.imageButton3);

        AC.setOnClickListener(this);
        TT.setOnClickListener(this);
        LS.setOnClickListener(this);
        C.setOnClickListener(this);
        NOT.setOnClickListener(this);
        CT.setOnClickListener(this);

    }

    @Override
    protected Dialog onCreateDialog(int id) {

        switch (id) {
            case 0:
                return createExampleDialog();
            default:
                return null;
        }
    }

    /**
     * If a dialog has already been created,
     * this is called to reset the dialog
     * before showing it a 2nd time. Optional.
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {

        switch (id) {
            case 0:
                // Clear the input box.
                EditText text = (EditText) dialog.findViewById(0);
                text.setText("");
                break;
        }
    }

    /**
     * Create and return an example alert dialog with an edit text box.
     */
    private Dialog createExampleDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("HOLA !!");
        builder.setMessage("Cual es tu Nombre:");

        // Use an EditText view to get user input.
        final EditText input = new EditText(this);
        input.setId(0);
        builder.setView(input);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                Log.d(TAG, "User name: " + value);
                UserList.user.put("Name", value);
                UserList.user.saveInBackground();
                return;
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        return builder.create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.about:
                try {
                    about();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void about() throws PackageManager.NameNotFoundException {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        String versionName = "";
        alertDialog.setTitle(R.string.about);
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        alertDialog.setMessage("La version actual es: " + versionName);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageButton:
                abrirAc();
                break;
            case R.id.imageButton2:
                startActivity(new Intent(this, TimeTracking_Activity.class));
                break;
            case R.id.imageButton4:
                String id = myPrefs.getString("User Id", "");
                Bundle uid = new Bundle();
                uid.putSerializable("uid", id);
                Log.e("id", id);
                startActivity(new Intent(this, LiveStreaming.class).putExtras(uid));

                break;
            case R.id.imageButton5:
                abrirCita();
                break;

            case R.id.imageButton6:
                startActivity(new Intent(this, Notification_List.class));
                break;
            case R.id.imageButton3:
                startActivity(new Intent(Home.this, UserList.class));
                break;
        }
    }

    public void abrirAc() {
        mContext = Home.this;
        myPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String id = myPrefs.getString("User Id", "");
        Log.e("id", id);
        new NumeroAcTask(getApplicationContext(), this.userinfos).execute(id);
    }

    public void abrirCita() {
        mContext = Home.this;
        myPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String id = myPrefs.getString("User Id", "");
        Log.e("id", id);
        Bundle uid = new Bundle();
        uid.putSerializable("uid", id);
        //startActivity(new Intent(this, Citas.class).putExtras(uid));
        new CitasTask(getApplicationContext(), id).execute();

    }


   /* class ChannelAcTask extends AsyncTask<String, Void, Boolean> {
        private Context context;
        private String canal;
        private Bundle informacion;

        public ChannelAcTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String usr_id = params[0];

            try {
                ClienteHttp clienteHttp = new ClienteHttp();

                Log.i("ID->>", usr_id);
                canal = clienteHttp.getChannel(usr_id);

                Log.e("CHANNEL-->",canal);
                setDefaults("channel",canal,channel_context);

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
            new GroupAcTask(getApplicationContext(), groups).execute(myPrefs.getString("User Id", ""));

        }

    }*/

    /*@Override
    protected void onResume() {
        super.onResume();
        new GroupAcTask(getApplicationContext(), groups).execute(myPrefs.getString("User Id", ""));
    }

    @Override
    protected void onPause() {
        super.onPause();
        new GroupAcTask(getApplicationContext(), groups).execute(myPrefs.getString("User Id", ""));
    }*/

    class NumeroAcTask extends AsyncTask<String, Void, Boolean> {
        private Context context;
        private ArrayList<User_info> userinfos;
        private ArrayList<User_info> divisa;
        private Bundle informacion;

        public NumeroAcTask(Context context, ArrayList<User_info> userinfos) {
            this.context = context;
            this.userinfos = userinfos;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String usr_id = params[0];

            try {
                ClienteHttp clienteHttp = new ClienteHttp();

                Log.i("ID->>", usr_id);
                this.userinfos = clienteHttp.acnum(usr_id);
                if (this.userinfos == null) {
                    Log.d("Message", "No existen contactos en tu libreta de direcciones");
                } else {
                    SharedPreferencesExecutor<User_info> shaEx = new SharedPreferencesExecutor<User_info>(this.context);
                    for (User_info userinfo : userinfos) {
                        shaEx.saveData(User_info.class.getName() + "_" + userinfo.getId(), userinfo);
                    }

                }
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
            //this.context.startActivity(new Intent(this.context, AudioConferencia.class));
            startActivity(new Intent(this.context, AudioConferencia.class));

        }

    }

    class GroupAcTask extends AsyncTask<String, Void, Boolean> {
        private Context context;
        private ArrayList<Group> groups;
        private Bundle informacion;

        public GroupAcTask(Context context, ArrayList<Group> groups) {
            this.context = context;
            this.groups = groups;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String usr_id = params[0];

            try {
                ClienteHttp clienteHttp = new ClienteHttp();

                Log.i("ID->>", usr_id);
                groups = clienteHttp.getGroups(usr_id);
                if (this.groups == null) {
                    Log.d("Message", "No existen contactos en tu libreta de direcciones");
                } else {

                    SharedPreferencesExecutor<Group> shaEx = new SharedPreferencesExecutor<Group>(this.context);
                    shaEx.removeAll(Group.class);
                    for (Group group : groups) {
                        shaEx.saveData(Group.class.getName() + "_" + group.getGroup_id(), group);
                    }

                }
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
            if (isMyServiceRunning(PusherService.class)) {
                Log.e("UserDetailScreen", "Service is already running");
                PusherService.achieveExpectedConnectionState();

            } else {
                Log.e("UserDetailScreen", "Service started first time");
                startService(new Intent(getApplicationContext(), PusherService.class));

            }


        }

    }

    public class CitasTask extends AsyncTask<String, Void, Boolean> {
        ArrayList<Cita> citas_list;
        private Context context;
        private Bundle informacion;
        private String id;

        public CitasTask(Context context, String id) {
            this.context = context;
            this.id = id;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                ClienteHttp clienteHttp = new ClienteHttp();

                Log.i("ID->>", this.id);
                citas_list = clienteHttp.GetCitas(this.id);
                Log.i("CITA_LIST--??", String.valueOf(citas_list));
                if (citas_list == null) {
                    Log.d("Message", "No existen citas");
                } else {

                    Log.e("CITAS", String.valueOf(citas_list));


                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("Groups", "Error Loading Groups");
            } catch (Exception e) {
                Log.e("Groups", "Unexpected Error");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("ContactosTask", "Entro onPostExecute");
            //this.context.startActivity(new Intent(this.context, AudioConferencia.class));
            //Log.d("CITAS == ", String.valueOf(this.citas_list.size()));
            String id = myPrefs.getString("User Id", "");
            Bundle uid = new Bundle();
            uid.putSerializable("uid", id);
            informacion = new Bundle();
            informacion.putSerializable("citas", citas_list);
            startActivity(new Intent(this.context, Citas_list.class).putExtras(informacion).putExtras(uid));

        }

    }
}

