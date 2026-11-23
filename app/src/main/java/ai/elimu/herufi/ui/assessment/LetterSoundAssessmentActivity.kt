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

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        ttsViewModel = ViewModelProvider(this)[TextToSpeechViewModelImpl::class.java]

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
                    onOptionSelected = viewModel::onOptionSelected,
                    onFinish = { finishWithResult() }
                )
            }
        }
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
