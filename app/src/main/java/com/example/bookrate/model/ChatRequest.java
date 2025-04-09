package com.example.bookrate.model;

import java.io.Serializable;

public class ChatRequest implements Serializable {
    private String requestId;
    private String bookId;
    private String bookTitle;
    private String requesterId;
    private String requesterName;
    private String authorId;
    private String authorName;
    private String message;
    private String status; // "pending", "accepted", "rejected"

    // Required empty constructor for Firebase
    public ChatRequest() {}

    public ChatRequest(String requestId, String bookId, String bookTitle, String requesterId,
                       String requesterName, String authorId, String authorName,
                       String message, String status) {
        this.requestId = requestId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.authorId = authorId;
        this.authorName = authorName;
        this.message = message;
        this.status = status;
    }

    // 🔹 Getters
    public String getRequestId() {
        return requestId;
    }

    public String getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    // 🔸 Setters
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
