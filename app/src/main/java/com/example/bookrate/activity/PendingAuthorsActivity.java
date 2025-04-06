package com.example.bookrate.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookrate.R;
import com.example.bookrate.adapter.AuthorPendingAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PendingAuthorsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AuthorPendingAdapter adapter;
    private final List<Map<String, String>> pendingAuthors = new ArrayList<>();
    private final List<String> userIds = new ArrayList<>();

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_authors);

        recyclerView = findViewById(R.id.pendingAuthorsLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AuthorPendingAdapter(this, pendingAuthors, userIds,
                this::onAuthorAccepted,
                this::onAuthorRejected);

        recyclerView.setAdapter(adapter);

        loadPendingAuthors();
    }

    private void loadPendingAuthors() {
        dbRef.child("pending_authors").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                pendingAuthors.clear();
                userIds.clear();

                for (DataSnapshot authorSnap : snapshot.getChildren()) {
                    String uid = authorSnap.getKey();
                    Map<String, String> data = new HashMap<>();

                    String name = authorSnap.child("name").getValue(String.class);
                    String email = authorSnap.child("email").getValue(String.class);
                    String phone = authorSnap.child("phone").getValue(String.class);
                    String role = authorSnap.child("role").getValue(String.class);

                    if (name != null && email != null && phone != null && role != null) {
                        data.put("name", name);
                        data.put("email", email);
                        data.put("phone", phone);
                        data.put("role", role);
                        pendingAuthors.add(data);
                        userIds.add(uid);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(PendingAuthorsActivity.this, "Failed to load authors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onAuthorAccepted(String uid, int position) {
        DatabaseReference pendingRef = dbRef.child("pending_authors").child(uid);
        DatabaseReference userRef = dbRef.child("users").child(uid);

        pendingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> authorData = (Map<String, Object>) snapshot.getValue();
                    if (authorData != null) {
                        authorData.put("role", "author");

                        userRef.setValue(authorData).addOnSuccessListener(aVoid -> {
                            pendingRef.removeValue();
                            pendingAuthors.remove(position);
                            userIds.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(PendingAuthorsActivity.this, "Author accepted", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(PendingAuthorsActivity.this, "Failed to accept author", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onAuthorRejected(String uid, int position) {
        dbRef.child("pending_authors").child(uid).removeValue()
                .addOnSuccessListener(aVoid -> {
                    pendingAuthors.remove(position);
                    userIds.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(PendingAuthorsActivity.this, "Author rejected", Toast.LENGTH_SHORT).show();
                });
    }
}
