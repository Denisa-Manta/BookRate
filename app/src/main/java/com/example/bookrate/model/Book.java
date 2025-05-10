package com.example.bookrate.model;

import java.io.Serializable;

public class Book implements Serializable {
    private String id;         // Firebase key for this book
    private String title;
    private String author;
    private String genre;
    private String imageUrl;
    private String authorId;
    private float averageRating;

    // These fields are user-specific and will be saved under users/<uid>/bookStates/<bookId>
    private String state;      // "Want to Read", "Currently Reading", "Read"
    private int rating;        // 0 to 5 stars

    // Default constructor for Firebase
    public Book() {}

    // Updated constructor to support authorId
    public Book(String title, String author, String genre, String imageUrl, String authorId) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.imageUrl = imageUrl;
        this.authorId = authorId;

        // Default user-specific state
        this.state = "Want to Read";
        this.rating = 0;
    }

    // 🔹 Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getState() {
        return state;
    }

    public int getRating() {
        return rating;
    }

    public String getAuthorId() {
        return authorId;
    }

    // 🔸 Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public float getAverageRating() {
        return averageRating;
    }
    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }


}
