package com.example.bookrate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookrate.R;
import com.example.bookrate.model.Book;

import java.util.List;

public class AdminBookAdapter extends RecyclerView.Adapter<AdminBookAdapter.ViewHolder> {

    private final Context context;
    private final List<Book> bookList;
    private final OnBookDeleteListener deleteListener;

    public interface OnBookDeleteListener {
        void onBookDelete(Book book);
    }

    public AdminBookAdapter(Context context, List<Book> bookList, OnBookDeleteListener listener) {
        this.context = context;
        this.bookList = bookList;
        this.deleteListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Book book = bookList.get(position);

        holder.title.setText(book.getTitle() != null ? book.getTitle() : "No Title");
        holder.author.setText(book.getAuthor() != null ? book.getAuthor() : "No Author");
        holder.genre.setText(book.getGenre() != null ? book.getGenre() : "No Genre");

        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(book.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.placeholder_image);
        }

        holder.deleteButton.setOnClickListener(v -> deleteListener.onBookDelete(book));
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, author, genre;
        Button deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.bookImage);
            title = itemView.findViewById(R.id.bookTitle);
            author = itemView.findViewById(R.id.bookAuthor);
            genre = itemView.findViewById(R.id.bookGenre);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
