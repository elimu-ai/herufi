package ai.elimu.herufi.ui

import ai.elimu.analytics.utils.LearningEventUtil
import ai.elimu.common.utils.data.model.tts.QueueMode
import ai.elimu.common.utils.ui.setLightStatusBar
import ai.elimu.common.utils.ui.setStatusBarColorCompat
import ai.elimu.common.utils.viewmodel.TextToSpeechViewModel
import ai.elimu.common.utils.viewmodel.TextToSpeechViewModelImpl
import ai.elimu.content_provider.utils.ContentProviderUtil.getAllLetterSoundGsons
import ai.elimu.herufi.BuildConfig
import ai.elimu.herufi.R
import ai.elimu.herufi.databinding.ActivityLetterSoundListBinding
import ai.elimu.herufi.databinding.ActivityLetterSoundListLetterViewBinding
import ai.elimu.model.v2.gson.content.LetterGson
import ai.elimu.model.v2.gson.content.SoundGson
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import java.util.stream.Collectors

@AndroidEntryPoint
class LetterSoundListActivity : AppCompatActivity() {
    
    private val TAG = "LetterSoundListActivity"
    private lateinit var binding: ActivityLetterSoundListBinding
    private lateinit var ttsViewModel: TextToSpeechViewModel

    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        binding = ActivityLetterSoundListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ttsViewModel = ViewModelProvider(this)[TextToSpeechViewModelImpl::class.java]

        window.apply {
            setLightStatusBar()
            setStatusBarColorCompat(R.color.colorPrimaryDark)
        }
    }

    override fun onStart() {
        Log.i(TAG, "onStart")
        super.onStart()

        val letterSoundGsons = getAllLetterSoundGsons(
            applicationContext, BuildConfig.CONTENT_PROVIDER_APPLICATION_ID
        )
        Log.i(TAG, "letterSoundGsons.size(): " + letterSoundGsons.size)

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
            letterSoundViewBinding.letterSoundViewTextView.text =
                getString(R.string.text_letter_sound, sounds, letters)

            // Play sound when pressed
            letterSoundViewBinding.root.setOnClickListener {
                Log.i(TAG, "letterView onClick")

                Log.i(TAG, "letterSoundGson.getId(): '" + letterSoundGson.id + "'")

                    ttsViewModel.speak(
                        letters,
                        QueueMode.FLUSH,
                        "letter_sound_" + letterSoundGson.id
                    )

                // Report learning event to the Analytics application (https://github.com/elimu-ai/analytics)
                LearningEventUtil.reportLetterSoundLearningEvent(
                    letterSoundGson,
                    applicationContext, BuildConfig.ANALYTICS_APPLICATION_ID
                )
            }

            binding.letterSoundListFlexboxLayout.addView(letterSoundViewBinding.root)
        }
    }
}
