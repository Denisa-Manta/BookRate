package com.example.bookrate.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bookrate.R;
import com.example.bookrate.model.ChatRequest;
import com.example.bookrate.util.ChatClickListener;
import com.example.bookrate.util.OnChatRequestActionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChatRequestAdapter extends RecyclerView.Adapter<ChatRequestAdapter.RequestViewHolder> {

    private final List<ChatRequest> requestList;
    private final OnChatRequestActionListener listener;
    private final ChatClickListener clickListener;
    private final boolean showActions;

    public ChatRequestAdapter(List<ChatRequest> requestList, boolean showActions, OnChatRequestActionListener listener, ChatClickListener clickListener) {
        this.requestList = requestList;
        this.listener = listener;
        this.clickListener = clickListener;
        this.showActions = showActions;
    }

    @Override
    public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RequestViewHolder holder, int position) {
        ChatRequest request = requestList.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView requesterNameText, bookTitleText, messageText;
        Button acceptButton, rejectButton;

        public RequestViewHolder(View itemView) {
            super(itemView);
            requesterNameText = itemView.findViewById(R.id.requesterName);
            bookTitleText = itemView.findViewById(R.id.requestBookTitle);
            messageText = itemView.findViewById(R.id.requestMessage);
            acceptButton = itemView.findViewById(R.id.acceptChatButton);
            rejectButton = itemView.findViewById(R.id.rejectChatButton);
        }


        public void bind(ChatRequest request) {
            String currentUserEmail = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getEmail();

            DatabaseReference usersRef = com.google.firebase.database.FirebaseDatabase.getInstance(
                    "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("users");

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String authorEmail = null;
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        String name = userSnap.child("name").getValue(String.class);
                        String email = userSnap.child("email").getValue(String.class);

                        if (name != null && name.equals(request.getAuthorName())) {
                            authorEmail = email;
                            break;
                        }
                    }

                    String fromText;
                    if (currentUserEmail.equals(request.getRequesterName())) {
                        // User is logged in → show author
                        fromText = "From: " + request.getAuthorName();
                    } else if (authorEmail != null && currentUserEmail.equals(authorEmail)) {
                        // Author is logged in → show requester
                        fromText = "From: " + request.getRequesterName();
                    } else {
                        fromText = "From: Unknown";
                    }

                    requesterNameText.setText(fromText);
                    bookTitleText.setText("Book: " + request.getBookTitle());
                    messageText.setText("Message: " + request.getMessage());
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    requesterNameText.setText("From: Error");
                }
            });

            if (showActions) {
                acceptButton.setVisibility(View.VISIBLE);
                rejectButton.setVisibility(View.VISIBLE);

                acceptButton.setOnClickListener(v -> listener.onAccept(request.getRequestId()));
                rejectButton.setOnClickListener(v -> listener.onReject(request.getRequestId()));
            } else {
                acceptButton.setVisibility(View.GONE);
                rejectButton.setVisibility(View.GONE);

                itemView.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onClick(request);
                    }
                });
            }
        }

    }
}
