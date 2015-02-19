package setec.g3.maincontroller;

import java.io.ObjectOutputStream;
import java.net.Socket;

import setec.g3.communication.CommEnumerators;
import setec.g3.communication.Message;
import setec.g3.communication.ReadNet;
import setec.g3.communication.ReadProtocol;
import setec.g3.communication.CommEnumerators.Protocol;
import setec.g3.heart.DeviceControlActivity;
import setec.g3.ui.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.gson.Gson;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.SharedPreferences.Editor;

public class Login extends Activity {

    /* The form Components */
	private Button loginBtn;
	private RadioGroup rankRadioGrp;
	private static EditText userText;
	private static EditText passText;
	private EditText portText;
	private EditText teamText;
	private EditText ipText;
	private LinearLayout connectForm, loginForm, teamForm;
	
	/* The parameters variables */
	//private String ninjaUsername="g3";
	//private String ninjaPassword="g3";
<<<<<<< HEAD
	private String defaultIP = "172.30.22.178";
=======
	private String defaultIP = "172.30.23.154";
>>>>>>> refs/remotes/origin/master
	private String defaultPort = "4444";
	private String defaultUser = "121212";
	private String defaultPass = "teste";
	private String defaultTeam = "1";
	private boolean isConnected=false;
	
	/*parameter for communication*/
	public static ObjectOutputStream output;
	public static ReadNet readNet;
	public static byte firemanID;
	public static ReadProtocol readProtocol;
	public Message messageHandler;
	public static CommEnumerators.Protocol protocolToUse = Protocol.PROTOCOL_G5;
	public String team;
	
	//String host = new String("172.30.23.124");
	//int port = 2000;
	
