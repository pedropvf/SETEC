package setec.g3.maincontroller;

import setec.g3.heart.DeviceScanActivity;
import setec.g3.ui.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends Activity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splashscreen);

        /* New Handler to start the Menu-Activity 
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent bleScanActivity = new Intent(Splash.this,DeviceScanActivity.class);
                Splash.this.startActivity(bleScanActivity);
                Splash.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH*4);
    }
}
