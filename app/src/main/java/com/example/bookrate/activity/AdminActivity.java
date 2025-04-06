package com.example.bookrate.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.example.bookrate.R;

public class AdminActivity extends AppCompatActivity {

    private TextView adminWelcomeText;
    private Button addBookButton, viewAuthorsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        adminWelcomeText = findViewById(R.id.adminWelcomeText);
        adminWelcomeText.setText("Welcome, Admin!");

        addBookButton = findViewById(R.id.addBookButton);
        addBookButton.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, AddBookActivity.class));
        });

        viewAuthorsButton = findViewById(R.id.viewAuthorsButton);
        viewAuthorsButton.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, PendingAuthorsActivity.class));
        });
    }
}
