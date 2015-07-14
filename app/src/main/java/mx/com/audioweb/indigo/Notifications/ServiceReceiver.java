package mx.com.audioweb.indigo.Notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Juan Acosta on 12/15/2014.
 */
public class ServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pusherserv = new Intent(context, PusherService.class);
            context.startService(pusherserv);
        }
    }
}