	/* ble extras */
	String deviceName;
	String deviceAddress;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.login_screen);
        
        attachComponents();
        initialiseComponents();
        addListeners();
        /* para testes fazer override ao request */
        
        Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		deviceName=bundle.getString("DEVICE_NAME", "lnull");
		deviceAddress=bundle.getString("DEVICE_ADDRESS", "lnull");
		
		Log.d("BLE", deviceName+"\n"+deviceAddress);
    }
    
    /*
     * to get the components from xml
     */
	private void attachComponents(){
		loginBtn = (Button) findViewById(R.id.login_btn);
		rankRadioGrp = (RadioGroup) findViewById(R.id.rank_radio);
		userText = (EditText) findViewById(R.id.login_user_tv);
		passText = (EditText) findViewById(R.id.login_password);
		portText = (EditText) findViewById(R.id.login_port);
		teamText = (EditText) findViewById(R.id.EditTextTeam);
		ipText = (EditText) findViewById(R.id.login_ip);
		connectForm = (LinearLayout) findViewById(R.id.login_port_ip_form_layout);
		loginForm = (LinearLayout) findViewById(R.id.login_form_layout);
		teamForm = (LinearLayout) findViewById(R.id.login_team);
	}
	
	/*
	 * To initialise the components 
	 */
	private void initialiseComponents(){
		userText.setText(defaultUser);
		passText.setText(defaultPass);
		portText.setText(defaultPort);
		ipText.setText(defaultIP);
		teamText.setText(defaultTeam);
		
		loginBtn.setText("Connect");
		loginForm.setVisibility(View.INVISIBLE);
		teamForm.setVisibility(View.INVISIBLE);
		toastMessage("Press \"Connect\" to try a Backend connection.", Toast.LENGTH_LONG, 0, 0);
	}
    
	/*
	 * to set component listeners
	 */
	public void addListeners(){
		loginBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	if(isConnected){
		    		login();
		    	} else{
		    		
		    		//protocol selection
		    		int radioButtonID = rankRadioGrp.getCheckedRadioButtonId();
		    		View radioButton = rankRadioGrp.findViewById(radioButtonID);
		    		int idx = rankRadioGrp.indexOfChild(radioButton);
		    		if(idx == 0){
		    			protocolToUse=Protocol.PROTOCOL_G5;
		    		}else{
		    			protocolToUse=Protocol.PROTOCOL_G6;
		    		}
		    		
		    		Toast.makeText(getApplicationContext(), "Will atempt to estabish a network connection, please wait...", Toast.LENGTH_SHORT).show();
		    		initialiseComm();
		    	}
		    }
		});
	/*	userText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					userText.setText("");
				}
			}
		});*/
	}
	
	/*
	 * to get connected
	 */
	public void connectToBackend(boolean success, byte _firemanID){
		if(success==true){
			firemanID = _firemanID;
			loginBtn.setText("Login");
			loginForm.setVisibility(View.VISIBLE);
			teamForm.setVisibility(View.VISIBLE);
			connectForm.setVisibility(View.INVISIBLE);
			rankRadioGrp.setVisibility(View.INVISIBLE);
			toastMessage("Connected.", Toast.LENGTH_SHORT, 0, 0);
			isConnected=true;
			
			//criar ReadProtocol object
			readProtocol = new ReadProtocol(protocolToUse, this, firemanID); //mudar para G6 se for o caso...
			readProtocol.setMessageObject(messageHandler);
			readProtocol.setLoginFlag(true);
			readProtocol.start();
			
			readNet.setProtocolCreated();
		} else {
			toastMessage("Connection could not be stablished.", Toast.LENGTH_SHORT, 0, 0);
		}
	}

    /*
     * To do the login and go to next screen
     */
	private void login(){
		//TODO
		
		//read the text the user provided by the login
		String user=userText.getText().toString().trim();
		String pass=passText.getText().toString().trim();
		
		//String user = ;
		//String pass = "teste";
		//team = "1";
		
		if(!user.equals("") && !pass.equals("")){
			//Send login information to backend
			Message.send((byte)CommEnumerators.FIREFIGHTER_TO_COMMAND_LOGIN,Integer.parseInt(user),pass);	
			//wait for the backend to respond... it will receive the response in Message.receive (case 133 or 134)
			//and will call loginResponse method
		}else{
			toastMessage("Please fill in the username and password", Toast.LENGTH_SHORT, 0, 0);
		}
		
    }
	
	//funcao que trata da resposta do backend (chamado pelas outras threads)
	public void loginResponse(boolean accepted, boolean isCommander){
		if(accepted == true){
			//store firemanID (on MainUI)
			//maybe put it in the intent 
			toastMessage("Login successful, welcome!", Toast.LENGTH_SHORT, 0, 0);
			Intent mainIntent = new Intent(Login.this,MainUI.class);
			saveRankAndBLE(mainIntent, isCommander);
	        Login.this.startActivity(mainIntent);
	        Login.this.finish();
		}else{
		//	userText.setText("");
		//	passText.setText("");
			toastMessage("Credentials could not be verified.", Toast.LENGTH_SHORT, 0, 0);
		}
	}
	
	/*
	 * To display Toast Messages
	 */
	public void toastMessage(String Message, int DurationMilis, int PositionX, int PositionY){
		Toast t=Toast.makeText(this.getApplicationContext(), Message, DurationMilis);
		t.setGravity(Gravity.BOTTOM|Gravity.CENTER_VERTICAL, PositionX, PositionY);
		t.show();
		t=null;
	}
	
	/*
	 * To save the rank of the user
	 */
	/*
	 * To save the rank of the user
	 */
	public void saveRankAndBLE(Intent i, boolean isCommander){
		Bundle extras = new Bundle();
		extras.putBoolean("COMMANDER_RANK", isCommander);
		extras.putString("DEVICE_NAME", deviceName);
		extras.putString("DEVICE_ADDRESS", deviceAddress);
		team = teamText.getText().toString().trim();
		extras.putString("FIREMAN_TEAM", team);
		i.putExtras(extras);
	}
	
	private void initialiseComm(){
		
		if(readNet==null){
			messageHandler = new Message();
			messageHandler.setLoginActivity(this);
			Message.firemanID = (byte)0;
			readNet = new ReadNet(ipText.getText().toString().trim(), Integer.parseInt(portText.getText().toString().trim()));
			readNet.setMessageObject(messageHandler);
			readNet.setActivityObject(this, false);
			readNet.start();
		}
		
	}
	
	@Override
    public void onDestroy()
    {
        super.onDestroy();
     /*   if(readNet!=null){
        	readNet.kill_thread();
        }
        if(readProtocol!=null){
        	readProtocol.kill_thread();
        }
        */
    }
}
