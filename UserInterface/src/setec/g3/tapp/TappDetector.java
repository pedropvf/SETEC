package setec.g3.tapp;

import java.util.ArrayList;
import java.util.Collections;

import setec.g3.communication.CommEnumerators;
import setec.g3.communication.Message;
import setec.g3.maincontroller.MainUI;
import setec.g3.ui.R;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.TextView;

public class TappDetector implements SensorEventListener{

	/* Class Variables */
	public boolean enabledTaps=false;
	
	/* Sensor stuff */
	private SensorManager senSensorManager;
	private Sensor senAccelerometer;
	private Sensor senProximity;
	
	/* sound */
	MediaPlayer okaysound;
	MediaPlayer mplayerShake;
	MediaPlayer alarm1;
	MediaPlayer alarm2;
	MediaPlayer deadManAlarm;
	MediaPlayer tap;

	/* accelerometer */
	int flagTap = 0, flag_descida=0, flag_subida=0, tap1=0, sor=0, ok=0;
	float va=0, tempo=0;
	ArrayList<Float> absval = new ArrayList<Float>();
	//ArrayList<Float> valz = new ArrayList<Float>();
	long lastimpulse = 0;
	private float vibrateThreshold = 0;
	private long lastUpdate = 0;
	private float last_x, last_y, last_z;
	private static final int SHAKE_THRESHOLD = 20;
	private static final int SAMPLE_PERIOD_MILIS = 40;
	private static final float DEAD_MAN_THRESHOLD = (float) 0.5;
	boolean trackDeadMan;
	boolean trackDeadMan2;
	boolean trackDeadMan3;
	int deadManPhase;
	private long startDeadManTime = 0;
	private long currentDeadManTime = 0;
	private long deadManTimeMilis = 30000;
	float[] gravity = new float[3];
	
	Handler h = new Handler();
	int delay = 30000; //milliseconds
	int ok_reproduced=0;
	Runnable runnable;

	
	/* to control */
	MainUI parentClass;
	Activity act;
	
    public TappDetector(MainUI _parent, Activity _act) {
    	parentClass=_parent;
    	act=_act;
    	
    	
        /* Sensor Management */
        senSensorManager = (SensorManager) _act.getSystemService(_act.getApplicationContext().SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senProximity = senSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        
        /* sound management */
        /* sound management */
        //okaysound = MediaPlayer.create(this, R.raw.okay);
        alarm1 = MediaPlayer.create(act, R.raw.dead_man_alert_1);
        alarm2 = MediaPlayer.create(act, R.raw.dead_man_alert_2);
        deadManAlarm = MediaPlayer.create(act, R.raw.dead_man_alert_3);
        tap = MediaPlayer.create(act, R.raw.tap);
        
        /* user interface */
    	vibrateThreshold = senAccelerometer.getMaximumRange() / 2;
        //initialize vibration
        
        if(senAccelerometer != null){
	        /*	More sensor speeds (taken from api docs)
		    		SENSOR_DELAY_FASTEST get sensor data as fast as possible
		    		SENSOR_DELAY_GAME	rate suitable for games
		 			SENSOR_DELAY_NORMAL	rate (default) suitable for screen orientation changes
	         */
	        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST); //mudei para normal
	        	        
	        trackDeadMan=false;
	        trackDeadMan2=false;
	        trackDeadMan3=false;
        } else {
        }
        if(senProximity != null){
        	senSensorManager.registerListener(this, senProximity , SensorManager.SENSOR_DELAY_NORMAL);
        } else {
        }
        
        gravity[0] = 0;
        gravity[1] = 0;
        gravity[2] = 0;

    }

    
	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor mySensor = event.sensor;
		 
