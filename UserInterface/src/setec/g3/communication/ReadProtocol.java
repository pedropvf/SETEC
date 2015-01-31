package setec.g3.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import setec.g3.communication.CommEnumerators.Protocol;
import setec.g3.maincontroller.Login;
import setec.g3.maincontroller.MainUI;

import android.util.Log;
import androidBackendAPI.Packet;

public class ReadProtocol extends Thread {
	
	static ObjectInputStream appIn;
    static ObjectInputStream proIn;
    static ObjectOutputStream appOut;
    static ObjectOutputStream proOut;
    
    Socket socketPro;
    Socket socketApp;
    
    private Message messageHandler;
    private MainUI mainUserInterface;
    private Login login_act;
    
    private Protocol protocolInUse;
    
    byte system_id;
    
    Thread threadG5;
    Thread threadG6;
    
    int port = 4444;
    
    boolean goOn = true;
    boolean loginFlag = false;
  
    public ReadProtocol (Protocol protocolToUse, MainUI _mainUserInterface, byte _system_id){
    	protocolInUse=protocolToUse;
    	mainUserInterface=_mainUserInterface;
    	system_id = _system_id;
    }
    
    public ReadProtocol (Protocol protocolToUse, Login _login, byte _system_id){
    	protocolInUse = protocolToUse;
    	login_act = _login;
    	system_id = _system_id;
    }
    
    public void setMessageObject(Message _messageHandler){
    	messageHandler = _messageHandler;
    }
    
    public void setLoginFlag(boolean _loginFlag){
    	loginFlag = _loginFlag;
    }
    
    public void kill_thread(){
		try {
			goOn = false;
			appIn.close();
			appOut.close();
			proIn.close();
			proOut.close();
			socketPro.close();
			socketApp.close();
		} catch (IOException e) {
			Log.e("ReadNet",e.toString());
		}
	}
    
	public void run(){
		
		Log.d("ReadProtocol","ReadProtocol initiated");
		
		initialize();
		
		while(goOn){
	        try {
	        	//receive request from protocol
				rqst request = (rqst)proIn.readObject();
		        Log.d("ReadProtocol","Protocol requested id=" + request.id + " spec=" + request.spec);
		        
				byte id = request.id;
				if (id == (byte) 0x11) {
					
					byte spec = request.spec;
					
					if(spec==0x00){
						//create packet object to send
						Packet sendingPacket = new Packet();
						sendingPacket.hasProtocolHeader = true;
						sendingPacket.packetContent = request.packet;
						
						NetThread n = new NetThread(sendingPacket, loginFlag);
						n.start(); //a thread tem que retornar para se
									// saber se foi bem sucedido ou não
						
						byte resp = (byte)0x00; //ou 0xFF ou 0xEE se deu erro
						
						rspns response = new rspns(resp); 
				        Log.d("ReadProtocol","App replied id=" + response.id);
				        //write response back to protocol
				        proOut.writeObject(response);
				        
					}else{
						//radio - preencher
						
						byte resp = (byte)0x00; //ou 0xFF ou 0xEE se deu erro tem se que acrescentar aqui alguma coisa...
						
						rspns response = new rspns(resp);
				        Log.d("ReadProtocol","App replied id=" + response.id);
				        //write response back to protocol
				        proOut.writeObject(response);
					}

				} else if (id == (byte) 0x22) {
					//message for us
					
					//send response to protocol
					rspns response = new rspns((byte)0x00); //Everything OK
			        Log.d("ReadProtocol","App replied id=" + response.id);
			        //write response back to protocol
			        proOut.writeObject(response);
			        
					//unpack and process
					Packet receivePacket = new Packet();
					receivePacket.hasProtocolHeader = true;
					receivePacket.packetContent = request.packet;
					messageHandler.receive(receivePacket);
					
				} else {
					Log.d("ReadProtocol", "id recebido errado.");
				}
		        
			} catch (IOException e) {
				Log.e("ReadProtocol", e.toString());
				if(goOn==false){
					return;
				}
			} catch (ClassNotFoundException e) {
				Log.e("ReadProtocol", e.toString());
				if(goOn==false){
					return;
				}
			}
		}
		
	}
	
	public void initialize() {
		
		ServerSocket serverSocket = null;

		try {
			// 0 means any available port
			serverSocket = new ServerSocket(port);
		} catch (IOException ex) {
			Log.d("ReadProtocol", "Could not connect to port...");
			return;
		}

		/*
		 * Create a thread that runs the protocol. In the android app, this
		 * thread must do so inside a service
		 */
		
		//TODO TODOTODOTODOTODOTODOTODOTODOTODOTODOTODOTODOTODOTODOTODOTODOTODO
		switch(protocolInUse){
			case PROTOCOL_G5:
				threadG5 = new Thread(){
			        public void run(){
			        	Log.d("ReadProtocol","Initiating G5 protocol");
			        	Protocol_G5 g5 = new Protocol_G5(true, port, (byte)111);
			        	g5.execute();
			        }
			    };
			    threadG5.start();
				break;
			case PROTOCOL_G6:
				threadG6 = new Thread(){
			        public void run(){
			        	Log.d("ReadProtocol","Initiating G6 protocol");
			        	Protocol_G6 g6 = new Protocol_G6(true, port, (byte)111);
			        	g6.execute();
			        }
			    };
			    threadG6.start();
				break;
		}

		/*
		 * Create socket SocketPro for the protocol's requests and the
		 * application's responses
		 */
		
		socketPro = null;
		try {
			Log.d("ReadProtocol", "Waiting for protocol socket to connect...");
			socketPro = serverSocket.accept();
		} catch (IOException ex) {
			Log.d("app_prot", "Application: Could not open SocketPro!\n");
			return;
		}

		/*
		 * Create socket SocketApp for the application's requests and the
		 * protocol's responses
		 */
		
		socketApp = null;
		try {
			Log.d("ReadProtocol", "Waiting for application socket to connect...");
			socketApp = serverSocket.accept();
		} catch (IOException ex) {
			Log.d("ReadProtocol", "Application: Could not open SocketApp!\n");
			return;
		}

		/*
		 * Transform socket streams into ObjectStreams in order to send objects
		 * instead of bytes (Out's must always be created first and flushed!)
		 */
		try {
			Log.d("ReadProtocol", "Creating Object Streams...");
			appOut = new ObjectOutputStream(socketApp.getOutputStream());
			proOut = new ObjectOutputStream(socketPro.getOutputStream());
			appOut.flush();
			proOut.flush();
			appOut.reset();
			proOut.reset();
			appIn = new ObjectInputStream(socketApp.getInputStream());
			proIn = new ObjectInputStream(socketPro.getInputStream());

		} catch (IOException ex) {
			Log.d("ReadProtocol", "Could not create Object Streams!\n");
			return;
		}
		
		Log.d("ReadProtocol", "Initialization finished");
		
		//send message to protocol saying that GSM is connected
		SendToProtocol s = new SendToProtocol((byte) 0x33, (byte) 0x00, false, null);
		s.start();

	}
	
	public void switchProtocol(){
		if(protocolInUse==Protocol.PROTOCOL_G5){
			protocolInUse=Protocol.PROTOCOL_G6;
			//TODO
		} else {
			protocolInUse=Protocol.PROTOCOL_G5;
			//TODO
		}
	}
	public void setProtocol(Protocol newProtocol){
		protocolInUse=newProtocol;
			//TODO
	}

}
