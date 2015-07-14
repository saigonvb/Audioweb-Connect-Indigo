package mx.com.audioweb.indigo.TimeTracker.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceStarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MainService.class);
        context.startService(i);
        Log.e("ServiceStarter", "Started");
    }
}
