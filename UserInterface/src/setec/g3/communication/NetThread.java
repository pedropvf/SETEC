package setec.g3.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Arrays;

import setec.g3.maincontroller.Login;
import setec.g3.maincontroller.MainUI;
import android.util.Log;
import android.widget.TextView;
import androidBackendAPI.Packet;

public class NetThread extends Thread{
	
	TextView answertxt;
	DataInputStream input;
	ObjectOutputStream output;
	Socket socket;
	String answer;
	boolean loginFlag = false;
	
	//packet object
	Packet sendingPacket;
	
	NetThread(Packet _sendingPacket){
		sendingPacket = _sendingPacket;
	}
	
	NetThread(Packet _sendingPacket, boolean loginFlag){
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
			
			int [] msgInt = new int[sendingPacket.packetContent.length];
			
			for(int msgnr=0; msgnr<sendingPacket.packetContent.length;msgnr++){
				msgInt[msgnr] = (int) (sendingPacket.packetContent[msgnr] & 0xFF);
			}
				
			
			Log.d("NetThread", Arrays.toString(msgInt));
			
			output.writeObject(sendingPacket);
			output.flush();
			Log.d("NetThread", "Enviou objecto");
		}catch (Exception e){
			Log.e("NetThread",e.toString());
		}
	
	}
	
}
