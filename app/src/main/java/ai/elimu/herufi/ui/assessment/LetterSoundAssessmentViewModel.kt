package ai.elimu.herufi.ui.assessment

import ai.elimu.analytics.utils.AssessmentEventUtil
import ai.elimu.content_provider.utils.ContentProviderUtil.getAllLetterSoundGsons
import ai.elimu.herufi.BuildConfig
import ai.elimu.model.v2.gson.content.LetterSoundGson
import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

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

    /** The letter-sound the student is being assessed for. Needed when reporting the result. */
    private var assessedLetterSoundGson: LetterSoundGson? = null

    /** When the question became visible; used to calculate `timeSpentMs`. */
    private var questionShownAtMs = 0L

    init {
        loadQuestion()
    }

    private class LoadedQuestion(
        val question: LetterSoundQuestion,
        val assessedLetterSoundGson: LetterSoundGson
    )

    private fun loadQuestion() {
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                try {
                    val letterSoundGsons = getAllLetterSoundGsons(
                        getApplication(), BuildConfig.CONTENT_PROVIDER_APPLICATION_ID
                    )
                    Log.i(TAG, "letterSoundGsons.size: " + letterSoundGsons.size)

                    // `id` and `usageCount` are nullable on the Java model, so fall back to
                    // values that cannot make two different letter-sounds look identical.
                    val gsonByCandidateId = letterSoundGsons.withIndex().associate { (index, gson) ->
                        (gson.id?.toString() ?: "index_$index") to gson
                    }
                    val candidates = gsonByCandidateId.map { (candidateId, gson) ->
                        LetterSoundCandidate(
                            id = candidateId,
                            letters = gson.letters.joinToString("") { it.text },
                            soundIpa = gson.sounds.joinToString("") { it.valueIpa },
                            usageCount = gson.usageCount ?: 0
                        )
                    }
                    val question = LetterSoundQuestionFactory.create(candidates)
                    val assessedGson = question?.let { gsonByCandidateId[it.correctOption.id] }
                    if (question != null && assessedGson != null) {
                        LoadedQuestion(question, assessedGson)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load letter-sounds", e)
                    null
                }
            }
            if (loaded != null) {
                assessedLetterSoundGson = loaded.assessedLetterSoundGson
                questionShownAtMs = SystemClock.elapsedRealtime()
                _uiState.value = LetterSoundAssessmentUiState.Question(loaded.question)
            } else {
                _uiState.value = LetterSoundAssessmentUiState.Unavailable
            }
        }
    }

    /**
     * Only the first answer counts and is reported; further presses are ignored.
     * Called from the main thread, so the check-then-set below cannot race.
     */
    fun onOptionSelected(option: LetterSoundOption) {
        val state = _uiState.value
        if (state !is LetterSoundAssessmentUiState.Question || state.isAnswered) return

        _uiState.value = state.copy(selectedOption = option)
        reportAssessmentEvent(state.question, option)
    }

    /**
     * Reports the result to the Analytics application (https://github.com/elimu-ai/analytics)
     */
    private fun reportAssessmentEvent(question: LetterSoundQuestion, selectedOption: LetterSoundOption) {
        val letterSoundGson = assessedLetterSoundGson ?: return
        val timeSpentMs = SystemClock.elapsedRealtime() - questionShownAtMs

        val additionalData = JSONObject()
            .put("selected_letters", selectedOption.letters)
            .put("offered_letters", JSONArray(question.options.map { it.letters }))

        AssessmentEventUtil.reportLetterSoundAssessmentEvent(
            letterSoundGson = letterSoundGson,
            masteryScore = question.masteryScoreFor(selectedOption),
            timeSpentMs = timeSpentMs,
            additionalData = additionalData,
            context = getApplication(),
            analyticsApplicationId = BuildConfig.ANALYTICS_APPLICATION_ID
        )
    }
}
