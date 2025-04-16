package ai.elimu.herufi.ui

import ai.elimu.analytics.utils.LearningEventUtil
import ai.elimu.common.utils.ui.setLightStatusBar
import ai.elimu.common.utils.ui.setStatusBarColorCompat
import ai.elimu.content_provider.utils.ContentProviderUtil.getAllLetterSoundGsons
import ai.elimu.herufi.BaseApplication
import ai.elimu.herufi.BuildConfig
import ai.elimu.herufi.R
import ai.elimu.herufi.databinding.ActivityLetterSoundListBinding
import ai.elimu.herufi.databinding.ActivityLetterSoundListLetterViewBinding
import ai.elimu.model.v2.gson.content.LetterGson
import ai.elimu.model.v2.gson.content.SoundGson
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.util.stream.Collectors

class LetterSoundListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLetterSoundListBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(javaClass.name, "onCreate")
        super.onCreate(savedInstanceState)

        binding = ActivityLetterSoundListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.apply {
            setLightStatusBar()
            setStatusBarColorCompat(R.color.colorPrimaryDark)
        }
    }

    override fun onStart() {
        Log.i(javaClass.name, "onStart")
        super.onStart()

        val letterSoundGsons = getAllLetterSoundGsons(
            applicationContext, BuildConfig.CONTENT_PROVIDER_APPLICATION_ID
        )
        Log.i(javaClass.name, "letterSoundGsons.size(): " + letterSoundGsons.size)

        // Create a view for each letter-sound correspondence in the list
        binding.letterSoundListFlexboxLayout.removeAllViews()
        for (letterSoundGson in letterSoundGsons) {
            val letterSoundViewBinding = ActivityLetterSoundListLetterViewBinding.inflate(
                layoutInflater, binding.letterSoundListFlexboxLayout, false
            )
            val sounds =
                letterSoundGson.sounds.stream().map { obj: SoundGson -> obj.valueIpa }.collect(
                    Collectors.joining()
                )
            val letters =
                letterSoundGson.letters.stream().map { obj: LetterGson -> obj.text }.collect(
                    Collectors.joining()
                )
            letterSoundViewBinding.letterSoundViewTextView.text = "/$sounds/\n⬇\n\"$letters\""

            // Play sound when pressed
            letterSoundViewBinding.root.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    Log.i(javaClass.name, "letterView onClick")

                    Log.i(javaClass.name, "letterSoundGson.getId(): '" + letterSoundGson.id + "'")

                    val baseApplication = application as BaseApplication
                    val tts = baseApplication.tts
                    tts?.speak(
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

            binding.letterSoundListFlexboxLayout.addView(letterSoundViewBinding.root)
        }
    }
}
