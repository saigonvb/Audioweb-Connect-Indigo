package mx.com.audioweb.indigo.Chat.Chat;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by jcal on 24/03/15.
 */
public class ChattApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, "x4pI9a420iigxF86PsGPEPWVCIP5ZpvHnzMNqMhM", "o3a4xTvmw3YQhVLd9ECbXBi6LGNfqce0RQYQMWOZ");
    }
}
