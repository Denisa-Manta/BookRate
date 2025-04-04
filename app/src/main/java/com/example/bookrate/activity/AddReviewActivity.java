package com.example.bookrate.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookrate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class AddReviewActivity extends AppCompatActivity {

    private EditText reviewEditText;
    private Button postReviewButton;

    private String bookId;
    private String userId;
    private DatabaseReference reviewRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);

        reviewEditText = findViewById(R.id.reviewEditText);
        postReviewButton = findViewById(R.id.postReviewButton);

        bookId = getIntent().getStringExtra("bookId");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // ✅ Directly point to reviews/{bookId}/{userId}
        reviewRef = FirebaseDatabase.getInstance(
                "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("reviews").child(bookId).child(userId);

        postReviewButton.setOnClickListener(v -> {
            String reviewText = reviewEditText.getText().toString().trim();

            if (reviewText.isEmpty()) {
                Toast.makeText(this, "Review cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (reviewText.length() > 500) {
                Toast.makeText(this, "Review cannot exceed 500 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get user's name from database
            DatabaseReference userNameRef = FirebaseDatabase.getInstance(
                    "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("users").child(userId).child("name");

            userNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String userName = snapshot.getValue(String.class);
                    if (userName == null || userName.trim().isEmpty()) {
                        userName = "Anonymous";
                    }

                    Map<String, Object> reviewMap = new HashMap<>();
                    reviewMap.put("userId", userId);
                    reviewMap.put("userName", userName);
                    reviewMap.put("content", reviewText);
                    reviewMap.put("timestamp", System.currentTimeMillis());

                    // ✅ Set using userId as key to enforce uniqueness
                    reviewRef.setValue(reviewMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddReviewActivity.this, "Review posted!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddReviewActivity.this, "Failed to post review", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(AddReviewActivity.this, "Failed to fetch user name", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
