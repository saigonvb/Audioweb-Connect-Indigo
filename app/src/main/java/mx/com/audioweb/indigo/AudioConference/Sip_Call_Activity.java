package mx.com.audioweb.indigo.AudioConference;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.sip.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import mx.com.audioweb.indigo.R;

import java.text.ParseException;

public class Sip_Call_Activity extends Activity implements View.OnClickListener {

    public String sipAddress = null;
    public String codigo;
    public boolean speaker = false;
    public SipProfile.Builder builder = null;
    public SipManager manager = null;
    public SipProfile me = null;
    public Button button, button2, button3;
    public SipAudioCall call = null;
    public IncomingCallReceiver callReceiver;
    public MyPhoneStateListener myListener;
    public boolean is3g;
    public Long time;
    public Chronometer chronometer;
    TextView labelView;
    Boolean mudo, conf = true;
    private Handler mDrawerHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sip__call_);
        initializeManager();
        mDrawerHandler = new Handler();
        labelView = (TextView) findViewById(R.id.sipLabel);
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        chronometer = (Chronometer) findViewById(R.id.chronometer2);
        button.setVisibility(View.INVISIBLE);
        button3.setVisibility(View.INVISIBLE);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.SipDemo.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initializeManager();

    }


    public void initializeManager() {
        if (manager == null) {
            manager = SipManager.newInstance(this);
        }
        initializeLocalProfile();
    }

    public void initializeLocalProfile() {
        if (manager == null) {
            return;
        }

        if (me != null) {
            closeLocalProfile();
        }

        String username = "9052";
        String domain = "189.201.130.153";
        String password = "audi0web";

        if (username.length() == 0 || domain.length() == 0 || password.length() == 0) {
            showDialog(3);
            return;
        }

        try {
            SipProfile.Builder builder = new SipProfile.Builder(username, domain);
            builder.setPassword(password);
            me = builder.build();
            Log.e("ENTRRO A INITIALIZE", me.toString());
            Intent i = new Intent();
            i.setAction("android.SipDemo.INCOMING_CALL");
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, Intent.FILL_IN_DATA);
            manager.open(me, pi, null);


            // This listener must be added AFTER manager.open is called,
            // Otherwise the methods aren't guaranteed to fire.

            manager.setRegistrationListener(me.getUriString(), new SipRegistrationListener() {
                public void onRegistering(String localProfileUri) {
                    updateStatus("Registering with SIP Server...");
                }

                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    updateStatus("Ready");
                    if (conf) {
                        conf = false;
                        mDrawerHandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                    initiateCall();
                                    chronometer.setBase(SystemClock.elapsedRealtime());
                                    chronometer.start();
                                } catch (InterruptedException e) {

                                    e.printStackTrace();
                                }
                            }
                        }, 600);
                    }
                }

                public void onRegistrationFailed(String localProfileUri, int errorCode,
                                                 String errorMessage) {
                    updateStatus("Registration failed.  Please check settings.");
                    Log.e("Error --> ", errorMessage + " error code" + String.valueOf(errorCode));
                }
            });
        } catch (ParseException pe) {
            updateStatus("Connection Error.");
        } catch (SipException se) {
            updateStatus("Connection error.");
        }
    }

    public void closeLocalProfile() {
        if (manager == null) {
            return;
        }
        try {
            if (me != null) {
                manager.close(me.getUriString());
            }
        } catch (Exception ee) {
            Log.d("WalkieTalkieActivity/onDestroy", "Failed to close local profile.", ee);
        }
    }

    public void initiateCall() {

        updateStatus("Llamando");

        try {
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                // Much of the client's interaction with the SIP Stack will
                // happen via listeners.  Even making an outgoing call, don't
                // forget to set up a listener to set things up once the call is established.
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    call.setSpeakerMode(false);
                    call.startAudio();
                    updateStatus("Conferencia establecida");
                }

                @Override
                public void onCallEnded(SipAudioCall call) {
                    updateStatus("Ready.");
                }
            };


            Uri phoneCall = Uri.parse("sip:4600@189.201.130.153");
            Log.e("NUMERO----->", phoneCall.toString());
            call = manager.makeAudioCall(me.getUriString(), "sip:4600@189.201.130.153", listener, 30);

            Log.e("CALL----->", String.valueOf(call));
            Log.e("CALL----->", call.toString());

            mDrawerHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent intent = getIntent();
                        Bundle b = intent.getExtras();
                        if (b != null) {
                            codigo = (String) b.get("Codigo");
                            Log.e("CODIGO AC--> ", codigo);
                        }
                        //codigo = "456495";

                        int num = Integer.parseInt(codigo);
                        String number = String.valueOf(num);

                        if (codigo.startsWith("0")) {
                            call.sendDtmf(+0);
                            Log.d("Digit ", String.valueOf(0));
                            Thread.sleep(600);
                        }

                        for (int i = 0; i < number.length(); i++) {
                            int j = Character.digit(number.charAt(i), 10);
                            call.sendDtmf(+j);
                            Thread.sleep(600);
                            Log.d("Digit ", String.valueOf(+j));
                        }
                        // 456495 //911911
                        Thread.sleep(600);
                        call.sendDtmf(11);
                        Thread.sleep(8000);
                        Log.e("HASHTAGENVIADO", "CHECK");
                        call.sendDtmf(11);

                        Log.e("CALL----------->>>>>>", String.valueOf(call));
                    } catch (InterruptedException e) {

                        e.printStackTrace();
                    }
                }
            }, 600);
        } catch (Exception e) {
            Log.i("WalkieTalkieActivity/InitiateCall", "Error when trying to close manager.", e);
            if (me != null) {
                try {
                    manager.close(me.getUriString());
                } catch (Exception ee) {
                    Log.i("WalkieTalkieActivity/InitiateCall",
                            "Error when trying to close manager.", ee);
                    ee.printStackTrace();
                }
            }
            if (call != null) {
                call.close();
            }
        }
    }

    public void updateStatus(final String status) {
        // Be a good citizen.  Make sure UI changes fire on the UI thread.
        this.runOnUiThread(new Runnable() {
            public void run() {
                labelView.setText(status);
            }
        });
    }

    public void updateStatus(SipAudioCall call) {
        String useName = call.getPeerProfile().getDisplayName();
        if (useName == null) {
            useName = call.getPeerProfile().getUserName();
        }
        updateStatus(useName + "@" + call.getPeerProfile().getSipDomain());
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button:

                /*mDrawerHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (mudo) {
                                //(button).setPressed(false);
                                call.sendDtmf(10);
                                Thread.sleep(600);
                                call.sendDtmf(6);
                                mudo = false;
                            } else {
                                //(button3).setPressed(true);
                                call.sendDtmf(10);
                                Thread.sleep(600);
                                call.sendDtmf(6);
                                mudo = true;
                            }

                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                    }
                }, 600);*/

                break;
            case R.id.button2:


                mDrawerHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            try {
                                Thread.sleep(200);
                                call.endCall();
                                Thread.sleep(600);
                                chronometer.stop();
                                Thread.sleep(600);
                                onBackPressed();

                                //finish();

                            } catch (SipException e) {
                                e.printStackTrace();
                            }
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                    }
                }, 600);


                break;
            case R.id.button3:
                /*if (speaker) {
                    (button3).setPressed(true);
                    call.setSpeakerMode(true);
                    speaker = false;
                } else {
                    call.setSpeakerMode(true);
                    speaker = true;
                }*/
                break;
        }
    }
}