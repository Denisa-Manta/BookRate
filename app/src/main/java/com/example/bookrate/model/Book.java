// ✅ Book.java (Model class)
package com.example.bookrate.model;

public class Book {
    private String title;
    private String author;
    private String genre;
    private String imageUrl; // URL to image stored in Firebase Storage

    public Book() {
        // Default constructor required for calls to DataSnapshot.getValue(Book.class)
    }

    public Book(String title, String author, String genre, String imageUrl) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.imageUrl = imageUrl;
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
}
