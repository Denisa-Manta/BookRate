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

public class UserProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView userNameTextView, booksReadTextView;
    private final int PICK_IMAGE_REQUEST = 1001;
    private RecyclerView currentlyReadingRecyclerView;
    private CurrentlyReadingAdapter currentlyReadingAdapter;
    private final List<Book> currentlyReadingBooks = new ArrayList<>();

    private final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("users").child(userId);

    private final StorageReference storageRef = FirebaseStorage.getInstance()
            .getReference("profile_images").child(userId + ".jpg");

    private final DatabaseReference booksRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("books");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        profileImageView = findViewById(R.id.profileImageView);

        currentlyReadingRecyclerView = findViewById(R.id.currentlyReadingRecyclerView);
        currentlyReadingAdapter = new CurrentlyReadingAdapter(this, currentlyReadingBooks);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        currentlyReadingRecyclerView.setLayoutManager(layoutManager);
        currentlyReadingRecyclerView.setAdapter(currentlyReadingAdapter);

        userNameTextView = findViewById(R.id.userNameTextView);
        booksReadTextView = findViewById(R.id.booksReadTextView);

        loadUserProfile();

        profileImageView.setOnClickListener(v -> pickImageFromGallery());
    }

    private void loadUserProfile() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                userNameTextView.setText(name != null ? name : "Unknown User");

                if (profileImageUrl != null) {
                    Glide.with(UserProfileActivity.this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.placeholder_image)
                            .into(profileImageView);
                }

                // Extract bookStates from user
                DataSnapshot bookStates = snapshot.child("bookStates");
                int readCount = 0;
                int currentlyReadingCount = 0;
                List<String> currentlyReadingTitles = new ArrayList<>();

                for (DataSnapshot bookSnap : bookStates.getChildren()) {
                    String state = bookSnap.child("state").getValue(String.class);
                    if ("Read".equalsIgnoreCase(state)) {
                        readCount++;
                    } else if ("Currently Reading".equalsIgnoreCase(state)) {
                        currentlyReadingCount++;
                        currentlyReadingTitles.add(bookSnap.getKey());  // key is the title
                    }
                }
                booksReadTextView.setText("Books Read: " + readCount);
                // Match titles with books under /books
                        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot booksSnapshot) {
                                for (DataSnapshot snap : booksSnapshot.getChildren()) {
                                    Book book = snap.getValue(Book.class);
                                    Log.d("DEBUG_BOOKS", "Entry book db: " + book.getTitle());
                                    if (book != null) {
                                        book.setId(snap.getKey());
                                        if(currentlyReadingTitles.contains(book.getId())) {
                                            currentlyReadingBooks.add(book);
                                            Log.d("DEBUG_BOOKS", "Added book: " + book.getTitle());
                                        }
                                    }
                                }
                                currentlyReadingAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {}
                        });

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
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
                    dbRef.child("profileImageUrl").setValue(downloadUrl);
                    Glide.with(UserProfileActivity.this).load(downloadUrl).into(profileImageView);
                    Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show());
    }
}
