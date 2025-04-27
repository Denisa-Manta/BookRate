package com.example.bookrate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookrate.R;
import com.example.bookrate.adapter.ChatRequestAdapter;
import com.example.bookrate.model.ChatRequest;
import com.example.bookrate.util.OnChatRequestActionListener;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AuthorAcceptedChatsActivity extends AppCompatActivity {

    private RecyclerView acceptedChatsRecyclerView;
    private ChatRequestAdapter adapter;
    private final List<ChatRequest> acceptedChats = new ArrayList<>();

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("chat_requests");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_accepted_chats);

        acceptedChatsRecyclerView = findViewById(R.id.acceptedChatsRecyclerView);
        acceptedChatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChatRequestAdapter(acceptedChats, false, null, chatRequest -> {
            openChat(chatRequest);
        });

        acceptedChatsRecyclerView.setAdapter(adapter);

        loadAcceptedChats();
    }

    private void loadAcceptedChats() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                acceptedChats.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    ChatRequest request = snap.getValue(ChatRequest.class);
                    if (request != null && "accepted".equals(request.getStatus())) {
                        acceptedChats.add(request);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AuthorAcceptedChatsActivity.this, "Failed to load accepted chats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChat(ChatRequest chatRequest) {
        Intent intent = new Intent(AuthorAcceptedChatsActivity.this, ChatActivity.class);
        intent.putExtra("authorName", chatRequest.getAuthorName());
        intent.putExtra("requesterName", chatRequest.getRequesterName());
        intent.putExtra("requestId", chatRequest.getRequestId());
        startActivity(intent);
    }
}
