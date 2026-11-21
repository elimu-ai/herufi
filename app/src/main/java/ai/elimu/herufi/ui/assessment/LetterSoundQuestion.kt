package ai.elimu.herufi.ui.assessment

import kotlin.random.Random

/**
 * A letter-sound correspondence reduced to what the assessment needs.
 */
data class LetterSoundCandidate(
    val id: String,
    val letters: String,
    val soundIpa: String,
    val usageCount: Int
)

/**
 * One answer option shown to the learner.
 */
data class LetterSoundOption(
    val id: String,
    val letters: String
)

/**
 * "Which letter(s) make this sound?" with exactly two options: one correct and one incorrect.
 */
data class LetterSoundQuestion(
    val soundIpa: String,
    val correctOption: LetterSoundOption,
    val options: List<LetterSoundOption>
) {
    fun isCorrect(option: LetterSoundOption) = option.id == correctOption.id

    /** The mastery score reported to Analytics: `1.0` for the correct option, `0.0` otherwise. */
    fun masteryScoreFor(option: LetterSoundOption): Float = if (isCorrect(option)) 1f else 0f
}

object LetterSoundQuestionFactory {

    /**
     * Builds a question about the most frequent letter-sound correspondence.
     *
     * The distractor is the most frequent *other* letter-sound that cannot also be a valid
     * answer, i.e. it must have different letters and a different sound. (For example, if
     * the correct answer is /k/ → "k", then "c" is not a fair distractor, since it can also
     * be pronounced /k/.)
     *
     * @return `null` if there is no usable letter-sound, or no valid distractor.
     */
    fun create(
        candidates: List<LetterSoundCandidate>,
        random: Random = Random.Default
    ): LetterSoundQuestion? {
        val byFrequency = candidates
            .filter { it.letters.isNotBlank() && it.soundIpa.isNotBlank() }
            .sortedByDescending { it.usageCount }

        val correct = byFrequency.firstOrNull() ?: return null
        val distractor = byFrequency.firstOrNull {
            it.id != correct.id
                    && !it.letters.equals(correct.letters, ignoreCase = true)
                    && it.soundIpa != correct.soundIpa
        } ?: return null

        val correctOption = LetterSoundOption(correct.id, correct.letters)
        val distractorOption = LetterSoundOption(distractor.id, distractor.letters)

        return LetterSoundQuestion(
            soundIpa = correct.soundIpa,
            correctOption = correctOption,
            options = listOf(correctOption, distractorOption).shuffled(random)
        )
    }
}
