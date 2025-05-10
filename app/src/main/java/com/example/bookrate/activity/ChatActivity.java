package com.example.bookrate.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookrate.R;
import com.example.bookrate.adapter.ChatMessageAdapter;
import com.example.bookrate.model.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private Button sendChatMessageButton;
    private ChatMessageAdapter adapter;
    private ChatMessage initialMessage;

    private final List<ChatMessage> messages = new ArrayList<>();

    private String requesterName;
    private String requestId;
    private String authorName;

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/"
    ).getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.chatMessageInput);
        sendChatMessageButton = findViewById(R.id.sendChatMessageButton);

        authorName = getIntent().getStringExtra("authorName");
        requesterName = getIntent().getStringExtra("requesterName");
        requestId = getIntent().getStringExtra("requestId");

        if (authorName == null || requesterName == null || requestId == null) {
            Toast.makeText(this, "Missing chat info", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String currentUserEmail = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getEmail();
        adapter = new ChatMessageAdapter(messages, currentUserEmail);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(adapter);

        sendChatMessageButton.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void loadMessages() {
        // ✅ Step 1: Load the initial message (first message written by user)
        dbRef.child("messages").child(requestId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        messages.clear();

                        String author = snapshot.child("authorName").getValue(String.class);
                        String user = snapshot.child("requesterName").getValue(String.class);
                        String text = snapshot.child("message").getValue(String.class);

                        if (author != null && user != null && text != null) {
                            ChatMessage first = new ChatMessage(
                                    user, author, text, System.currentTimeMillis()
                            );

                            Log.d("message_debug_load:", author);
                            messages.add(first);
                            adapter.notifyDataSetChanged();
                        }

                        // ✅ Step 2: Start listening for conversation under /thread
                        listenToThread();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ChatActivity.this, "Failed to load initial message", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void listenToThread() {
        dbRef.child("messages").child(requestId).child("thread")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        messages.subList(1, messages.size()).clear();
                        for (DataSnapshot msgSnap : snapshot.getChildren()) {
                            ChatMessage msg = msgSnap.getValue(ChatMessage.class);
                            if (msg != null) {
                                messages.add(msg);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        chatRecyclerView.scrollToPosition(messages.size() - 1);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ChatActivity.this, "Failed to load chat thread", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) return;

        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // If current user is the requester, we must find author's email
        if (currentUserEmail.equals(requesterName)) {
            // 🔍 Lookup author's email
            Log.d("message_log:", authorName);
            DatabaseReference usersRef = FirebaseDatabase.getInstance("https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String authorEmail = null;

                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        String name = userSnap.child("name").getValue(String.class);
                        String email = userSnap.child("email").getValue(String.class);
                        Log.d("message_log:", name);
                        Log.d("message_log:", email);
                        Log.d("message_log:", "next_user");

                        if (name != null && name.equals(authorName)) {
                            authorEmail = email;
                            Log.d("message_log:", "authorEmail");
                            break;
                        }
                    }

                    if (authorEmail != null) {
                        sendChatMessageTo(content, authorEmail);
                    } else {
                        Toast.makeText(ChatActivity.this, "Could not find email for " + authorName, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(ChatActivity.this, "Failed to fetch author email", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            sendChatMessageTo(content, requesterName);
            Log.d("message_log:", "else if");
        }
    }

    private void sendChatMessageTo(String content, String receiverEmail) {
        String messageId = dbRef.child("messages").child(requestId).child("thread").push().getKey();
        if (messageId == null) return;

        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        ChatMessage message = new ChatMessage(
                currentUserEmail,
                receiverEmail,
                content,
                System.currentTimeMillis()
        );

        Log.d("message_debug_send", "Receiver email: " + receiverEmail);

        dbRef.child("messages").child(requestId).child("thread").child(messageId)
                .setValue(message)
                .addOnSuccessListener(aVoid -> messageInput.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show()
                );
    }


}
