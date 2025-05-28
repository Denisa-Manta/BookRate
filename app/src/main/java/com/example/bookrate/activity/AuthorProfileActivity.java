package com.example.bookrate.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookrate.R;
import com.example.bookrate.adapter.CurrentlyReadingAdapter;
import com.example.bookrate.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AuthorProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private Button logoutButton1, createReaderAccount;
    private TextView authorNameTextView;
    private CurrentlyReadingAdapter booksAdapter;
    private final List<Book> booksWritten = new ArrayList<>();

    private final int PICK_IMAGE_REQUEST = 2001;

    private final String authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final DatabaseReference usersRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("users").child(authorId);
    private final DatabaseReference booksRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("books");
    private final StorageReference storageRef = FirebaseStorage.getInstance("gs://bookrate-4dc23.firebasestorage.app")
            .getReference("profile_images")
            .child(authorId + ".jpg");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_profile);

        logoutButton1 = findViewById(R.id.logoutButton1);
        createReaderAccount = findViewById(R.id.createReaderAccount);
        profileImageView = findViewById(R.id.authorProfileImageView);
        authorNameTextView = findViewById(R.id.authorNameTextView);

        booksAdapter = new CurrentlyReadingAdapter(this, booksWritten);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);


        loadAuthorProfile();
        profileImageView.setOnClickListener(v -> pickImageFromGallery());

        logoutButton1.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AuthorProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        createReaderAccount.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AuthorProfileActivity.this, RegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void loadAuthorProfile() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                Log.d("AUTHOR_DEBUG", "Fetched name from DB: " + name);
                authorNameTextView.setText(name != null ? name : "Unknown Author");

                if (profileImageUrl != null) {
                    Glide.with(AuthorProfileActivity.this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.placeholder_image)
                            .into(profileImageView);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AuthorProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadProfileImage(imageUri);
            }
        }
    }

    private void uploadProfileImage(Uri imageUri) {
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    usersRef.child("profileImageUrl").setValue(downloadUrl);
                    Glide.with(AuthorProfileActivity.this).load(downloadUrl).into(profileImageView);
                    Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show());
    }
}
