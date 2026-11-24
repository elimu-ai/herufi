package ai.elimu.herufi.ui.assessment

import ai.elimu.herufi.R
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private val Teal = Color(0xFF429488)

/** The tile the learner picked turns dark blue (correct) or dark orange (incorrect). */
private val SelectedCorrectColor = Color(0xFF1565C0)
private val SelectedIncorrectColor = Color(0xFFE65100)

/** The whole screen turns light blue after a correct answer, and light orange after an incorrect one. */
private val CorrectBackgroundColor = Color(0xFFBBDEFB)
private val IncorrectBackgroundColor = Color(0xFFFFE0B2)

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
    val answerCorrect = (uiState as? LetterSoundAssessmentUiState.Question)?.isCorrect
    val background by animateColorAsState(
        targetValue = when (answerCorrect) {
            true -> CorrectBackgroundColor
            false -> IncorrectBackgroundColor
            null -> MaterialTheme.colorScheme.background // not answered yet
        },
        animationSpec = tween(durationMillis = 300),
        label = "background"
    )

    Surface(modifier = modifier.fillMaxSize(), color = background) {
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
                RaisedButton(
                    text = stringResource(R.string.assessment_finish),
                    onClick = onFinish
                )
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
        // The learner has to listen before answering: the options can't be pressed until the
        // sound has been played at least once.
        var soundPlayed by rememberSaveable(state.question.correctOption.id) {
            mutableStateOf(false)
        }

        // The sound is played by the learner pressing the ear. Nothing else is written on the
        // screen, so the learner has to rely on what they hear.
        PlaySoundButton(onClick = {
            soundPlayed = true
            onPlaySound(state.question)
        })

        Spacer(Modifier.height(48.dp))

        // The two options: one correct, one incorrect. Square tiles, side by side.
        Row(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.question.options.forEach { option ->
                OptionTile(
                    option = option,
                    state = state,
                    enabled = soundPlayed,
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Reserve the space so that the tiles don't move when the button appears.
        Box(modifier = Modifier.height(56.dp), contentAlignment = Alignment.Center) {
            if (state.isAnswered) {
                RaisedButton(
                    text = stringResource(R.string.assessment_finish),
                    onClick = onFinish
                )
            }
        }
    }
}

private val TileShape = RoundedCornerShape(12.dp)
private val RaisedEdgeHeight = 6.dp

/**
 * A button that looks raised: its face sits on a solid, darker edge of the same shape.
 * While pressed, the face drops onto the edge, so the button looks flat. When released, it
 * animates back up.
 */
@Composable
private fun RaisedSurface(
    faceColor: Color,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    edgeHeight: Dp = RaisedEdgeHeight,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val drop by animateDpAsState(
        targetValue = if (isPressed) edgeHeight else 0.dp,
        // Down quickly, back up a little slower so that the release can be seen
        animationSpec = tween(durationMillis = if (isPressed) 60 else 140),
        label = "raised-surface-drop"
    )

    Box(
        // The caller's size applies to the face; the edge sticks out below it
        propagateMinConstraints = true,
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null, // the face dropping is the feedback
            enabled = enabled,
            role = Role.Button,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = edgeHeight)
                .background(lerp(faceColor, Color.Black, 0.25f), shape)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset { IntOffset(0, drop.roundToPx()) }
                .clip(shape)
                .background(faceColor),
            content = content
        )
    }
}

