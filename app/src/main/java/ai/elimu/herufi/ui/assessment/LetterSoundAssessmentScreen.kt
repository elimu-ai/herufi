package ai.elimu.herufi.ui.assessment

import ai.elimu.herufi.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Teal = Color(0xFF429488)
private val IncorrectColor = Color(0xFFC62828)

/** Tile that is not (or no longer) in play: pale grey, with a muted letter. */
private val InactiveTileColor = Color(0xFFD6D7D7)
private val InactiveLetterColor = Color(0xFFA6A6A6)

/** Tiles waiting for an answer: same grey, but with a letter that is easy to read. */
private val UnansweredLetterColor = Color(0xFF5F6368)

/** Flat teal-on-off-white look used by the assessment screen. */
internal val AssessmentColorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFAFAFA)
)

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // The sound is played by the learner pressing the ear. Nothing else is written on the
        // screen, so the learner has to rely on what they hear.
        PlaySoundButton(onClick = { onPlaySound(state.question) })

        Spacer(Modifier.height(48.dp))

        // The two options: one correct, one incorrect. Flat square tiles, almost touching.
        Row(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            state.question.options.forEach { option ->
                OptionTile(
                    option = option,
                    state = state,
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Reserve the space so that the tiles don't move when the button appears.
        Box(modifier = Modifier.height(48.dp), contentAlignment = Alignment.Center) {
            if (state.isAnswered) {
                Button(onClick = onFinish) {
                    Text(stringResource(R.string.assessment_finish))
                }
            }
        }
    }
}

@Composable
private fun PlaySoundButton(onClick: () -> Unit) {
    val description = stringResource(R.string.assessment_play_sound)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_hearing),
            contentDescription = null, // described by the parent
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
private fun OptionTile(
    option: LetterSoundOption,
    state: LetterSoundAssessmentUiState.Question,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = state.selectedOption?.id == option.id
    val isCorrectOption = state.question.isCorrect(option)

    // Before answering: both tiles look the same, so nothing gives the answer away.
    // After answering: the correct tile is revealed in teal, a wrong pick turns red, and the
    // remaining tile fades to pale grey. The state is also exposed to screen readers, so the
    // colour is not the only signal.
    val container: Color
    val letterColor: Color
    val result: Int?
    when {
        !state.isAnswered -> {
            container = InactiveTileColor
            letterColor = UnansweredLetterColor
            result = null
        }

        isCorrectOption -> {
            container = MaterialTheme.colorScheme.primary
            letterColor = MaterialTheme.colorScheme.onPrimary
            result = R.string.assessment_correct
        }

        isSelected -> {
            container = IncorrectColor
            letterColor = Color.White
            result = R.string.assessment_incorrect
        }

        else -> {
            container = InactiveTileColor
            letterColor = InactiveLetterColor
            result = null
        }
    }
    val resultDescription = result?.let { stringResource(it) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .background(container)
            .clickable(enabled = !state.isAnswered, role = Role.Button, onClick = onClick)
            .semantics { resultDescription?.let { stateDescription = it } }
    ) {
        Text(
            text = option.letters,
            color = letterColor,
            fontSize = 72.sp,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun QuestionPreview() {
    val correct = LetterSoundOption("1", "a")
    val wrong = LetterSoundOption("2", "u")
    MaterialTheme(colorScheme = AssessmentColorScheme) {
        LetterSoundAssessmentScreen(
            uiState = LetterSoundAssessmentUiState.Question(
                LetterSoundQuestion("æ", correct, listOf(correct, wrong))
            ),
            onPlaySound = {},
            onOptionSelected = {},
            onFinish = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun AnsweredCorrectPreview() {
    val correct = LetterSoundOption("1", "a")
    val wrong = LetterSoundOption("2", "u")
    MaterialTheme(colorScheme = AssessmentColorScheme) {
        LetterSoundAssessmentScreen(
            uiState = LetterSoundAssessmentUiState.Question(
                question = LetterSoundQuestion("æ", correct, listOf(correct, wrong)),
                selectedOption = correct
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
    val wrong = LetterSoundOption("2", "u")
    MaterialTheme(colorScheme = AssessmentColorScheme) {
        LetterSoundAssessmentScreen(
            uiState = LetterSoundAssessmentUiState.Question(
                question = LetterSoundQuestion("æ", correct, listOf(correct, wrong)),
                selectedOption = wrong
            ),
            onPlaySound = {},
            onOptionSelected = {},
            onFinish = {}
        )
    }
}
