package mx.com.audioweb.indigo.TimeTracker.api;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RestService {

    public static HttpResponse doGet(String url) throws ClientProtocolException, IOException {

        HttpClient httpclient = new DefaultHttpClient();
        // Prepare a request object
        HttpGet httpget = new HttpGet(url);
        // Accept JSON
        httpget.addHeader("accept", "application/json");
        // Execute the request
        return httpclient.execute(httpget);
    }

    public static JSONObject JSONFormResponse(HttpResponse response) {
        JSONObject json = null;
        try {

            HttpEntity entity = response.getEntity();
            // If response entity is not null
            if (entity != null) {
                // get entity contents and convert it to string
                InputStream instream = entity.getContent();
                String result = convertStreamToString(instream);
                // construct a JSON object with result
                json = new JSONObject(result);
                // Closing the input stream will trigger connection release
                instream.close();
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // Return the json
        return json;

    }

    public static String convertStreamToString(InputStream inputStream) {

        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "ISO-8859-1"));
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //   Log.i("JSON Parser", sb.toString());
        return sb.toString();

		/*if (inputStream != null) {
            StringBuilder sb = new StringBuilder();
            String line;
                try {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            return sb.toString();
            } else {
            return "";
            }*/

    }

    public static HttpResponse doPost(String url, JSONObject c) throws ClientProtocolException, IOException {
        HttpClient httpclient = new DefaultHttpClient();
        //Log.e("Httppost URL:",url.toString());
        HttpPost request = new HttpPost(url);
        StringEntity s = new StringEntity(c.toString());

        s.setContentEncoding("ISO-8859-1");
        s.setContentType("application/x-www-form-urlencoded");

        request.setEntity(s);
        request.addHeader("accept", "application/json");

        return httpclient.execute(request);
    }

    public static HttpResponse doPut(String url, JSONObject c) throws ClientProtocolException, IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPut request = new HttpPut(url);
        StringEntity s = new StringEntity(c.toString());
        s.setContentEncoding("ISO-8859-1");
        s.setContentType("application/json");

        request.setEntity(s);
        request.addHeader("accept", "application/json");

        return httpclient.execute(request);
    }

    public static HttpResponse doDelete(String url) throws ClientProtocolException, IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpDelete delete = new HttpDelete(url);
        delete.addHeader("accept", "application/json");
        return httpclient.execute(delete);

    }
}



