package com.example.bookrate.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookrate.R;
import com.example.bookrate.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatRequestActivity extends AppCompatActivity {

    private TextView bookTitleText, bookAuthorText;
    private EditText messageInput;
    private Button sendButton;

    private String currentUserId;
    private DatabaseReference dbRef;
    private Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_request);

        bookTitleText = findViewById(R.id.chatRequestBookTitle);
        bookAuthorText = findViewById(R.id.chatRequestBookAuthor);
        messageInput = findViewById(R.id.chatRequestMessageInput);
        sendButton = findViewById(R.id.chatRequestSendButton);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance("https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference();

        book = (Book) getIntent().getSerializableExtra("book");
        if (book == null) {
            Toast.makeText(this, "Error loading book info", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bookTitleText.setText(book.getTitle());
        bookAuthorText.setText("Author: " + book.getAuthor());

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
                return;
            }

            String bookId = book.getId();
            String authorId = book.getAuthorId();
            if (bookId == null || authorId == null) {
                Toast.makeText(this, "Missing author or book data", Toast.LENGTH_SHORT).show();
                return;
            }

            String requestId = UUID.randomUUID().toString();

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("requestId", requestId);
            requestData.put("authorId", authorId);
            requestData.put("authorName", book.getAuthor());
            requestData.put("bookId", bookId);
            requestData.put("bookTitle", book.getTitle());
            requestData.put("message", message);
            requestData.put("requesterId", currentUserId);
            requestData.put("requesterName", FirebaseAuth.getInstance().getCurrentUser().getEmail());
            requestData.put("status", "pending");

            Map<String, Object> requestDataMessage = new HashMap<>();
            requestDataMessage.put("authorName", book.getAuthor());
            requestDataMessage.put("requesterName", FirebaseAuth.getInstance().getCurrentUser().getEmail());

            dbRef.child("chat_requests").child(requestId).setValue(requestData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Request sent to author!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to send request", Toast.LENGTH_SHORT).show();
                    });

            dbRef.child("messages").child(requestId).setValue(requestDataMessage)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Request sent to author!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to send request", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
