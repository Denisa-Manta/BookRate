package com.example.bookrate.activity;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookrate.R;
import com.example.bookrate.adapter.ChatMessageAdapter;
import com.example.bookrate.model.ChatMessage;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private Button sendChatMessageButton;
    private ChatMessageAdapter adapter;

    private final List<ChatMessage> messages = new ArrayList<>();

    private String senderName;
    private String requesterName;
    private String requestId;

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

        senderName = getIntent().getStringExtra("authorName");
        requesterName = getIntent().getStringExtra("requesterName");
        requestId = getIntent().getStringExtra("requestId");

        if (senderName == null || requesterName == null || requestId == null) {
            Toast.makeText(this, "Missing chat info", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adapter = new ChatMessageAdapter(messages, senderName);
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
                        for (DataSnapshot msgSnap : snapshot.getChildren()) {
                            ChatMessage msg = msgSnap.getValue(ChatMessage.class);
                            if (msg != null && !messages.contains(msg)) {
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

        String messageId = dbRef.child("messages").child(requestId).child("thread").push().getKey();
        if (messageId == null) return;

        ChatMessage message = new ChatMessage(senderName, requesterName, content, System.currentTimeMillis());

        dbRef.child("messages").child(requestId).child("thread").child(messageId)
                .setValue(message)
                .addOnSuccessListener(aVoid -> messageInput.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show()
                );
    }
}