	    if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER && this.enabledTaps==true) {
	    	
	    	//high pass filter to filter out gravity
	    	// alpha is calculated as t / (t + dT)

	        final float alpha = (float) 0.8;

	       // Log.d("tapp" , String.valueOf(gravity[0]));
	        
	        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
	        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
	        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

	        float x = event.values[0] - gravity[0];
	        float y = event.values[1] - gravity[1];
	        float z = event.values[2] - gravity[2];
	        
	        float xg = event.values[0];
	        float yg = event.values[1];
	        float zg = event.values[2];	        
	        
	    	
	    	/*old code
	    	float x = (float) (event.values[0]);
	    	float y = (float) (event.values[1]);
	    	float z = (float) (event.values[2]);
	        */
	        
	          
	        long curTime = System.currentTimeMillis();
	        
	        if ((curTime - lastUpdate) > SAMPLE_PERIOD_MILIS) {
	            long diffTime = (curTime - lastUpdate);
	            lastUpdate = curTime;
	            
	            float deltaX = x-last_x;
	            float deltaY = y-last_y;
	            float deltaZ = z-last_z;
	            float abs_acc = (float) Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
	            //Log.d("teste" , "y = " + String.valueOf(deltaY));
	            float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

	            
	            if ((zg > 9.5) && (xg < 2 ) && (yg < 2)){
	           	    if(trackDeadMan==false){
	            		deadManPhase=1;
		            	trackDeadMan=true;
		            	startDeadManTime=System.currentTimeMillis();
	            	} else {
	            		currentDeadManTime=System.currentTimeMillis();
	            		if((currentDeadManTime-startDeadManTime)>=deadManTimeMilis){
	            			Log.d("teste_dead" , "entrou no dead man");
	            			if( (deadManPhase==1) && (trackDeadMan2==false) ){
	            				trackDeadMan2=true;
	    		            	startDeadManTime=System.currentTimeMillis();
	    		            	deadManPhase=2;
	            			} else if((deadManPhase==2) && (trackDeadMan3==false)){
	            				trackDeadMan3=true;
	    		            	startDeadManTime=System.currentTimeMillis();
	    		            	deadManPhase=3;
	            			}
	            			else if((deadManPhase==3) && (trackDeadMan3==true)){
	    		            	deadManPhase=4;
	            			}
	            		}
	            	}
	            } else {
	            	trackDeadMan=false;
	            	trackDeadMan2=false;
	            	trackDeadMan3=false;
	            	deadManPhase=0;
	            }
	            switch (deadManPhase){
	            	case 1:
	            	break;
	            	case 2:
	            		Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_DEAD_MAN_ALERT);
	            		if(!(alarm1.isPlaying())){
	            			alarm1.start();
	                    }
	            		//adicionei isto porque estava a enviar várias vezes a mensagem
	            		trackDeadMan=false;
		            	trackDeadMan2=false;
		            	trackDeadMan3=false;
		            	deadManPhase=0;
	            	break;
	            	case 3:
	            		if((alarm1.isPlaying())){
	            			alarm1.pause();
	                    }
	            		if(!(alarm2.isPlaying())){
	            			alarm2.start();
	                    }
	            	break;
	            	case 4:
	            		if((alarm2.isPlaying())){
	            			alarm2.pause();
	                    }
	            		if(!(deadManAlarm.isPlaying())){
	            			deadManAlarm.start();
	                    }
	            	break;
	            	case 0:
	            		if((alarm1.isPlaying())){
	            			alarm1.pause();
	                    }
	            		if((alarm2.isPlaying())){
	            			alarm2.pause();
	                    }
	            		if((deadManAlarm.isPlaying())){
	            			deadManAlarm.pause();
	                    }
	            	break;
	            }
	 
	            
	            if(((abs_acc > 3.0) && (absval.size()==0)) || (absval.size() != 0))
	            {
	            	//Log.d("tapi", String.valueOf(abs_acc));
	            	//if(z >Math.abs(0.5))
	            		absval.add(abs_acc);
	            	//valz.add(deltaZ);
	            }
	            
	            if((absval.size() == 10))
	            {
	            	for(int i=0; i<10; i++){
	            		//Log.d("vals" ,  String.valueOf(absval.get(i)));
	            		//Log.d("val2" ,  String.valueOf(valz.get(i)));
	            	}
	            	
	            	if(((absval.get(9)<1) && (absval.get(8)<1) && (absval.get(7)<1) && (absval.get(6)<1)))
	            	{	
	            		//if(Math.abs(Collections.max(valz)) > 1.0)
	            		//{
	            			tap1=1;
	            			//Log.d("tapi", "TAP");
	            			tap.start();
	            		//}
	            		
	            	}
	            	absval.removeAll(absval);
	          //  	valz.removeAll(valz);
	            }
	            
	            
	            //tap detector
	            if((curTime-lastimpulse)>2000){
	            	flagTap=0;
	            	
	            	if(ok==1)
	            	{
	            		Log.d("coisa" , "OK message");
	            		parentClass.speak("OK");
	            		ok_reproduced=1;
	            		ok=0;
	            	}
	            	
	            }
	            
	            //TAPS	
	            if(((tempo=(curTime-lastimpulse))>200 || flagTap==0) && (tap1==1)){
                    flagTap++;
                    tap1=0;
                    lastimpulse = curTime;
                    Log.d("coisa" ,  String.valueOf(flagTap) + " taps detected" + "tempo=" + String.valueOf(tempo));
                   

                    if((flagTap==2) && (tempo < 900)){
	                     
	                     ok=1;
	                     lastimpulse = curTime;
                    }    
                    
                    if((flagTap==3) && (ok==0) && (tempo>900)){
	                     Log.d("coisa" , "SOS message");
	                     parentClass.speak("SOS");
	                     Log.d("coisa" , "tempo" + tempo);

						Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_SOS);
	                     
                    }
                    if((flagTap==3) && (ok==1) && (tempo < 900))
                    {
                    	Log.d("coisa" , "surrounded");
                    	ok=0;
                    	

	            		parentClass.speak("Rodeado por chamas");
	            		Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_SURROUNDED_BY_FLAMES);
                    	
                    }
                    
                    if((flagTap==3) && (ok==1) && (tempo > 900))
                    {
                    	Log.d("coisa" , "nada");
                    	ok=0;
                    }
                }
	           // }
	            
	            
	            last_x = x;
	            last_y = y;
	            last_z = z;
	        }
	    }else if(event.sensor.getType()==Sensor.TYPE_PROXIMITY){
	     float prox = event.values[0];
	    }
	    
	    if((setec.g3.maincontroller.MainUI.waiting_ok==1) && (ok_reproduced==0))
	    {
	        if(h == null) {
	            h = new Handler();
	         }
			runnable = new Runnable(){
	    	    public void run(){
	    	        //send message again
	    	        
	    	        if(ok_reproduced==1)
	    	        {
	    	        	h.removeCallbacksAndMessages(runnable);
	    	        }
	    	        else
	    	        {
	    	        	//repetir mensagem
	    	        	h.postDelayed(this, 30000); 
	    	        }
	    	        
	    	    }
	    	};
	    	h.postDelayed(runnable, 30000); 
	    }
	    
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	

}