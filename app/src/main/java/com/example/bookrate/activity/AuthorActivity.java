package com.example.bookrate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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
    private TextView chatBadgeTextView;

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

        ImageButton openChatRequestsButton = findViewById(R.id.openChatRequestsButton);
        openChatRequestsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AuthorActivity.this, AuthorChatRequestsActivity.class);
            startActivity(intent);
        });

        ImageButton openActiveChatsButton = findViewById(R.id.openActiveChatsButton);
        openActiveChatsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AuthorActivity.this, AuthorAcceptedChatsActivity.class);
            startActivity(intent);
        });

        ImageButton authorProfileButton = findViewById(R.id.authorProfileButton);
        authorProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(AuthorActivity.this, AuthorProfileActivity.class);
            startActivity(intent);
        });

        chatBadgeTextView = findViewById(R.id.chatBadgeTextView);
        listenForUnreadMessages();

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

    private void listenForUnreadMessages() {
        DatabaseReference messagesRef = dbRef.child("messages");

        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getEmail()
                        : null;

                if (currentUserEmail == null) {
                    chatBadgeTextView.setVisibility(View.GONE);
                    return;
                }

                int unreadCount = 0;

                for (DataSnapshot threadSnap : snapshot.getChildren()) {
                    DataSnapshot thread = threadSnap.child("thread");
                    for (DataSnapshot msgSnap : thread.getChildren()) {
                        String receiver = msgSnap.child("receiverName").getValue(String.class);
                        Boolean read = msgSnap.child("read").getValue(Boolean.class);

                        if (receiver != null && receiver.equals(currentUserEmail)
                                && (read == null || !read)) {
                            unreadCount++;
                            Log.d("badge_count_increment_author: ", String.valueOf(unreadCount));
                        }
                    }
                }

                if (unreadCount > 0) {
                    chatBadgeTextView.setText(String.valueOf(unreadCount));
                    chatBadgeTextView.setVisibility(View.VISIBLE);
                    Log.d("badge_count_view_author: ", String.valueOf(unreadCount));
                } else {
                    chatBadgeTextView.setVisibility(View.GONE);
                    Log.d("badge_count_gone_author: ", String.valueOf(unreadCount));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                chatBadgeTextView.setVisibility(View.GONE);
            }
        });
    }



}
