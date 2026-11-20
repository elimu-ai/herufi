package ai.elimu.herufi.ui.assessment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LetterSoundQuestionFactoryTest {

    private fun candidate(id: String, letters: String, ipa: String, usage: Int) =
        LetterSoundCandidate(id, letters, ipa, usage)

    @Test
    fun usesMostFrequentLetterSoundAsCorrectAnswer() {
        val question = LetterSoundQuestionFactory.create(
            listOf(
                candidate("1", "m", "m", 10),
                candidate("2", "a", "æ", 99),
                candidate("3", "s", "s", 50)
            )
        )!!

        assertEquals("æ", question.soundIpa)
        assertEquals("a", question.correctOption.letters)
    }

    @Test
    fun offersExactlyTwoOptions_oneCorrectOneIncorrect() {
        val question = LetterSoundQuestionFactory.create(
            listOf(
                candidate("1", "a", "æ", 99),
                candidate("2", "s", "s", 50),
                candidate("3", "m", "m", 10)
            )
        )!!

        assertEquals(2, question.options.size)
        assertEquals(1, question.options.count { question.isCorrect(it) })
        assertEquals(1, question.options.count { !question.isCorrect(it) })
    }

    @Test
    fun distractorIsNextMostFrequent() {
        val question = LetterSoundQuestionFactory.create(
            listOf(
                candidate("1", "a", "æ", 99),
                candidate("2", "s", "s", 50),
                candidate("3", "m", "m", 10)
            )
        )!!

        val distractor = question.options.first { !question.isCorrect(it) }
        assertEquals("s", distractor.letters)
    }

    @Test
    fun distractorCannotBeAnotherValidAnswer() {
        val question = LetterSoundQuestionFactory.create(
            listOf(
                candidate("1", "k", "k", 99),
                candidate("2", "c", "k", 90), // also /k/ → not a fair distractor
                candidate("3", "K", "g", 80), // same letter, different case → not fair either
                candidate("4", "m", "m", 10)
            )
        )!!

        val distractor = question.options.first { !question.isCorrect(it) }
        assertEquals("m", distractor.letters)
    }

    @Test
    fun skipsEntriesWithoutLettersOrSounds() {
        val question = LetterSoundQuestionFactory.create(
            listOf(
                candidate("1", "", "x", 999),
                candidate("2", "x", " ", 998),
                candidate("3", "a", "æ", 5),
                candidate("4", "m", "m", 1)
            )
        )!!

        assertEquals("a", question.correctOption.letters)
    }

    @Test
    fun returnsNullWhenThereIsNoValidDistractor() {
        assertNull(LetterSoundQuestionFactory.create(emptyList()))
        assertNull(LetterSoundQuestionFactory.create(listOf(candidate("1", "a", "æ", 1))))
        assertNull(
            LetterSoundQuestionFactory.create(
                listOf(candidate("1", "a", "æ", 1), candidate("2", "A", "æ", 1))
            )
        )
    }

    @Test
    fun optionOrderIsRandomised() {
        val candidates = listOf(candidate("1", "a", "æ", 99), candidate("2", "s", "s", 50))
        val firstOptionIds = (0 until 50).map { seed ->
            LetterSoundQuestionFactory.create(candidates, Random(seed))!!.options.first().id
        }.toSet()

        assertTrue("correct option should not always be first", firstOptionIds.size == 2)
    }

    @Test
    fun masteryScoreIsOneForCorrectAndZeroForIncorrect() {
        val question = LetterSoundQuestionFactory.create(
            listOf(candidate("1", "a", "æ", 99), candidate("2", "s", "s", 50))
        )!!

        assertEquals(1f, question.masteryScoreFor(question.correctOption), 0f)
        val incorrect = question.options.first { !question.isCorrect(it) }
        assertEquals(0f, question.masteryScoreFor(incorrect), 0f)
    }

    @Test
    fun onlyTheCorrectOptionIsCorrect() {
        val question = LetterSoundQuestionFactory.create(
            listOf(candidate("1", "a", "æ", 99), candidate("2", "s", "s", 50))
        )
        assertNotNull(question)
        assertTrue(question!!.isCorrect(LetterSoundOption("1", "a")))
        assertFalse(question.isCorrect(LetterSoundOption("2", "s")))
    }
}
