package com.prolific.prolib.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prolific.prolib.app.R;

public class BookListFragment extends Fragment {
    private View mCachedView;
    private ViewGroup mCachedViewGroup;

    public BookListFragment() {
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        mCachedViewGroup = container;
        mCachedView = inflater.inflate(R.layout.fragment_book_list, container, false);
        mCachedView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        return mCachedView;
    }

}
