package mx.com.audioweb.indigo.TimeTracker.api;

import android.app.Application;

import com.voicevault.vvlibrary.ViGoLibrary;

/**
 * Application class where the library will be initialized, all the activities
 * can retrieve the instance from here
 */
public class VigoCONFIG extends Application {

    private final static String WEBSERVICE_USERNAME = "DzWRwv3HHY3eT47Rea8c";
    private final static String WEBSERVICE_PASSWORD = "vqByfcw6D6XcNNmVY4vY6xQJQraADe";

    // "*** VIGO_CREDENTIAL_ID ***";
    private final static String WEBSERVICE_URL = "https://a9i1.voicevault.net/RestApi850/";
    // "*** VIGO_CREDENTIAL_PWD ***";
    private final static String ORG_ID = "7ad659dc-f613-4b28-83a6-e9608a2d2bc3";
    // "*** VIGO_SERVER_URL ***";
    private ViGoLibrary mViGoLibrary;

    // "*** VIGO_APP_ID ***";

    @Override
    public void onCreate() {
        super.onCreate();

        //mViGoLibrary = ViGoLibrary.initLib(WEBSERVICE_USERNAME, WEBSERVICE_PASSWORD, WEBSERVICE_URL, ORG_ID);

        ViGoLibrary.getInstance().init(WEBSERVICE_USERNAME, WEBSERVICE_PASSWORD, WEBSERVICE_URL, ORG_ID);

    }

    /**
     * Returns the library instance
     *
     * @return library instance
     */
    /*public ViGoLibrary getViGoLibrary() {
		return mViGoLibrary;
	}*/
}
