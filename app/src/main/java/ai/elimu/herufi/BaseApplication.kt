package ai.elimu.herufi

import android.app.Application
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log

class BaseApplication : Application() {
    var tts: TextToSpeech? = null

    override fun onCreate() {
        Log.i(javaClass.name, "onCreate")
        super.onCreate()

        // Initialize Text-to-Speech
        tTS
    }

    val tTS: TextToSpeech
        get() {
            if (tts == null) {
                tts = TextToSpeech(applicationContext, object : OnInitListener {
                    override fun onInit(status: Int) {
                        Log.i(javaClass.name, "TextToSpeech onInit")
                    }
                })
            }
            return tts as TextToSpeech
        }

    override fun onTerminate() {
        Log.i(javaClass.name, "onTerminate")
        super.onTerminate()

        // Release the resources used by the TextToSpeech engine
        if (tts != null) {
            tts!!.shutdown()
        }
    }
}
