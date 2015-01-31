package setec.g3.communication;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import setec.g3.maincontroller.Login;
import setec.g3.maincontroller.MainUI;
import setec.g3.maincontroller.MainUI;
import setec.g3.userinterface.InterfaceStatusEnumerators.PriorityLevel;

import android.content.Intent;
import android.util.Log;
import androidBackendAPI.Packet;

public class Message {

	public static byte firemanID;
	private MainUI userInterface;
	private Login login_act;

	public void setLoginActivity(Login loginact) {
		login_act = loginact;
	}

	public void setMainUIActivity(MainUI _userInterface) {
		userInterface = _userInterface;
	}

	public static void send(int messageType, String send) {
		// type 11
		// TESTED TESTED
		//firemanID = MainUI.firemanID;
		byte[] bytesString = null;
		try {
			bytesString = send.getBytes("ISO-8859-1");
			byte[] p = new byte[] { (byte) messageType, firemanID };
			byte[] pfinal = new byte[p.length + bytesString.length];
			System.arraycopy(p, 0, pfinal, 0, p.length);
			System.arraycopy(bytesString, 0, pfinal, p.length,
					bytesString.length);

			String personMessage = new String(pfinal, "ISO-8859-1");
			Log.d("Message", "personMessage sent ="+personMessage);
			
			// action 6 - mandar mensagem para o protocolo
			SendToProtocol s = new SendToProtocol((byte) 0x55, (byte) 0x00, false,
					pfinal);
			s.start();

			// para efeitos de teste
			// packet loginPacket = new packet();
			// loginPacket.hasProtocolHeader = false;
			// loginPacket.packetContent = pfinal;
			// NetThread n = new NetThread(loginPacket, true);
			// n.start();


		} catch (UnsupportedEncodingException e) {
			Log.e("Message", e.toString());
		}

	}

	public static void send(int messageType, float lat, float longi) {
		// types 0 and 9
		// TESTED TESTED
		//firemanID = MainUI.firemanID;
		
		Log.d("Message", "Recebido pelo send: lat= "+String.valueOf(lat)+"  long= "+String.valueOf(longi));
		
		byte[] p = new byte[] { (byte) messageType, firemanID };
		
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putFloat(lat);
		byte[] p2 = bb.array();
		
		ByteBuffer bb2 = ByteBuffer.allocate(4);
		bb2.order(ByteOrder.LITTLE_ENDIAN);
		bb2.putFloat(longi);
		byte[] p3 = bb2.array();
		
		/*
		int bits = Float.floatToIntBits(lat);
		int bits2 = Float.floatToIntBits(longi);

		byte[] p2 = intToByteArray4(bits);
		byte[] p3 = intToByteArray4(bits2);
		*/
		
		byte[] pfinal = new byte[p.length + p2.length + p3.length];
		System.arraycopy(p, 0, pfinal, 0, p.length);
		System.arraycopy(p2, 0, pfinal, p.length, p2.length);
		System.arraycopy(p3, 0, pfinal, p.length + p2.length, p3.length);

		Log.d("Message", "Tamanho do pfinal: "+pfinal.length);
		
		byte[] latByte = new byte[4];
		System.arraycopy(pfinal, 2, latByte, 0, 4);
		int i1 = byteArrayToInt4(latByte);
		Float latF = Float.intBitsToFloat(i1);
		// longitude
		byte[] longByte = new byte[4];
		System.arraycopy(pfinal, 6, longByte, 0, 4);
		int i2 = byteArrayToInt4(longByte);
		Float longiF = Float.intBitsToFloat(i2);
		Log.d("Message","received: " + "lat=" + String.valueOf(latF)
				+ " long=" + String.valueOf(longiF));
		
		// action 6 - mandar mensagem para o protocolo
		SendToProtocol s = new SendToProtocol((byte) 0x55, (byte) 0x00, false,
				pfinal);
		s.start();

		// para efeitos de teste
		// packet loginPacket = new packet();
		// loginPacket.hasProtocolHeader = false;
		// loginPacket.packetContent = pfinal;
		// NetThread n = new NetThread(loginPacket, true);
		// n.start();

	}

	public static void send(int messageType, int number) {
		// types 1, 2, 10, 14, 15, 16, 17, 18
		//firemanID = MainUI.firemanID;
		byte[] pfinal = new byte[] { (byte) messageType, firemanID,
				(byte) number };

		// action 6 - mandar mensagem para o protocolo
		SendToProtocol s = new SendToProtocol((byte) 0x55, (byte) 0x00, false,
				pfinal);
		s.start();

		// para efeitos de teste
		// packet loginPacket = new packet();
		// loginPacket.hasProtocolHeader = false;
		// loginPacket.packetContent = pfinal;
		// NetThread n = new NetThread(loginPacket, true);
		// n.start();

	}

