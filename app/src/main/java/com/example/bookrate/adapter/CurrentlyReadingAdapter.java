package com.example.bookrate.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookrate.R;
import com.example.bookrate.activity.BookDetailActivity;
import com.example.bookrate.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CurrentlyReadingAdapter extends RecyclerView.Adapter<CurrentlyReadingAdapter.ViewHolder> {

    private final List<Book> bookList;
    private final Context context;
    private final DatabaseReference usersRef = FirebaseDatabase.getInstance("https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");

    public CurrentlyReadingAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_currently_reading_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Book book = bookList.get(position);

        holder.title.setText(book.getTitle() != null ? book.getTitle() : "Unknown Title");
        holder.author.setText(book.getAuthor() != null ? book.getAuthor() : "Unknown Author");

        // Temporarily hide image completely
        holder.image.setVisibility(View.GONE); // Optional: hide ImageView

        holder.itemView.setOnClickListener(v -> {
            String bookId = book.getId();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


            usersRef.child(userId).child("bookStates").child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String state = snapshot.child("state").getValue(String.class);
                        Long ratingLong = snapshot.child("rating").getValue(Long.class);

                        if (state != null) {
                            book.setState(state);
                        }
                        if (ratingLong != null) {
                            book.setRating(ratingLong.intValue());
                        }
                    }

                    // ✅ Proceed to detail view only after state is loaded
                    Intent intent = new Intent(holder.itemView.getContext(), BookDetailActivity.class);
                    intent.putExtra("book", book);
                    holder.itemView.getContext().startActivity(intent);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // fallback if needed
                    Intent intent = new Intent(holder.itemView.getContext(), BookDetailActivity.class);
                    intent.putExtra("book", book);
                    holder.itemView.getContext().startActivity(intent);
                }
            });
        });

    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, author;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.bookImage);
            title = itemView.findViewById(R.id.bookTitle);
            author = itemView.findViewById(R.id.bookAuthor);
        }
    }
}
