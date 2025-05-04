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

import java.util.List;

public class CurrentlyReadingAdapter extends RecyclerView.Adapter<CurrentlyReadingAdapter.ViewHolder> {

    private final List<Book> bookList;
    private final Context context;

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
            Intent intent = new Intent(holder.itemView.getContext(), BookDetailActivity.class);
            intent.putExtra("book", book);  // book must implement Serializable or Parcelable
            holder.itemView.getContext().startActivity(intent);
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
