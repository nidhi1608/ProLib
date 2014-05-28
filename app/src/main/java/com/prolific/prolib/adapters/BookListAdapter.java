package com.prolific.prolib.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prolific.prolib.app.R;
import com.prolific.prolib.helpers.UIUtils;
import com.prolific.prolib.helpers.ViewHolder;
import com.prolific.prolib.net.ProLibClient;
import com.prolific.prolib.net.ProLibClient.Book;

import java.util.ArrayList;
import java.util.List;


public class BookListAdapter extends ArrayAdapter<Book> {
    LayoutInflater mInflater;

    public BookListAdapter(final Context context) {
        super(context, 0);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = mInflater.inflate(R.layout.book_list_item, null);
            final ViewHolder.BookItem holder = new ViewHolder.BookItem();
            holder.tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            holder.tvAuthor = (TextView) view.findViewById(R.id.tvAuthor);
            holder.llCategories = (LinearLayout) view.findViewById(R.id.llCategories);
            holder.ivAvailability = (ImageView) view.findViewById(R.id.ivAvailability);
            view.setTag(holder);
        }
        final ViewHolder.BookItem holder = (ViewHolder.BookItem) view.getTag();
        final Book book = getItem(position);
        holder.book = book;
        UIUtils.populateListItem(holder, book, getContext(), mInflater, true);
        return view;
    }


    public void addRangeToTop(List<Book> books)
    {
        for (Book book : books)
        {
            this.insert(book, 0);
        }
    }

}
