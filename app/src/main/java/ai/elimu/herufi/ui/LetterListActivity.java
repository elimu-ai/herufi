package ai.elimu.herufi.ui;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

import ai.elimu.analytics.utils.LearningEventUtil;
import ai.elimu.content_provider.utils.ContentProviderHelper;
import ai.elimu.herufi.BaseApplication;
import ai.elimu.herufi.BuildConfig;
import ai.elimu.herufi.R;
import ai.elimu.model.enums.analytics.LearningEventType;
import ai.elimu.model.v2.gson.content.LetterGson;

public class LetterListActivity extends AppCompatActivity {

    private FlexboxLayout flexboxLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(getClass().getName(), "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_letter_list);

        flexboxLayout = findViewById(R.id.letter_list_flexbox_layout);
    }

    @Override
    protected void onStart() {
        Log.i(getClass().getName(), "onStart");
        super.onStart();

        List<LetterGson> letterGsons = ContentProviderHelper.getLetterGsons(getApplicationContext(), BuildConfig.CONTENT_PROVIDER_APPLICATION_ID);
        Log.i(getClass().getName(), "letterGsons.size(): " + letterGsons.size());

        // Create a view for each letter in the list
        flexboxLayout.removeAllViews();
        for (final LetterGson letterGson : letterGsons) {
            View letterView = LayoutInflater.from(this).inflate(R.layout.activity_letter_list_letter_view, flexboxLayout, false);

            TextView textView = letterView.findViewById(R.id.letter_view_text_view);
            textView.setText(letterGson.getText());

            // Play letter sound when pressed
            letterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(getClass().getName(), "letterView onClick");

                    Log.i(getClass().getName(), "letterGson.getText(): '" + letterGson.getText() + "'");

                    BaseApplication baseApplication = (BaseApplication) getApplication();
                    TextToSpeech tts = baseApplication.getTTS();
                    tts.speak(letterGson.getText(), TextToSpeech.QUEUE_FLUSH, null, "letter_" + letterGson.getId());

                    // Report learning event to the Analytics application (https://github.com/elimu-ai/analytics)
                    LearningEventUtil.reportLetterLearningEvent(BuildConfig.APPLICATION_ID, letterGson, LearningEventType.LETTER_PRESSED, getApplicationContext(), BuildConfig.ANALYTICS_APPLICATION_ID);
                }
            });

            flexboxLayout.addView(letterView);
        }
    }
}
