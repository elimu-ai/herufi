package ai.elimu.herufi.ui;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.flexbox.FlexboxLayout;

import java.util.List;
import java.util.stream.Collectors;

import ai.elimu.analytics.utils.LearningEventUtil;
import ai.elimu.content_provider.utils.ContentProviderUtil;
import ai.elimu.herufi.BaseApplication;
import ai.elimu.herufi.BuildConfig;
import ai.elimu.herufi.R;
import ai.elimu.model.v2.gson.content.LetterGson;
import ai.elimu.model.v2.gson.content.LetterSoundGson;
import ai.elimu.model.v2.gson.content.SoundGson;

public class LetterSoundListActivity extends AppCompatActivity {

    private FlexboxLayout flexboxLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(getClass().getName(), "onCreate");
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_letter_sound_list);

        flexboxLayout = findViewById(R.id.letter_sound_list_flexbox_layout);
    }

    @Override
    protected void onStart() {
        Log.i(getClass().getName(), "onStart");
        super.onStart();

        List<LetterSoundGson> letterSoundGsons = ContentProviderUtil.INSTANCE.getAllLetterSoundGsons(getApplicationContext(), BuildConfig.CONTENT_PROVIDER_APPLICATION_ID);
        Log.i(getClass().getName(), "letterSoundGsons.size(): " + letterSoundGsons.size());

        // Create a view for each letter-sound correspondence in the list
        flexboxLayout.removeAllViews();
        for (final LetterSoundGson letterSoundGson : letterSoundGsons) {
            View letterSoundView = LayoutInflater.from(this).inflate(R.layout.activity_letter_sound_list_letter_view, flexboxLayout, false);

            TextView textView = letterSoundView.findViewById(R.id.letter_sound_view_text_view);
            String sounds = letterSoundGson.getSounds().stream().map(SoundGson::getValueIpa).collect(Collectors.joining());
            String letters = letterSoundGson.getLetters().stream().map(LetterGson::getText).collect(Collectors.joining());
            textView.setText("/" + sounds + "/\n⬇\n\"" + letters + "\"");

            // Play sound when pressed
            letterSoundView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(getClass().getName(), "letterView onClick");

                    Log.i(getClass().getName(), "letterSoundGson.getId(): '" + letterSoundGson.getId() + "'");

                    BaseApplication baseApplication = (BaseApplication) getApplication();
                    TextToSpeech tts = baseApplication.getTTS();
                    tts.speak(letters, TextToSpeech.QUEUE_FLUSH, null, "letter_sound_" + letterSoundGson.getId());

                    // Report learning event to the Analytics application (https://github.com/elimu-ai/analytics)
                    LearningEventUtil.reportLetterSoundLearningEvent(letterSoundGson, getApplicationContext(), BuildConfig.ANALYTICS_APPLICATION_ID);
                }
            });

            flexboxLayout.addView(letterSoundView);
        }
    }
}
