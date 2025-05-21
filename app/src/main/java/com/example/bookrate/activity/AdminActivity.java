package com.example.bookrate.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.example.bookrate.R;
import android.content.Intent;

public class AdminActivity extends AppCompatActivity {

    private Button addBookButton, viewAuthorsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        addBookButton = findViewById(R.id.addBookButton);
        addBookButton.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, AddBookActivity.class));
        });

        viewAuthorsButton = findViewById(R.id.viewAuthorsButton);
        viewAuthorsButton.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, PendingAuthorsActivity.class));
        });

        Button viewAllBooksButton = findViewById(R.id.viewAllBooksButton);
        viewAllBooksButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminBookActivity.class);
            startActivity(intent);
        });

    }
}
