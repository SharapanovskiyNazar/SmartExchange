package com.futurecoders.smartexchange.ui;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.futurecoders.smartexchange.R;

public class DeveloperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);

        setTitle("About");
    }
}
