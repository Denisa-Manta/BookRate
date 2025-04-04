package com.example.bookrate.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookrate.R;
import com.example.bookrate.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class BookDetailActivity extends AppCompatActivity {

    private TextView detailBookTitle, detailBookAuthor, detailBookGenre, averageRatingText;
    private ImageView detailBookImage;
    private Spinner detailBookStateSpinner;
    private RatingBar detailUserRatingBar, detailAverageRatingBar;

    private final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

    private Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        detailBookTitle = findViewById(R.id.detailBookTitle);
        detailBookAuthor = findViewById(R.id.detailBookAuthor);
        detailBookGenre = findViewById(R.id.detailBookGenre);
        detailBookImage = findViewById(R.id.detailBookImage);
        detailBookStateSpinner = findViewById(R.id.detailBookStateSpinner);
        detailUserRatingBar = findViewById(R.id.detailUserRatingBar);
        detailAverageRatingBar = findViewById(R.id.detailAverageRatingBar);
        averageRatingText = findViewById(R.id.averageRatingText);

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

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.book_states, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        detailBookStateSpinner.setAdapter(adapter);
        detailBookStateSpinner.setSelection(getIndexForState(book.getState()));

        detailUserRatingBar.setRating(book.getRating());
        detailUserRatingBar.setIsIndicator(false);
        detailUserRatingBar.setStepSize(1);
        detailUserRatingBar.setVisibility("Read".equals(book.getState()) ? android.view.View.VISIBLE : android.view.View.GONE);

        detailUserRatingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser && "Read".equals(book.getState())) {
                book.setRating((int) rating);
                saveUserStateAndRating();
            }
        });

        detailBookStateSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            boolean firstLoad = true;

            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int pos, long id) {
                if (firstLoad) {
                    firstLoad = false;
                    return;
                }

                String newState = parent.getItemAtPosition(pos).toString();
                book.setState(newState);

                if (!"Read".equals(newState)) {
                    book.setRating(0);
                    detailUserRatingBar.setRating(0);
                    detailUserRatingBar.setVisibility(android.view.View.GONE);
                } else {
                    detailUserRatingBar.setVisibility(android.view.View.VISIBLE);
                }

                saveUserStateAndRating();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        loadAverageRating();
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
}
