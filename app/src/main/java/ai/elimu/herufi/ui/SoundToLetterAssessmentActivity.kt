package ai.elimu.herufi.ui

import ai.elimu.content_provider.utils.ContentProviderUtil
import ai.elimu.herufi.BuildConfig
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ai.elimu.herufi.ui.ui.theme.HerufiTheme
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp

class SoundToLetterAssessmentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(javaClass.simpleName, "onCreate")

        // Load data
        val letterSoundGsons = ContentProviderUtil.getAllLetterSoundGsons(applicationContext, BuildConfig.CONTENT_PROVIDER_APPLICATION_ID)
        Log.i(javaClass.simpleName, "letterSoundGsons.size: ${letterSoundGsons.size}")

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HerufiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Log.i(SoundToLetterAssessmentActivity::class.simpleName, "Greeting")
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "Hello, ${name}!",
            fontSize = 30.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Log.i(SoundToLetterAssessmentActivity::class.simpleName, "GreetingPreview")
    HerufiTheme {
        Greeting("Android")
    }
}