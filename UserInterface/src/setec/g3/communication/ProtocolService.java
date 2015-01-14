package setec.g3.communication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ProtocolService extends Service {
	
	//constructor
	public ProtocolService(){
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
	}
	
	public void onStart() {
	}
	
	public void onDestroy() {
	}
	

}
