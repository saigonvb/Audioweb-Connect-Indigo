package mx.com.audioweb.indigo.Notifications;

/**
 * Created by Juan Acosta on 12/22/2014.
 */

import android.app.Activity;
import android.os.Bundle;

import mx.com.audioweb.indigo.R;

public class NotificationView extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);
    }

}
