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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class UserAcceptedChatsActivity extends AppCompatActivity {

    private RecyclerView userAcceptedChatsRecyclerView;
    private ChatRequestAdapter adapter;
    private final List<ChatRequest> userChats = new ArrayList<>();

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("chat_requests");

    private final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_accepted_chats);

        userAcceptedChatsRecyclerView = findViewById(R.id.userAcceptedChatsRecyclerView);
        userAcceptedChatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChatRequestAdapter(userChats, false, new OnChatRequestActionListener() {
            @Override
            public void onAccept(String requestId) {
                for (ChatRequest req : userChats) {
                    if (req.getRequestId().equals(requestId)) {
                        openChat(req);
                        break;
                    }
                }
            }

            @Override
            public void onReject(String requestId) {
                // Not needed, just displaying active chats
            }
        }, chatRequest -> {
            openChat(chatRequest);
        });


        userAcceptedChatsRecyclerView.setAdapter(adapter);

        loadUserAcceptedChats();
    }

    private void loadUserAcceptedChats() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userChats.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    ChatRequest request = snap.getValue(ChatRequest.class);
                    if (request != null &&
                            "accepted".equals(request.getStatus()) &&
                            currentUserId.equals(request.getRequesterId())) {
                        userChats.add(request);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UserAcceptedChatsActivity.this, "Failed to load your active chats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChat(ChatRequest chatRequest) {
        Intent intent = new Intent(UserAcceptedChatsActivity.this, ChatActivity.class);
        intent.putExtra("authorName", chatRequest.getAuthorName());
        intent.putExtra("requesterName", chatRequest.getRequesterName());
        intent.putExtra("requestId", chatRequest.getRequestId());
        startActivity(intent);
    }
}
