package ai.elimu.herufi.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

import ai.elimu.herufi.R;
import ai.elimu.herufi.util.ContentProviderHelper;
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

        List<LetterGson> letterGsons = ContentProviderHelper.getLetterGsons(getApplicationContext());
        Log.i(getClass().getName(), "letterGsons.size(): " + letterGsons.size());

        // Create a view for each letter in the list
        flexboxLayout.removeAllViews();
        for (LetterGson letterGson : letterGsons) {
            View letterView = LayoutInflater.from(this).inflate(R.layout.activity_letter_list_letter_view, flexboxLayout, false);
            TextView textView = letterView.findViewById(R.id.letter_view_text_view);
            textView.setText(letterGson.getText());
            flexboxLayout.addView(letterView);
        }
    }
}