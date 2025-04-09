package com.example.bookrate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookrate.R;
import com.example.bookrate.adapter.ChatRequestAdapter;
import com.example.bookrate.model.ChatRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AuthorChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatRequestAdapter adapter;
    private final List<ChatRequest> acceptedChats = new ArrayList<>();

    private final String authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("chat_requests");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_chat_list);

        recyclerView = findViewById(R.id.acceptedChatsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChatRequestAdapter(acceptedChats, chatRequest -> {
            Intent intent = new Intent(AuthorChatListActivity.this, ChatActivity.class);
            intent.putExtra("chatId", chatRequest.getRequestId());
            intent.putExtra("authorId", chatRequest.getAuthorId());
            intent.putExtra("userId", chatRequest.getRequesterId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        loadAcceptedChats();
    }

    private void loadAcceptedChats() {
        dbRef.orderByChild("authorId").equalTo(authorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
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
                        Toast.makeText(AuthorChatListActivity.this, "Failed to load accepted chats", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
