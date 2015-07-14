package mx.com.audioweb.indigo.Chat;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by pramos on 2/18/15.
 */
public class ChatApplication extends Application {
    public static final String YOUR_APPLICATION_ID = "SHH3f1mDN0lf90BfFoKwhso5IB4JHpZIiRivKr4K";
    public static final String YOUR_CLIENT_KEY = "moYoZwbSdbYQ0me3KUFC9odDrcRbyRKhfuTSp9SL";

    @Override
    public void onCreate() {
        super.onCreate();

        // Register your parse models here
        //ParseObject.registerSubclass(Message.class);

        // Add your initialization code here
        Parse.initialize(this, YOUR_APPLICATION_ID, YOUR_CLIENT_KEY);

        // Test creation of object
    }
}