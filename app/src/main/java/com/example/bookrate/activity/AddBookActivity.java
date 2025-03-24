package com.example.bookrate.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookrate.R;
import com.example.bookrate.model.Book;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddBookActivity extends AppCompatActivity {

    private EditText titleEditText, authorEditText, genreEditText;
    private Button uploadButton;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference booksRef = database.getReference("books");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        titleEditText = findViewById(R.id.titleEditText);
        authorEditText = findViewById(R.id.authorEditText);
        genreEditText = findViewById(R.id.genreEditText);
        uploadButton = findViewById(R.id.uploadButton);

        uploadButton.setOnClickListener(v -> uploadBook());
    }

    private void uploadBook() {
        String title = titleEditText.getText().toString().trim();
        String author = authorEditText.getText().toString().trim();
        String genre = genreEditText.getText().toString().trim();

        if (title.isEmpty() || author.isEmpty() || genre.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use a dummy placeholder image or null since Storage is disabled
        Book book = new Book(title, author, genre, null);

        booksRef.push().setValue(book).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Book uploaded successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to upload book", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
