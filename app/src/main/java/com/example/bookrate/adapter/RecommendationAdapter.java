package com.example.bookrate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookrate.R;
import com.example.bookrate.model.Book;

import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private final Context context;
    private final List<Book> recommendedBooks;

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
