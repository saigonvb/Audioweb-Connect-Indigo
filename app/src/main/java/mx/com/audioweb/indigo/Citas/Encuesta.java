package mx.com.audioweb.indigo.Citas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import mx.com.audioweb.indigo.ClienteHttp;
import mx.com.audioweb.indigo.R;

public class Encuesta extends Activity {

    CheckBox s1, s2, s3, s4;
    RatingBar rating;
    SharedPreferences myPrefs;
    Context mContext;
    SharedPreferences.Editor edit;
    public static String Value = "hola";

    String ser1 = "0", ser2 = "0", ser3 = "0", ser4 = "0", cita_id, calif, des;
    EditText descripcion;
    double latitude, longitude;
    Button enviar;
    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encuesta);

        myPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        edit = myPrefs.edit();

        s1 = (CheckBox) findViewById(R.id.checkBox);
        s2 = (CheckBox) findViewById(R.id.checkBox2);
        s3 = (CheckBox) findViewById(R.id.checkBox3);
        s4 = (CheckBox) findViewById(R.id.checkBox4);
        rating = (RatingBar) findViewById(R.id.ratingBar);
        descripcion = (EditText) findViewById(R.id.descripcion_text);
        enviar = (Button) findViewById(R.id.buttonEnviar);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            cita_id = extras.getString("citaid");
            Log.e("EXTRAS-->citas_id", cita_id);
        }
        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {

                calif = String.valueOf(rating);
                Log.d("Rate", calif);

            }
        });

        enviar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Bundle uid = new Bundle();
                //uid.putSerializable("uid", id);
                //Bundle extras = getIntent().getExtras();
                //String cita_id;
                gps = new GPSTracker(Encuesta.this);
                if (gps.canGetLocation()) {

                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();

                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
                //ArrayList<String> latlon = getIntent().getStringArrayListExtra("mStringArray");
                Log.e("LAT,Long", "LAT: " + latitude + "LON: " + longitude);
                des = descripcion.getText().toString();

                new FinCitaTask(getApplicationContext(), cita_id, String.valueOf(latitude), String.valueOf(longitude), ser1, ser2, ser3, ser4, calif, des).execute();
            }
        });
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.checkBox:
                if (checked) {
                    ser1 = "1";
                } else {
                    ser1 = "0";
                }
                // Remove the meat
                break;
            case R.id.checkBox2:
                if (checked) {
                    ser2 = "1";
                }
                // Cheese me
                else {
                    ser2 = "0";
                }
                // I'm lactose intolerant
                break;
            case R.id.checkBox3:
                if (checked) {
                    ser3 = "1";
                } else {
                    ser3 = "0";
                }
                break;
            case R.id.checkBox4:
                if (checked) {
                    ser4 = "1";
                } else {
                    ser4 = "0";
                }
                break;
            // TODO: Veggie sandwich
        }
    }

    public class FinCitaTask extends AsyncTask<String, Void, Boolean> {

        private Context context;
        private String cita_id;
        private String latitud2;
        private String longitud2;
        private String serv1;
        private String serv2;
        private String serv3;
        private String serv4;
        private String rate;
        private String descripcion;

        public FinCitaTask(Context ctx, String cita_id, String latitud2, String longitud2, String serv1, String serv2, String serv3, String serv4, String rate, String descripcion) {
            this.context = ctx;
            this.cita_id = cita_id;
            this.latitud2 = latitud2;
            this.longitud2 = longitud2;
            this.serv1 = serv1;
            this.serv2 = serv2;
            this.serv3 = serv3;
            this.serv4 = serv4;
            this.rate = rate;
            this.descripcion = descripcion;
        }


        @Override
        protected Boolean doInBackground(String... params) {
            Log.d("LoginTask", "Entra a doInBack..");
            JSONArray jsonArray;
            JSONObject jsonObject;
            String result;
            try {
                result = ClienteHttp.finCita(ClienteHttp.URL, this.cita_id, this.latitud2, this.longitud2, this.serv1, this.serv2, this.serv3, this.serv4, this.rate, this.descripcion);
                String id;
                Log.e("Try  ", result);
                if (cita_id != null) {
                    id = result;
                    Log.d("JSON ", id);

                    return false;
                } else {
                    //this.message = "Error Login";
                    Log.d("LoginTask", "ErrorLogin");
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
                //this.message = "Error Inesperado";
                return true;
            }
        }


        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("LoginTask", "Entra a onPostExecute..");

            //Toast.makeText(this.context, "Fin Cita", Toast.LENGTH_SHORT).show();
            //Intent typeIntent = new Intent(this.context, Home.class);
            Value = "Fin Cita";
            String id = myPrefs.getString("User Id", "");
            Bundle uid = new Bundle();
            uid.putSerializable("uid", id);
            new CitasTask(this.context,id).execute();
            //Citas.ccita.finish();
            //finish();


        }

    }

    public class CitasTask extends AsyncTask<String, Void, Boolean> {
        private Context context;
        ArrayList<Cita> citas_list;
        private Bundle informacion;
        private String id;

        public CitasTask(Context context,  String id) {
            this.context = context;
            this.id = id;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                ClienteHttp clienteHttp = new ClienteHttp();

                Log.i("ID->>", this.id);
                citas_list = clienteHttp.GetCitas(this.id);
                Log.i("CITA_LIST--??",String.valueOf(citas_list));
                if (citas_list == null) {
                    Log.d("Message", "No existen citas");
                } else {

                    Log.e("CITAS",String.valueOf(citas_list));


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
            //Log.d("CITAS == ", String.valueOf(this.citas_list.size()));
            String id = myPrefs.getString("User Id", "");
            Bundle uid = new Bundle();
            uid.putSerializable("uid", id);
            informacion = new Bundle();
            informacion.putSerializable("citas", citas_list);
            startActivity(new Intent(this.context, Citas_list.class).putExtras(informacion).putExtras(uid));
            finish();
        }

    }
}
