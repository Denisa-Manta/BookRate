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
            requesterNameText.setText("From: " + request.getRequesterName());
            bookTitleText.setText("Book: " + request.getBookTitle());
            messageText.setText("Message: " + request.getMessage());

            if (showActions) {
                acceptButton.setVisibility(View.VISIBLE);
                rejectButton.setVisibility(View.VISIBLE);

                acceptButton.setOnClickListener(v -> listener.onAccept(request.getRequestId()));
                rejectButton.setOnClickListener(v -> listener.onReject(request.getRequestId()));
            } else {
                acceptButton.setVisibility(View.GONE);
                rejectButton.setVisibility(View.GONE);

                // ➡️ ADD THIS: open chat when item is clicked
                itemView.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onClick(request);
                    }
                });
            }
        }
    }
}
