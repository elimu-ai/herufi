package ai.elimu.herufi.ui

import ai.elimu.content_provider.utils.ContentProviderUtil
import ai.elimu.herufi.BuildConfig
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ai.elimu.herufi.ui.ui.theme.HerufiTheme
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class SoundToLetterAssessmentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(javaClass.simpleName, "onCreate")

        // Load data
        val letterSoundGsons = ContentProviderUtil.getAllLetterSoundGsons(applicationContext, BuildConfig.CONTENT_PROVIDER_APPLICATION_ID)
        Log.i(javaClass.simpleName, "letterSoundGsons.size: ${letterSoundGsons.size}")

        val firstLetterSoundGson = letterSoundGsons.first()
        Log.i(javaClass.simpleName, "firstLetterSoundGson: ${firstLetterSoundGson}")

        val soundsString = firstLetterSoundGson.sounds.joinToString(separator = "") { it.valueIpa }
        Log.i(javaClass.simpleName, "soundsString: ${soundsString}")

        val lettersString = firstLetterSoundGson.letters.joinToString(separator = "") { it.text }
        Log.i(javaClass.simpleName, "lettersString: ${lettersString}")

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HerufiTheme {
                Assessment(
                    sounds = soundsString,
                    letters = lettersString,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AssessmentPreview() {
    Log.i(SoundToLetterAssessmentActivity::class.simpleName, "AssessmentPreview")
    HerufiTheme {
        Assessment(
            sounds = "θ",
            letters = "th",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun Assessment(sounds: String, letters: String, modifier: Modifier = Modifier) {
    Log.i(SoundToLetterAssessmentActivity::class.simpleName, "Assessment")
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = "/${sounds}/",
            fontSize = 60.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row {
            Button(onClick = { /* TODO */ }) {
                Text(
                    text = "\"${letters}\"",
                    fontSize = 60.sp
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
            Button(onClick = { /* TODO */ }) {
                Text(
                    text = "\"${letters}\" (rand)",
                    fontSize = 60.sp
                )
            }
        }
    }
}
