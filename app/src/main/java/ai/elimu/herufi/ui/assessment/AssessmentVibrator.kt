package ai.elimu.herufi.ui.assessment

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Haptic feedback for the assessment. */
class AssessmentVibrator(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /** Two short pulses, matching the two notes of the "uh-oh" sound. */
    fun vibrateIncorrect() {
        val vibrator = vibrator ?: return
        if (!vibrator.hasVibrator()) return
        // Timings: wait 0ms, vibrate 70ms, pause 70ms, vibrate 90ms (-1 = don't repeat)
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 70, 70, 90), -1))
    }
}
