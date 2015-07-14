package mx.com.audioweb.indigo.AudioConference;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;

/**
 * Created by Juan Acosta on 10/6/2014.
 */

public class MyPhoneStateListener extends PhoneStateListener {
    public int singalStenths = 0;

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        int singalStrength = signalStrength.getGsmSignalStrength();
        singalStenths = signalStrength.getGsmSignalStrength();
        System.out.println("----- gsm strength" + singalStrength);
        System.out.println("----- gsm strength" + singalStenths);

        if (singalStenths > 30) {
            //  signalstrength.setText("Signal Str : Good");
            //    signalstrength.setTextColor(getResources().getColor(R.color.good));
            Log.e("Strenght", "Good");
        } else if (singalStenths > 20 && singalStenths < 30) {
            //      signalstrength.setText("Signal Str : Average");
            //        signalstrength.setTextColor(getResources().getColor(R.color.average));
            Log.e("Strenght", "Average");
        } else if (singalStenths < 20) {
//            signalstrength.setText("Signal Str : Weak");
            //          signalstrength.setTextColor(getResources().getColor(R.color.weak));
            Log.e("Strenght", "WEAK");
        }
    }

}

