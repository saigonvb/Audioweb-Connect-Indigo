package mx.com.audioweb.indigo.Chat.Chat;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.PushService;
import com.parse.SaveCallback;

import mx.com.audioweb.indigo.Home;


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
        PushService.setDefaultPushCallback(this, Home.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();


        ParsePush.subscribeInBackground("Audioweb", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });


    }
}