@Composable
private fun RaisedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    RaisedSurface(
        faceColor = MaterialTheme.colorScheme.primary,
        shape = TileShape,
        onClick = onClick,
        edgeHeight = 4.dp,
        modifier = modifier
            .height(48.dp)
            .widthIn(min = 160.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

/** How long one glow pulse takes, and how many pulses are shown after each press. */
private const val PULSE_DURATION_MS = 750
private const val PULSE_COUNT = 2

@Composable
private fun PlaySoundButton(onClick: () -> Unit) {
    val description = stringResource(R.string.assessment_play_sound)
    val glowColor = MaterialTheme.colorScheme.primary

    // A soft glow pulses outwards from the button to show that audio is playing. The
    // text-to-speech engine is not observed, so the glow runs for a fixed time (about as long
    // as a letter-sound takes to say) after each press. Pressing again restarts it.
    var pressCount by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    val pulseProgress = remember { Animatable(0f) }
    LaunchedEffect(pressCount) {
        if (pressCount == 0) return@LaunchedEffect
        isPlaying = true
        repeat(PULSE_COUNT) {
            pulseProgress.snapTo(0f)
            pulseProgress.animateTo(1f, tween(PULSE_DURATION_MS, easing = LinearOutSlowInEasing))
        }
        isPlaying = false
    }

    RaisedSurface(
        faceColor = MaterialTheme.colorScheme.primary,
        shape = CircleShape,
        onClick = {
            pressCount++
            onClick()
        },
        modifier = Modifier
            .size(72.dp)
            // Drawn behind everything, so the glow is not cut off at the edge of the circle
            .drawBehind {
                if (!isPlaying) return@drawBehind
                // Two rings, half a cycle apart, so there is always one in view
                listOf(pulseProgress.value, (pulseProgress.value + 0.5f) % 1f).forEach { p ->
                    val radius = size.minDimension / 2f * (1f + 0.9f * p)
                    drawCircle(
                        brush = Brush.radialGradient(
                            0.5f to glowColor.copy(alpha = 0.45f * (1f - p)),
                            1f to glowColor.copy(alpha = 0f),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                }
            }
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

/** One star of the burst: where it flies, how big it is, how fast it spins, and its colour. */
private class StarParticle(
    val angleRad: Float,
    val distanceDp: Float,
    val sizeDp: Float,
    val spinDegrees: Float,
    val color: Color
)

/** Warm, soft colours that show up on both the light blue and the light orange background. */
private val StarColors = listOf(
    Color(0xFFFFC107),
    Color(0xFFFFCA28),
    Color(0xFFFFD54F),
    Color(0xFFFFA726)
)

private const val STAR_COUNT = 12

// A fixed seed: the burst looks the same every time (and in previews)
private val StarParticles: List<StarParticle> = Random(7).let { random ->
    List(STAR_COUNT) { i ->
        StarParticle(
            angleRad = (i * 2 * PI / STAR_COUNT + (random.nextFloat() - 0.5f) * 0.4f).toFloat(),
            distanceDp = 120f + random.nextFloat() * 100f,
            sizeDp = 14f + random.nextFloat() * 14f,
            spinDegrees = (if (random.nextBoolean()) 1 else -1) * (120f + random.nextFloat() * 120f),
            color = StarColors[i % StarColors.size]
        )
    }
}

/** A five-pointed star with a radius of 1, centred on (0, 0). */
private val UnitStar = Path().apply {
    for (i in 0 until 10) {
        val radius = if (i % 2 == 0) 1f else 0.5f
        val angle = -PI / 2 + i * PI / 5
        val x = (radius * cos(angle)).toFloat()
        val y = (radius * sin(angle)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

/** Draws the stars flying out from the centre; [progress] goes from 0 (start) to 1 (gone). */
private fun DrawScope.drawStarBurst(progress: Float) {
    if (progress <= 0f || progress >= 1f) return

    val flown = 1f - (1f - progress) * (1f - progress) * (1f - progress) // fast start, slow end
    val fadeIn = (progress / 0.08f).coerceAtMost(1f)
    val alpha = fadeIn * (1f - progress)

    StarParticles.forEach { star ->
        val distance = star.distanceDp.dp.toPx() * flown
        val sink = 48.dp.toPx() * progress * progress // slowly drifts down, like confetti
        val x = center.x + cos(star.angleRad) * distance
        val y = center.y + sin(star.angleRad) * distance + sink
        val size = star.sizeDp.dp.toPx() * (1f - 0.4f * progress)

        translate(x, y) {
            rotate(star.spinDegrees * progress, Offset.Zero) {
                scale(size, Offset.Zero) {
                    drawPath(UnitStar, star.color, alpha)
                    // A rounded outline softens the points of the star
                    drawPath(
                        UnitStar,
                        star.color,
                        alpha,
                        Stroke(width = 0.3f, join = StrokeJoin.Round)
                    )
                }
            }
        }
    }
}

/** Animations of an option tile, played when it is picked. */
private class TileAnimation {
    /** How far the tile is lifted, in dp. */
    val lift = Animatable(0f)
    val scaleX = Animatable(1f)
    val scaleY = Animatable(1f)

    /** How far the tile is shifted to the right, in dp. */
    val shift = Animatable(0f)

    /** Progress of the star burst, from 0 to 1. */
    val burst = Animatable(0f)

    /** A gentle burst of stars: they fly out from the tile, slowly sink, and fade away. */
    suspend fun starBurst() {
        burst.snapTo(0f)
        burst.animateTo(1f, tween(STAR_BURST_DURATION_MS, easing = LinearOutSlowInEasing))
    }

    /**
     * A jump: the tile stretches and flies up, falls back and lands with a squash (wider and
     * shorter), rebounds a little, and settles.
     */
    suspend fun jump() = coroutineScope {
        launch {
            lift.animateTo(JUMP_HEIGHT_DP, tween(160, easing = LinearOutSlowInEasing)) // up
            lift.animateTo(0f, tween(150, easing = FastOutLinearInEasing)) // down
            lift.animateTo(JUMP_HEIGHT_DP / 3f, tween(110, easing = LinearOutSlowInEasing)) // rebound
            lift.animateTo(0f, tween(110, easing = FastOutLinearInEasing))
        }
        launch {
            scaleY.animateTo(1.06f, tween(160)) // stretched while flying up
            scaleY.animateTo(0.86f, tween(150)) // squashed on landing
            scaleY.animateTo(1.03f, tween(110))
            scaleY.animateTo(1f, tween(110))
        }
        launch {
            scaleX.animateTo(0.97f, tween(160))
            scaleX.animateTo(1.08f, tween(150))
            scaleX.animateTo(0.99f, tween(110))
            scaleX.animateTo(1f, tween(110))
        }
    }

    /** A shake: the tile swings from side to side, with a smaller swing each time. */
    suspend fun shake() {
        for (strength in SHAKE_STRENGTHS) {
            shift.animateTo(strength * SHAKE_DISTANCE_DP, tween(SHAKE_STEP_MS))
        }
        shift.animateTo(0f, tween(SHAKE_STEP_MS))
    }
}

private const val JUMP_HEIGHT_DP = 16f
private const val STAR_BURST_DURATION_MS = 1400
private const val SHAKE_DISTANCE_DP = 10f
private const val SHAKE_STEP_MS = 45
private val SHAKE_STRENGTHS = listOf(-1f, 1f, -0.8f, 0.8f, -0.5f, 0.5f, -0.25f, 0.25f)

@Composable
private fun OptionTile(
    option: LetterSoundOption,
    state: LetterSoundAssessmentUiState.Question,
    /** False until the sound has been played: the tile can't be pressed, and its letter is pale. */
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = state.selectedOption?.id == option.id
    val isCorrectOption = state.question.isCorrect(option)

    // Before answering: both tiles look the same, so nothing gives the answer away.
    // After answering: the tile that was picked is dark blue (correct) or dark orange (incorrect),
    // with white letters. The other tile does not change colour if it is the correct one (the
    // right answer is not revealed after a wrong pick), and otherwise fades to pale grey. The
    // result of the picked tile is also exposed to screen readers, so the colour is not the only
    // signal.
    val container: Color
    val letterColor: Color
    val result: Int?
    when {
        !state.isAnswered -> {
            container = InactiveTileColor
            letterColor = if (enabled) UnansweredLetterColor else InactiveLetterColor
            result = null
        }

        isCorrectOption && isSelected -> {
            container = SelectedCorrectColor
            letterColor = Color.White
            result = R.string.assessment_correct
        }

        isCorrectOption -> {
            // Not picked: stays exactly as it was before answering
            container = InactiveTileColor
            letterColor = UnansweredLetterColor
            result = null
        }

        isSelected -> {
            container = SelectedIncorrectColor
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

    // The tile jumps for joy when the correct one is picked, and shakes when a wrong one is
    val animation = remember { TileAnimation() }
    val jumps = isCorrectOption && state.isCorrect == true
    val shakes = isSelected && !isCorrectOption
    LaunchedEffect(jumps) {
        if (jumps) {
            coroutineScope {
                launch { animation.jump() }
                launch { animation.starBurst() }
            }
        }
    }
    LaunchedEffect(shakes) {
        if (shakes) animation.shake()
    }

    RaisedSurface(
        faceColor = container,
        shape = TileShape,
        onClick = onClick,
        enabled = enabled && !state.isAnswered,
        modifier = modifier
            .aspectRatio(1f)
            // Drawn behind the tile (so they never cover the letter), and outside of the jumping
            // layer, so that the stars are not squashed
            .drawBehind { drawStarBurst(animation.burst.value) }
            .graphicsLayer {
                translationX = animation.shift.value.dp.toPx()
                translationY = -animation.lift.value.dp.toPx()
                scaleX = animation.scaleX.value
                scaleY = animation.scaleY.value
                // Squash against the "ground": the bottom edge of the tile
                transformOrigin = TransformOrigin(0.5f, 1f)
            }
            .semantics { resultDescription?.let { stateDescription = it } }
    ) {
        Text(
            text = option.letters,
            color = letterColor,
            fontSize = 108.sp,
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
