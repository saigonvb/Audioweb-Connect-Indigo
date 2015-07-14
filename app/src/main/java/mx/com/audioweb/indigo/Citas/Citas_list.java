package mx.com.audioweb.indigo.Citas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

import java.util.ArrayList;

import mx.com.audioweb.indigo.R;

public class Citas_list extends Activity {

    private ListView CitasList;
    private ArrayList<Cita> citas;
    private CitaAdapter adapter;
    public static int citalist = 0;
    Button addcita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citas_list);

        CitasList = (ListView) findViewById(R.id.ListViewCitas);
        addcita = (Button) findViewById(R.id.butonAddDirections);
        addcita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extras = getIntent().getExtras();
                String id = extras.getString("uid");
                Log.d("VALOR ID-->>", id);
                Bundle informacion = new Bundle();
                informacion.putSerializable("uid", id);
                startActivity(new Intent(getApplicationContext(), Citas.class).putExtras(informacion));
            }
        });

        String valor = Encuesta.Value;
        Log.d("Valor",valor);
        if (valor.equals("Fin Cita")) {
            SnackbarManager.show(
                    Snackbar.with(getApplicationContext()) // context
                            .text("Fin de la cita") // text to display
                    // action button's ActionClickListener
                    , this); // activity where it is displayed
        }
        else if(valor.equals("Inicio Cita")){
            SnackbarManager.show(
                    Snackbar.with(getApplicationContext()) // context
                            .text("Inicio su Cita") // text to display
                    // action button's ActionClickListener
                    , this); // activity where it is displayed
        }


        citas = (ArrayList<Cita>) getIntent().getSerializableExtra("citas");
        //refresh();

        if (citas != null) {
            Log.e("ARRAYLIST", String.valueOf(citas.get(0)));
            adapter = new CitaAdapter(getApplicationContext(), citas);
            CitasList.setAdapter(adapter);
        } else {
            Log.e("No tiene citas", "Activas");

        }
    }

}
