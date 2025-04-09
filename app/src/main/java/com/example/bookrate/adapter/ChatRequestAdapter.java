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

import java.util.List;

public class ChatRequestAdapter extends RecyclerView.Adapter<ChatRequestAdapter.ViewHolder> {

    private final List<ChatRequest> chatRequests;
    private final OnChatRequestActionListener actionListener;
    private final ChatClickListener clickListener;

    public interface ChatClickListener {
        void onClick(ChatRequest request);
    }

    // Constructor for Accept/Reject flow
    public ChatRequestAdapter(List<ChatRequest> chatRequests, OnChatRequestActionListener actionListener) {
        this.chatRequests = chatRequests;
        this.actionListener = actionListener;
        this.clickListener = null;
    }

    // Constructor for Click-only flow
    public ChatRequestAdapter(List<ChatRequest> chatRequests, ChatClickListener clickListener) {
        this.chatRequests = chatRequests;
        this.clickListener = clickListener;
        this.actionListener = null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatRequest request = chatRequests.get(position);

        holder.requesterName.setText("From: " + request.getRequesterName());
        holder.bookTitle.setText("Book: " + request.getBookTitle());
        holder.message.setText("Message: " + request.getMessage());

        if (actionListener != null) {
            // Show Accept/Reject buttons
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.rejectButton.setVisibility(View.VISIBLE);

            holder.acceptButton.setOnClickListener(v -> actionListener.onAccept(request.getRequestId()));
            holder.rejectButton.setOnClickListener(v -> actionListener.onReject(request.getRequestId()));

        } else if (clickListener != null) {
            // Hide Accept/Reject and set click for whole item
            holder.acceptButton.setVisibility(View.GONE);
            holder.rejectButton.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(v -> clickListener.onClick(request));
        }
    }

    @Override
    public int getItemCount() {
        return chatRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView requesterName, bookTitle, message;
        Button acceptButton, rejectButton;

        public ViewHolder(View itemView) {
            super(itemView);
            requesterName = itemView.findViewById(R.id.requesterName);
            bookTitle = itemView.findViewById(R.id.requestBookTitle);
            message = itemView.findViewById(R.id.requestMessage);
            acceptButton = itemView.findViewById(R.id.acceptChatButton);
            rejectButton = itemView.findViewById(R.id.rejectChatButton);
        }
    }
}
