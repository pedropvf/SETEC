package setec.g3.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import android.util.Log;

public class SendToProtocol extends Thread{

	ObjectInputStream in = ReadProtocol.appIn;
	ObjectOutputStream out = ReadProtocol.appOut;
	byte id; 
	byte spec;
	rqst request;
	byte[] packet;
	boolean priority;

	SendToProtocol(byte _id, byte _spec, boolean _priority, byte[] _packet) {

		id = _id;
		spec = _spec;
		packet = _packet;
		priority = _priority;
		request = new rqst(id, spec, priority, packet);
		
	}

	public void run() {

		try {
            
            Log.d("SendToProtocol","App is requesting id=" + request.id + " spec=" + request.spec);
            
            //Send request to protocol
            out.writeObject(request);
            
            Log.d("SendToProtocol","App sent object to protocol");
            
            //Protocol replies but never important
            rspns response = (rspns)in.readObject();
            Log.d("SendToProtocol","Protocol replied id=" + response.id);

		} catch (IOException e) {
			Log.e("ReadProtocol", e.toString());
		} catch (ClassNotFoundException e) {
			Log.e("ReadProtocol", e.toString());
		}
	}
}
