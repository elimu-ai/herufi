package ai.elimu.herufi;

import android.app.Application;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class BaseApplication extends Application {

    private TextToSpeech tts;

    @Override
    public void onCreate() {
        Log.i(getClass().getName(), "onCreate");
        super.onCreate();

        // Initialize Text-to-Speech
        getTTS();
    }

    public TextToSpeech getTTS() {
        if (tts == null) {
            tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    Log.i(getClass().getName(), "TextToSpeech onInit");
                }
            });
        }
        return tts;
    }

    @Override
    public void onTerminate() {
        Log.i(getClass().getName(), "onTerminate");
        super.onTerminate();

        // Release the resources used by the TextToSpeech engine
        if (tts != null) {
            tts.shutdown();
        }
    }
}
