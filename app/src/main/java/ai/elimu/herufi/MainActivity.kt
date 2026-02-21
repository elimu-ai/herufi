package ai.elimu.herufi

import ai.elimu.herufi.databinding.ActivityMainBinding
import ai.elimu.herufi.ui.LetterSoundListActivity
import ai.elimu.herufi.ui.SoundToLetterAssessmentActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(javaClass.name, "onCreate")
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verify that the content-provider APK has been installed
        // TODO
    }

    override fun onStart() {
        Log.i(javaClass.name, "onStart")
        super.onStart()

//        val intent = Intent(applicationContext, LetterSoundListActivity::class.java)
        val intent = Intent(applicationContext, SoundToLetterAssessmentActivity::class.java)
        startActivity(intent)
        finish()
    }
}
