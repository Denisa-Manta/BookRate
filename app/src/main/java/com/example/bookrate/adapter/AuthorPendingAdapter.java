package com.example.bookrate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bookrate.R;

import java.util.List;
import java.util.Map;

public class AuthorPendingAdapter extends RecyclerView.Adapter<AuthorPendingAdapter.ViewHolder> {

    private final Context context;
    private final List<Map<String, String>> pendingAuthors;
    private final List<String> userIds;
    private final OnAcceptCallback acceptCallback;
    private final OnRejectCallback rejectCallback;

    public interface OnAcceptCallback {
        void onAccept(String uid, int position);
    }

    public interface OnRejectCallback {
        void onReject(String uid, int position);
    }

    public AuthorPendingAdapter(Context context,
                                List<Map<String, String>> pendingAuthors,
                                List<String> userIds,
                                OnAcceptCallback acceptCallback,
                                OnRejectCallback rejectCallback) {
        this.context = context;
        this.pendingAuthors = pendingAuthors;
        this.userIds = userIds;
        this.acceptCallback = acceptCallback;
        this.rejectCallback = rejectCallback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pending_author, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Map<String, String> author = pendingAuthors.get(position);
        String uid = userIds.get(position);

        holder.nameText.setText("Name: " + author.get("name"));
        holder.emailText.setText("Email: " + author.get("email"));

        holder.acceptButton.setOnClickListener(v -> {
            if (acceptCallback != null) {
                acceptCallback.onAccept(uid, position);
            }
        });

        holder.rejectButton.setOnClickListener(v -> {
            if (rejectCallback != null) {
                rejectCallback.onReject(uid, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pendingAuthors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, emailText;
        Button acceptButton, rejectButton;

        public ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.pendingAuthorName);
            emailText = itemView.findViewById(R.id.pendingAuthorEmail);
            acceptButton = itemView.findViewById(R.id.acceptAuthorButton);
            rejectButton = itemView.findViewById(R.id.rejectAuthorButton);
        }
    }
}
