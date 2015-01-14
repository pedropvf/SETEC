package setec.g3.texttospeech;

import java.util.Locale;

import android.app.Activity;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class TextToSpeechHandler {
	/** Called when the activity is first created. */

	private TextToSpeech tts;
	public String text;
	private int result;
	Activity act;
	
	public TextToSpeechHandler(Activity _act) {
		act = _act;
		tts = new TextToSpeech(act.getApplicationContext(), 
			      new TextToSpeech.OnInitListener() {
			      @Override
			      public void onInit(int status) {
				  		if (status == TextToSpeech.SUCCESS) {
				  			Log.e("TTS", "Success");
				  			//result = tts.setLanguage(Locale.US);
				  			result = tts.setLanguage(new Locale("pt"));
	
				  			// tts.setPitch(5); // set pitch level
	
				  			// tts.setSpeechRate(2); // set speech speed rate
				  			speakOut("Olá. O meu nome é Siomara e serei a sua guia por hoje. Boa sorte!");
				  			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				  				Log.e("TTS", "Language is not supported");
				  			} 
				  		} else {
				  			Log.e("TTS", "Initilization Failed");
				  		}				
			        }
			      });
	}
	
	public void textToSpeechFinish(){
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
	}

	
	public void speakOut(String text) {
		try{
    		/*AudioManager audioManager = (AudioManager) act.getSystemService((act.getApplicationContext()).AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.startBluetoothSco();
            audioManager.setBluetoothScoOn(true);*/
			Log.e("TTS", text);
    		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            
    		//MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.kalimba);
    		//mediaPlayer.start(); 
    	}catch( Exception e){
    		Log.d("errors", e.toString());
    	}
		
		
	}
}