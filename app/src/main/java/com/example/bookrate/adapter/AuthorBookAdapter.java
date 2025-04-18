package com.example.bookrate.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookrate.R;
import com.example.bookrate.activity.BookDetailAuthorActivity;
import com.example.bookrate.model.Book;

import java.util.List;

public class AuthorBookAdapter extends RecyclerView.Adapter<AuthorBookAdapter.BookViewHolder> {

    private final List<Book> bookList;

    public AuthorBookAdapter(List<Book> bookList) {
        this.bookList = bookList;
    }

    @Override
    public AuthorBookAdapter.BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_author, parent, false);
        return new AuthorBookAdapter.BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AuthorBookAdapter.BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        holder.titleText.setText(book.getTitle());
        holder.authorText.setText(book.getAuthor());
        holder.genreText.setText(book.getGenre());

        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(book.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.bookImage);
        } else {
            holder.bookImage.setImageResource(R.drawable.placeholder_image);
        }

        // Navigate to BookDetailAuthorActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), BookDetailAuthorActivity.class);
            intent.putExtra("book", book);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, authorText, genreText;
        ImageView bookImage;

        public BookViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.bookTitle);
            authorText = itemView.findViewById(R.id.bookAuthor);
            genreText = itemView.findViewById(R.id.bookGenre);
            bookImage = itemView.findViewById(R.id.bookImage);
        }
    }
}
