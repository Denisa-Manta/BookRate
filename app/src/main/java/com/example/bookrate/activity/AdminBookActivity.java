package com.example.bookrate.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookrate.R;
import com.example.bookrate.adapter.AdminBookAdapter;
import com.example.bookrate.model.Book;
import com.google.firebase.auth.FirebaseAuth;
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

    private final DatabaseReference usersRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_book);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // 🔙 closes current activity and goes back

        adminBooksRecyclerView = findViewById(R.id.adminBooksRecyclerView);
        adminBooksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new AdminBookAdapter(this, bookList, (Book bookToDelete) -> {
            booksRef.child(bookToDelete.getId()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("admin_debug", "Book deleted from admin side");
                    Toast.makeText(AdminBookActivity.this, "Book deleted", Toast.LENGTH_SHORT).show();
                    bookList.remove(bookToDelete);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.d("admin_debug", "Book could not be deleted from admin side");
                    Toast.makeText(AdminBookActivity.this, "Failed to delete book", Toast.LENGTH_SHORT).show();
                }
            });

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        DatabaseReference userBookRef = userSnap.getRef()
                                .child("bookStates")
                                .child(bookToDelete.getId());

                        userBookRef.removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("admin_debug", "Deleted book " + bookToDelete.getId() + " from user " + userSnap.getKey());
                            } else {
                                Log.d("admin_debug", "Failed to delete book " + bookToDelete.getId() + " from user " + userSnap.getKey());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("admin_debug", "Failed to fetch users for cleanup: " + error.getMessage());
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
