package com.example.bookrate.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookrate.R;
import com.example.bookrate.model.Book;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class BookDetailAuthorActivity extends AppCompatActivity {

    private TextView titleText, authorText, genreText, averageRatingText, reviewSectionTitle;
    private ImageView bookImage;
    private LinearLayout reviewsContainer;
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference();

    private Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail_author);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // 🔙 closes current activity and goes back

        titleText = findViewById(R.id.detailBookTitle);
        authorText = findViewById(R.id.detailBookAuthor);
        genreText = findViewById(R.id.detailBookGenre);
        bookImage = findViewById(R.id.detailBookImage);
        averageRatingText = findViewById(R.id.averageRatingText);
        reviewSectionTitle = findViewById(R.id.reviewSectionTitle);
        reviewsContainer = findViewById(R.id.reviewsContainer);

        book = (Book) getIntent().getSerializableExtra("book");
        if (book == null) return;

        titleText.setText(book.getTitle());
        authorText.setText(book.getAuthor());
        genreText.setText(book.getGenre());

        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            Glide.with(this).load(book.getImageUrl()).into(bookImage);
        } else {
            bookImage.setImageResource(R.drawable.placeholder_image);
        }

        loadAverageRating();
        loadReviews();
    }

    private void loadAverageRating() {
        dbRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                float total = 0;
                int count = 0;

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    DataSnapshot bookState = userSnap.child("bookStates").child(book.getId());
                    if (bookState.exists() && bookState.child("rating").exists()) {
                        Long rating = bookState.child("rating").getValue(Long.class);
                        if (rating != null && rating > 0) {
                            total += rating;
                            count++;
                        }
                    }
                }

                float average = count > 0 ? total / count : 0;
                averageRatingText.setText(String.format("Average: %.2f", average));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(BookDetailAuthorActivity.this, "Failed to load average rating", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadReviews() {
        reviewsContainer.removeAllViews();

        dbRef.child("reviews").child(book.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot reviewSnap : snapshot.getChildren()) {
                            String userId = reviewSnap.getKey();
                            String content = reviewSnap.child("content").getValue(String.class);

                            if (userId == null || content == null || content.trim().isEmpty()) continue;

                            dbRef.child("users").child(userId).child("name")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot nameSnap) {
                                            String userName = nameSnap.getValue(String.class);
                                            if (userName == null || userName.trim().isEmpty()) {
                                                userName = "Anonymous";
                                            }

                                            TextView reviewText = new TextView(BookDetailAuthorActivity.this);
                                            reviewText.setText(userName + ": " + content);
                                            reviewText.setPadding(8, 8, 8, 8);
                                            reviewText.setTextSize(14f);
                                            reviewsContainer.addView(reviewText);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError error) {}
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }
}
