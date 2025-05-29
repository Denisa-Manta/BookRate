package com.example.bookrate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookrate.R;
import com.example.bookrate.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class BookDetailActivity extends AppCompatActivity {

    private TextView detailBookTitle, detailBookAuthor, detailBookGenre, averageRatingText, reviewSectionTitle;
    private ImageView detailBookImage;
    private Spinner detailBookStateSpinner;
    private RatingBar detailUserRatingBar, detailAverageRatingBar;
    private Button writeReviewButton, startChatButton;
    private LinearLayout reviewsContainer;

    private final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

    private Book book;

    @Override
    protected void onResume() {
        super.onResume();
        loadReviews();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // 🔙 closes current activity and goes back

        detailBookTitle = findViewById(R.id.detailBookTitle);
        detailBookAuthor = findViewById(R.id.detailBookAuthor);
        detailBookGenre = findViewById(R.id.detailBookGenre);
        detailBookImage = findViewById(R.id.detailBookImage);
        detailBookStateSpinner = findViewById(R.id.detailBookStateSpinner);
        detailUserRatingBar = findViewById(R.id.detailUserRatingBar);
        detailAverageRatingBar = findViewById(R.id.detailAverageRatingBar);
        averageRatingText = findViewById(R.id.averageRatingText);
        writeReviewButton = findViewById(R.id.writeReviewButton);
        startChatButton = findViewById(R.id.startChatButton);
        reviewSectionTitle = findViewById(R.id.reviewSectionTitle);
        reviewsContainer = findViewById(R.id.reviewsContainer);

        book = (Book) getIntent().getSerializableExtra("book");
        if (book == null) return;

        detailBookTitle.setText(book.getTitle());
        detailBookAuthor.setText(book.getAuthor());
        detailBookGenre.setText(book.getGenre());

        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            Glide.with(this).load(book.getImageUrl()).into(detailBookImage);
        } else {
            detailBookImage.setImageResource(R.drawable.placeholder_image);
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.book_states, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        detailBookStateSpinner.setAdapter(adapter);
        detailBookStateSpinner.setSelection(getIndexForState(book.getState()));

        updateReviewVisibility(book.getState());

        detailUserRatingBar.setRating(book.getRating());
        detailUserRatingBar.setIsIndicator(false);
        detailUserRatingBar.setStepSize(1);
        detailUserRatingBar.setVisibility("Read".equals(book.getState()) ? View.VISIBLE : View.GONE);

        // Show chat button only if book is in "Read" state
        if ("Read".equals(book.getState())) {
            startChatButton.setVisibility(View.VISIBLE);
            startChatButton.setOnClickListener(v -> {
                Intent intent = new Intent(BookDetailActivity.this, ChatRequestActivity.class);
                intent.putExtra("book", book); // ✅ send the book!
                intent.putExtra("authorName", book.getAuthor());
                intent.putExtra("bookId", book.getId());
                intent.putExtra("bookTitle", book.getTitle());
                startActivity(intent);
            });
        } else {
            startChatButton.setVisibility(View.GONE);
        }

        detailUserRatingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser && "Read".equals(book.getState())) {
                book.setRating((int) rating);
                saveUserStateAndRating();
            }
        });

        detailBookStateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstLoad = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (firstLoad) {
                    firstLoad = false;
                    return;
                }

                String newState = parent.getItemAtPosition(pos).toString();
                book.setState(newState);

                if (!"Read".equals(newState)) {
                    book.setRating(0);
                    detailUserRatingBar.setRating(0);
                }

                detailUserRatingBar.setVisibility("Read".equals(newState) ? View.VISIBLE : View.GONE);
                startChatButton.setVisibility("Read".equals(newState) ? View.VISIBLE : View.GONE);
                updateReviewVisibility(newState);
                saveUserStateAndRating();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        writeReviewButton.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, AddReviewActivity.class);
            intent.putExtra("bookId", book.getId());
            startActivity(intent);
        });

        loadAverageRating();
    }

    private void updateReviewVisibility(String state) {
        if ("Read".equals(state)) {
            writeReviewButton.setVisibility(View.VISIBLE);
            reviewSectionTitle.setVisibility(View.VISIBLE);
            reviewsContainer.setVisibility(View.VISIBLE);
        } else {
            writeReviewButton.setVisibility(View.GONE);
            reviewSectionTitle.setVisibility(View.GONE);
            reviewsContainer.setVisibility(View.GONE);
        }
    }

    private int getIndexForState(String state) {
        String[] states = getResources().getStringArray(R.array.book_states);
        for (int i = 0; i < states.length; i++) {
            if (states[i].equals(state)) return i;
        }
        return 0;
    }

    private void saveUserStateAndRating() {
        if (book.getId() == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("state", book.getState());
        data.put("rating", book.getRating());

        dbRef.child("users").child(userId).child("bookStates").child(book.getId()).updateChildren(data);
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
                detailAverageRatingBar.setRating(average);
                averageRatingText.setText(String.format("Average: %.2f", average));
            }

            @Override
            public void onCancelled(DatabaseError error) {}
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

                            if (userId == null || content == null || content.trim().isEmpty()) {
                                continue;
                            }

                            dbRef.child("users").child(userId).child("name")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot nameSnap) {
                                            String userName = nameSnap.getValue(String.class);
                                            if (userName == null || userName.trim().isEmpty()) {
                                                userName = "Anonymous";
                                            }

                                            TextView reviewText = new TextView(BookDetailActivity.this);
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