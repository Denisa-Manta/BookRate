package com.example.bookrate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookrate.R;
import com.example.bookrate.adapter.ChatRequestAdapter;
import com.example.bookrate.model.ChatMessage;
import com.example.bookrate.model.ChatRequest;
import com.example.bookrate.util.OnChatRequestActionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthorChatRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatRequestAdapter adapter;
    private final List<ChatRequest> requests = new ArrayList<>();

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("chat_requests");

    String currentAuthorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_chat_requests);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // 🔙 closes current activity and goes back

        recyclerView = findViewById(R.id.chatRequestsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChatRequestAdapter(requests, true, new OnChatRequestActionListener() {
            @Override
            public void onAccept(String requestId) {
                for (ChatRequest req : requests) {
                    if (req.getRequestId().equals(requestId)) {
                        handleAccept(req);
                        break;
                    }
                }
            }

            @Override
            public void onReject(String requestId) {
                handleReject(requestId);
            }
        }, null);

        recyclerView.setAdapter(adapter);
        loadChatRequests();
    }

    private void loadChatRequests() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                requests.clear();
                for (DataSnapshot requestSnap : snapshot.getChildren()) {
                    ChatRequest request = requestSnap.getValue(ChatRequest.class);
                    if (request != null &&
                            "pending".equals(request.getStatus()) &&
                            currentAuthorId.equals(request.getAuthorId())) {
                        requests.add(request);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AuthorChatRequestsActivity.this, "Failed to load requests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleAccept(ChatRequest request) {
        String requestId = request.getRequestId();
        String authorName = request.getAuthorName();
        String requesterName = request.getRequesterName();

        if (requestId == null || authorName == null || requesterName == null) {
            Toast.makeText(this, "Missing request data", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Mark the chat request as accepted
        dbRef.child(requestId).child("status").setValue("accepted")
                .addOnSuccessListener(aVoid -> {
                    removeRequestById(requestId); // ✅ Remove from pending list in UI
                    Toast.makeText(this, "Request accepted", Toast.LENGTH_SHORT).show();

                    // ✅ Launch chat activity
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("authorName", authorName);
                    intent.putExtra("requesterName", requesterName);
                    intent.putExtra("requestId", requestId);  // just in case you want to track it later
                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to accept request", Toast.LENGTH_SHORT).show()
                );
    }


    private void handleReject(String requestId) {
        dbRef.child(requestId).child("status").setValue("rejected")
                .addOnSuccessListener(aVoid -> {
                    removeRequestById(requestId);
                    Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeRequestById(String requestId) {
        for (int i = 0; i < requests.size(); i++) {
            if (requestId.equals(requests.get(i).getRequestId())) {
                requests.remove(i);
                adapter.notifyItemRemoved(i);
                break;
            }
        }
    }
}
