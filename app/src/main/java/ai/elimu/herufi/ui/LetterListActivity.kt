package ai.elimu.herufi.ui

import ai.elimu.analytics.utils.LearningEventUtil
import ai.elimu.content_provider.utils.ContentProviderUtil.getAvailableLetterGsons
import ai.elimu.herufi.BaseApplication
import ai.elimu.herufi.BuildConfig
import ai.elimu.herufi.R
import ai.elimu.model.v2.enums.analytics.LearningEventType
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayout

class LetterListActivity : AppCompatActivity() {
    private var flexboxLayout: FlexboxLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(javaClass.name, "onCreate")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_letter_list)

        flexboxLayout = findViewById(R.id.letter_list_flexbox_layout)
    }

    override fun onStart() {
        Log.i(javaClass.name, "onStart")
        super.onStart()

        val letterGsons = getAvailableLetterGsons(
            applicationContext,
            BuildConfig.CONTENT_PROVIDER_APPLICATION_ID,
            BuildConfig.ANALYTICS_APPLICATION_ID
        )
        Log.i(javaClass.name, "letterGsons.size(): " + letterGsons.size)

        // Create a view for each letter in the list
        flexboxLayout!!.removeAllViews()
        for (letterGson in letterGsons) {
            val letterView = LayoutInflater.from(this)
                .inflate(R.layout.activity_letter_list_letter_view, flexboxLayout, false)

            val textView = letterView.findViewById<TextView>(R.id.letter_view_text_view)
            textView.text = letterGson.text

            // Play letter sound when pressed
            letterView.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    Log.i(javaClass.name, "letterView onClick")

                    Log.i(javaClass.name, "letterGson.getText(): '" + letterGson.text + "'")

                    val baseApplication = application as BaseApplication
                    val tts = baseApplication.tts
                    tts.speak(
                        letterGson.text,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "letter_" + letterGson.id
                    )

                    // Report learning event to the Analytics application (https://github.com/elimu-ai/analytics)
                    LearningEventUtil.reportLetterLearningEvent(
                        letterGson, LearningEventType.LETTER_PRESSED,
                        applicationContext, BuildConfig.ANALYTICS_APPLICATION_ID
                    )
                }
            })

            flexboxLayout!!.addView(letterView)
        }
    }
}
