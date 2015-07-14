package mx.com.audioweb.indigo.Citas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import mx.com.audioweb.indigo.R;

/**
 * Created by Juan Acosta on 11/10/2014.
 */
public class CitaAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Cita> cita_items;

    public CitaAdapter(Context context, ArrayList<Cita> cita_items) {
        this.context = context;
        this.cita_items = cita_items;
    }

    public int getCount() {
        return this.cita_items.size();
    }

    public Object getItem(int position) {
        return this.cita_items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.cita, null);
        }

        TextView CF = (TextView) convertView.findViewById(R.id.CFText);
        TextView min = (TextView) convertView.findViewById(R.id.minText);
        Button delete = (Button) convertView.findViewById(R.id.buttonDeleteCita);

        CF.setText(cita_items.get(position).getEmpresa());
        min.setText(cita_items.get(position).getFechaInicio());
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = cita_items.get(position).getId();
                Bundle cid = new Bundle();
                cid.putSerializable("citaid", id);
                Log.d("ID-->",id);
                context.startActivity(new Intent(context, Encuesta.class).putExtras(cid).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        return convertView;
    }

}