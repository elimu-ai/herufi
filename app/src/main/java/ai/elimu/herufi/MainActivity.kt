package ai.elimu.herufi

import ai.elimu.herufi.ui.LetterSoundListActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(javaClass.name, "onCreate")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Verify that the content-provider APK has been installed
        // TODO
    }

    override fun onStart() {
        Log.i(javaClass.name, "onStart")
        super.onStart()

        val intent = Intent(applicationContext, LetterSoundListActivity::class.java)
        startActivity(intent)
        finish()
    }
}
