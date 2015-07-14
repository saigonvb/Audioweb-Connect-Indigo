package mx.com.audioweb.indigo;

/**
 * Created by Juan Acosta on 10/22/2014.
 */

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import mx.com.audioweb.indigo.Citas.Cita;
import mx.com.audioweb.indigo.Notifications.Group;

/**
 * Created by Juan Acosta on 8/11/2014.
 */
public class ClienteHttp {

    public static final String URL = "http://66.226.72.48/connect/webServices/";
    public static final String Transmicion_Url ="http://66.226.72.48/connect/liveStreaming/transmision_adaptable.php?id=";
    private static final String DATEF = "yyyy-MM-dd HH:mm:ss";
    private Gson gson = new GsonBuilder().setDateFormat(DATEF).create();


    public ArrayList<User_info> acnum(String uid) throws JSONException {
        BufferedReader bufferedReader = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost request = new HttpPost(URL + "getConferenceNumber.php");
        List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("uid", uid));
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
            request.setEntity(entity);
            HttpResponse response = httpClient.execute(request);
            bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            bufferedReader.close();
            JSONArray jsonArray = new JSONArray(stringBuffer.toString());
            if (jsonArray != null) {
                ArrayList<User_info> alertas = new ArrayList<User_info>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    alertas.add(gson.fromJson(jsonArray.getJSONObject(i).toString(), User_info.class));
                    Log.e("JSONOBJ", jsonArray.getJSONObject(i).toString());
                }
                return alertas;
            } else {
                return null;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public ArrayList<Cita> GetCitas(String id) throws JSONException {
        BufferedReader bufferedReader = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost request = new HttpPost(URL + "getOpenCitas.php");
        List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("id", id));
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
            request.setEntity(entity);
            HttpResponse response = httpClient.execute(request);
            bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            bufferedReader.close();
            String value = stringBuffer.toString();
            if (value.equals("null")) {
                return null;
            } else {
                JSONArray jsonArray = new JSONArray(value);
                Log.d("JARRAY",String.valueOf(jsonArray));
                ArrayList<Cita> alertas = new ArrayList<Cita>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    alertas.add(gson.fromJson(jsonArray.getJSONObject(i).toString(), Cita.class));
                    Log.e("JSONOBJ", jsonArray.getJSONObject(i).toString());
                }
                return alertas;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static String iniciaCita(String smen_id, String tipo, String empresa, String contacto, String latitud1, String longitud1) {
        BufferedReader bufferedReader = null;
        DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost request = new HttpPost(URL + "doCrearCita.php");
        List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("smen_id", smen_id));
        postParameters.add(new BasicNameValuePair("tipo", tipo));
        postParameters.add(new BasicNameValuePair("empresa", empresa));
        postParameters.add(new BasicNameValuePair("contacto", contacto));
        postParameters.add(new BasicNameValuePair("latitud1", latitud1));
        postParameters.add(new BasicNameValuePair("longitud1", longitud1));

        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
                    postParameters);
            request.setEntity(entity);

            HttpResponse response = httpClient.execute(request);

            bufferedReader = new BufferedReader(new InputStreamReader(response
                    .getEntity().getContent()));
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            bufferedReader.close();

            //JSONArray jsonArray = new JSONArray(stringBuffer.toString());
            String cita_id = new String(stringBuffer.toString());

            return cita_id;

        } catch (ClientProtocolException e) {

            e.printStackTrace();
            Log.d("ClientProtocolException", e.toString());

        } catch (IOException e) {

            e.printStackTrace();

            Log.d("Exception", e.toString());

        } finally {
            if (bufferedReader != null) {
                try {

                    bufferedReader.close();

                } catch (IOException e) {

                    e.printStackTrace();

                }
            }
        }
        return null;
    }

    public static String finCita(String URL, String cita_id, String latitud2, String longitud2, String serv1, String serv2, String serv3, String serv4, String rate, String descripcion) {
        BufferedReader bufferedReader = null;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost request = new HttpPost(URL + "doCerrarCita.php");
        List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("cita_id", cita_id));
        postParameters.add(new BasicNameValuePair("latitud2", latitud2));
        postParameters.add(new BasicNameValuePair("longitud2", longitud2));
        postParameters.add(new BasicNameValuePair("serv1", serv1));
        postParameters.add(new BasicNameValuePair("serv2", serv2));
        postParameters.add(new BasicNameValuePair("serv3", serv3));
        postParameters.add(new BasicNameValuePair("serv4", serv4));
        postParameters.add(new BasicNameValuePair("rate", rate));
        postParameters.add(new BasicNameValuePair("descripcion", descripcion));


        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
                    postParameters);
            request.setEntity(entity);

            HttpResponse response = httpClient.execute(request);

            bufferedReader = new BufferedReader(new InputStreamReader(response
                    .getEntity().getContent()));
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            bufferedReader.close();

            String result = new String(stringBuffer.toString());

            return result;

        } catch (ClientProtocolException e) {

            e.printStackTrace();
            Log.d("ClientProtocolException", e.toString());

        } catch (IOException e) {

            e.printStackTrace();

            Log.d("Exception", e.toString());

        } finally {
            if (bufferedReader != null) {
                try {

                    bufferedReader.close();

                } catch (IOException e) {

                    e.printStackTrace();

                }
            }
        }
        return null;
    }

    public ArrayList<Group> getGroups(String uid) throws JSONException {
        BufferedReader bufferedReader = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost request = new HttpPost(URL + "getGroups.php");
        List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("userId", uid));
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
            request.setEntity(entity);
            HttpResponse response = httpClient.execute(request);
            bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            bufferedReader.close();
            Log.e("JSON-->>",stringBuffer.toString());
            JSONArray jsonArray = new JSONArray(stringBuffer.toString());
            String string = stringBuffer.toString();
            if (!string.equals("null")) {

                ArrayList<Group> groups = new ArrayList<Group>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    groups.add(gson.fromJson(jsonArray.getJSONObject(i).toString(), Group.class));
                    Log.e("JSONOBJ", jsonArray.getJSONObject(i).toString());
                }
                return groups;
            } else {
                return null;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
