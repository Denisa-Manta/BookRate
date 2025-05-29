package com.example.bookrate.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookrate.R;
import com.example.bookrate.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.UUID;

public class AddBookActivity extends AppCompatActivity {

    private EditText titleEditText, authorEditText;
    private Spinner genreSpinner;
    private Button uploadButton, selectImageButton;
    private ImageView previewImageView;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference booksRef = database.getReference("books");
    private final FirebaseStorage storage = FirebaseStorage.getInstance("gs://bookrate-4dc23.firebasestorage.app");

    private FirebaseAuth mAuth;
    private Uri selectedImageUri;

    private static final int IMAGE_PICK_CODE = 3001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // 🔙 closes current activity and goes back

        titleEditText = findViewById(R.id.titleEditText);
        authorEditText = findViewById(R.id.authorEditText);
        uploadButton = findViewById(R.id.uploadButton);
        selectImageButton = findViewById(R.id.chooseImageButton);
        previewImageView = findViewById(R.id.bookImageView);

        genreSpinner = findViewById(R.id.genreSpinner);
        String[] genres = {
                "Drama", "Comedy", "Children", "Romance", "Adventure",
                "Fantasy", "Thriller", "Science Fiction", "Horror", "Personal Development"
        };

        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genres);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genreSpinner.setAdapter(genreAdapter);

        mAuth = FirebaseAuth.getInstance();

        selectImageButton.setOnClickListener(v -> openGallery());
        uploadButton.setOnClickListener(v -> uploadBook());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).into(previewImageView);
        }
    }

    private void uploadBook() {
        String title = titleEditText.getText().toString().trim();
        String author = authorEditText.getText().toString().trim();
        String genre = genreSpinner.getSelectedItem().toString();

        if (title.isEmpty() || author.isEmpty() || genre.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String authorId = currentUser.getUid();

        if (selectedImageUri != null) {
            String imageName = UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storage.getReference("book_images").child(imageName);

            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveBookToDatabase(title, author, genre, uri.toString(), authorId);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
        } else {
            saveBookToDatabase(title, author, genre, null, authorId);
        }
    }

    private void saveBookToDatabase(String title, String author, String genre, String imageUrl, String authorId) {
        Book book = new Book(title, author, genre, imageUrl, authorId);
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
