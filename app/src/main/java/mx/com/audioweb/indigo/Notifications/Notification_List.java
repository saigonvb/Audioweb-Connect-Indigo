package mx.com.audioweb.indigo.Notifications;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import mx.com.audioweb.indigo.R;

public class Notification_List extends Activity {

    public static List<Notification_Info> result = new ArrayList<Notification_Info>();
    public static ContactAdapter ca;
    public static RecyclerView recList;
    public static LinearLayoutManager llm;
    public static Context baseContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseContext = getBaseContext();
        setContentView(R.layout.activity_main);
        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        ca = new ContactAdapter(result);
        recList.setAdapter(ca);


    }

    private List<Notification_Info> createList(int size) {

        List<Notification_Info> result = new ArrayList<Notification_Info>();
        for (int i = 1; i <= size; i++) {
            Notification_Info ci = new Notification_Info();
            ci.name = Notification_Info.NAME_PREFIX + i;
            ci.surname = Notification_Info.SURNAME_PREFIX + i;
            ci.email = Notification_Info.EMAIL_PREFIX + i + "@test.com";

            result.add(ci);

        }

        return result;
    }

    public static void log() {
        LogTask task = (LogTask) new LogTask().execute();
    }

    static class LogTask extends AsyncTask<Void, Void, Void> {

        String msg;
        Context context;

        public LogTask() {
        }

        @Override
        protected Void doInBackground(Void... args) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // System.out.println(msg);

       /* String currentLog = view.getText().toString();
        String newLog = msg + "\n" + currentLog;
        view.setText(newLog);*/
            Log.e("THIS", "ASDASDASDASDASDASDASD");


            recList.setHasFixedSize(true);
            llm = new LinearLayoutManager(baseContext);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            recList.setLayoutManager(llm);
            ca.notifyDataSetChanged();

            super.onPostExecute(result);
        }


    }

    public static boolean isActivityVisible() {
        return activityVisible;

    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible;

    @Override
    protected void onResume() {
        super.onResume();
        activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityPaused();
    }

}

