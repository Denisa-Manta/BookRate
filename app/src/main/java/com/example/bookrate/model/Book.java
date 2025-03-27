package com.example.bookrate.model;

public class Book {
    private String id;         // 🔹 Unique Firebase key
    private String title;
    private String author;
    private String genre;
    private String imageUrl;

    private String state;      // "Want to Read", "Currently Reading", "Read"
    private int rating;        // 0 to 5

    // Required empty constructor for Firebase
    public Book() {
    }

    // Full constructor (imageUrl can be null if storage is disabled)
    public Book(String title, String author, String genre, String imageUrl) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.imageUrl = imageUrl;
        this.state = "Want to Read"; // Default
        this.rating = 0;             // Default
    }

    // 🔹 ID Accessors
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // 🔸 Standard Getters
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

    // 🔸 Standard Setters
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
}
