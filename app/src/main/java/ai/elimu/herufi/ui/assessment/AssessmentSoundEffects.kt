package ai.elimu.herufi.ui.assessment

import ai.elimu.herufi.R
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Soft feedback sounds for the assessment: a gentle chime for a correct answer, and a
 * falling "uh-oh" for an incorrect one. Call [release] when done.
 */
class AssessmentSoundEffects(context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Loaded asynchronously, but they are tiny and are long loaded by the time the learner answers
    private val successId = soundPool.load(context, R.raw.assessment_success, 1)
    private val uhOhId = soundPool.load(context, R.raw.assessment_uh_oh, 1)

    fun play(correct: Boolean) {
        soundPool.play(if (correct) successId else uhOhId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
