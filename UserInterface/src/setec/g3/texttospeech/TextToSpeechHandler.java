package setec.g3.texttospeech;

import java.util.Locale;
import setec.g3.ui.R;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
				  			speakOut("Bem vindo!");
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
    		AudioManager audioManager = (AudioManager) act.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.startBluetoothSco();
            audioManager.setBluetoothScoOn(true);
			Log.d("TTS", text);
			
		/*	MediaPlayer mediaPlayer = MediaPlayer.create(act.getApplicationContext(), R.raw.alarm);
    		mediaPlayer.start();
    		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
    	         public void onCompletion(MediaPlayer mp) {
    	             mp.release();
    	         }
    	     });
			*/
    		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            
    	}catch( Exception e){
    		Log.d("errors", e.toString());
    	}
		
		
	}
}