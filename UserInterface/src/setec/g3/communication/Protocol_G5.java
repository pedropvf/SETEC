package setec.g3.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.net.Socket;

import android.util.Log;

public class Protocol_G5 {
	
	boolean machine;
	int port;
	byte system_id;
	
	ObjectOutputStream appOut;
	ObjectOutputStream proOut;
	ObjectInputStream appIn;
	ObjectInputStream proIn;

	public Protocol_G5(boolean _machine, int _port, byte _system_id){
		machine = _machine;
		port = _port;
		system_id = _system_id;
	}

	public void execute() {
		
		Log.d("Protocol_G5", "Protocol G5 is executing...");
		
		try {
			Socket socketPro = new Socket("localhost",port);
			Socket socketApp = new Socket("localhost",port);
			
			appOut = new ObjectOutputStream(socketApp.getOutputStream());
			proOut = new ObjectOutputStream(socketPro.getOutputStream());
			appOut.flush();
			proOut.flush();
			appOut.reset();
			proOut.reset();
			appIn = new ObjectInputStream(socketApp.getInputStream());
			proIn = new ObjectInputStream(socketPro.getInputStream());
			
			while(true){
				try {
					//ler request da aplicação (action 6)
					Log.d("Protocol_G5", "A ler...");
					rqst request = (rqst)appIn.readObject();
					
					Log.d("Protocol_G5", "Pacote recebido");
					byte id = request.id;
					byte[] packet_received = request.packet;
					
					switch(id){
					
					case (byte)0x00: 
						
						//action 1
						//responder à app
						Log.d("Protocol_G5", "A responder...");
						byte resp1 = (byte)0x00;
						rspns response1 = new rspns(resp1);
						appOut.writeObject(response1);
						
						//unpack packet and deliver it to the app 
						Log.d("Protocol_G5", "A fazer novo request");
						rqst request1 = new rqst((byte)0x22, (byte) 0x00, packet_received);
						proOut.writeObject(request1);
						
						Log.d("Protocol_G5", "A receber resposta da app");
						rspns response1_2 = (rspns)proIn.readObject();
				        Log.d("app_prot","App replied id=" + response1_2.id);
						
						break;
					case (byte)0x33:
						
						//action 4
						//responder à app
						Log.d("Protocol_G5", "A responder...");
						byte resp3 = (byte)0x00;
						rspns response3 = new rspns(resp3);
						appOut.writeObject(response3);
						
						break;
					case (byte)0x55:
						//action 6
						//responder à app
						Log.d("Protocol_G5", "A responder...");
						byte resp6 = (byte)0x00;
						rspns response6 = new rspns(resp6);
						appOut.writeObject(response6);
						
						//fazer um request à app
						Log.d("Protocol_G5", "A fazer novo request");
						rqst request6 = new rqst((byte)0x11, (byte) 0x00, packet_received);
						proOut.writeObject(request6);
						
						Log.d("Protocol_G5", "A receber resposta da app");
						rspns response6_2 = (rspns)proIn.readObject();
				        Log.d("app_prot","App replied id=" + response6_2.id);
						
						break;
					default:
						
  					}
					
				} catch (OptionalDataException e) {
					Log.d("Protocol_G5", e.toString());
				} catch (ClassNotFoundException e) {
					Log.d("Protocol_G5", e.toString());
				} catch (IOException e) {
					Log.d("Protocol_G5", e.toString());
				}
			
			}
		} catch (IOException e) {
			Log.d("Protocol_G5", e.toString());
		}
		
		
	}


}
