package com.example.bookrate.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

        Button addBookButton = findViewById(R.id.addBookButton);
        addBookButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AddBookActivity.class);
            startActivity(intent);
        });


    }
}
