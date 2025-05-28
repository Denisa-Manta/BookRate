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

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private final Context context;
    private final List<Book> recommendedBooks;
    private final DatabaseReference usersRef = FirebaseDatabase.getInstance("https://bookrate-4dc23-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");

    public RecommendationAdapter(Context context, List<Book> recommendedBooks) {
        this.context = context;
        this.recommendedBooks = recommendedBooks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recommendation_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Book book = recommendedBooks.get(position);
        holder.title.setText(book.getTitle());
        holder.genre.setText(book.getGenre());
        Glide.with(context).load(book.getImageUrl()).placeholder(R.drawable.placeholder_image).into(holder.imageView);

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
        return recommendedBooks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title, genre;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.recommendationBookImage);
            title = itemView.findViewById(R.id.recommendationBookTitle);
            genre = itemView.findViewById(R.id.recommendationBookGenre);
        }
    }
}
