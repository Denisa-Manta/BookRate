package com.example.bookrate.model;

public class ChatMessage {

    private String senderName;
    private String receiverName;
    private String message;
    private long timestamp;
    private boolean read = false;  // ✅ Add this field

    // Required empty constructor for Firebase
    public ChatMessage() {}

    public ChatMessage(String senderName, String receiverName, String message, long timestamp) {
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.message = message;
        this.timestamp = timestamp;
        this.read = false;  // ✅ Default to false
    }

    // 🔹 Getters
    public String getSenderName() {
        return senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return read;
    }

    // 🔸 Setters
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
