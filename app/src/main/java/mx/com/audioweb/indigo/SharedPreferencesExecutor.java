package mx.com.audioweb.indigo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Juan Acosta on 9/3/2014.
 */
public class SharedPreferencesExecutor<T> {
    private Context context;

    public SharedPreferencesExecutor(Context context){
        this.context = context;
    }

    public void saveData(String Key, T sharedPerferencesEntry){
        SharedPreferences appSharedPerfs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor perfsEditor = appSharedPerfs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(sharedPerferencesEntry);
        perfsEditor.putString(Key, json);
        perfsEditor.commit();
        Log.d("JSONVAL", json);
    }

    public T retreive(String Key, Class<T> clazz){
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        Gson gson = new Gson();
        String json = appSharedPrefs.getString(Key, "");
        return (T) gson.fromJson(json, clazz);
    }

    public ArrayList<T> retreiveAll(Class<T> clazz){
        ArrayList<T> arrliTs = new ArrayList<T>();
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        Gson gson = new Gson();
        Map<String, ?> jsonS = appSharedPrefs.getAll();
        for(Map.Entry<String,?> json : jsonS.entrySet()){
            if(json.getKey().split("_")[0].equals(clazz.getName())){
                arrliTs.add((T) gson.fromJson(json.getValue().toString(), clazz));
            }
        }
        return arrliTs;
    }

    public void removeAll(Class<T> clazz){
        SharedPreferences appSharedPerfs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor perfsEditor = appSharedPerfs.edit();
        Map<String, ?> mapEntryList = appSharedPerfs.getAll();
        for (Map.Entry<String, ?> entry : mapEntryList.entrySet()) {
            if(entry.getKey().split("_")[0].equals(clazz.getName())){
                perfsEditor.remove(entry.getKey());
                perfsEditor.commit();
            }
        }


    }
}
