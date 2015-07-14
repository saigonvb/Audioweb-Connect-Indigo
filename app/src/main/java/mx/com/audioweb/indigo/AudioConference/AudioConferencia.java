package mx.com.audioweb.indigo.AudioConference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import mx.com.audioweb.indigo.R;

public class AudioConferencia extends Activity {

    Button IAC;
    TextView CA;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_conferencia);

        CA = (TextView) findViewById(R.id.pinText);
        IAC = (Button) findViewById(R.id.unirse_reunion);
        IAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isOnline()) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);
                    alertDialogBuilder
                            .setMessage("Tipo de Llamada")
                            .setCancelable(false)
                            .setPositiveButton("Llamada por Datos", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, close
                                    // current activity
                                    //AudioConferencia.this.finish();
                                    Intent intent = new Intent(context, Sip_Call_Activity.class).putExtra("Codigo", CA.getText().toString());
                                    context.startActivity(intent);
                                    finish();

                                }
                            })
                            .setNegativeButton("Llamada por tiempo Aire", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, just close
                                    // the dialog box and do nothing
                                    //dialog.cancel();
                                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                                    //callIntent.setData(Uri.parse("tel:018000832002" + PhoneNumberUtils.PAUSE + npin + "#"));
                                    callIntent.setData(Uri.parse("tel:5528814600" + PhoneNumberUtils.PAUSE + CA.getText().toString() + "#" + PhoneNumberUtils.PAUSE + "#"));
                                    startActivity(callIntent);
                                    Log.i("LLAMANDO ", callIntent.toString());
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();

                } else {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    //callIntent.setData(Uri.parse("tel:018000832002" + PhoneNumberUtils.PAUSE + npin + "#"));
                    callIntent.setData(Uri.parse("tel:5528814600" + PhoneNumberUtils.PAUSE + CA.getText().toString() + "#" + PhoneNumberUtils.PAUSE + "#"));
                    startActivity(callIntent);
                    Log.i("LLAMANDO ", callIntent.toString());
                }
            }
        });

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

}
