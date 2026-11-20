package ai.elimu.herufi.ui.assessment

import ai.elimu.content_provider.utils.ContentProviderUtil.getAllLetterSoundGsons
import ai.elimu.herufi.BuildConfig
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface LetterSoundAssessmentUiState {
    data object Loading : LetterSoundAssessmentUiState

    /** The content-provider could not be read, or it has too few letter-sounds to build a question. */
    data object Unavailable : LetterSoundAssessmentUiState

    /** @param selectedOption `null` until the learner has pressed one of the options. */
    data class Question(
        val question: LetterSoundQuestion,
        val selectedOption: LetterSoundOption? = null
    ) : LetterSoundAssessmentUiState {
        val isAnswered get() = selectedOption != null
        val isCorrect get() = selectedOption?.let { question.isCorrect(it) }
    }
}

class LetterSoundAssessmentViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "LetterSoundAssessmentViewModel"

    private val _uiState = MutableStateFlow<LetterSoundAssessmentUiState>(
        LetterSoundAssessmentUiState.Loading
    )
    val uiState: StateFlow<LetterSoundAssessmentUiState> = _uiState.asStateFlow()

    init {
        loadQuestion()
    }

    private fun loadQuestion() {
        viewModelScope.launch {
            val question = withContext(Dispatchers.IO) {
                try {
                    val letterSoundGsons = getAllLetterSoundGsons(
                        getApplication(), BuildConfig.CONTENT_PROVIDER_APPLICATION_ID
                    )
                    Log.i(TAG, "letterSoundGsons.size: " + letterSoundGsons.size)

                    val candidates = letterSoundGsons.map { letterSoundGson ->
                        LetterSoundCandidate(
                            id = letterSoundGson.id.toString(),
                            letters = letterSoundGson.letters.joinToString("") { it.text },
                            soundIpa = letterSoundGson.sounds.joinToString("") { it.valueIpa },
                            usageCount = letterSoundGson.usageCount
                        )
                    }
                    LetterSoundQuestionFactory.create(candidates)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load letter-sounds", e)
                    null
                }
            }
            _uiState.value = if (question != null) {
                LetterSoundAssessmentUiState.Question(question)
            } else {
                LetterSoundAssessmentUiState.Unavailable
            }
        }
    }

    /** Only the first answer counts; further presses are ignored. */
    fun onOptionSelected(option: LetterSoundOption) {
        _uiState.update { state ->
            if (state is LetterSoundAssessmentUiState.Question && !state.isAnswered) {
                state.copy(selectedOption = option)
            } else {
                state
            }
        }
    }
}
