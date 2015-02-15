package setec.g3.maincontroller;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import setec.g3.batterylevel.BatteryLevelChecker;
import setec.g3.communication.CommEnumerators;
import setec.g3.communication.CommEnumerators.Protocol;
import setec.g3.communication.Message;
import setec.g3.communication.ReadNet;
import setec.g3.communication.ReadProtocol;
import setec.g3.gps.GPSTracker;
import setec.g3.heart.BluetoothLeService;
import setec.g3.heart.SampleGattAttributes;
import setec.g3.tapp.TappDetector;
import setec.g3.texttospeech.TextToSpeechHandler;
import setec.g3.ui.R;
import setec.g3.userinterface.FlyOutContainer;
import setec.g3.userinterface.InterfaceStatusEnumerators;
import setec.g3.userinterface.InterfaceStatusEnumerators.LineOfFireSituation;
import setec.g3.userinterface.InterfaceStatusEnumerators.PriorityLevel;
import setec.g3.userinterface.InterfaceStatusEnumerators.UILanguage;
import setec.g3.userinterface.InterfaceStatusEnumerators.indicatorStates;
import setec.g3.userinterface.InterfaceStatusEnumerators.userRanks;
import setec.g3.userinterface.InterfaceStatusEnumerators.utilityStates;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.renderscript.RenderScript.Priority;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MainUI extends Activity implements SensorEventListener{

	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * APP VARIABLES
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/* app usage control */
	public userRanks userLevel;
	public Context context;
	public UILanguage language=UILanguage.PT;
	
	/* for communication */
	/* for communication */
	public static byte firemanID = Message.firemanID;
	public static ObjectOutputStream output;
	private ReadNet readNet;
	private ReadProtocol readProtocol;
	private String host;
	private int port;
	private Protocol protocol;
	private Message msg;
	private ImageView protocolToggler;
	private TextView protocolTogglerText;
	private boolean isCommander;
	private String team;
	
	private Message messageHandler;
	
	/* the user interface */
	public FlyOutContainer root;

	/* screen */
	float center[];
	float screenWH[];
	
	/* alarm utility */
	private MediaPlayer sounds;
	private ImageView toggleAlarm;
		private utilityStates alarmUtility=utilityStates.UTILITY_OFF;
		
	/* led utility */
	private ImageView toggleLantern;
		private utilityStates lanternUtility=utilityStates.UTILITY_OFF;
		private Camera camera;
		private Parameters p;
	
    /* line of fire distance measurement */
    private TextView thousandsText, hundredsText, dozensText, unitsText;
    private Button sendDistance;
    private LineOfFireSituation lineOfFireSituation=LineOfFireSituation.ACTIVE;
    private int lineOfFireDistanceSends=0;
	
	/* Compass */
	private boolean targetMode=false;
	private boolean compassEnabled=true;
		
	/* gps tracker */
	private SensorManager mSensorManager;
	private GPSTracker gps;
	public int rt=6371000;
	double Olat=41.178317;//autoestrada
	double Olong=-8.593368;
	protected Handler gpsCompassAngleHandler = new Handler();
	protected Runnable gpsCompassAngleRunnable  = new GpsCompassAngleRunnable();
	protected Handler gpsDistanceHandler = new Handler();
	protected Runnable gpsDistanceRunnable  = new GpsDistanceRunnable();
	private long gpsDistanceRunnableDelta = 1000;
	private long gpsCompassAngleDelta=50;
	protected Handler gpsTargetGuideHandler = new Handler();
	protected Runnable gpsTargetGuideRunnable;
	private long gpsTargetGuideDelta = 3000;
	double northDegree;
	double targetDegree;
	
	/* logout and exit */
	private Button logoutBtn, exitBtn;
	
	/* text to speech */
	TextToSpeechHandler textToSpeechHandler;
	boolean textToSpeechEnabled=true;
	
	/* Tap Detector */
	private TappDetector tapDetector;
	private MediaPlayer deadManAlarms;
	
	/* Bluetooth Hardware Interface */
	Handler h;
    final int RECIEVE_MESSAGE = 1;        // Status  for Handler
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();
    private ConnectedThread mConnectedThread;
    //Used vars
    public static byte nrSessao=0x00;
    public static byte delimiter=0x0A;
    static byte[] dataE = new byte[6];
    static byte[] dataR = new byte[6];
    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // MAC-address of Bluetooth module (you must edit this line)
    //TODO
    private static String address = "F8:A9:D0:F2:D9:39";
    
    /* Bluetooth Low Energy */
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	public BluetoothLeService mBluetoothLeService= null;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	public BluetoothGattCharacteristic mNotifyCharacteristic=null;
	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";
	// Various UI stuff
	private String mDeviceAddress;
	private String mDeviceName;
	
	//bleHandler para correr aquilo passando x tempo
	private final long interval = 2000;
	
	/* HeartRate shit */
	//Variavel onde fica guardado o ritmo cardiaco
	private int heart=0;
	private int heartPassado=0;
	private Handler heartRateHandler=new Handler();
	private HeartRateRunnable heartRateDisplayRunnable = new HeartRateRunnable();
	private long heartRateDelta=1000;
	private boolean bleServiceInitialised=false;
	private boolean canDoBle=false;
	private Handler heartRateSenderHandler=new Handler();
	private HeartRateSenderRunnable heartRateSenderRunnable = new HeartRateSenderRunnable();
	private long heartRateSendDelta=60000;
	
	/* low battery */
	BatteryLowReceiver batteryLow;
	private BatteryLevelChecker bateryChecker;
	
	/*Network strength*/
	TelephonyManager Tel;
	MyPhoneStateListener MyListener;
	
	/*Power manager*/
	PowerManager pMg;
	WakeLock wakeLock; 
	
	public boolean waiting_ok=false;
	
	/* To send the team */
	public Button btn_send_team;
	public EditText txt_send_team;
	/************************************************************************************************************************************
	 *************************************************************************************************************************************/

	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * APP LIFETIME FLOW
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/	
	/* 
	 * Upon creation do:
	 * 		0- get user privileges
	 * 		1- inflate main_ui.xml (convert from xml into java object)
	 * 		2- put it as root view
	 * 		3- attach its components to handlers
	 * 		4- add the components listeners
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context=this.getApplicationContext();
		this.root = (FlyOutContainer) this.getLayoutInflater().inflate(R.layout.main_ui, null);
		this.setContentView(root);
		root.setParentActivity(this);
		getUserLevel();
		initialiseController();
		initialiseComm();
		onBleCreate();
		
		lowBatteryCreate();
		
		//setUpHwBluetoothHandler();
		
		//set up partial wakeLock
		pMg = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if (pMg == null) {
			Log.e("MainUI", "PowerManager is null");
		}else{
			wakeLock = pMg.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK , "MainUI");
			wakeLock.acquire();
		}

	}
	/*
	 * Inflate the menu and adds items to the action bar if it is present.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sample, menu);
		
		
		
		return true;
	}
	@Override
	protected void onResume() {
		super.onResume();
		
		// for the system's orientation sensor registered listeners
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
		//gps.initializeGPS();
		//onResumeHwBluetoothSetUp();
		onBleResume();
	}
	@Override
	protected void onPause() {
		super.onPause();
		
		// to stop the listener and save battery
		//mSensorManager.unregisterListener(this);
		//gps.stopUsingGPS();
		//onPauseHwBluetoothSetUp();
		//onBlePause();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mSensorManager.unregisterListener(this);
		//tapDetector.
		camera.release();
		textToSpeechHandler.textToSpeechFinish();

		if(readNet!=null){
        	readNet.kill_thread();
        }
        if(readProtocol!=null){
        	readProtocol.kill_thread();
        }
        bateryChecker.trun=false;
        
		onBleDestroy();
		
		wakeLock.release();
	}
	/*************************************************************************************************************************************
	 ************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * INITIALISATION
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/	
	/*
	 * To Initialise the Controller
	 */
	private void initialiseController(){
		attachComponents();
		initialiseUI();
		addInterfaceListeners();
		initializeFeatures();
		
		resetUI();
	}
	
	/* 
	 * Used to fetch the components from XML 
	 */	 
	public void attachComponents(){
		/* utilities */
		toggleAlarm = (ImageView) findViewById(R.id.utility_alarm);
		toggleLantern = (ImageView) findViewById(R.id.utility_lamp);
		
		/* distance to line of fire */
	    thousandsText = (TextView)  findViewById(R.id.thousands);
	    hundredsText = (TextView)  findViewById(R.id.hundreds);
	    dozensText = (TextView)  findViewById(R.id.dozens);
	    unitsText = (TextView)  findViewById(R.id.units);
	    sendDistance = (Button) findViewById(R.id.btn_send_distance);
	    
	    /* logout and exit */
	    logoutBtn = (Button) findViewById(R.id.logout_btn);
	    exitBtn = (Button) findViewById(R.id.exit_btn);
	    
	    /* communication */
	    protocolToggler = (ImageView) findViewById(R.id.protocol_selector_btn);
	    protocolTogglerText = (TextView ) findViewById(R.id.protocol_selector_text);
	    
	    btn_send_team = (Button) findViewById(R.id.send_team_btn);
	    txt_send_team = (EditText) findViewById(R.id.send_team_txt);
	}
	 /* 
	  * To set the components listeners. 
	  */
	public void addInterfaceListeners(){
			
		toggleAlarm.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	root.vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
			    if(alarmUtility==utilityStates.UTILITY_OFF){
			    	alarmUtility=utilityStates.UTILITY_ON;
			    	toggleAlarm.setImageResource(R.drawable.utility_alarm_on);
			    	if(language==UILanguage.EN){
				    	toastMessage("Alarm ON",Toast.LENGTH_SHORT, 0, 0);
			    	} else if (language==UILanguage.PT){
				    	toastMessage("Alarme LIGADO",Toast.LENGTH_SHORT, 0, 0);
			    	}
			    	playAlarm();
			    } else {
			    	alarmUtility=utilityStates.UTILITY_OFF;
			    	toggleAlarm.setImageResource(R.drawable.utility_alarm_off);
			    	if(language==UILanguage.EN){
				    	toastMessage("Alarm OFF",Toast.LENGTH_SHORT, 0, 0);
			    	} else if (language==UILanguage.PT){
				    	toastMessage("Alarme DESLIGADO",Toast.LENGTH_SHORT, 0, 0);
			    	}
			    	stopAlarm();
			    }
		    }
		});
		toggleLantern.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	root.vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
			    if(lanternUtility==utilityStates.UTILITY_OFF){
			    	lanternUtility=utilityStates.UTILITY_ON;
			    	toggleLantern.setImageResource(R.drawable.utility_lamp_on);
			    	if(language==UILanguage.EN){
				         toastMessage("Lantern ON",Toast.LENGTH_SHORT, 0, 0);
			    	} else if (language==UILanguage.PT){
				         toastMessage("Lanterna LIGADA",Toast.LENGTH_SHORT, 0, 0);
			    	}
			         p.setFlashMode(Parameters.FLASH_MODE_TORCH);
			         camera.setParameters(p);
			         camera.startPreview();
			    } else {
			    	lanternUtility=utilityStates.UTILITY_OFF;
			    	toggleLantern.setImageResource(R.drawable.utility_lamp_off);
			    	if(language==UILanguage.EN){
				         toastMessage("Lantern OFF",Toast.LENGTH_SHORT, 0, 0);
			    	} else if (language==UILanguage.PT){
				         toastMessage("Lanterna DESLIGADA",Toast.LENGTH_SHORT, 0, 0);
			    	}
			    	p.setFlashMode(Parameters.FLASH_MODE_OFF);
			    	camera.setParameters(p);
			    	camera.stopPreview();
			    }
		    }
		});
		
	    sendDistance.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	int distanceValue = Integer.parseInt(thousandsText.getText().toString())*(int)1000 +  Integer.parseInt(hundredsText.getText().toString())*(int)100 + Integer.parseInt(dozensText.getText().toString())*(int)10 + Integer.parseInt(unitsText.getText().toString());
		    	
		    	int lineOfFireSituationCode=0;
		    	switch (lineOfFireSituation){
		    	case ACTIVE:
		    		lineOfFireSituationCode=0;
		    		break;
		    	case CONTROLLED:
		    		lineOfFireSituationCode=1;
		    		break;
		    	case VIGILLANCE:
		    		lineOfFireSituationCode=2;
		    		break;
		    	case DELETED:
		    		lineOfFireSituationCode=3;
		    		break;
		    	}
		    	
		    	//sendDistance(distanceValue, lineOfFireSituationCode);
		    	double[] coords=gps.NewCoord(distanceValue, northDegree);
		    	if(coords[0]!=-1 && coords[1]!=-1){
			    	Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_FIRELINE_GPS, (float)coords[0],  (float)coords[1]);
			    	Log.d("lineOfFire", "Lat="+(float)coords[0]+"    Long= "+(float)coords[1]);
			    	//TODO as coordenadAS estão a estourar
			    	root.vibrate(200);
			    	root.resetDistance();
			    	lineOfFireDistanceSends++;
			    	if(lineOfFireDistanceSends<2){
			    		String m=new String();
				    	if(language==UILanguage.EN){
					    	m=new String("Distance reported. Please repeat "+(2-lineOfFireDistanceSends)+" time(s).");
				    	} else if (language==UILanguage.PT){
				    		m=new String("Distância reportada. Por favor reenvie mais "+(2-lineOfFireDistanceSends)+" vez(es).");
				    	}
				    	toastMessage(m,Toast.LENGTH_SHORT, 0, (int)(center[1]*2-500));
			    	} else {
			    		lineOfFireDistanceSends=0;
			    		if(language==UILanguage.EN){
			    			toastMessage("Distance reported.",Toast.LENGTH_SHORT, 0, (int)(center[1]*2-500));
				    	} else if (language==UILanguage.PT){
				    		toastMessage("Distância reportada.",Toast.LENGTH_SHORT, 0, (int)(center[1]*2-500));
				    	}
			    	}
		    	}
		    }
		});
	    root.lineOfFireSituationSenderSelector.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	int lineOfFireSituationCode=0;
		    	switch (lineOfFireSituation){
		    	case ACTIVE:
		    		lineOfFireSituationCode=0;
		    		break;
		    	case CONTROLLED:
		    		lineOfFireSituationCode=1;
		    		break;
		    	case VIGILLANCE:
		    		lineOfFireSituationCode=2;
		    		break;
		    	case DELETED:
		    		lineOfFireSituationCode=3;
		    		break;
		    	}
		    	
		    	
		    	root.vibrate(200);
		    	root.resetDistance();
		    	if(language==UILanguage.EN){
			    	toastMessage("Line of Fire Situation reported.",Toast.LENGTH_SHORT, 0, (int)(center[1]*2-500));
		    	} else if (language==UILanguage.PT){
			    	toastMessage("Situação da Linha de Fogo reportada.",Toast.LENGTH_SHORT, 0, (int)(center[1]*2-500));
		    	}
		    	
		    	Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_FIRELINE_STATUS, lineOfFireSituationCode);
		    	Log.d("lineOfFire", "Line of fire="+lineOfFireSituationCode);
		    	
		    }
		});
	    logoutBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	buildLogoutDialogue();
		    }
		});
	    exitBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	buildExitDialogue();
		    }
		});
	    protocolToggler.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	if(protocol == Protocol.PROTOCOL_G5){
		    		protocol = Protocol.PROTOCOL_G6;
		    	}else{
		    		protocol = Protocol.PROTOCOL_G5;
		    	}
		    	root.vibrate(200);
		    	toggleProtocol();
		    }
		});
	    root.compassEnablerBtn.setOnTouchListener(
	       		new RelativeLayout.OnTouchListener() {
	       			public boolean onTouch(View v, MotionEvent m) {
	       				switch (m.getAction()){
		       				case MotionEvent.ACTION_DOWN:
		       					setCompassOperation(!compassEnabled);    						
		       					break;
		       				case MotionEvent.ACTION_MOVE:
		       					break;
		       				case MotionEvent.ACTION_UP:
		       					break;
		       			} 				
	       			    return true;
	       			}
	       		}
	       );
	    root.voiceOverBtn.setOnTouchListener(
	       		new RelativeLayout.OnTouchListener() {
	       			public boolean onTouch(View v, MotionEvent m) {
	       				switch (m.getAction()){
		       				case MotionEvent.ACTION_DOWN:
		       					textToSpeechEnabled=!textToSpeechEnabled;
		       					root.setTextToSpeech(textToSpeechEnabled);
		       					break;
		       				case MotionEvent.ACTION_MOVE:
		       					break;
		       				case MotionEvent.ACTION_UP:
		       					break;
		       			}
		       			return true;
	       			}
	       		}
	       );
	    btn_send_team.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
	       				root.vibrate(200);		       					
		       			Message.send(15, Integer.parseInt(txt_send_team.getText().toString()));
	       			}
	       		}
	       );
	    root.lineOfFireSituationSelector.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	root.vibrate(InterfaceStatusEnumerators.buttonTapVibrationDuration);
		    	switch (lineOfFireSituation){
		    	case ACTIVE:
		    		lineOfFireSituation=LineOfFireSituation.CONTROLLED;
		    		root.lineOfFireSituationSelector.setImageResource(R.drawable.selector_line_of_fire_controlled);
		    		if(language==UILanguage.EN){
			    		root.toastMessage("Line of Fire Situation: Under Control", Toast.LENGTH_SHORT, 0, 0);
			    	} else if (language==UILanguage.PT){
			    		root.toastMessage("Situação da Linha de Fogo: Sob Controlo", Toast.LENGTH_SHORT, 0, 0);
			    	}
		    		break;
		    	case CONTROLLED:
		    		lineOfFireSituation=LineOfFireSituation.VIGILLANCE;
		    		root.lineOfFireSituationSelector.setImageResource(R.drawable.selector_line_of_fire_vigillance);
		    		if(language==UILanguage.EN){
			    		root.toastMessage("Line of Fire Situation: Under Vigillance", Toast.LENGTH_SHORT, 0, 0);
			    	} else if (language==UILanguage.PT){
			    		root.toastMessage("Situação da Linha de Fogo: Sob Vigilância", Toast.LENGTH_SHORT, 0, 0);
			    	}
		    		break;
		    	case VIGILLANCE:
		    		lineOfFireSituation=LineOfFireSituation.DELETED;
		    		root.lineOfFireSituationSelector.setImageResource(R.drawable.selector_line_of_fire_deleted);
		    		if(language==UILanguage.EN){
			    		root.toastMessage("Line of Fire Situation: Deleted", Toast.LENGTH_SHORT, 0, 0);
			    	} else if (language==UILanguage.PT){
			    		root.toastMessage("Situação da Linha de Fogo: Removida", Toast.LENGTH_SHORT, 0, 0);
			    	}
		    		break;
		    	case DELETED:
		    		lineOfFireSituation=LineOfFireSituation.ACTIVE;
		    		root.lineOfFireSituationSelector.setImageResource(R.drawable.selector_line_of_fire_active);
		    		if(language==UILanguage.EN){
			    		root.toastMessage("Line of Fire Situation: Active", Toast.LENGTH_SHORT, 0, 0);
			    	} else if (language==UILanguage.PT){
			    		root.toastMessage("Situação da Linha de Fogo: Ativa", Toast.LENGTH_SHORT, 0, 0);
			    	}
		    		break;
		    	}
		    }
		});
	}
	/*
	 * Prepares the User Interface 
	 */
	private void initialiseUI(){
		center=getCenterOfScreen();
		screenWH=getScreenSize();
		root.setScreenData(screenWH, center);
		root.attachComponents();
		root.resetViewsVisibility();
		root.prepareUI();
		root.setListeners();
		root.initialiseMessagingInterface();
	}
	/*
	 * Initializes features to be used by the UI
	 */
	
	private void initializeFeatures(){
		/* text to Speech */
		textToSpeechHandler = new TextToSpeechHandler(this);
		
		/* camera access for LED usage */
		camera = Camera.open(); 
        p = camera.getParameters(); 
        
        /* vibrator */
        root.setVibrator((Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE));
        
        /* initialize your android device sensor capabilities */
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		gps = new GPSTracker(this);
		gpsTargetGuideRunnable  = new GpsTargetGuideRunnable();
		
		/* activate compass */
		setCompassOperation(compassEnabled);
		
		/* tap detector */
		Log.d("MainUI", "Starting tappdetector");
		tapDetector = new TappDetector(this, this);
	}
	/*
	 * To ensure proper component positioning
	 */
	private void resetUI(){
		
		root.resetUI();
	}
	/*
	 * initializes the Backend communication
	 */
	private void initialiseComm(){
		readNet = Login.readNet;
		output = Login.output;
		readProtocol = Login.readProtocol;
		protocol = Login.protocolToUse;
		
		messageHandler = new Message();
		messageHandler.setMainUIActivity(this);
		
		readNet.setMessageObject(messageHandler);
		readProtocol.setMessageObject(messageHandler);
		readProtocol.setLoginFlag(false);
		readNet.setActivityObject(this, true);
		
	}
	
	private void lowBatteryCreate(){
		//comentei a recepção de pouca bateria
		//IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_LOW);
		//BatteryLowReceiver batteryLow = new BatteryLowReceiver();
		//registerReceiver(batteryLow, filter);
		MyListener = new MyPhoneStateListener();
        Tel = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		
		bateryChecker=new BatteryLevelChecker(this);
	}
	/*************************************************************************************************************************************
	 ************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * ALARMS AND SOUNDS
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/	
	/*
	 * Alarm methods: triggered from the quickscreen button.
	 */
	public void playAlarm(){
		sounds=MediaPlayer.create(this, R.raw.alarm);
		sounds.start();
	}
	public void stopAlarm(){
		sounds.stop();
		sounds.reset();
		sounds.release();
		sounds=null;
	}
	/*
	 * Other Sounds
	 */
	public void playDeadManAlert(int deadManPhase){
		switch (deadManPhase){
		case 2:
			deadManAlarms=MediaPlayer.create(this, R.raw.dead_man_alert_1);
			deadManAlarms.start();
			break;
		case 3:
			deadManAlarms.stop();
			deadManAlarms.reset();
			deadManAlarms=MediaPlayer.create(this, R.raw.dead_man_alert_2);
			deadManAlarms.start();
			break;
		case 4:
			deadManAlarms.stop();
			deadManAlarms.reset();
			deadManAlarms=MediaPlayer.create(this, R.raw.dead_man_alert_3);
			deadManAlarms.start();
			break;
		}
	}
	public boolean isDeadManPlaying(){
		return deadManAlarms.isPlaying();
	}
	public void stopDeadManAlert(){
		if(deadManAlarms!=null){
			deadManAlarms.stop();
			deadManAlarms.reset();
			deadManAlarms.release();
			deadManAlarms=null;
		}
	}
	public void playPredefinedMessageReceived(){
		sounds=MediaPlayer.create(this, R.raw.predefmessage);
		sounds.start();
		sounds.setOnCompletionListener(new OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mp) {
				sounds.stop();
				sounds.reset();
				sounds.release();
				sounds=null;
			}
			
		});
	}
	public void playMessageReceived(){
		sounds=MediaPlayer.create(this, R.raw.message);
		sounds.start();
		sounds.setOnCompletionListener(new OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mp) {
				sounds.stop();
				sounds.reset();
				sounds.release();
				sounds=null;
			}
			
		});
	}
	public void playObjectiveReceived(){
		sounds=MediaPlayer.create(this, R.raw.objective_received);
		sounds.start();
		sounds.setOnCompletionListener(new OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mp) {
				sounds.stop();
				sounds.reset();
				sounds.release();
				sounds=null;
			}
			
		});
	}
	public void playBackendRequestReceived(){
		sounds=MediaPlayer.create(this, R.raw.backend_request);
		sounds.start();
		sounds.setOnCompletionListener(new OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mp) {
				sounds.stop();
				sounds.reset();
				sounds.release();
				sounds=null;
			}
			
		});
	}
	/*************************************************************************************************************************************
	 ************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * APP STATUS INDICATORS
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/	
	/*
	 * quick screen status indicators
	 * these methods update the state of the indicator on the UI.
	 */
	public void updateBatteryStatus(int percentage){
		indicatorStates batteryStatus=indicatorStates.EMPTY;
		if(percentage<=10){
			batteryStatus=indicatorStates.EMPTY;
			Log.d("BatteryLow", "The battery from the smartphone is very low.");
			Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_LOW_BATTERY);
		} else if( (percentage>10) && (percentage<=30) ){
			Log.d("BatteryLow", "The battery from the smartphone is low, <30%.");
			batteryStatus=indicatorStates.LOW;
		} else if( (percentage>30) && (percentage<=70) ){
			batteryStatus=indicatorStates.MEDIUM;
		} else if( (percentage>70) && (percentage<=90) ){
			batteryStatus=indicatorStates.HIGH;
		} else {
			batteryStatus=indicatorStates.FULL;
		}
		root.updateBatteryIndicator(batteryStatus);
	}
	public void updateNetworkStatus(int status){
		indicatorStates netStatus=indicatorStates.EMPTY;
		switch (status){
			case 0:
				netStatus=indicatorStates.EMPTY;
				break;
			case 1:
				netStatus=indicatorStates.LOW;
				break;
			case 2:
				netStatus=indicatorStates.MEDIUM;
				break;
			case 3:
				netStatus=indicatorStates.HIGH;
				break;
			case 4:
				netStatus=indicatorStates.FULL;
				break;
		}
		root.updateNetworkStatus(netStatus);
	}
	public void updateWifiStatus(int status){
		indicatorStates wifiStatus=indicatorStates.EMPTY;
		switch (status){
			case 0:
				wifiStatus=indicatorStates.EMPTY;
				break;
			case 1:
				wifiStatus=indicatorStates.LOW;
				break;
			case 2:
				wifiStatus=indicatorStates.MEDIUM;
				break;
			case 3:
				wifiStatus=indicatorStates.HIGH;
				break;
			case 4:
				wifiStatus=indicatorStates.FULL;
				break;
		}
		root.updateWifiIndicator(wifiStatus);
	}
	public void updateBluetoothStatus(boolean status){
		indicatorStates bluetoothStatus=indicatorStates.EMPTY;
		if(status==true){
			bluetoothStatus=indicatorStates.FULL;
		} else {
			bluetoothStatus=indicatorStates.EMPTY;
		}
		root.updateBluetoothIndicator(bluetoothStatus);
	}
	/*************************************************************************************************************************************
	 ************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * GPS METHODS
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/* 
	 * gps computations
	 * this calculates the angle of the compass and issues the change on the UI
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		
		northDegree= -Math.round(event.values[0]);
		
		
		if (targetMode){
			double Deltalat;
			double Deltalong;
			double theta;
			double gama;
			double alpha; //angulo que o objecto faz com o norte
			
			
			alpha= Math.toRadians(Math.round(event.values[0]));

			Location location=gps.getLocation();
			if(location!=null){
				double Mlat= location.getLatitude();
				double Mlong= location.getLongitude();
				
				Deltalat= Olat-Mlat;
				Deltalong= Olong-Mlong;
			
				gama= Math.atan2(Deltalat, Deltalong);
						
				gama= (-gama + Math.PI/2);

				theta = (gama-alpha);
					
				targetDegree= Math.toDegrees(theta);			
			} else {
				root.reportNoGPSSignal();
			}
		} 	
	}
	/*
	 * Unimplemented Method
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// not necessary
	}
	/*
	 * To reduce angle to an interval between -pi and pi
	 */
	private double volta1(double degree){
		
		while(degree <= -180){
			degree+=360;
		}
		while(degree > 180){
			degree-=360;
		}
		
		return degree;
			
	}
	
	/*
	 * this Runnable is to update the distance text in the quickScreen view text field
	 */
	protected class GpsDistanceRunnable implements Runnable {			
		@Override
		public void run() {
			Location location=gps.getLocation();
			if(location!=null){ 
				
				double tolerancia=20; // Fronteira para considerar que o bombeiro chegou ao destino
				
				double dist=coordDist(location.getLatitude(),location.getLongitude(),Olat,Olong);
				
				if(dist<=tolerancia){
		
					Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_REACH_DESTINATION);
					
					//falta por aqui a fun��o para sair do modo guia
				}
				
				root.postDistanceToObjective(dist);
				
			} else {
				//nothing
			}
			
			gpsDistanceHandler.postDelayed(this, gpsDistanceRunnableDelta);
		}
	}
	
	public double coordDist(double Mlat, double Mlong, double Olat, double Olong) {
	
		double Deltalat= Olat-Mlat;
		double Deltalong= Olong-Mlong;
		
		Deltalong=(float) (Math.PI*Deltalong*Math.cos(Math.toRadians(Math.abs(Mlat)))*((float)rt)/180);
		Deltalat=(float) (Math.PI*Deltalat*((float)rt)/180);
		
		return Math.sqrt(Deltalong*Deltalong + Deltalat*Deltalat);
	}
	
	/*
	 * This runnable controls the voice guidance system of the target mode
	 */
	protected class GpsTargetGuideRunnable implements Runnable {
		
		private boolean front=false;
		private boolean back=false;
		private boolean left=false;
		private boolean right=false;
		
		Location location=gps.getLocation();
		
		@Override
		public void run() {
			if (targetMode){
					double margem=10;
					Log.d("GPS Guide", "Angulo reduzido= " + volta1(targetDegree));
					if((volta1(targetDegree) >= -130) && (volta1(targetDegree) <= -10) ){
						speak("Vire � esquerda," + Double.toString((int) coordDist(location.getLatitude(),location.getLongitude(),Olat,Olong)) + "metros.");
						left=true;
						front=false;
						back=false;
						right=false;
					} else if((volta1(targetDegree) <= 130) && (volta1(targetDegree) >= 10) ) {
						speak("Vire � direita,"  + Double.toString((int) coordDist(location.getLatitude(),location.getLongitude(),Olat,Olong)) + "metros." );
						left=false;
						front=false;
						back=false;
						right=true;
					} else if(((volta1(targetDegree) >= -10) && (volta1(targetDegree) <= 10) && front==false) ) {
						speak("Siga em frente," + Double.toString((int) coordDist(location.getLatitude(),location.getLongitude(),Olat,Olong)) + "metros.");
						left=false;
						front=true;
						back=false;
						right=false;
					} else if ((volta1(targetDegree) < -130) || (volta1(targetDegree) > 130) ) {
						speak("Volte para tr�s" + Double.toString((int) coordDist(location.getLatitude(),location.getLongitude(),Olat,Olong)) + "metros.");
						left=false;
						front=false;
						back=false;
						right=false;
					} else {
						speak("Continue por" + Double.toString((int) coordDist(location.getLatitude(),location.getLongitude(),Olat,Olong)) + "metros.");
					}
			}
			gpsTargetGuideHandler.postDelayed(gpsTargetGuideRunnable, gpsTargetGuideDelta);
		}
	}
	/*
	 * this Runnable is to update the distance text in the quickScreen view text field
	 */
	protected class GpsCompassAngleRunnable implements Runnable {			
		@Override
		public void run() {
			if(targetMode){
				// create a rotation animation (reverse turn degree degrees)
				root.setCompassAngleNow((float)targetDegree);
			} else {
				root.setCompassAngleNow((float)northDegree);
			}
			gpsCompassAngleHandler.postDelayed(this, gpsCompassAngleDelta);
		}
	}
	/*
	 * To toggle the compass visibility and operation
	 */
	private void setCompassOperation(boolean compassIsToOperate){
		compassEnabled=compassIsToOperate;
		if(compassEnabled){
			gpsCompassAngleHandler.postDelayed(gpsCompassAngleRunnable, gpsCompassAngleDelta);		
		} else {
			gpsCompassAngleHandler.removeCallbacks(gpsCompassAngleRunnable);
		}
		root.setCompassEnabledState(compassIsToOperate);
	}
	/*
	 * Updates the compass appearance and enables the distance poster runnable
	 */
	public void setCompassTargetMode(){
		targetMode=true;
		root.setCompassTargetMode(targetMode);
		root.updateCompassAppearance();
		gpsDistanceHandler.postDelayed(gpsDistanceRunnable, gpsDistanceRunnableDelta);
		gpsTargetGuideHandler.postDelayed(gpsTargetGuideRunnable, gpsTargetGuideDelta);
	}
	/*
	 * Updates the compass appearance and disables the distance poster runnable
	 */
	public void disableCompassTargetMode(){
		targetMode=false;
		root.setCompassTargetMode(targetMode);
		root.updateCompassAppearance();
		gpsDistanceHandler.removeCallbacks(gpsDistanceRunnable);
		gpsTargetGuideHandler.removeCallbacks(gpsTargetGuideRunnable);
		root.postLastMessage();
	}
	/*
	 * Toggles between thes last two methods
	 */
	public void toggleCompassTargetMode(){
		targetMode=!targetMode;
		if(targetMode){
			setCompassTargetMode();
		} else {
			disableCompassTargetMode();
		}	
	}
	/*
	 * Posts the message on the UI and sends it
	 */
	public void sendDistance(int d, int situationCode){
		String codeDescription=new String("");
		switch (situationCode){
		case 0:
			if(language==UILanguage.EN){
				codeDescription=new String("Active");
	    	} else if (language==UILanguage.PT){
				codeDescription=new String("Ativa");
	    	}
			break;
		case 1:
			if(language==UILanguage.EN){
				codeDescription=new String("Controlled");
	    	} else if (language==UILanguage.PT){
				codeDescription=new String("Controlada");
	    	}
			break;
		case 2:
			if(language==UILanguage.EN){
				codeDescription=new String("Under Surveillance");
	    	} else if (language==UILanguage.PT){
				codeDescription=new String("Sob Vigilância");
	    	}
			break;
		case 3:
			if(language==UILanguage.EN){
				codeDescription=new String("Deleted");
	    	} else if (language==UILanguage.PT){
				codeDescription=new String("Removed");
	    	}
			break;
		default:
			if(language==UILanguage.EN){
				codeDescription=new String("Active");
	    	} else if (language==UILanguage.PT){
				codeDescription=new String("Ativa");
	    	}
			break;
		}
		if (this.language==UILanguage.EN){
			root.sendMessage(new StringBuilder("Distance to Line of Fire: ").append(d).append("m\nSituation: ").append(codeDescription).toString());
		} else if (this.language==UILanguage.PT){
			root.sendMessage(new StringBuilder("Distância à Linha de Fogo: ").append(d).append("m\nSituação: ").append(codeDescription).toString());
		}
	}
	public void parseObjectiveRecived(float lat, float lon){
		Log.d("TARGET", "NEw coords-> lat: "+lat+" & long: "+lon);
		this.playObjectiveReceived();
		this.Olat=(double)lat;
		this.Olong=(double)lon;
		if(targetMode){
			this.speak("Objetivo Recebido, para entrar em modo de objetivo, bata duas vezes no telemóvel.");
			tapDetector.startWaitingForOkForTarget();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			if(language==UILanguage.EN){
				builder.setTitle("Target Mode");
			} else if (language==UILanguage.PT){
				builder.setTitle("Modo de Objetivo");
			}
			Log.d("logout", "Building Logout dialog");
			if(language==UILanguage.EN){
				builder.setMessage("Do you want to go into Target Mode?");
				builder.setPositiveButton(" Yes ", new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int which) {
				    	toggleCompassTargetMode();
				        dialog.dismiss();
				    }
				});
				builder.setNegativeButton(" No ", new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        dialog.dismiss();
				    }
				});
			} else if (language==UILanguage.PT){
				builder.setMessage("Quer entrar em Modo de Objetivo??");
				builder.setPositiveButton(" Sim ", new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int which) {
				    	toggleCompassTargetMode();
				        dialog.dismiss();
				    }
				});
				builder.setNegativeButton(" Não ", new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        dialog.dismiss();
				    }
				});
			}
			AlertDialog alert = builder.create();
			alert.show();
		}
		
	}
	/*************************************************************************************************************************************
	 ************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * TOASTS
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/*
	 * Makes a toast
	 */
	public void toastMessage(String Message, int DurationMilis, int PositionX, int PositionY){
		root.toastMessage(Message,DurationMilis, PositionX, PositionY);
	}
	/*************************************************************************************************************************************
	 ************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * MISCELANEOUS METHODS
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/	
	/* 
	 * disables hardware back button
	 */
	@Override
	public void onBackPressed() {
		// empty stub to ensure back button does nothing
	}
	/*
	 * Methods to use on the computation of the components positions
	 */
	public float[] getCenterOfScreen(){
		float center[]={0,0};
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		center[0]=(float)displaymetrics.widthPixels/2.0f;
		center[1]=(float)displaymetrics.heightPixels/2.0f;
		return center;
	}
	public float[] getScreenSize(){
		float widthAndHeight[]={center[0]*2,center[1]*2};
		return widthAndHeight;
	}
	/*************************************************************************************************************************************
	 ************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * FOR APP FLOW AND MULTITHREAD HANDLING
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/*
	 * to get user level
	 */
	private void getUserLevel(){
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle.getBoolean("COMMANDER_RANK", false)==true){
			userLevel=userRanks.COMMANDER;
			if(language==UILanguage.EN){
				toastMessage("Rank: Commander",Toast.LENGTH_SHORT, 0, 0);
			} else if (language==UILanguage.EN){
				toastMessage("Escalão: Comandante",Toast.LENGTH_SHORT, 0, 0);
			}
		} else {
			userLevel=userRanks.OPERATIONAL;
			if(language==UILanguage.EN){
				toastMessage("Rank: Operational",Toast.LENGTH_SHORT, 0, 0);
			} else if (language==UILanguage.EN){
				toastMessage("Escalão: Operacional",Toast.LENGTH_SHORT, 0, 0);
			}
		}
		//put firemanID
		team = bundle.getString("FIREMAN_TEAM");
		if(team.equals("")==false){
			//send team info to backend
			Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_TEAM_UPDATE, Integer.parseInt(team));
			Log.d("MainUI", "Team = " + team);
		}
	}
	
	 /*For logging out. Returns to the log in window, but remains connected to the grid. (TODO verify if this works like this)
	 */
	public void logout(){
		Log.d("MainUI", "Entrou no logout");
		Context c=this.getApplicationContext();
		try {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = new Intent(MainUI.this.getApplicationContext(),Splash.class);
                    mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					//create a pending intent so the application is restarted after System.exit(0) was called. 
					// We use an AlarmManager to call this intent in 100ms
					int mPendingIntentId = 223344;
					PendingIntent mPendingIntent = PendingIntent.getActivity(c, mPendingIntentId, mStartActivity,PendingIntent.FLAG_CANCEL_CURRENT);
					AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
					mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
					//kill the application
					System.exit(0);
        } catch (Exception ex) {
            Log.e("LOGOUT", "Was not able to restart application");
        }
	}
	/*
	 * For app termination
	 */
	private void exit(){
		//MainUI.this.finish();
		System.exit(0);
	}
	/*************************************************************************************************************************************
	 ************************************************************************************************************************************/
	
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * FOR PROTOCOL HANDLING
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/	
	/*
	 * Toggles between the two protocol modules and changes the image view appearance
	 */
	private void toggleProtocol(){
    	switch(protocol){
	    	case PROTOCOL_G5:
	    		protocol=Protocol.PROTOCOL_G6;
	    		protocolToggler.setImageResource(R.drawable.selector_protocol_g6);
	    		protocolTogglerText.setText("Protocol: G6");
	    		
	    		//readProtocol.setProtocol(protocol);
	    		//TODO propagate these changes to the classes below
	    		break;
	    	case PROTOCOL_G6:
	    		protocol=Protocol.PROTOCOL_G5;
	    		protocolToggler.setImageResource(R.drawable.selector_protocol_g5);
	    		protocolTogglerText.setText("Protocol: G5");
	    		
	    		//readProtocol.setProtocol(protocol);
	    		//TODO propagate these changes to the classes below
	    		break;
    	}	
    	
    	readProtocol.kill_thread();
		//criar ReadProtocol object com protocol
		readProtocol = new ReadProtocol(protocol, this, firemanID); //mudar para G6 se for o caso...
		readProtocol.setMessageObject(messageHandler);
		readProtocol.setLoginFlag(false);
		readProtocol.start();
	}
	/*************************************************************************************************************************************
	 ************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * DIALOGS
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/*
	 * Creates a dialog to prompt the user whether he is sure about logging out
	 */
	private void buildLogoutDialogue(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Logout");
		Log.d("logout", "Building Logout dialog");
		
		if(language==UILanguage.EN){
			builder.setMessage("Are you sure you wnat to logout?");
			builder.setPositiveButton(" Yes ", new DialogInterface.OnClickListener() {

			    public void onClick(DialogInterface dialog, int which) {
			        //logout();
			    	Log.d("MainUI", "Entrou no enviar logout");
			        root.toastMessage("Waiting for backend permission to logout...", Toast.LENGTH_SHORT, 0, 0);
			        Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_LOGOUT);
			        dialog.dismiss();
			    }

			});
			builder.setNegativeButton(" No ", new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        dialog.dismiss();
			    }
			});
		} else if (language==UILanguage.PT){
			builder.setMessage("Tem a certeza que quer sair?");
			builder.setPositiveButton(" Sim ", new DialogInterface.OnClickListener() {

			    public void onClick(DialogInterface dialog, int which) {
			    	 //logout();
			    	Log.d("MainUI", "Entrou no enviar logout");
			        root.toastMessage("A espera de permissão do backend...", Toast.LENGTH_SHORT, 0, 0);
			        Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_LOGOUT);
			        dialog.dismiss();
			    }

			});
			builder.setNegativeButton(" Não ", new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        dialog.dismiss();
			    }
			});
		}
		
		

		

		AlertDialog alert = builder.create();
		alert.show();
	}
	/*
	 * Creates a dialog to prompt the user whether he is sure about logging out
	 */
	private void buildExitDialogue(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		if(language==UILanguage.EN){
			builder.setTitle("Exit");
			builder.setMessage("Are you sure you want to quit?");

			builder.setPositiveButton(" Yes ", new DialogInterface.OnClickListener() {

			    public void onClick(DialogInterface dialog, int which) {
			        exit();
			        dialog.dismiss();
			    }

			});

			builder.setNegativeButton(" No ", new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        dialog.dismiss();
			    }
			});
		} else if (language==UILanguage.PT){
			builder.setTitle("Sair");
			builder.setMessage("Tem a certeza que quer sair?");

			builder.setPositiveButton(" Sim ", new DialogInterface.OnClickListener() {

			    public void onClick(DialogInterface dialog, int which) {
			        exit();
			        dialog.dismiss();
			    }

			});

			builder.setNegativeButton(" Não ", new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        dialog.dismiss();
			    }
			});
		}
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	/*************************************************************************************************************************************
	 ************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * COMMUNICATION
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	public void parseIncommingPredefinedMessage(int predefinedMessageCode){
		this.playPredefinedMessageReceived();
		switch (predefinedMessageCode){
			case 0:
				root.postMessage("Command", "Afirmativo.", PriorityLevel.CRITICAL, false);
				break;
			case 1:
				root.postMessage("Command", "Aguarde.", PriorityLevel.CRITICAL, false);
				break;
			case 2:
				root.postMessage("Command", "Assim farei.", PriorityLevel.CRITICAL, false);
				break;
			case 3:
				root.postMessage("Command", "Correto.", PriorityLevel.CRITICAL, false);
				break;
			case 4:
				root.postMessage("Command", "Errado.", PriorityLevel.CRITICAL, false);
				break;
			case 5:
				root.postMessage("Command", "Informe.", PriorityLevel.CRITICAL, false);
				break;
			case 6:
				root.postMessage("Command", "Negativo.", PriorityLevel.CRITICAL, false);
				break;
			case 7:
				root.postMessage("Command", "A caminho.", PriorityLevel.CRITICAL, false);
				break;
			case 8:
				root.postMessage("Command", "No local.", PriorityLevel.CRITICAL, false);
				break;
			case 9:
				root.postMessage("Command", "No hospital.", PriorityLevel.CRITICAL, false);
				break;
			case 10:
				root.postMessage("Command", "Disponível.", PriorityLevel.CRITICAL, false);
				break;
			case 11:
				root.postMessage("Command", "De regresso.", PriorityLevel.CRITICAL, false);
				break;
			case 12:
				root.postMessage("Command", "INOP.", PriorityLevel.CRITICAL, false);
				break;
			case 13:
				root.postMessage("Command", "No quartel.", PriorityLevel.CRITICAL, false);
				break;
			case 14:
				root.postMessage("Command", "Necessito de Reforços.", PriorityLevel.CRITICAL, false);
				break;
			case 15:
				root.postMessage("Command", "Casa em Perigo.", PriorityLevel.CRITICAL, false);
				break;
			case 16:
				root.postMessage("Command", "Preciso de Descansar.", PriorityLevel.CRITICAL, false);
				break;
			case 17:
				root.postMessage("Command", "Carro em Perigo.", PriorityLevel.CRITICAL, false);
				break;
			case 18:
				root.postMessage("Command", "Descanse.", PriorityLevel.CRITICAL, false);
				break;
			case 19:
				root.postMessage("Command", "Fogo a Alastrar.", PriorityLevel.CRITICAL, false);
				break;
			default:
				break;
		}
	}
	public void parseOutgoingPredefinedMessage(int predefinedMessageCode){
		final StringBuilder sb;
		String prefix=new String("PDM sent: ");
		if(language==UILanguage.EN){
			prefix=new String("PDM sent: ");
		} else if (language == UILanguage.PT) {
			prefix=new String("MPD enviada: ");
		}
		//TODO
		switch (predefinedMessageCode){
			case CommEnumerators.NEED_SUPPORT:
				sb=new StringBuilder(prefix).append(Integer.toString(predefinedMessageCode));
				root.toastMessage(sb.toString(), Toast.LENGTH_SHORT, 0, 0);
				break;
			case CommEnumerators.NEED_TO_BACK_DOWN:
				sb=new StringBuilder(prefix).append(Integer.toString(predefinedMessageCode));
				root.toastMessage(sb.toString(), Toast.LENGTH_SHORT, 0, 0);
				break;
			case CommEnumerators.FIRETRUCK_IS_IN_TROUBLE:
				sb=new StringBuilder(prefix).append(Integer.toString(predefinedMessageCode));
				root.toastMessage(sb.toString(), Toast.LENGTH_SHORT, 0, 0);
				break;
			case CommEnumerators.NEED_AERIAL_SUPPORT:
				sb=new StringBuilder(prefix).append(Integer.toString(predefinedMessageCode));
				root.toastMessage(sb.toString(), Toast.LENGTH_SHORT, 0, 0);
				break;
			case CommEnumerators.FIRE_SPREADING:
				sb=new StringBuilder(prefix).append(Integer.toString(predefinedMessageCode));
				root.toastMessage(sb.toString(), Toast.LENGTH_SHORT, 0, 0);
				break;
			case CommEnumerators.WE_ARE_LEAVING:
				sb=new StringBuilder(prefix).append(Integer.toString(predefinedMessageCode));
				root.toastMessage(sb.toString(), Toast.LENGTH_SHORT, 0, 0);
				break;
			case CommEnumerators.FIRE_GETTING_CLOSE_TO_HOUSE:
				sb=new StringBuilder(prefix).append(Integer.toString(predefinedMessageCode));
				root.toastMessage(sb.toString(), Toast.LENGTH_SHORT, 0, 0);
				break;
			case CommEnumerators.HOUSE_BURNED:
				sb=new StringBuilder(prefix).append(Integer.toString(predefinedMessageCode));
				root.toastMessage(sb.toString(), Toast.LENGTH_SHORT, 0, 0);
				break;
		}
	}
	public void parseMessageFromBackend(int infoCode){
		this.playMessageReceived();
		switch(infoCode){
			case CommEnumerators.COMMAND_TO_FIREFIGHTER_PREDEFINED_MESSAGE:
			{	
				break;
			}	
			case CommEnumerators.COMMAND_TO_FIREFIGHTER_MESSAGE:
			{
				break;
			}	
			case CommEnumerators.COMMAND_TO_FIREFIGHTER_FIRELINE_UPDATE_REQUEST:
				break;
			case CommEnumerators.COMMAND_TO_FIREFIGHTER_TEAM_INFORMATION_REQUEST:
				break;
			case CommEnumerators.COMMAND_TO_FIREFIGHTER_ID:
				break;
			case CommEnumerators.COMMAND_TO_FIREFIGHTER_LOGIN_DENIED:
				break;
			case CommEnumerators.COMMAND_TO_FIREFIGHTER_LOGING_ACCEPTED:
				break;
			case CommEnumerators.COMMAND_TO_FIREFIGHTER_GO_TO_COORDINATE:
				break;
		}
	}
	/*************************************************************************************************************************************
	 ************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * TEXT TO SPEECH
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	public void speak(String text){
		if (textToSpeechEnabled){
			textToSpeechHandler.speakOut(text);
		}
	}
	/*************************************************************************************************************************************
	 ************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * TAP DETECTOR
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	public void setTapDetectorOperation(boolean tapDetectorIsEnabled){
		if(tapDetectorIsEnabled){
				tapDetector.enabledTaps=true;
				if(language==UILanguage.EN) {
					root.toastMessage("Tap Interface: Enabled", Toast.LENGTH_SHORT, 0, 0);
				} else if (language==UILanguage.PT) {
					root.toastMessage("Interface de Taps: Ativada", Toast.LENGTH_SHORT, 0, 0);
				}
		} else {
				if(language==UILanguage.EN) {
					root.toastMessage("Tap Interface: Disabled", Toast.LENGTH_SHORT, 0, 0);
				} else if (language==UILanguage.PT) {
					root.toastMessage("Interface de Taps: Desativada", Toast.LENGTH_SHORT, 0, 0);
				}
				tapDetector.enabledTaps=false;
				
		}
	}
	/*************************************************************************************************************************************
	 ************************************************************************************************************************************/
	
	
	
	/*************************************************************************************************************************************
	 *************************************************************************************************************************************
	 * 
	 * BLUETOOTH HARDWARE INTERFACE
	 *
	 *************************************************************************************************************************************
	 *************************************************************************************************************************************/
	/**
	   * Threads de recepçao e envio + funcao de escrita
	   */
	  private class ConnectedThread extends Thread {
	      private final InputStream mmInStream;
	      private final OutputStream mmOutStream;

	      public ConnectedThread(BluetoothSocket socket) {
	          InputStream tmpIn = null;
	          OutputStream tmpOut = null;

	          // Get the input and output streams, using temp objects because
	          // member streams are final
	          try {
	              tmpIn = socket.getInputStream();
	              tmpOut = socket.getOutputStream();
	          } catch (IOException e) { }

	          mmInStream = tmpIn;
	          mmOutStream = tmpOut;
	      }

	      public void run() {
	          byte[] buffer = new byte[256];  // buffer store for the stream
	          int bytes; // bytes returned from read()

	          // Keep listening to the InputStream until an exception occurs
	          while (true) {
	              try {
	                  // Read from the InputStream
	                  bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
	                  h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
	              } catch (IOException e) {
	                  break;
	              }
	          }
	      }

	      /* Call this from the main activity to send data to the
	remote device */
	      public void write(byte[] bs) {
	          //Log.d(TAG, "...Data to send: " + message + "...");
	          //byte[] msgBuffer = bs.getBytes();
	          try {
	              mmOutStream.write(bs);
	          } catch (IOException e) {
	              //Log.d(TAG, "...Error data send: " + e.getMessage()+ "...");
	            }
	      }
	  }
	  
	  private void setUpHwBluetoothHandler(){
		  h = new Handler() {
		        public void handleMessage(android.os.Message msg){
		            switch (msg.what) {
		            case RECIEVE_MESSAGE:
		      // if receive massage
		                byte[] readBuf = (byte[]) msg.obj;
		                if (readBuf[1] == 0x50)
		                {
		                    System.out.println("entrou na recepçao do PING");
		                    //responde com o PONG com o mesmo nrSession que recebe
		                    mConnectedThread.write(pong(readBuf[0]));
		                }

		                if (readBuf[1] == 0x62)
		                {
		                    System.out.println("entrou na recepçao da Bateria");
		                    //nao ha resposta, mas temos que ver o 1º byte dos argumentos com o estado
		                    int bateria = ((readBuf[2] & 0xff) << 8);
		                    System.out.println("Estado bateria:"+bateria);
		                }
		                if (readBuf[1] == 0x73)
		                {
		                    //System.out.println("entrou na resposta quanto ao envio do DG");
		                    if ((readBuf[2]==0x01) && (readBuf[3]==0))
		                    {
		                        mConnectedThread.write(sendDG("francisco"));
		                    }
		                    if ((readBuf[2]==0xFF) && (readBuf[3]==0) )
		                    {
		                        System.out.println("Recusado o envio do DG");
		                    }
		                    System.out.println("sucesso ou nao sucesso");
		                    if ((readBuf[2]==0) && (readBuf[3]==0x01))
		                    {
		                        System.out.println("DG enviado");
		                    }
		                    if ((readBuf[2]==0) && (readBuf[3]==0xFF) )
		                    {
		                        System.out.println("DG nao enviado");
		                    }
		                }
		                if (readBuf[1] == 0x54)
		                {
		                    //System.out.println("entrou no GPS TIME");
		                    mConnectedThread.write(responseGPS(readBuf[0]));
		                }
		                break;
		                }

		            }
		        };
		            checkBTState();
		            btAdapter = BluetoothAdapter.getDefaultAdapter();       // getBluetooth adapter
		    };
			 private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
				              if(Build.VERSION.SDK_INT >= 10){
	                  try {
	                      final Method  m =device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord",new Class[] { UUID.class });
	                      return (BluetoothSocket) m.invoke(device, MY_UUID);
	                  } catch (Exception e) {
	                      //Log.e(TAG, "Could not create Insecure RFComm Connection",e);
	                  }
	              }
	              return  device.createRfcommSocketToServiceRecord(MY_UUID);
	          }
			 private void onResumeHwBluetoothSetUp(){
				// Set up a pointer to the remote node using it's address.
				    BluetoothDevice device = btAdapter.getRemoteDevice(address);

				    // Two things are needed to make a connection:
				    //   A MAC address, which we got above.
				    //   A Service ID or UUID.  In this case we are using the
				    //     UUID for SPP.

				    try {
				        btSocket = createBluetoothSocket(device);
				    } catch (IOException e) {
				        System.exit(1);
				    }

				    // Discovery is resource intensive.  Make sure it isn't going on
				    // when you attempt to connect and pass your message.
				    btAdapter.cancelDiscovery();

				    // Establish the connection.  This will block until it connects.
				   // Log.d(TAG, "...Connecting...");
				    try {
				      btSocket.connect();
				     // Log.d(TAG, "....Connection ok...");
				    } catch (IOException e) {
				      try {
				        btSocket.close();
				      } catch (IOException e2) {
				          System.exit(1);
				      }
				    }

				    // Create a data stream so we can talk to server.
				    //Log.d(TAG, "...Create Socket...");

				    mConnectedThread = new ConnectedThread(btSocket);
				    mConnectedThread.start();
			}
			 private void onPauseHwBluetoothSetUp(){
				 try{
				      btSocket.close();
				    } catch (IOException e2) {
				        System.exit(1);
				    }
			 	}

			 private void checkBTState() {
				    // Check for Bluetooth support and then check to make sure it is turned on
				    // Emulator doesn't support Bluetooth and will return null
				    if(btAdapter==null) {
				      System.exit(1);
				    } else {
				      if (btAdapter.isEnabled()) {
				        //Log.d(TAG, "...Bluetooth ON...");
				      } else {
				        //Prompt user to turn on Bluetooth
				        //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				        //startActivityForResult(enableBtIntent, 1);
				      }
				    }
				  }
			//se android quer comecar a sessao envia PING
             public static byte[] ping() {

                     dataE[0]=nrSessao++;
                     dataE[1]=0x50;
                     dataE[2]=0x00;
                     dataE[3]=0x00;
                     dataE[4]=delimiter;

                     /*System.out.println("teste PING :");
                     for (int i = 0; i < dataE.length; i++) {
                             System.out.println("0x" + Integer.toHexString(dataE[i]));
                     }
                     System.out.println("Fim Teste PING!!!!");*/

                     return dataE;

             }

             //se for enviado PING para Android responder com Pong
             public static byte[] pong(byte numero) {
                     dataE[0]=numero;
                     dataE[1]=0x70;
                     dataE[2]=0x00;
                     dataE[3]=0x00;
                     dataE[4]=delimiter;

                     /*System.out.println("teste PONG:");
                     for (int i = 0; i < dataE.length; i++) {
                             System.out.println("0x" + Integer.toHexString(dataE[i]));
                     }
                     System.out.println("Fim Teste PONG");*/

                     return dataE;
             }

             //Se Android quer saber Bateria - esta resposta e tratada na class de recepcao
             public static byte[] requestBattery(byte numero) {
                     dataE[0]=numero;
                     dataE[1]=0x42;
                     dataE[2]=0x00;
                     dataE[3]=0x00;
                     dataE[4]=delimiter;

                     /*System.out.println("teste bateria:");
                     for (int i = 0; i < dataE.length; i++) {
                             System.out.println("0x" + Integer.toHexString(dataE[i]));
                     }
                     System.out.println("Fim Teste bateria");*/

                     return dataE;
             }

             //quando Android quer passar DG para hardware dever primeiro fazer request
             public static byte[] requestDG(byte numero, int tamanho) {

                     dataE[0]=numero;
                     dataE[1]=0x53;
                     dataE[2]=(byte)(tamanho & 0xFF);
                     dataE[3]=(byte)((tamanho >> 8) & 0xFF);
                     dataE[4]=delimiter;

/*                      System.out.println("teste requestDG:");
                     for (int i = 0; i < dataE.length; i++) {
                             System.out.println("0x" + Integer.toHexString(dataE[i]));
                     }
                     System.out.println("Fim teste requestDG");*/

                     return dataE;
             }

             //se for aceite envia a mensagem
             public static byte[] sendDG(String message) {
                       dataE = message.getBytes();

                       /*System.out.println("teste sendDG:");
                       for (int i = 0; i < dataE.length; i++) {
                                     System.out.println("0x" + Integer.toHexString(dataE[i]));
                             }
                       System.out.println("Fim Teste sendDG");*/

                       return dataE;
             }

             //se Android recebe um pedido de GPS, enviar esta trama
             public static byte[] responseGPS(byte numero) {

                     dataE[0]=numero;
                     dataE[1]=0x74;
                     dataE[2]=0x00;
                     dataE[3]=0x00;
                     dataE[4]=delimiter;

                     /*System.out.println("teste responseGPS:");
                     for (int i = 0; i < dataE.length; i++) {
                             System.out.println("0x" + Integer.toHexString(dataE[i]));
                     }
                     System.out.println("Fim Teste responseGPS");*/

                     return dataE;
             }

             //teste para ver se detecta a recepçao e as respostas que deve fazer
             public static byte[] recepcao(byte[] data) {
                     dataE=data;
                     if (dataE[1] == 0x50) {
                             System.out.println("entrou na recepçao do PING");
                             //responde com o PONG com o mesmo nrSession que recebe
                             pong(dataE[0]);
                     }

                     if (dataE[1] == 0x62) {
                             System.out.println("entrou na recepçao da Bateria");
                             //nao ha resposta, mas temos que ver o 1º byte dos argumentos com o estado
                             int bateria = ((dataE[2] & 0xff) << 8);
                             System.out.println("Estado bateria:"+bateria);
                     }
                     if (dataE[1] == 0x73) {
                             System.out.println("entrou na resposta quanto ao envio do DG");
                             if ((dataE[2]==0x01) && (dataE[3]==0)) {
                                     sendDG("francisco");
                             }
                             if ((dataE[2]==0xFF) && (dataE[3]==0) ) {
                                     System.out.println("Recusado");
                             }
                             System.out.println("entrou na parte da segunda resposta sucesso ou nao sucesso");
                             if ((dataE[2]==0) && (dataE[3]==0x01)) {
                                     System.out.println("Sucesso");
                             }
                             if ((dataE[2]==0) && (dataE[3]==0xFF) ) {
                                     System.out.println("nao enviado");
                             }
                     }
                     if (dataE[1] == 0x54) {
                             System.out.println("entrou no GPS TIME");
                             responseGPS(dataE[0]);
                     }
                     return dataE;
             }
	/*************************************************************************************************************************************
	 ************************************************************************************************************************************/
             
             
     
     /*************************************************************************************************************************************
 	 *************************************************************************************************************************************
 	 * 
 	 * BLUETOOTH LOW ENERGY
 	 *
 	 *************************************************************************************************************************************
 	 *************************************************************************************************************************************/ 	 	
          // Code to manage Service lifecycle.
         	private final ServiceConnection mServiceConnection = new ServiceConnection() {

         		@Override
         		public void onServiceConnected(ComponentName componentName,IBinder service) {
         			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
         			if (!mBluetoothLeService.initialize()) {
         				Log.d("BLE", "Unable to initialize Bluetooth");
         				finish();
         			} else {
         				Log.d("BLE", "Bluetooth Initialized");
         			}
         			// Automatically connects to the device upon successful start-up initialization.
         			Log.d("BLE", "Attempting Connect to :"+mDeviceAddress);
         			if(canDoBle==true){
	         			if(mBluetoothLeService.connect(mDeviceAddress)){
	         				Log.d("BLE", "Connected to :"+mDeviceAddress);
	         			} else {
	         				Log.d("BLE", "Could not connect to "+mDeviceAddress);
	         			}
         			}
         		}

         		@Override
         		public void onServiceDisconnected(ComponentName componentName) {
         			mBluetoothLeService = null;
         			Log.d("BLE", "mBluetoothLeService just became null");
         		}
         	};

         	// Handles various events fired by the Service.
         	// ACTION_GATT_CONNECTED: connected to a GATT server.
         	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
         	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
         	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
         	// result of read
         	// or notification operations.
         	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
         		@Override
         		public void onReceive(Context context, Intent intent) {
         			final String action = intent.getAction();
         			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
         				invalidateOptionsMenu();
         			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
         				invalidateOptionsMenu();
         			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
         				// Show all the supported services and characteristics on the user interface.
         				displayGattServices(mBluetoothLeService.getSupportedGattServices());
         			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
         				displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
         			}
         		}
         	};
	
         	
         	private void displayData(String data) {
        		if (data != null) {
        			heart= Integer.valueOf(data);
                }		
        	}
         	
         // Demonstrates how to iterate through the supported GATT
        	// Services/Characteristics.
        	// In this sample, we populate the data structure that is bound to the
        	// ExpandableListView
        	// on the UI.
        	private void displayGattServices(List<BluetoothGattService> gattServices) {
        		if (gattServices == null)
        			return;
        		String uuid = null;
        		String unknownServiceString = getResources().getString(R.string.unknown_service);
        		String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        		// Loops through available GATT Services.
        		for (BluetoothGattService gattService : gattServices) {
        			HashMap<String, String> currentServiceData = new HashMap<String, String>();
        			uuid = gattService.getUuid().toString();
        			currentServiceData.put(LIST_NAME,
        					SampleGattAttributes.lookup(uuid, unknownServiceString));
        			currentServiceData.put(LIST_UUID, uuid);
        			gattServiceData.add(currentServiceData);

        			ArrayList<HashMap<String, String>> gattCharacteristicGroupData = 
        					new ArrayList<HashMap<String, String>>();
        			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
        					.getCharacteristics();
        			ArrayList<BluetoothGattCharacteristic> charas = 
        					new ArrayList<BluetoothGattCharacteristic>();

        			// Loops through available Characteristics.
        			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

        				if (UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT)
        						.equals(gattCharacteristic.getUuid())) {
        					Log.d("BLE", "Found heart rate");
        					mNotifyCharacteristic = gattCharacteristic;
        				}

        				charas.add(gattCharacteristic);
        				HashMap<String, String> currentCharaData = new HashMap<String, String>();
        				uuid = gattCharacteristic.getUuid().toString();
        				currentCharaData.put(LIST_NAME,
        						SampleGattAttributes.lookup(uuid, unknownCharaString));
        				currentCharaData.put(LIST_UUID, uuid);
        				gattCharacteristicGroupData.add(currentCharaData);
        			}
        			mGattCharacteristics.add(charas);
        			gattCharacteristicData.add(gattCharacteristicGroupData);
        		}

        	}

        	private static IntentFilter makeGattUpdateIntentFilter() {
        		final IntentFilter intentFilter = new IntentFilter();
        		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        		intentFilter
        				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        		return intentFilter;
        	}
         	
	private void onBleCreate(){
		Log.d("BLE", "On BLE Create");
		
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		if(bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE)==false){
			Log.d("BLE", "Service not bound");
			root.updateBluetoothIndicator(indicatorStates.EMPTY);
		} else {
			Log.d("BLE", "Service bound.");
			root.updateBluetoothIndicator(indicatorStates.FULL);
		}
		
		if(getDeviceParameters()==true){
			Log.d("BLE", "Device Address: "+mDeviceAddress);
			enableHeartRateRunnable();
			canDoBle=true;
		} else {
			Log.d("BLE", "BLE skipped");
		}
			
	}
	
	private void onBlePause(){
		root.updateBluetoothIndicator(indicatorStates.EMPTY);
	}
	
	private void onBleResume(){
		if(canDoBle==true){
			registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
			if (mBluetoothLeService != null) {
				final boolean result = mBluetoothLeService.connect(mDeviceAddress);
				Log.d("BLE", "Connect request result=" + result);
				root.updateBluetoothIndicator(indicatorStates.FULL);
			}
		}
	}
	
	private void onBleDestroy(){
		unbindService(mServiceConnection);
		mBluetoothLeService = null;
		root.updateBluetoothIndicator(indicatorStates.EMPTY);
	}
	
	protected class HeartRateRunnable implements Runnable {			
		@Override
		public void run() {
			if(bleServiceInitialised==false){
				//Log.d("BLE", "Verifying services functionalities...");
		    	if((mBluetoothLeService!=null) && mNotifyCharacteristic!=null) {
		    		Log.d("BLE", "BLE services not null");
		    		mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
		    		bleServiceInitialised=true;
		    	} else {
		    		//Log.d("BLE", "BLE services still null");
		    	}
		    	heartRateHandler.postDelayed(this, heartRateDelta);
			} else {
				root.setHeartRate(heart);
				Log.d("BLE", "HeartRate: "+heart+"BMP");
				heartRateHandler.postDelayed(this, heartRateDelta);
			}
		}
	}
	protected class HeartRateSenderRunnable implements Runnable {
		@Override
		public void run() {
			if((heartPassado==heart) || (heart==0) ){
				Log.d("BLE", "Igual ou e zero");
			}
			else
			{
				Log.d("BLE", "Enviou");
				Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_HEART_RATE, heart);
				heartPassado=heart;
			}
			heartRateSenderHandler.postDelayed(this, heartRateSendDelta);
		}
	}
	private void enableHeartRateRunnable(){
		heartRateHandler.postDelayed(heartRateDisplayRunnable, heartRateDelta*2);
		heartRateSenderHandler.postDelayed(heartRateSenderRunnable, heartRateSendDelta);
	}
	private void disableHeartRateRunnable(){
		heartRateHandler.removeCallbacks(heartRateDisplayRunnable);
		heartRateSenderHandler.removeCallbacks(heartRateSenderRunnable);
	}
	private boolean getDeviceParameters(){
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		
		mDeviceAddress=bundle.getString("DEVICE_ADDRESS", "mnull");
		mDeviceName=bundle.getString("DEVICE_NAME", "mnull");
		
		Log.d("BLE", "BLE device parameters from extras: "+mDeviceName+" ("+mDeviceAddress+")");
		
		if((mDeviceAddress.compareTo("null")==0) || (mDeviceAddress.compareTo("lnull")==0) || (mDeviceAddress.compareTo("mnull")==0)){
			return false;
		}
		canDoBle=true;
		Log.d("BLE", "BLE device parameters: "+mDeviceName+" ("+mDeviceAddress+")");
		return true;
	}
	/*************************************************************************************************************************************
	 ************************************************************************************************************************************/
             
             
     
     /*************************************************************************************************************************************
 	 *************************************************************************************************************************************
 	 * 
 	 * LOW BATTERY
 	 *
 	 *************************************************************************************************************************************
 	 *************************************************************************************************************************************/
	public class BatteryLowReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			final Context ctx = context;
			
			if(intent.getAction() == Intent.ACTION_BATTERY_LOW)
			{
				Thread thread = new Thread(new Runnable(){

					@Override
					public void run() {
						AlertDialog.Builder alert = new Builder(ctx);
						alert.setTitle("Bateria Fraca");
						alert.setMessage("Bateria Fraca!");
						alert.setNeutralButton("OK", null);
						alert.show();
					}
				});
				thread.start();
		    }
	    }
	}
	/*************************************************************************************************************************************
	 ***********************************************************************************************************************************/
	
	/*signal strength*/ 
	
	private class MyPhoneStateListener extends PhoneStateListener
    {
      /* Get the Signal strength from the provider, each tiome there is an update */
      @Override
      public void onSignalStrengthsChanged(SignalStrength signalStrength)
      {
         super.onSignalStrengthsChanged(signalStrength);
         int statusWifi = 0;
         int signalStrength_dBm = (2 * signalStrength.getGsmSignalStrength()) - 113;
         
         if(signalStrength_dBm < -107){
        	 Log.d("Signal", "Sinal nivel 0");
        	 statusWifi = 0;
         }else if(signalStrength_dBm < -103){
        	 Log.d("Signal", "Sinal nivel 1");
        	 statusWifi = 1;
         }else if(signalStrength_dBm < -98.5){
        	 Log.d("Signal", "Sinal nivel 2");
        	 statusWifi = 2;
         }else if(signalStrength_dBm < -89.5){
        	 Log.d("Signal", "Sinal nivel 3");
        	 statusWifi = 3;
         }else{
        	 Log.d("Signal", "Sinal nivel 4");
        	 statusWifi = 4;
         }
         
         Toast.makeText(getApplicationContext(), "Signal strength = "
            + String.valueOf(signalStrength_dBm), Toast.LENGTH_SHORT).show();
         
         //update indicator
         updateWifiStatus(statusWifi);
      }

    };/*end of signal class*/
    
}
