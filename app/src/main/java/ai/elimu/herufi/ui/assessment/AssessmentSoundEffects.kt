package ai.elimu.herufi.ui.assessment

import ai.elimu.herufi.R
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Soft feedback sounds for the assessment: a rising two-note tune for a correct answer, and
 * its opposite, a falling "uh-oh", for an incorrect one. Call [release] when done.
 */
class AssessmentSoundEffects(context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(
            AudioAttributes.Builder()
                // Played on the media stream, so the volume follows the same volume buttons as
                // the spoken letter-sound (the "sonification" usage can be much quieter)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Loaded asynchronously, but they are tiny and are long loaded by the time the learner answers
    private val successId = soundPool.load(context, R.raw.assessment_success, 1)
    private val failureId = soundPool.load(context, R.raw.assessment_failure, 1)

    fun play(correct: Boolean) {
        soundPool.play(if (correct) successId else failureId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
