package mx.com.audioweb.indigo.TimeTracker.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.json.JSONObject;

import mx.com.audioweb.indigo.TimeTracker.api.RestService;

public class SalesCurrentLocationTask extends
        AsyncTask<String, Void, HttpResponse> {

    // ProgressDialog dialog;
    HttpResponse response = null;
    JSONObject jData;
    Context mContext;
    ProgressDialog progress;

    public SalesCurrentLocationTask(Context con, JSONObject json) {
        // TODO Auto-generated constructor stub
        jData = new JSONObject();
        this.mContext = con;
        this.jData = json;
    }

    public SalesCurrentLocationTask() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onPreExecute() {
        progress = ProgressDialog.show(mContext, "Loading", "Please Wait");
    }

    @Override
    protected HttpResponse doInBackground(String... urls) {
        try {
            // Log.e("URL",urls[0].toString());

            response = RestService.doPost(urls[0], jData);
            Log.e("Response:", response.toString());
            return response;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("Exception", e.toString());

        }
        return null;
    }

    @Override
    protected void onPostExecute(HttpResponse res) {
        try {
            // Parsing JSON response,if required
            // JSONObject jsonObject = RestService.JOSNFormResponse(res);
            int code = res.getStatusLine().getStatusCode();


            if (code == 200) {
                Log.e("RESPONSE:", "DONE");
            } else if (code == 424) {
                Log.e("RESPONSE:", "METHOD FAILED");
            } else if (code == 404) {
                Log.e("RESPONSE:", "User Name already Exist");
            } else if (code == 406) {
                Log.e("RESPONSE:", "User Name already Exist");
            } else if (code == 500) {
                Log.e("RESPONSE:", "Internal server error");
            } else {
                Log.e("RESPONSE:", "No response from server");
            }
            if (progress.isShowing()) {
                progress.dismiss();
            }
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
    }

}
