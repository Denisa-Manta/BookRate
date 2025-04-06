package com.example.bookrate.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookrate.R;
import com.example.bookrate.adapter.AuthorBookAdapter;
import com.example.bookrate.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class AuthorActivity extends AppCompatActivity {

    private RecyclerView authorBooksRecyclerView;
    private AuthorBookAdapter bookAdapter;
    private ArrayList<Book> bookList;

    private final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);

        TextView welcomeText = findViewById(R.id.authorWelcomeText);
        welcomeText.setText("Welcome, Author!");

        authorBooksRecyclerView = findViewById(R.id.authorBooksRecyclerView);
        authorBooksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookList = new ArrayList<>();
        bookAdapter = new AuthorBookAdapter(bookList);
        authorBooksRecyclerView.setAdapter(bookAdapter);

        loadBooks();
    }

    private void loadBooks() {
        dbRef.child("users").child(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot nameSnapshot) {
                String authorName = nameSnapshot.getValue(String.class);

                if (authorName == null) {
                    Toast.makeText(AuthorActivity.this, "Failed to retrieve your name", Toast.LENGTH_SHORT).show();
                    return;
                }

                dbRef.child("books").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        bookList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Book book = snap.getValue(Book.class);
                            if (book != null &&
                                    authorName.equals(book.getAuthor())) {
                                book.setId(snap.getKey());
                                bookList.add(book);
                            }
                        }
                        bookAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(AuthorActivity.this, "Failed to load books", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AuthorActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
