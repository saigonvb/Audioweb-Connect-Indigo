package mx.com.audioweb.indigo.TimeTracker.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.json.JSONObject;

import mx.com.audioweb.indigo.TimeTracker.api.RestService;

public class ChangePasswordTask extends AsyncTask<String, Void, Integer> {
    HttpResponse response = null;
    JSONObject data = new JSONObject();
    Context mContext;
    ProgressDialog dialog;

    public ChangePasswordTask(Context context, JSONObject data) {
        this.mContext = context;
        this.data = data;
        dialog = new ProgressDialog(mContext);
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(mContext, "Change Password,", "Please wait...", true);
    }

    @Override
    protected Integer doInBackground(String... urls) {
        // TODO Auto-generated method stub
        try {
            Log.e("Change Password Data", data.toString());
            response = RestService.doPut(urls[0], data);
            Log.e("Response", response.getAllHeaders().toString());
            return response.getStatusLine().getStatusCode();

        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer code) {
        dialog.dismiss();
        try {

            //int code = res.getStatusLine().getStatusCode();

            Log.e("CODE:", "" + code);
            if ((code == 200)) {
                Log.e("RESPONSE:", "" + code);

            } else {
                Log.e("RESPONSE:", "No response from server");
            }

        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
    }

}
