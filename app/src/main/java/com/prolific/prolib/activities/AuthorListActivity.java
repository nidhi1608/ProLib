package com.prolific.prolib.activities;


import android.content.Context;
import android.os.Bundle;
import android.widget.ExpandableListView;

import com.prolific.prolib.adapters.AuthorListAdapter;
import com.prolific.prolib.app.R;

public class AuthorListActivity extends AppActivity {

    public static void populate(final ExpandableListView lvAuthors, final Context context) {
        AuthorListAdapter authorListAdapter;
        authorListAdapter = new AuthorListAdapter(context);
        lvAuthors.setAdapter(authorListAdapter);
        for (int i = 0; i < authorListAdapter.getGroupCount(); i++) {
            lvAuthors.expandGroup(i);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_author_list);
    }
}
