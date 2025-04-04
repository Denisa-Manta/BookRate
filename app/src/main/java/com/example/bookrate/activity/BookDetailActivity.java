package com.example.bookrate.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookrate.R;
import com.example.bookrate.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class BookDetailActivity extends AppCompatActivity {

    private ImageView bookImage;
    private TextView titleText, authorText, genreText;
    private Spinner stateSpinner;
    private RatingBar ratingBar;

    private DatabaseReference userBookStatesRef;
    private Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // Get book from intent
        book = (Book) getIntent().getSerializableExtra("book");

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userBookStatesRef = FirebaseDatabase.getInstance(
                "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users").child(userId).child("bookStates");

        initViews();
        displayBookInfo();
        setupListeners();
    }

    private void initViews() {
        bookImage = findViewById(R.id.detailBookImage);
        titleText = findViewById(R.id.detailBookTitle);
        authorText = findViewById(R.id.detailBookAuthor);
        genreText = findViewById(R.id.detailBookGenre);
        stateSpinner = findViewById(R.id.detailBookStateSpinner);
        ratingBar = findViewById(R.id.detailBookRatingBar);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.book_states,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(adapter);
    }

    private void displayBookInfo() {
        titleText.setText(book.getTitle());
        authorText.setText(book.getAuthor());
        genreText.setText(book.getGenre());

        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(book.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(bookImage);
        } else {
            bookImage.setImageResource(R.drawable.placeholder_image);
        }

        stateSpinner.setSelection(((ArrayAdapter) stateSpinner.getAdapter()).getPosition(book.getState()));
        ratingBar.setRating(book.getRating());
        ratingBar.setVisibility("Read".equals(book.getState()) ? View.VISIBLE : View.GONE);
    }

    private void setupListeners() {
        stateSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            boolean first = true;
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (first) {
                    first = false;
                    return;
                }

                String newState = parent.getItemAtPosition(position).toString();
                book.setState(newState);

                if (!"Read".equals(newState)) {
                    book.setRating(0);
                    ratingBar.setRating(0);
                    ratingBar.setVisibility(View.GONE);
                } else {
                    ratingBar.setVisibility(View.VISIBLE);
                }

                saveStateAndRating();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser && "Read".equals(book.getState())) {
                book.setRating((int) rating);
                saveStateAndRating();
            }
        });
    }

    private void saveStateAndRating() {
        if (book.getId() == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("state", book.getState());
        updates.put("rating", book.getRating());

        userBookStatesRef.child(book.getId()).updateChildren(updates);
    }
}
