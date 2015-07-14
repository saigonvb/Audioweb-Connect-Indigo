package mx.com.audioweb.indigo.TimeTracker.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.json.JSONObject;

import mx.com.audioweb.indigo.TimeTracker.api.RestService;

public class UserLoginTask extends AsyncTask<String, Void, JSONObject> {
    HttpResponse response = null;
    JSONObject data = new JSONObject();
    Context mContext;
    ProgressDialog dialog;

    public UserLoginTask(Context context, JSONObject data) {
        this.mContext = context;
        this.data = data;
        dialog = new ProgressDialog(mContext);
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(mContext, "Login,", "Please wait...", true);
    }

    @Override
    protected JSONObject doInBackground(String... urls) {
        // TODO Auto-generated method stub
        try {
            response = RestService.doPut(urls[0], data);
            Log.e("Response", response.getAllHeaders().toString());
            return RestService.JSONFormResponse(response);
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject json) {
        dialog.dismiss();
        try {

            Log.e("Voice Id", json.toString());

            int code = response.getStatusLine().getStatusCode();

            Log.e("CODE:", "" + code);

            if ((code == 200)) {
                Log.e("RESPONSE:", "" + code);
            } else if ((code == 404)) {
                Log.e("RESPONSE:", "" + code);
                Toast.makeText(mContext, "Invalid Username", Toast.LENGTH_SHORT).show();
            } else if ((code == 406)) {
                Log.e("RESPONSE:", "" + code);
                Toast.makeText(mContext, "Password is wrong", Toast.LENGTH_SHORT).show();
            } else if ((code == 408)) {
                Log.e("RESPONSE:", "" + code);
                Toast.makeText(mContext, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("RESPONSE:", "No response from server");
            }

        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
    }

}
