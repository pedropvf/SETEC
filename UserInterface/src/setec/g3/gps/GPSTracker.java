package setec.g3.gps;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import setec.g3.communication.CommEnumerators;
import setec.g3.communication.Message;
import setec.g3.maincontroller.MainUI;
import setec.g3.userinterface.InterfaceStatusEnumerators.indicatorStates;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class GPSTracker implements LocationListener, GpsStatus.Listener {

	public Context mContext;
	public Activity act;
	public MainUI parentClass;

	// flag for GPS status
	boolean isGPSEnabled = false;

	static Location gpsLocation; // location
	double latitude; // latitude
	double longitude; // longitude
	
	//timer
	Timer timer;
	TimerTask timerTask;
	boolean changed = false;
	boolean gpsFixed = false;
	
	

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 50; // 50 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 0; // 0 seg sempre que mexer
       
	// Declaring a Location Manager
	protected LocationManager locationManager;

	public GPSTracker(Activity activity) {
		act = activity;
		initializeGPS();
		parentClass=(MainUI)act;
	}

	public void initializeGPS() {
		try {
			Log.d("GPSTRacker", "Starting");
			locationManager = (LocationManager) act.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
			
			// getting GPS status
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			
			locationManager.addGpsStatusListener(this);

			if (isGPSEnabled) {
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
				startTimer();
			} else {
				
				showSettingsAlert();
			}	

		} catch (Exception e) {
			Log.e("posicao", e.toString());
			//showToast(e.toString());
		}

	}

	//Location listener call-back function
	public void onLocationChanged(Location location) {
		parentClass.root.updateGpsIndicator(indicatorStates.FULL);
		gpsLocation = location;
		changed = true;
		gpsFixed = true;
		Log.d("posicao", "Location changed \nLat: " + location.getLatitude() + "\nLong: " + location.getLongitude());
		Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_GPS, (float)location.getLatitude(),(float)location.getLongitude());
	 	//showToast("Location changed \nLat: " + location.getLatitude() + "\nLong: " + location.getLongitude());
		
	}
	
	public void sendLastLocation(){
		try{
		gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Log.d("Posicao", "Location is the same \nLat: " + gpsLocation.getLatitude() + "\nLong: " + gpsLocation.getLongitude());
		//showToast("Location is the same \nLat: " + gpsLocation.getLatitude() + "\nLong: " + gpsLocation.getLongitude());
		
		if(gpsFixed){
			Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_GPS, (float)gpsLocation.getLatitude(),(float)gpsLocation.getLongitude());
		}
		
		
			
		}catch (Exception e){
			Log.e("posicao", e.toString());
			//showToast(e.toString());
		}
	}
	
	public Location getLocation(){
		if(gpsLocation!=null){
		//	parentClass.root.updateGpsIndicator(indicatorStates.FULL);
			return gpsLocation;
		} else {
		//	parentClass.root.updateGpsIndicator(indicatorStates.EMPTY);
			return null;
		}
	}
	
	/**
	 * Stop using GPS listener
	 * Calling this function will stop using GPS in your app
	 * */
	public void stopUsingGPS(){
		if(locationManager != null){
			locationManager.removeUpdates(GPSTracker.this);
		}
		stoptimertask();
		Log.d("GPSTRacker", "Stopping");
	}
	
	/**
	 * Function to check GPS/wifi enabled
	 * @return boolean
	 * */
	public boolean canGetLocation() {
		
		// getting GPS status
		isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (isGPSEnabled) {
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Function to show settings alert dialog
	 * On pressing Settings button will launch Settings Options
	 * */
	public void showSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
   	 
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
 
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
 
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            	mContext.startActivity(intent);
            }
        });
 
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();
	}


	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	/*@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d("statusGPS", ""+status);
		switch (status) {
	    case LocationProvider.OUT_OF_SERVICE:
	    	//showToast("GPS Status Changed: Out of Service");
	    	parentClass.root.updateGpsIndicator(indicatorStates.EMPTY);
	        break;
	    case LocationProvider.TEMPORARILY_UNAVAILABLE:
	    	//showToast("GPS Status Changed: Temporarily Unavailable");
	    	parentClass.root.updateGpsIndicator(indicatorStates.EMPTY);
	        break;
	    case LocationProvider.AVAILABLE:
	    	//showToast("GPS Status Changed: Available");
	    	parentClass.root.updateGpsIndicator(indicatorStates.FULL);
	        break;
		}
	}*/
	
	public void onGpsStatusChanged(int event) {
	
		GpsStatus mStatus = null; 
		mStatus = locationManager.getGpsStatus(mStatus);
	    switch (event) {
	        case GpsStatus.GPS_EVENT_STARTED:
	        	Log.d("statusGPS2", "" + event);
	            break;

	        case GpsStatus.GPS_EVENT_STOPPED:
	        	Log.e("statusGPS2", "" + event);
	            break;

	        case GpsStatus.GPS_EVENT_FIRST_FIX:
	        	Log.e("statusGPS2", "" + event);
	        	parentClass.root.updateGpsIndicator(indicatorStates.FULL);
	    		gpsFixed = true;
	            break;

	        case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
	        	
	        	final GpsStatus gs = locationManager.getGpsStatus(null);
	            int i = 0;
	            final Iterator<GpsSatellite> it = gs.getSatellites().iterator();
	            while( it.hasNext() ) {
	                if(it.next().usedInFix()){
	                	i += 1;
	                }
	            }
	            Log.d("statusGPS2", "satelites = " + i);
	            if(i<4){
	            	parentClass.root.updateGpsIndicator(indicatorStates.EMPTY);
	            	gpsFixed = false;
	            }
	            if(i>=4){
	            	parentClass.root.updateGpsIndicator(indicatorStates.FULL);
	            	gpsFixed = true;
	            }
	            
	        	/*Log.d("statusGPS2", "" + event);
	        	if(mStatus!=null){
	        		//Iterable<GpsSatellite> coisa = mStatus.getSatellites();
	        		int num = mStatus.getMaxSatellites();
	        		if(num<3){
	        			Log.e("statusGPS2", "Nao ha GPS");
	        		}
	        	}
	        	*/
	            break;
	    }

	}
	
	
	
	 public void startTimer() {
	    	
    	 timer = new Timer();
    	 initializeTimerTask();
    	 timer.schedule(timerTask, 30000, 45000); 
    }

    public void stoptimertask() {
    	
    	if (timer != null) {
    	       timer.cancel();
    	       timer = null;
    	}
    }

    public void initializeTimerTask() {
    	
    	timerTask = new TimerTask() {
    		public void run() {
    			Log.d("posicao", "TIMER");
    			if (canGetLocation()){
    				if(changed==false){
        				sendLastLocation();
        				Log.d("posicao", "changed=FALSE");
        			}else{
        				Log.d("posicao", "changed=TRUE");
        			}
        			changed = false;
    			}else{
    				Log.d("posicao", "GPS not found (yet)");
    			}
    			
    		}
    	};
    }
    
    public void setChangedFlag(boolean _changed){
    	changed = _changed;
    }
    
    public void showToast(final String toast){
    	act.runOnUiThread(new Runnable() {
		    public void run() {
		        Toast.makeText(act.getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
		    }
		});
    }
    
    public String  GPStime(){
    	String aux = null;
    	
		long hours;
		long minutes;
    	long seconds;
    	long miligps=gpsLocation.getTime();
    	
    	hours=(miligps % (24*60*60*1000))/(60*60*1000);
    	minutes=(miligps % (60*60*1000))/(60*1000);
    	seconds=(miligps % (60*1000))/1000;
    	
    	aux = Long.toString(hours) + ":" + Long.toString(minutes) + ":" + Long.toString(seconds); 
    	
       	return aux;
    }
    
    public double[] NewCoord(int dist, double Ndegrees) {
    	double[] coord = new double[2];
    	coord[0]=-1;
    	coord[1]=-1;
	   	if(gpsLocation==null){
	   		showToast("Sem sinal.");
	   		return coord;
	   	} else {
	    	int rt=6371000;
	    	
	        double Mlat = gpsLocation.getLatitude();
	    	double Mlong = gpsLocation.getLongitude();
	    	
	    	Mlat = Mlat + (180*dist*Math.cos(Math.toRadians(Ndegrees))/(Math.PI*(rt)));
	    	Mlong = Mlong + ((180*dist*Math.sin(Math.toRadians(Ndegrees)))/(Math.PI*Math.cos(Math.abs(Mlat))*(rt)));
	    	
	    	coord[0]=Mlat;
	    	coord[1]=Mlong;
	    	
	    	return coord;
	   	}
    }

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
