package com.example.bookrate.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookrate.R;
import com.example.bookrate.adapter.AdminBookAdapter;
import com.example.bookrate.model.Book;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AdminBookActivity extends AppCompatActivity {

    private RecyclerView adminBooksRecyclerView;
    private AdminBookAdapter adapter;
    private final List<Book> bookList = new ArrayList<>();

    private final DatabaseReference booksRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("books");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_book);

        adminBooksRecyclerView = findViewById(R.id.adminBooksRecyclerView);
        adminBooksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminBookAdapter(this, bookList, (Book bookToDelete) -> {
            booksRef.child(bookToDelete.getId()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AdminBookActivity.this, "Book deleted", Toast.LENGTH_SHORT).show();
                    bookList.remove(bookToDelete);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AdminBookActivity.this, "Failed to delete book", Toast.LENGTH_SHORT).show();
                }
            });
        });


        adminBooksRecyclerView.setAdapter(adapter);
        fetchBooks();
    }

    private void fetchBooks() {
        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdminBookActivity.this, "Error loading books", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
