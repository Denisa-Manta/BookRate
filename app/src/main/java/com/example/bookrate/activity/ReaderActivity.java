// ✅ ReaderActivity.java with user-specific state & rating logic
package com.example.bookrate.activity;

import android.os.Bundle;
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
    private BookAdapter bookAdapter;
    private List<Book> bookList = new ArrayList<>();

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
        bookAdapter = new BookAdapter(bookList, userBookStatesRef);
        recyclerView.setAdapter(bookAdapter);

        fetchBooksFromFirebase();
    }

    private void fetchBooksFromFirebase() {
        booksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                bookList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Book book = snap.getValue(Book.class);
                    if (book != null) {
                        book.setId(snap.getKey());
                        bookList.add(book);
                    }
                }
                bookAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });
    }
}
