package mx.com.audioweb.indigo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.json.JSONObject;

import mx.com.audioweb.indigo.Chat.Chat.UserList;
import mx.com.audioweb.indigo.TimeTracker.Shared_notifications;
import mx.com.audioweb.indigo.TimeTracker.api.CONFIG;
import mx.com.audioweb.indigo.TimeTracker.task.UserLoginTask;


public class Login extends Activity {

    public static String UserName;
    EditText User, Pass;
    JSONObject jData, json;
    Button LogIn;
    String userName, password, webURL, getUserName, android_id;
    SharedPreferences preferences;
    Context mContext;
    boolean authVoiceID = false, isLogin, isAuth = false, callService = true;
    private Bundle id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = Login.this;
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        final Shared_notifications session = new Shared_notifications(mContext);
        final SharedPreferences.Editor edit = preferences.edit();

        if (!CONFIG.isNetworkAvailable(mContext)) {
            Toast.makeText(mContext, "Check your internet connection", Toast.LENGTH_LONG).show();
        }

        if (preferences.getBoolean("isUserLogin", false)) {

            if (!preferences.getBoolean("isVoiceAuthenticated", false)) {
                Intent mainIntent = new Intent(mContext, Home.class);
                startActivity(mainIntent);
                finish();
            } else {
                /*Intent mainIntent = new Intent(mContext, ActivityUserDetailScreen.class);
                startActivity(mainIntent);
                finish();*/
            }
        }

        User = (EditText) findViewById(R.id.userText);
        Pass = (EditText) findViewById(R.id.passwordText);

        LogIn = (Button) findViewById(R.id.loginButton);

        json = new JSONObject();
        jData = new JSONObject();

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            android_id = TelephonyMgr.getDeviceId();
        } else {
            WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (wm.isWifiEnabled() == false) {
                // enable wifi if it is disabled
                wm.setWifiEnabled(true);
                Toast.makeText(Login.this, "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            }
            if (wm.isWifiEnabled() == true) {
                android_id = wm.getConnectionInfo().getMacAddress();
            }
        }
        LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!CONFIG.isNetworkAvailable(mContext)) {
                    Toast.makeText(mContext, "Check your internet connection", Toast.LENGTH_LONG).show();
                } else {
                    //final ProgressDialog dialog = ProgressDialog.show(getApplicationContext(),null,getString(R.string.alert_wait));
                    final ParseUser parseUser = new ParseUser();
                    userName = User.getText().toString();
                    password = Pass.getText().toString();
                    preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    edit.putString("userName", userName);
                    edit.putString("passWord", password);
                    edit.commit();

                    webURL = CONFIG.SERVER_URL + "salesmen/login";
                    try {
                        UserName = userName;
                        json.put("username", userName);
                        json.put("password", password);
                        json.put("device_id", "0");

                        Log.e("Logon json", json.toString());
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    UserLoginTask login = new UserLoginTask(mContext, json);
                    try {
                        if (!CONFIG.isNetworkAvailable(mContext)) {
                            Toast.makeText(mContext, "Check your internet connection", Toast.LENGTH_LONG).show();
                        } else {
                            jData = login.execute(webURL).get();
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    edit.putString("User Name", userName);
                    try {
                        if (jData.getString("status").matches("SUCCESS")) {
                            Log.e("USER ID--> ", jData.getString("smen_id").toString());
                            String user_id = jData.getString("smen_id");
                            id = new Bundle();
                            id.putSerializable("uid", user_id);
                            edit.putBoolean("isUserLogin", true);
                            edit.putBoolean("notification", false);
                            session.createNotification();
                            edit.putBoolean("Call_Service", callService);
                            edit.commit();
                            authVoiceID = jData.getString("smen_auth_voice_id").matches("0");

                            parseUser.setEmail(userName + "@audioweb.com.mx");
                            parseUser.setPassword(password);
                            parseUser.setUsername(userName);
                            parseUser.put("Name", "0");

                            parseUser.signUpInBackground(new SignUpCallback() {
                                @Override
                                public void done(ParseException e) {
                                    //dialog.dismiss();
                                    if (e == null) {
                                        UserList.user = parseUser;
                                        //startActivity(new Intent(Register.this,UserList.class));
                                        startActivity(new Intent(mContext, Home.class).putExtras(id));
                                        setResult(RESULT_OK);
                                        finish();
                                    } else {
                                        //Utils.showDialog(Register.this, getString(R.string.err_singup) + " " + e.getMessage());
                                        Log.e("ERROR REGISTRO-->", getString(R.string.err_singup) + " " + e.getMessage());
                                        e.printStackTrace();
                                        ParseUser.logInInBackground(userName, password, new LogInCallback() {

                                            @Override
                                            public void done(ParseUser parseUser, ParseException e) {
                                                //dialog.dismiss();
                                                if (parseUser != null) {
                                                    Log.e("ERROR REGISTRO-->", getString(R.string.title_activity_login) + " " + userName);
                                                    UserList.user = parseUser;
                                                    startActivity(new Intent(mContext, Home.class).putExtras(id));
                                                    finish();
                                                } else {
                                                    Log.e("ERROR REGISTRO-->", getString(R.string.err_login) + " " + e.getMessage());
                                                    //Utils.showDialog(Login.this, getString(R.string.err_login) + " " + e.getMessage());
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                }
                            });

                            /*startActivity(new Intent(mContext, Home.class).putExtras(id));
                            finish();*/
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}