	public static void send(int messageType, float lat, float longi, int number) {
		// types 3 and 4
		// TESTED TESTED
		//firemanID = MainUI.firemanID;
		byte[] p = new byte[] { (byte) messageType, firemanID };

		int bits = Float.floatToIntBits(lat);
		int bits2 = Float.floatToIntBits(longi);

		byte[] p2 = intToByteArray4(bits);
		byte[] p3 = intToByteArray4(bits2);
		byte b = (byte) number;
		byte[] p4 = new byte[] { b };

		byte[] pfinal = new byte[p.length + p2.length + p3.length + p4.length];
		System.arraycopy(p, 0, pfinal, 0, p.length);
		System.arraycopy(p2, 0, pfinal, p.length, p2.length);
		System.arraycopy(p3, 0, pfinal, p.length + p2.length, p3.length);
		System.arraycopy(p4, 0, pfinal, p.length + p2.length + p3.length,
				p4.length);

		// action 6 - mandar mensagem para o protocolo
		SendToProtocol s = new SendToProtocol((byte) 0x55, (byte) 0x00, false,
				pfinal);
		s.start();

		// para efeitos de teste
		// packet loginPacket = new packet();
		// loginPacket.hasProtocolHeader = false;
		// loginPacket.packetContent = pfinal;
		// NetThread n = new NetThread(loginPacket, true);
		// n.start();

	}

	public static void send(byte messageType, int number, int number2) {
		// type 5
		//firemanID = MainUI.firemanID;

		byte[] pfinal = new byte[] { messageType, firemanID, (byte) number,
				(byte) number2 };

		//action 6 - mandar mensagem para o protocolo
		SendToProtocol s = new SendToProtocol((byte) 0x55, (byte) 0x00,
		false, pfinal);
		s.start();

		// para efeitos de teste
		//packet loginPacket = new packet();
		//loginPacket.hasProtocolHeader = false;
		//loginPacket.packetContent = pfinal;
		//NetThread n = new NetThread(loginPacket, true);
		//n.start();

	}

	public static void send(byte messageType, float lat, float longi,
			int number, int number2) {
		// type 6
		// não foi testada mas a outra muito parecida foi
		//firemanID = MainUI.firemanID;
		byte[] p = new byte[] { messageType, firemanID };

		int bits = Float.floatToIntBits(lat);
		int bits2 = Float.floatToIntBits(longi);

		byte[] p2 = intToByteArray4(bits);
		byte[] p3 = intToByteArray4(bits2);

		byte b = (byte) number;
		byte b2 = (byte) number;
		byte[] p4 = new byte[] { b, b2 };

		byte[] pfinal = new byte[p.length + p2.length + p3.length + p4.length];
		System.arraycopy(p, 0, pfinal, 0, p.length);
		System.arraycopy(p2, 0, pfinal, p.length, p2.length);
		System.arraycopy(p3, 0, pfinal, p.length + p2.length, p3.length);
		System.arraycopy(p4, 0, pfinal, p.length + p2.length + p3.length,
				p4.length);

		// action 6 - mandar mensagem para o protocolo
		SendToProtocol s = new SendToProtocol((byte) 0x55, (byte) 0x00, false,
				pfinal);
		s.start();

		// para efeitos de teste
		// packet loginPacket = new packet();
		// loginPacket.hasProtocolHeader = false;
		// loginPacket.packetContent = pfinal;
		// NetThread n = new NetThread(loginPacket, true);
		// n.start();

	}

	public static void send(byte messageType, int number, String send) {
		// type 7
		// nao testado
		//firemanID = MainUI.firemanID;
		try {

			byte[] p = new byte[] { messageType, firemanID };
			// username
			byte[] p2 = intToByteArray3(number);
			// password
			byte[] p3 = new byte[] { (byte) 0 };
			try {
				byte[] bytesToEncode = send.getBytes("ISO-8859-1");
				MessageDigest md5processor;
				md5processor = MessageDigest.getInstance("MD5");
				p3 = md5processor.digest(bytesToEncode);
			} catch (NoSuchAlgorithmException e) {
				Log.e("Message", e.toString());
			}

			byte[] pfinal = new byte[p.length + p2.length + p3.length];
			System.arraycopy(p, 0, pfinal, 0, p.length);
			System.arraycopy(p2, 0, pfinal, p.length, p2.length);
			System.arraycopy(p3, 0, pfinal, p.length + p2.length, p3.length);

			// action 6 - mandar mensagem para o protocolo
			SendToProtocol s = new SendToProtocol((byte) 0x55, (byte) 0x00, false,
					pfinal);
			s.start();

			// para efeitos de teste
			// packet loginPacket = new packet();
			// loginPacket.hasProtocolHeader = false;
			// loginPacket.packetContent = pfinal;
			// NetThread n = new NetThread(loginPacket, true);
			// n.start();

		} catch (UnsupportedEncodingException e) {
			Log.e("Message", e.toString());
		}

	}

	public static void send(byte messageType) {
		// type 8,12,13,19,20,22,25 - duvida quanto ao 22
		//firemanID = MainUI.firemanID;
		byte[] pfinal = new byte[] { messageType, firemanID };

		// action 6 - mandar mensagem para o protocolo
		SendToProtocol s = new SendToProtocol((byte) 0x55, (byte) 0x00, false,
				pfinal);
		s.start();
	}

