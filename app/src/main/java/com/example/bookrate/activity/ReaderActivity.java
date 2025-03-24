package com.example.bookrate.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.example.bookrate.R;

public class ReaderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        welcomeTextView.setText("Hello!");
    }
}
