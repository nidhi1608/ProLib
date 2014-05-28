package com.prolific.prolib.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;
import com.prolific.prolib.app.ProLibApp;
import com.prolific.prolib.app.R;
import com.prolific.prolib.helpers.Async;
import com.prolific.prolib.helpers.UIUtils;
import com.prolific.prolib.helpers.ViewHolder;
import com.prolific.prolib.net.ProLibClient;
import com.prolific.prolib.net.ProLibClient.Book;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

public class AuthorListAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private final LayoutInflater mInflater;
    private final TreeMap<String, ArrayList<Book>> mBooks = new TreeMap<String, ArrayList<Book>>();
    private final ArrayList<String> mKeySet = new ArrayList<String>();

    public AuthorListAdapter(Context context) {
        this.mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        getBooksPerAuthor();
    }

    private void getBooksPerAuthor() {
        Book.allBooks(mContext, new Async.Block<List<Book>>() {
            @Override
            public void call(final List<Book> books) {
                // We transform the list of books to list of books by each author.
                for (final Book book : books) {
                    // We also need to handle the case where there are multiple authors per book.
                    final String authors = book.author;
                    final String[] authorCandidateList = authors.split(",");
                    final ArrayList<String> authorList = new ArrayList<String>();
                    for (final String authorCandidate : authorCandidateList) {
                        authorList.add(authorCandidate.trim());
                    }
                    for (final String author : authorList) {
                        if (!mBooks.containsKey(author)) {
                            mBooks.put(author, new ArrayList<Book>());
                        }
                        final ArrayList<Book> booksByAuthor = mBooks.get(author);
                        booksByAuthor.add(book);
                    }
                }
                mKeySet.clear();
                mKeySet.addAll(mBooks.keySet());
            }
        }, false);
    }

    @Override
    public int getGroupCount() {
        return mKeySet.size();
    }

    @Override
    public int getChildrenCount(final int groupPosition) {
        return getGroup(groupPosition).size();
    }

    @Override
    public ArrayList<Book> getGroup(final int groupPosition) {
        final String key = mKeySet.get(groupPosition);
        return mBooks.get(key);
    }

    @Override
    public Book getChild(final int groupPosition, final int childPosition) {
        return getGroup(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(final int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(final int groupPosition, final int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.author_group, null);
            final ViewHolder.AuthorGroup holder = new ViewHolder.AuthorGroup();
            holder.tvAuthor = (TextView) convertView.findViewById(R.id.tvAuthor);
            convertView.setTag(holder);
        }
        final ViewHolder.AuthorGroup holder = (ViewHolder.AuthorGroup) convertView.getTag();
        holder.tvAuthor.setText(mKeySet.get(groupPosition));
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mInflater.inflate(R.layout.book_list_item, null);
            final ViewHolder.BookItem holder = new ViewHolder.BookItem();
            holder.tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            holder.tvAuthor = (TextView) view.findViewById(R.id.tvAuthor);
            holder.llCategories = (LinearLayout) view.findViewById(R.id.llCategories);
            holder.ivAvailability = (ImageView) view.findViewById(R.id.ivAvailability);
            view.setTag(holder);
        }
        final ViewHolder.BookItem holder = (ViewHolder.BookItem) view.getTag();
        final Book book = getChild(groupPosition, childPosition);
        holder.book = book;
        UIUtils.populateListItem(holder, book, mContext, mInflater, false);
        return view;
    }

    @Override
    public boolean isChildSelectable(final int groupPosition, final int childPosition) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }
}
