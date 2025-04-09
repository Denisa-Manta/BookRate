package com.example.bookrate.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bookrate.R;
import com.example.bookrate.model.ChatMessage;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> messageList;
    private final String currentUserName;  // 🔄 we're using NAME, not UID

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public ChatMessageAdapter(List<ChatMessage> messageList, String currentUserName) {
        this.messageList = messageList;
        this.currentUserName = currentUserName;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        if (message.getSenderName() != null && message.getSenderName().equals(currentUserName)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedViewHolder) {
            ((ReceivedViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // 🔹 Sent message layout
    public static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        public SentViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.sentMessageText);
        }

        public void bind(ChatMessage message) {
            messageText.setText(message.getMessage());
        }
    }

    // 🔹 Received message layout
    public static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        public ReceivedViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.receivedMessageText);
        }

        public void bind(ChatMessage message) {
            messageText.setText(message.getMessage());
        }
    }
}
