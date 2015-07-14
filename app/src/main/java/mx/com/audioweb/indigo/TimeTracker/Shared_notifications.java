package mx.com.audioweb.indigo.TimeTracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Juan Acosta on 11/5/2014.
 */
public class Shared_notifications {
    private static final String PREF_NAME = "indigoPref";
    private static final String IS_NOTIFICATION = "isNotification";
    public static String valor = "";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;


    public Shared_notifications(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createNotification() {
        editor.putString(IS_NOTIFICATION, "true");
        valor = "true";
        Log.e("Valor--Not --", valor);
        editor.commit();
    }

    //Metodo que verifica si ya se hizo el login
    public boolean checkNotification() {
        SharedPreferences sharedPreferences = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        String checkBoxValue = sharedPreferences.getString(IS_NOTIFICATION, "");
        if (checkBoxValue.equals("true")) {
            valor = "true";
            Log.e("Valor--Not --", valor);
            return true;

        } else {
            valor = "false";
            Log.e("Valor--Not --", valor);
            return false;
        }
    }

    public void clearNotification() {
        editor.putString(IS_NOTIFICATION, "false");
        valor = "false";
        Log.e("Valor--Not --", valor);
        editor.commit();
    }

    public void RestartNotification() {
        editor.clear();
        editor.commit();
    }

}
