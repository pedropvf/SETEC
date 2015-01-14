package setec.g3.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import setec.g3.maincontroller.Login;
import setec.g3.maincontroller.MainUI;
import android.util.Log;
import android.widget.TextView;

public class NetThread extends Thread{
	
	TextView answertxt;
	DataInputStream input;
	ObjectOutputStream output;
	Socket socket;
	String answer;
	boolean loginFlag = false;
	
	//packet object
	packet sendingPacket;
	
	NetThread(packet _sendingPacket){
		sendingPacket = _sendingPacket;
	}
	
	NetThread(packet _sendingPacket, boolean loginFlag){
		sendingPacket = _sendingPacket;
		this.loginFlag = loginFlag;
	}
		
	public void run(){
		
		try{
			
			if(loginFlag==false){
				output = MainUI.output;
			}else{
				output = Login.output;
			}
			
			if(output==null){
				Log.d("NetThread", "OutputStream was not created, will return now");
				return;
			}
			
			output.writeObject(sendingPacket);
			output.flush();
			Log.d("NetThread", "Enviou objecto");
		}catch (Exception e){
			Log.e("NetThread",e.toString());
		}
	
	}
	
}
