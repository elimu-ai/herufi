package ai.elimu.herufi.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import ai.elimu.herufi.R;
import ai.elimu.herufi.util.ContentProviderHelper;
import ai.elimu.model.v2.gson.content.LetterGson;

public class LetterListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(getClass().getName(), "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_letter_list);
    }

    @Override
    protected void onStart() {
        Log.i(getClass().getName(), "onStart");
        super.onStart();

        List<LetterGson> letterGsons = ContentProviderHelper.getLetterGsons(getApplicationContext());
        Log.i(getClass().getName(), "letterGsons.size(): " + letterGsons.size());

        //
    }
}
