package setec.g3.batterylevel;

import setec.g3.maincontroller.MainUI;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class BatteryLevelChecker {
	private String[] strText = new String[] {"Battery Level", "Voltage", "Status"};  
    private int voltage = 0;  
    public boolean trun = true;  
    private Handler myHandler = new Handler();
    private Activity act;
    MainUI parentClass;
    
    
    public BatteryLevelChecker(Activity _act){
    	this.act=_act;
    	parentClass=(MainUI)_act;
    	startMonitor();  
    }
    
    // using Thread to keep the process running  
    private Thread myThread = new Thread() {  
         public void run () {  
              do {  
                batteryLevelUpdate();  
                try {  
                        Thread.sleep(300000);  
                   } catch (InterruptedException e) {  
                        // TODO Auto-generated catch block  
                        e.printStackTrace();  
                   }                   
              } while (trun);  
         }  
    };  
 

	 private void startMonitor() {  
	      myThread.start();  
	 }
 
 private void batteryLevelUpdate() {  
   BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {  
     public void onReceive(Context context, Intent intent) {  
          context.unregisterReceiver(this);  
       int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);  
       int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);  
       int level = -1;  
       if (rawlevel >= 0 && scale > 0) {  
         level = (rawlevel * 100) / scale;  
       }  
       voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);  
       int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);  
       int onplug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);  
       boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;  
       boolean onUSB = onplug == BatteryManager.BATTERY_PLUGGED_USB;  
       boolean onAC = onplug == BatteryManager.BATTERY_PLUGGED_AC;  
       String strStatus = "Charging on ";  
       if (isCharging && onUSB)  
            strStatus += "USB";  
       else if (isCharging && onAC)  
            strStatus += "AC Power";  
       else  
            strStatus = "Battery Discharging";  
       /*strText[0] = "Battery Level: " + Integer.toString(level) + "%";  
       strText[1] = "Voltage: " + Integer.toString(voltage) + "mV";  
       strText[2] = strStatus; */ 
       parentClass.updateBatteryStatus(level);
       Log.d("Battery", "Battery= "+String.valueOf(level));
     }  
   };  
   IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);  
  
   act.registerReceiver(batteryLevelReceiver, batteryLevelFilter);  
 } 
 
}
