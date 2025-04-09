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

        senderName = getIntent().getStringExtra("authorName");  // author
        requesterName = getIntent().getStringExtra("requesterName");  // user

        if (senderName == null || requesterName == null) {
            Toast.makeText(this, "Missing names for chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adapter = new ChatMessageAdapter(messages, senderName);  // sender is this device's name
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(adapter);

        sendChatMessageButton.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void loadMessages() {
        dbRef.child("messages").child(senderName).child(requesterName)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        messages.clear();
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
                        Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) return;

        String messageId = dbRef.child("messages").child(senderName).child(requesterName).push().getKey();
        if (messageId == null) return;

        ChatMessage message = new ChatMessage(senderName, requesterName, content, System.currentTimeMillis());

        dbRef.child("messages").child(senderName).child(requesterName).child(messageId)
                .setValue(message)
                .addOnSuccessListener(aVoid -> messageInput.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show()
                );
    }
}
