package ai.elimu.herufi.ui

import ai.elimu.analytics.utils.LearningEventUtil
import ai.elimu.content_provider.utils.ContentProviderUtil.getAllLetterSoundGsons
import ai.elimu.herufi.BaseApplication
import ai.elimu.herufi.BuildConfig
import ai.elimu.herufi.R
import ai.elimu.model.v2.gson.content.LetterGson
import ai.elimu.model.v2.gson.content.SoundGson
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayout
import java.util.stream.Collectors

class LetterSoundListActivity : AppCompatActivity() {
    private var flexboxLayout: FlexboxLayout? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(javaClass.name, "onCreate")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_letter_sound_list)

        flexboxLayout = findViewById(R.id.letter_sound_list_flexbox_layout)
    }

    override fun onStart() {
        Log.i(javaClass.name, "onStart")
        super.onStart()

        val letterSoundGsons = getAllLetterSoundGsons(
            applicationContext, BuildConfig.CONTENT_PROVIDER_APPLICATION_ID
        )
        Log.i(javaClass.name, "letterSoundGsons.size(): " + letterSoundGsons.size)

        // Create a view for each letter-sound correspondence in the list
        flexboxLayout!!.removeAllViews()
        for (letterSoundGson in letterSoundGsons) {
            val letterSoundView = LayoutInflater.from(this)
                .inflate(R.layout.activity_letter_sound_list_letter_view, flexboxLayout, false)

            val textView = letterSoundView.findViewById<TextView>(R.id.letter_sound_view_text_view)
            val sounds =
                letterSoundGson.sounds.stream().map { obj: SoundGson -> obj.valueIpa }.collect(
                    Collectors.joining()
                )
            val letters =
                letterSoundGson.letters.stream().map { obj: LetterGson -> obj.text }.collect(
                    Collectors.joining()
                )
            textView.text = "/$sounds/\n⬇\n\"$letters\""

            // Play sound when pressed
            letterSoundView.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    Log.i(javaClass.name, "letterView onClick")

                    Log.i(javaClass.name, "letterSoundGson.getId(): '" + letterSoundGson.id + "'")

                    val baseApplication = application as BaseApplication
                    val tts = baseApplication.tts
                    tts.speak(
                        letters,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "letter_sound_" + letterSoundGson.id
                    )

                    // Report learning event to the Analytics application (https://github.com/elimu-ai/analytics)
                    LearningEventUtil.reportLetterSoundLearningEvent(
                        letterSoundGson,
                        applicationContext, BuildConfig.ANALYTICS_APPLICATION_ID
                    )
                }
            })

            flexboxLayout!!.addView(letterSoundView)
        }
    }
}
