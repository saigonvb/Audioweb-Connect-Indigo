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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;

import mx.com.audioweb.indigo.ClienteHttp;
import mx.com.audioweb.indigo.R;

public class Citas extends Activity {
    String name, mydate, userName, contact, tipo = "remota", emp, cont;
    int duration = Toast.LENGTH_SHORT, size;
    Button btnShowLocation;
    public static Activity ccita;
    TextView txtLat, txtTime;
    boolean citainic, cita = true;
    EditText Empresa, Contacto;
    Context context;
    Switch type;
    SharedPreferences myPrefs;
    SharedPreferences.Editor edit;
    Context mContext;

    double latitude, longitude;

    // GPSTracker class
    GPSTracker gps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citas);
        mContext = Citas.this;
        ccita = this;
        myPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        edit = myPrefs.edit();
        txtLat = (TextView) findViewById(R.id.textviewLocation);
        txtTime = (TextView) findViewById(R.id.textViewtime);
        Empresa = (EditText) findViewById(R.id.editTextEmpresa);
        Contacto = (EditText) findViewById(R.id.editTextContacto);
        type = (Switch) findViewById(R.id.switch1);
        emp = Empresa.getText().toString();
        cont = Contacto.getText().toString();
        Log.d("DATOS", emp + " || " + cont);
        Bundle extras = getIntent().getExtras();
        if (cita) {

            //btnShowLocation.setText("Finalizar Cita");
            Log.d("Cita", String.valueOf(cita));
            Empresa.setText(emp);
            Contacto.setText(cont);
            cita = false;
        } else {
            //btnShowLocation.setText("Iniciar Cita");
            Log.d("Cita", String.valueOf(cita));
            cita = true;

        }

        if (extras != null) {
            userName = extras.getString("uid");
            Log.e("EXTRAS-->citas", String.valueOf(userName));
        }
        type.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    tipo = "presencial";
                } else {
                    // The toggle is disabled
                    tipo = "remota";
                }
            }
        });
        btnShowLocation = (Button) findViewById(R.id.cita);

        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // create class object
                mContext = Citas.this;
                myPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                gps = new GPSTracker(Citas.this);

                // check if GPS enabled
                if (gps.canGetLocation()) {

                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();

                    // \n is for new line
                    //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

                    mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

                    Log.d("Location", "Latitude:" + latitude + ", Longitude:" + longitude);
                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
                citainic = myPrefs.getBoolean("cita_inicio", false);

                if (!citainic) {
                    emp = Empresa.getText().toString();
                    cont = Contacto.getText().toString();
                    if (Empresa.getText().toString().trim().length() > 0 && Contacto.getText().toString().trim().length() > 0) {
                        Log.d("DATOS", emp + " || " + cont);
                        name = Empresa.getText().toString();
                        contact = Contacto.getText().toString();
                        edit.putBoolean("cita_inicio", false);
                        edit.commit();
                        new IniciaCitaTask(getApplicationContext(), userName, tipo, name, contact, String.valueOf(latitude), String.valueOf(longitude)).execute();
                    } else {
                        Toast.makeText(getApplicationContext(), "Favor de llenar todos los campos", Toast.LENGTH_SHORT).show();
                        Log.d("DATOS", emp + " || " + cont);

                    }
                } else {
                    edit.putBoolean("cita_inicio", false);
                    edit.commit();
                    Log.e("ArrayCita", name + " ," + String.valueOf(latitude) + " " + String.valueOf(longitude) + " +" + mydate);

                    String citaid = myPrefs.getString("Cita_id", "");
                    Log.e("ID CITA", citaid);
                    Bundle cid = new Bundle();
                    cid.putSerializable("citaid", citaid);
                    startActivity(new Intent(Citas.this, Encuesta.class).putExtras(cid));
                    //finish();

                }

            }

        });
    }


    public class IniciaCitaTask extends AsyncTask<String, Void, Boolean> {

        private Context context;
        private String smen_id;
        private String tipo;
        private String empresa;
        private String contacto;
        private String latitud1;
        private String longitud1;

        public IniciaCitaTask(Context ctx, String smen_id, String tipo, String empresa, String contacto, String latitud1, String longitud1) {
            this.context = ctx;
            this.smen_id = smen_id;
            this.tipo = tipo;
            this.empresa = empresa;
            this.contacto = contacto;
            this.latitud1 = latitud1;
            this.longitud1 = longitud1;
        }


        @Override
        protected Boolean doInBackground(String... params) {
            Log.d("LoginTask", "Entra a doInBack..");
            String cita_id;
            try {
                cita_id = ClienteHttp.iniciaCita(this.smen_id, this.tipo, this.empresa, this.contacto, this.latitud1, this.longitud1);
                String id;
                Log.e("Try  ", cita_id);
                if (cita_id != null) {
                    id = cita_id;
                    edit.putString("Cita_id", id);
                    edit.commit();
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

            //Toast.makeText(this.context, "Inicio Cita", Toast.LENGTH_SHORT).show();
            Encuesta.Value = "Inicio Cita";

            String id = myPrefs.getString("User Id", "");
            new CitasTask(this.context, id).execute();

        }

    }

    public class CitasTask extends AsyncTask<String, Void, Boolean> {
        private Context context;
        ArrayList<Cita> citas_list;
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
                Log.e("Contactos", "Error cargando citas");
            } catch (Exception e) {
                Log.e("Contactos", "Error inesperado");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d("ContactosTask", "Entro onPostExecute");
            //this.context.startActivity(new Intent(this.context, AudioConferencia.class));
            Log.d("CITAS == ", String.valueOf(this.citas_list.size()));
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