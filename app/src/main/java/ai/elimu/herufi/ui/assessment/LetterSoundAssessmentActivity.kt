package ai.elimu.herufi.ui.assessment

import ai.elimu.common.utils.data.model.tts.QueueMode
import ai.elimu.common.utils.viewmodel.TextToSpeechViewModel
import ai.elimu.common.utils.viewmodel.TextToSpeechViewModelImpl
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint

/**
 * Assesses knowledge of sound-to-letter correspondences.
 *
 * Loads the most frequent letter-sound, plays/shows its sound, and lets the learner press
 * one of two letter options (one correct, one incorrect).
 *
 * The activity finishes with [RESULT_OK] and the extra [EXTRA_CORRECT] once the learner taps
 * "Finish" after answering, so callers can use `registerForActivityResult(StartActivityForResult())`.
 */
@AndroidEntryPoint
class LetterSoundAssessmentActivity : ComponentActivity() {

    private val TAG = "LetterSoundAssessmentActivity"

    private val viewModel: LetterSoundAssessmentViewModel by viewModels()
    private lateinit var ttsViewModel: TextToSpeechViewModel
    private lateinit var soundEffects: AssessmentSoundEffects
    private lateinit var vibrator: AssessmentVibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        ttsViewModel = ViewModelProvider(this)[TextToSpeechViewModelImpl::class.java]
        soundEffects = AssessmentSoundEffects(this)
        vibrator = AssessmentVibrator(this)

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            MaterialTheme(colorScheme = AssessmentColorScheme) {
                LetterSoundAssessmentScreen(
                    uiState = uiState,
                    onPlaySound = { question ->
                        // The sound is spoken by saying the correct letter(s), so the answer is
                        // never displayed as text before the learner has chosen.
                        ttsViewModel.speak(
                            question.correctOption.letters,
                            QueueMode.FLUSH,
                            "letter_sound_" + question.correctOption.id
                        )
                    },
                    onOptionSelected = ::onOptionSelected,
                    onFinish = { finishWithResult() }
                )
            }
        }
    }

    override fun onDestroy() {
        soundEffects.release()
        super.onDestroy()
    }

    private fun onOptionSelected(option: LetterSoundOption) {
        val state = viewModel.uiState.value
        // Only the first answer counts (and is reported), so only that one gets feedback
        if (state !is LetterSoundAssessmentUiState.Question || state.isAnswered) return

        viewModel.onOptionSelected(option)
        val correct = state.question.isCorrect(option)
        soundEffects.play(correct)
        if (!correct) vibrator.vibrateIncorrect()
    }

    private fun finishWithResult() {
        val state = viewModel.uiState.value
        if (state is LetterSoundAssessmentUiState.Question && state.isAnswered) {
            setResult(RESULT_OK, Intent().putExtra(EXTRA_CORRECT, state.isCorrect == true))
        } else {
            setResult(RESULT_CANCELED)
        }
        finish()
    }

    companion object {
        /** Boolean: whether the learner pressed the correct option. */
        const val EXTRA_CORRECT = "ai.elimu.herufi.extra.CORRECT"
    }
}
