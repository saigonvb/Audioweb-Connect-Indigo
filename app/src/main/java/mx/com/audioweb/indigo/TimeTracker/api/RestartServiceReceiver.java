package mx.com.audioweb.indigo.TimeTracker.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestartServiceReceiver extends BroadcastReceiver {

    private static final String TAG = "RestartServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive");
        //   context.startService(new Intent(context, MainService.class));
        Intent inService = new Intent(context, MainService.class);
        context.startService(inService);
    }

}