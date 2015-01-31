package setec.g3.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import setec.g3.maincontroller.Login;
import setec.g3.maincontroller.MainUI;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidBackendAPI.Packet;

public class ReadNet extends Thread{
	
	TextView answertxt;
	ObjectInputStream input;
	ObjectOutputStream output;
	Socket socket;
	String sendtxt;
	Activity main;
	String server_name;
	int port;
	Message messageHandler;
	boolean goOn = true;
	boolean mainUIflag = false;
	boolean isProtocolCreated = false;
	
	public ReadNet(String dstName, int _port, boolean _mainUIflag){
		server_name = dstName;
		port = _port;
		mainUIflag = _mainUIflag;
	}
	
	public ReadNet(String dstName, int _port){
		server_name = dstName;
		port = _port;
	}
	
	public void setMessageObject(Message _messageHandler){
    	messageHandler = _messageHandler;
    }
	
	public void setActivityObject(Activity act, boolean mainUi){
    	main = act;
    	mainUIflag = mainUi;
    }
	
	public void setProtocolCreated(){
		isProtocolCreated = true;
	}
	
	public void kill_thread(){
		try {
			goOn = false;
			input.close();
			output.close();
			socket.close();
		} catch (IOException e) {
			Log.e("ReadNet",e.toString());
		}
	}

	public void run(){
		
		while(goOn == true){
			connectivityCheck();
			initiateRead();
		}
		
	}
	
	public void initiateRead() {

		if (isProtocolCreated == true) {
			// send message to protocol saying that GSM is connected
			SendToProtocol s = new SendToProtocol((byte) 0x33, (byte) 0x00,
					false, null);
			s.start();
		}
		while (true) {

			try {
				//Log.d("ReadNet", "A ler ...");
				Packet message;
				message = (Packet) input.readObject();
				Log.d("ReadNet", "Pacote recebido");
				boolean sendToProtocol = message.hasProtocolHeader;
				byte[] messageBytes = message.packetContent;
				
				int [] msgInt = new int[messageBytes.length];
				
				for(int msgnr=0; msgnr<messageBytes.length;msgnr++){
					msgInt[msgnr] = (int) (messageBytes[msgnr] & 0xFF);
				}
					
				Log.d("ReadNet", Arrays.toString(msgInt));

				if (sendToProtocol == true) {
					SendToProtocol s2 = new SendToProtocol((byte) 0x00,
							(byte) 0x00, false, messageBytes);
					s2.start();
				} else {
					messageHandler.receive(message);
				}

			} catch (OptionalDataException e) {
				Log.e("ReadNet", "Error " + e);
			} catch (ClassNotFoundException e) {
				Log.e("ReadNet", "Error " + e);
			} catch (IOException e) {
				Log.e("ReadNet", "Entrou no IO exeption");
				Log.e("ReadNet", "Error " + e);
				try {
					input.close();
					socket.close();
					output.close();
				} catch (IOException e1) {
					Log.e("ReadNet", "Error " + e1);
				}
				
				if (isProtocolCreated == true){
					// send message to protocol saying that GSM is connected
					SendToProtocol s2 = new SendToProtocol((byte) 0x33,
								(byte) 0x11, false, null );
					s2.start();
				}
				return;
			}
		}
	}

	public void connectSocket(){
		
		int count = 0;
		
		Log.d("ReadNet", "attempting to connect to server");
		while(trySocket()==false){
			count++;
			if(count==10){
				return;
			}
			try {
				Thread.sleep(5000);
			} catch (Exception e) {
				Log.e("ReadNet",e.toString());
			}
		}
		
	}
	
	public boolean trySocket(){
		try {
			//connect to the backend server (with name server_name and port port)
			
			socket = new Socket(server_name,port);
			output = new ObjectOutputStream(socket.getOutputStream());
			output.flush();
			input = new ObjectInputStream(socket.getInputStream());
			if(mainUIflag==true){
				MainUI.output = output;
			}else{
				Login.output = output;
			}
			return true;
			
		} catch (Exception e) {
			Log.d("ReadNet","erro?");
			Log.e("ReadNet",e.toString());
			if(mainUIflag==true){
				MainUI.output = null;
			}else{
				Login.output = null;
			}
			return false;
		}
	}
	
    //check if a mobile network is availabe (ou sem rede)
    public boolean isGSMAvailable(){
    	ConnectivityManager cm =
                (ConnectivityManager) main.getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE); 
    	boolean isMobileAvai = networkInfo.isAvailable();
    	return isMobileAvai;
    }
    
    //check internet connection. still don't know where to put it. in a thread maybe?
    public boolean isNetworkConnected() {
        ConnectivityManager cm =
            (ConnectivityManager) main.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }
    
    //
    public void connectivityCheck(){

    	Log.d("ReadNet", "ReadNet started...");
    	int flag = 0;
		while (isNetworkConnected() == false) {
			
			if(flag==0 && isProtocolCreated==true){
				//send message to protocol saying that GSM connection was lost or is not connected
				SendToProtocol s = new SendToProtocol((byte) 0x33, (byte) 0x11, false, null);
				s.start();
			}
			if (isGSMAvailable() == true && flag<1) {
				Log.d("ReadNet", "GSM is available but not connected");
				flag++;
				//send to UI (a dialog opening the connectivity)
				main.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(main.getApplicationContext(), "Couldn't connect to the internet, check that you have an internet connection", Toast.LENGTH_LONG).show();
					}
				});
				
			} else {
				// SEM REDE
				Log.d("ReadNet", "não há rede ou o utilizador cancelou ou não ligou à net");
			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Log.e("ReadNet", e.toString());
			}
		
    	}
		
		// ONLINE
		Log.d("ReadNet", "online");
		connectSocket();
    }
    
}