package com.example.bookrate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookrate.R;
import com.example.bookrate.adapter.BookAdapter;
import com.example.bookrate.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ReaderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText searchEditText;
    private Button searchButton;
    private BookAdapter bookAdapter;
    private List<Book> bookList = new ArrayList<>();
    private List<Book> fullBookList = new ArrayList<>(); // for search reset

    private final DatabaseReference booksRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("books");

    private final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final DatabaseReference userBookStatesRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("users").child(userId).child("bookStates");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        recyclerView = findViewById(R.id.bookRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);

        bookAdapter = new BookAdapter(bookList, userBookStatesRef);
        recyclerView.setAdapter(bookAdapter);

        fetchBooksAndUserStates();

        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim().toLowerCase();
            bookList.clear();

            if (query.isEmpty()) {
                bookList.addAll(fullBookList);
            } else {
                for (Book book : fullBookList) {
                    if (book.getTitle().toLowerCase().contains(query) ||
                            book.getAuthor().toLowerCase().contains(query)) {
                        bookList.add(book);
                    }
                }
            }

            bookAdapter.notifyDataSetChanged();
        });

        ImageButton openUserChatsButton = findViewById(R.id.openUserChatsButton);
        openUserChatsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ReaderActivity.this, UserAcceptedChatsActivity.class);
            startActivity(intent);
        });

        ImageButton userProfileButton = findViewById(R.id.userProfileButton);
        userProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ReaderActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

    }

    // 🔁 Refresh list when returning from detail activity
    @Override
    protected void onResume() {
        super.onResume();
        fetchBooksAndUserStates();
    }

    private void fetchBooksAndUserStates() {
        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot booksSnapshot) {
                userBookStatesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot statesSnapshot) {
                        bookList.clear();
                        fullBookList.clear();

                        for (DataSnapshot snap : booksSnapshot.getChildren()) {
                            Book book = snap.getValue(Book.class);
                            Log.d("DEBUG_BOOKS_READER", "Entry book db: " + book.getTitle());
                            if (book != null) {
                                book.setId(snap.getKey());

                                if (statesSnapshot.hasChild(book.getId())) {
                                    DataSnapshot userBookData = statesSnapshot.child(book.getId());

                                    if (userBookData.child("state").exists()) {
                                        book.setState(userBookData.child("state").getValue(String.class));
                                    }

                                    if (userBookData.child("rating").exists()) {
                                        Long rating = userBookData.child("rating").getValue(Long.class);
                                        book.setRating(rating != null ? rating.intValue() : 0);
                                    }
                                }

                                bookList.add(book);
                                fullBookList.add(book);
                            }
                        }

                        bookAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}
