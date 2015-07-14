package mx.com.audioweb.indigo.LiveStreaming;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import mx.com.audioweb.indigo.ClienteHttp;
import mx.com.audioweb.indigo.R;

public class LiveStreaming extends Activity {

    WebView web;
    ProgressBar progressBar;
    TextView tv;
    private static final String TAG = "Live Streaming";
    String uid;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_live_streaming);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        tv =(TextView) findViewById(R.id.LSTV);
        //FullScreen

        int mUIFlag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;

        getWindow().getDecorView().setSystemUiVisibility(mUIFlag);

        web = (WebView) findViewById(R.id.webview01);
        web.getSettings().setJavaScriptEnabled(true);
        web.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        web.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);
        web.setBackgroundColor(0);
        web.setBackgroundResource(android.R.color.black);

        Log.d(TAG, "Mandar a llamar Liga");

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            uid = extras.getString("uid");
            Log.e("EXTRAS-->citas_id", uid);

            String URL = ClienteHttp.Transmicion_Url+uid;
            web.loadUrl(URL);
        }
        else {
            web.loadUrl("http://www.audiowebtv.com/transmision_adaptable.php");
        }
        //Toast.makeText(this, web.toString(), Toast.LENGTH_LONG).show();


        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            // javascript
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "Llamar Java Script");

                progressBar.setVisibility(View.GONE);
                tv.setVisibility(View.GONE);
                web.loadUrl("javascript:(function() { document.getElementsByTagName('video').play();})()");


            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
            }
        });
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Pausa");
        super.onPause();
        super.onStop();
        web.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Resumir");
        web.onResume();
        super.onResume();
    }

}
