package com.prolific.prolib.helpers;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prolific.prolib.net.ProLibClient.Book;

public class ViewHolder {
    public static class BookItem {
        public TextView tvTitle;
        public TextView tvAuthor;
        public LinearLayout llCategories;
        public ImageView ivAvailability;
        public Book book;
    }

    public static class AuthorGroup {
        public TextView tvAuthor;
    }
}
