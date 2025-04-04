package com.example.bookrate.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookrate.R;
import com.example.bookrate.activity.BookDetailActivity;
import com.example.bookrate.model.Book;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final List<Book> bookList;
    private final DatabaseReference userBookStatesRef;

    public BookAdapter(List<Book> bookList, DatabaseReference userBookStatesRef) {
        this.bookList = bookList;
        this.userBookStatesRef = userBookStatesRef;
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.titleText.setText(book.getTitle());
        holder.authorText.setText(book.getAuthor());
        holder.genreText.setText(book.getGenre());

        // Load image
        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(book.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.bookImage);
        } else {
            holder.bookImage.setImageResource(R.drawable.placeholder_image);
        }

        // Spinner adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                holder.itemView.getContext(),
                R.array.book_states,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.bookStateSpinner.setAdapter(adapter);

        // Set current state and rating
        holder.bookStateSpinner.setSelection(adapter.getPosition(book.getState()));
        holder.bookRatingBar.setRating(book.getRating());
        holder.bookRatingBar.setVisibility("Read".equals(book.getState()) ? View.VISIBLE : View.GONE);

        // Spinner listener
        holder.bookStateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstCall = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (firstCall) {
                    firstCall = false;
                    return;
                }

                String newState = parent.getItemAtPosition(pos).toString();
                book.setState(newState);

                if (!"Read".equals(newState)) {
                    book.setRating(0);
                    holder.bookRatingBar.setRating(0);
                    holder.bookRatingBar.setVisibility(View.GONE);
                } else {
                    holder.bookRatingBar.setVisibility(View.VISIBLE);
                }

                saveStateAndRating(book);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // RatingBar listener
        holder.bookRatingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser && "Read".equals(book.getState())) {
                book.setRating((int) rating);
                saveStateAndRating(book);
            }
        });

        // 🔥 NEW: Handle click → go to BookDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), BookDetailActivity.class);
            intent.putExtra("book", book);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    private void saveStateAndRating(Book book) {
        if (book.getId() == null) return;

        Map<String, Object> map = new HashMap<>();
        map.put("state", book.getState());
        map.put("rating", book.getRating());

        userBookStatesRef.child(book.getId()).updateChildren(map);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, authorText, genreText;
        ImageView bookImage;
        Spinner bookStateSpinner;
        RatingBar bookRatingBar;

        public BookViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.bookTitle);
            authorText = itemView.findViewById(R.id.bookAuthor);
            genreText = itemView.findViewById(R.id.bookGenre);
            bookImage = itemView.findViewById(R.id.bookImage);
            bookStateSpinner = itemView.findViewById(R.id.bookStateSpinner);
            bookRatingBar = itemView.findViewById(R.id.bookRatingBar);
        }
    }
}