	public void receive(Packet message) {

		byte[] messageArray = message.packetContent;
		byte messageType = messageArray[0];
		int type = (int) (messageType & 0xFF);
		byte id_received = messageArray[1];
		int id_int = (int) (id_received & 0xFF);
		Log.d("Message", "message type: " + String.valueOf(type));
		Log.d("Message", "id firefighter: " + String.valueOf(id_int));

		switch (type) {
		case 128:
			// predefined message
			byte preByte = messageArray[2];
			final int pre_message = (int) (preByte & 0xFF);
			userInterface.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					userInterface.parseIncommingPredefinedMessage(pre_message);
				}
			});
			break;
		case 129:
			// personalized message
			byte[] messageByte = Arrays.copyOfRange(messageArray, 2,
					messageArray.length);
			try {
				final String personMessage = new String(messageByte,
						"ISO-8859-1");
				// send to UI
				Log.d("Message", "received: " + personMessage);
				userInterface.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						userInterface.root.postMessage("Command",
								personMessage, PriorityLevel.NORMAL, false);
						userInterface.playMessageReceived();
					}

				});
			} catch (UnsupportedEncodingException e) {
				Log.e("ReadNet", e.toString());
			}
			break;
		case 130:
			userInterface.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					userInterface.root.postMessage("Command",
							"Por favor atualizar situação da linha de fogo.", PriorityLevel.CRITICAL, false);
					userInterface.playBackendRequestReceived();
				}

			});
			break;
		case 131:
			// team information
			// firefighter id
			byte idByte = messageArray[2];
			int id = (int) (idByte & 0xFF);
			// latitude
			byte[] latByte131 = new byte[4];
			System.arraycopy(messageArray, 3, latByte131, 0, 4);
			int i131 = byteArrayToInt4(latByte131);
			Float lat131 = Float.intBitsToFloat(i131);
			// longitude
			byte[] longByte131 = new byte[4];
			System.arraycopy(messageArray, 7, longByte131, 0, 4);
			int i2131 = byteArrayToInt4(longByte131);
			Float longi131 = Float.intBitsToFloat(i2131);
			// send to UI as message with many things

			Log.d("Message", "id: " + String.valueOf(id));
			Log.d("Message", "received: " + "lat=" + String.valueOf(lat131)
					+ " long=" + String.valueOf(longi131));
			break;
		case 132:
			// firefighter ID
			byte iDByte = messageArray[1];
			//final int iD = (int) (iDByte & 0xFF);
			firemanID = iDByte;
			
			login_act.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					login_act.connectToBackend(true, firemanID);
				}
			});
			
			break;
		case 133:
			// deny log-in request

			login_act.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					login_act.loginResponse(false, false);
				}
			});

			break;
		case 134:
			// accept log-in request
			// firefighter ID
			Log.d("Message", "Entrou no accept login");
			byte commanderbyte = messageArray[2];
			final boolean isCommander;
			if(commanderbyte == (byte)0x00){
				isCommander = false;
			}else{
				isCommander = true;
			}
			
			login_act.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					login_act.loginResponse(true, isCommander);
				}
			});
			
			break;
		case 135:
			// GPS of injured fireman
			// latitude
			byte[] latByte = new byte[4];
			System.arraycopy(messageArray, 2, latByte, 0, 4);
			int i1 = byteArrayToInt4(latByte);
			final Float lat = Float.intBitsToFloat(i1);
			// longitude
			byte[] longByte = new byte[4];
			System.arraycopy(messageArray, 6, longByte, 0, 4);
			int i2 = byteArrayToInt4(longByte);
			final Float longi = Float.intBitsToFloat(i2);
			// send to UI
			Log.d("Message", "int2: " + String.valueOf(i1));
			Log.d("Message", "received: " + "lat=" + String.valueOf(lat)
					+ " long=" + String.valueOf(longi));
			userInterface.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					userInterface.parseObjectiveRecived(lat, longi);
				}
			});
			break;
		case 136:
			//activate automatic alert
			break;
		default:
			Log.e("ReadNet", "Wrong type of message.");
			break;
		}

	}

	public static final byte[] intToByteArray3(int value) {
		return new byte[] { (byte) (value >>> 16), (byte) (value >>> 8),
				(byte) value };
	}

	public static final byte[] intToByteArray4(int value) {
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16),
				(byte) (value >>> 8), (byte) value };
	}

	public static final int byteArrayToInt3(byte[] b) {
		int l = 0;
		l |= b[0] & 0xFF;
		l <<= 8;
		l |= b[1] & 0xFF;
		l <<= 8;
		l |= b[2] & 0xFF;
		return l;
	}

	public static final int byteArrayToInt4(byte[] b) {
		int l = 0;
		l |= b[0] & 0xFF;
		l <<= 8;
		l |= b[1] & 0xFF;
		l <<= 8;
		l |= b[2] & 0xFF;
		l <<= 8;
		l |= b[3] & 0xFF;
		return l;
	}

}
