package com.example.bookrate.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookrate.R;
import com.example.bookrate.adapter.CurrentlyReadingAdapter;
import com.example.bookrate.adapter.RecommendationAdapter;
import com.example.bookrate.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;


public class UserProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private ImageButton logoutButton;
    private TextView userNameTextView, booksReadTextView;
    private final int PICK_IMAGE_REQUEST = 1001;
    private RecyclerView currentlyReadingRecyclerView;
    private CurrentlyReadingAdapter currentlyReadingAdapter;
    private final List<Book> currentlyReadingBooks = new ArrayList<>();
    private RecyclerView recommendationsRecyclerView;
    private RecommendationAdapter recommendationsAdapter;
    private final List<Book> recommendedBooks = new ArrayList<>();


    private final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("users").child(userId);

    private final StorageReference storageRef = FirebaseStorage.getInstance("gs://bookrate-4dc23.firebasestorage.app")
            .getReference("profile_images")
            .child(userId + ".jpg");

    private final DatabaseReference booksRef = FirebaseDatabase.getInstance(
            "https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("books");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // 🔙 closes current activity and goes back

        profileImageView = findViewById(R.id.profileImageView);
        logoutButton = findViewById(R.id.logoutButton);

        currentlyReadingRecyclerView = findViewById(R.id.currentlyReadingRecyclerView);
        currentlyReadingAdapter = new CurrentlyReadingAdapter(this, currentlyReadingBooks);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        currentlyReadingRecyclerView.setLayoutManager(layoutManager);
        currentlyReadingRecyclerView.setAdapter(currentlyReadingAdapter);

        recommendationsRecyclerView = findViewById(R.id.recommendationsRecyclerView);
        recommendationsAdapter = new RecommendationAdapter(this, recommendedBooks);
        LinearLayoutManager recLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recommendationsRecyclerView.setLayoutManager(recLayoutManager);
        recommendationsRecyclerView.setAdapter(recommendationsAdapter);

        userNameTextView = findViewById(R.id.userNameTextView);
        booksReadTextView = findViewById(R.id.booksReadTextView);

        loadUserProfile();

        profileImageView.setOnClickListener(v -> pickImageFromGallery());
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

    }
    private void loadUserProfile() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                userNameTextView.setText(name != null ? name : "Unknown User");

                if (profileImageUrl != null) {
                    Glide.with(UserProfileActivity.this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.placeholder_image)
                            .into(profileImageView);
                }

                // Extract bookStates from user
                DataSnapshot bookStates = snapshot.child("bookStates");
                int readCount = 0;
                int currentlyReadingCount = 0;
                List<String> currentlyReadingTitles = new ArrayList<>();

                for (DataSnapshot bookSnap : bookStates.getChildren()) {
                    String state = bookSnap.child("state").getValue(String.class);
                    if ("Read".equalsIgnoreCase(state)) {
                        readCount++;
                    } else if ("Currently Reading".equalsIgnoreCase(state)) {
                        currentlyReadingCount++;
                        currentlyReadingTitles.add(bookSnap.getKey());  // key is the title
                    }
                }
                booksReadTextView.setText("Books Read: " + readCount);
                // Match titles with books under /books
                        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot booksSnapshot) {
                                for (DataSnapshot snap : booksSnapshot.getChildren()) {
                                    Book book = snap.getValue(Book.class);
                                    Log.d("DEBUG_BOOKS", "Entry book db: " + book.getTitle());
                                    if (book != null) {
                                        book.setId(snap.getKey());
                                        if(currentlyReadingTitles.contains(book.getId())) {
                                            currentlyReadingBooks.add(book);
                                            Log.d("DEBUG_BOOKS", "Added book: " + book.getTitle());
                                        }
                                    }
                                }
                                currentlyReadingAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {}
                        });

                // Fetch all books & calculate recommendations
                booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot booksSnapshot) {
                        HashMap<String, Integer> genreStarCount = new HashMap<>();
                        HashMap<String, String> userStates = new HashMap<>();
                        HashMap<String, Integer> userRatings = new HashMap<>();
                        List<Book> allBooks = new ArrayList<>();

                        // Load user states and ratings
                        DataSnapshot bookStates = snapshot.child("bookStates");
                        for (DataSnapshot bookSnap : bookStates.getChildren()) {
                            String state = bookSnap.child("state").getValue(String.class);
                            Long rating = bookSnap.child("rating").getValue(Long.class);
                            Log.d("STATE_BOOK_GENRE_ID:", bookSnap.getKey());
                            Log.d("STATE_BOOK_GENRE_RATING:", String.valueOf(rating));
                            Log.d("STATE_BOOK_GENRE_STATE:", state);
                            Log.d("STATE_BOOK_GENRE", "NEXT_BOOK");
                            if (state != null) userStates.put(bookSnap.getKey(), state);
                            if (rating != null) userRatings.put(bookSnap.getKey(), rating.intValue());
                        }

                        // Process books and compute genre totals
                        for (DataSnapshot snap : booksSnapshot.getChildren()) {
                            Book book = snap.getValue(Book.class);
                            if (book != null) {
                                String id = snap.getKey();
                                book.setId(id);
                                allBooks.add(book);

                                if (userStates.containsKey(id) && "Read".equalsIgnoreCase(userStates.get(id))) {
                                    int rating = userRatings.containsKey(id) ? userRatings.get(id) : 0;
                                    String genre = book.getGenre();
                                    Log.d("STATE_BOOK_GENRE_IF_RATING:", String.valueOf(rating));
                                    Log.d("STATE_BOOK_GENRE_IF_GENRE", genre);

                                    int currentTotal = genreStarCount.containsKey(genre) ? genreStarCount.get(genre) : 0;
                                    Log.d("STATE_BOOK_GENRE_IF_GENRE_CURRENT_TOTAL:", String.valueOf(currentTotal));

                                    genreStarCount.put(genre, currentTotal + rating);
                                    Log.d("STATE_BOOK_GENRE_IF", "NEXT IT");

                                }
                            }
                        }

                        for (HashMap.Entry<String, Integer> entry : genreStarCount.entrySet()) {
                            Log.d("STATE_BOOK_GENRE_HASH", "Key: " + entry.getKey() + ", Value: " + entry.getValue());
                        }


                        // Sort genres by rating sum
                        List<String> topGenres = new ArrayList<>(genreStarCount.keySet());
                        Collections.sort(topGenres, new Comparator<String>() {
                            @Override
                            public int compare(String g1, String g2) {
                                return genreStarCount.get(g2) - genreStarCount.get(g1);
                            }
                        });

                        for (String item : topGenres) {
                            Log.d("STATE_BOOK_GENRE_TOP:", item);
                        }


                        // Recommend up to 3 books in top genres in "Want to Read"
                        Set<String> usedGenres = new HashSet<>();
                        for (String genre : topGenres) {
                            for (Book book : allBooks) {
                                if (genre.equals(book.getGenre()) &&
                                        "Want to Read".equalsIgnoreCase(userStates.get(book.getId())) &&
                                        !"Currently Reading".equalsIgnoreCase(userStates.get(book.getId())) &&
                                        !"Read".equalsIgnoreCase(userStates.get(book.getId())) &&
                                        !usedGenres.contains(genre)) {
                                    Log.d("STATE_BOOK_GENRE_USED:", book.getId());
                                    recommendedBooks.add(book);
                                    usedGenres.add(genre);
                                    break;
                                }
                            }
                            if (recommendedBooks.size() >= 3) break;
                        }

                        // Fallback: recommend top 3 highest average-rated books (unique genres)
                        if (recommendedBooks.isEmpty()) {
                            // You must implement getAverageRating() or skip this
                            Collections.sort(allBooks, new Comparator<Book>() {
                                @Override
                                public int compare(Book b1, Book b2) {
                                    return Float.compare(b2.getAverageRating(), b1.getAverageRating());  // descending
                                }
                            });

                            Set<String> seenGenres = new HashSet<>();
                            for (Book book : allBooks) {
                                String genre = book.getGenre();
                                String state = userStates.get(book.getId());

                                if (!seenGenres.contains(genre)
                                        && !"Currently Reading".equalsIgnoreCase(state)
                                        && !"Read".equalsIgnoreCase(state)) {
                                    recommendedBooks.add(book);
                                    seenGenres.add(genre);
                                }

                                if (recommendedBooks.size() >= 3) break;
                            }
                        }

                        for(Book book : recommendedBooks){
                            Log.d("STATE_BOOK_GENRE_RECOMMENDED:", book.getTitle());
                        }

                        recommendationsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });


            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadProfileImage(imageUri);
            }
        }
    }

    private void uploadProfileImage(Uri imageUri) {
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    dbRef.child("profileImageUrl").setValue(downloadUrl);
                    Glide.with(UserProfileActivity.this).load(downloadUrl).into(profileImageView);
                    Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show());
    }
}
