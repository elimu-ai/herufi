package ai.elimu.herufi.ui.assessment

import ai.elimu.herufi.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CorrectColor = Color(0xFF388E3C) // green_darken_2
private val IncorrectColor = Color(0xFFC62828)

@Composable
fun LetterSoundAssessmentScreen(
    uiState: LetterSoundAssessmentUiState,
    onPlaySound: (LetterSoundQuestion) -> Unit,
    onOptionSelected: (LetterSoundOption) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (uiState) {
            LetterSoundAssessmentUiState.Loading -> Centered {
                CircularProgressIndicator()
            }

            LetterSoundAssessmentUiState.Unavailable -> Centered {
                Text(
                    text = stringResource(R.string.assessment_unavailable),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
                Button(onClick = onFinish) {
                    Text(stringResource(R.string.assessment_finish))
                }
            }

            is LetterSoundAssessmentUiState.Question -> QuestionContent(
                state = uiState,
                onPlaySound = onPlaySound,
                onOptionSelected = onOptionSelected,
                onFinish = onFinish
            )
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

@Composable
private fun QuestionContent(
    state: LetterSoundAssessmentUiState.Question,
    onPlaySound: (LetterSoundQuestion) -> Unit,
    onOptionSelected: (LetterSoundOption) -> Unit,
    onFinish: () -> Unit
) {
    val question = state.question

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.assessment_prompt),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        // The sound, shown as IPA, with a button for hearing it
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.assessment_sound_ipa, question.soundIpa),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = { onPlaySound(question) }) {
                Text(
                    text = stringResource(R.string.assessment_play_sound),
                    fontSize = 24.sp
                )
            }
        }

        // The two options: one correct, one incorrect
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            question.options.forEach { option ->
                OptionButton(
                    option = option,
                    state = state,
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Feedback, shown once the learner has answered
        if (state.isAnswered) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val correct = state.isCorrect == true
                Text(
                    text = stringResource(
                        if (correct) R.string.assessment_correct else R.string.assessment_incorrect
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (correct) CorrectColor else IncorrectColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onFinish) {
                    Text(stringResource(R.string.assessment_finish))
                }
            }
        }
    }
}

@Composable
private fun OptionButton(
    option: LetterSoundOption,
    state: LetterSoundAssessmentUiState.Question,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = state.selectedOption?.id == option.id
    val isCorrectOption = state.question.isCorrect(option)

    // After answering: reveal the correct option in green, and mark a wrong pick in red.
    // The ✓/✗ mark means colour is not the only signal.
    val (container, mark) = when {
        !state.isAnswered -> MaterialTheme.colorScheme.primary to ""
        isCorrectOption -> CorrectColor to " ✓"
        isSelected -> IncorrectColor to " ✗"
        else -> MaterialTheme.colorScheme.surfaceVariant to ""
    }
    val content = when {
        !state.isAnswered -> MaterialTheme.colorScheme.onPrimary
        isCorrectOption || isSelected -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Button(
        onClick = onClick,
        enabled = !state.isAnswered,
        shape = RoundedCornerShape(24.dp),
        // Keep answered colours vivid instead of the default greyed-out disabled look
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = container,
            disabledContentColor = content
        ),
        modifier = modifier.height(160.dp)
    ) {
        Text(
            text = option.letters + mark,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun QuestionPreview() {
    val correct = LetterSoundOption("1", "a")
    val wrong = LetterSoundOption("2", "m")
    MaterialTheme {
        LetterSoundAssessmentScreen(
            uiState = LetterSoundAssessmentUiState.Question(
                LetterSoundQuestion("æ", correct, listOf(wrong, correct))
            ),
            onPlaySound = {},
            onOptionSelected = {},
            onFinish = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun AnsweredIncorrectPreview() {
    val correct = LetterSoundOption("1", "a")
    val wrong = LetterSoundOption("2", "m")
    MaterialTheme {
        LetterSoundAssessmentScreen(
            uiState = LetterSoundAssessmentUiState.Question(
                question = LetterSoundQuestion("æ", correct, listOf(wrong, correct)),
                selectedOption = wrong
            ),
            onPlaySound = {},
            onOptionSelected = {},
            onFinish = {}
        )
    }
}
