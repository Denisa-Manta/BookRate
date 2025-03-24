package com.example.bookrate.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.bookrate.R;

public class AdminActivity extends AppCompatActivity {

    private TextView adminWelcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        adminWelcomeText = findViewById(R.id.adminWelcomeText);

        // You can update the text dynamically if needed (e.g., show admin's name)
        adminWelcomeText.setText("Welcome, Admin!");
    }
}
