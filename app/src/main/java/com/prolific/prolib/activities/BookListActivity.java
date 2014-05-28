package com.prolific.prolib.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.RelativeLayout;

import com.prolific.prolib.adapters.BookListAdapter;
import com.prolific.prolib.app.ProLibApp;
import com.prolific.prolib.app.R;
import com.prolific.prolib.fragments.AuthorListFragment;
import com.prolific.prolib.fragments.BookListFragment;
import com.prolific.prolib.helpers.Async;
import com.nhaarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;

import com.prolific.prolib.helpers.UIUtils;
import com.prolific.prolib.helpers.ViewHolder;
import com.prolific.prolib.net.ProLibClient;
import com.prolific.prolib.net.ProLibClient.Book;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class BookListActivity extends AppActivity implements FragmentManager.OnBackStackChangedListener {
    private BookListAdapter mBookListAdapter;
    private boolean mShowingBack;
    private static final String TAG = "BookListActivity";
    private final ProLibClient.ProLib proLib = ProLibApp.getProLib();

    private static final BookListFragment sBookListFragment = new BookListFragment();
    private static final AuthorListFragment sAuthorListFragment = new AuthorListFragment();
    public static final int REQUEST_CODE = 20;
    private boolean mAddedEmptyView = false;
    private boolean mFlipping = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        setContentView(R.layout.activity_book_list);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.rlContainer, sBookListFragment)
                    .commit();
        } else {
            mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
        }
        Async.dispatchMain(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        });
        getFragmentManager().addOnBackStackChangedListener(BookListActivity.this);
    }

    @Override
     public void onBackStackChanged() {
        mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mShowingBack && mBookListAdapter != null && mBookListAdapter.getCount() > 0) {
            getMenuInflater().inflate(R.menu.author_list, menu);
        } else {
            getMenuInflater().inflate(R.menu.book_list, menu);
        }
        return true;
    }

    public void onShowAuthorList(final MenuItem mi) {
        if (mShowingBack) {
            flipCard();
            Async.dispatchMain(new Runnable() {
                @Override
                public void run() {
                    refresh(false);
                    setRefreshing(false);
                }
            });
        } else {
            flipCard();
            Async.dispatchMain(new Runnable() {
                @Override
                public void run() {
                    refresh(false);
                }
            });
        }
    }

    public void onAddBook(final MenuItem mi) {
        Intent intent = new Intent(BookListActivity.this, AddBookActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE) {
            refresh();
            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void flipCard() {
        if (mFlipping) return;
        mFlipping = true;
        Async.dispatchMain(new Runnable() {
            @Override
            public void run() {
                mFlipping = false;
            }
        }, 1000);
        if (mShowingBack) {
            getFragmentManager().popBackStack();
            return;
        }
        mShowingBack = true;
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                .replace(R.id.rlContainer, sAuthorListFragment)
                .addToBackStack(null)
                .commit();
        Async.dispatch(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
            }
        });
    }

    private Book bookFromView(final View view) {
        if (view.getTag() instanceof ViewHolder.BookItem) {
            return ((ViewHolder.BookItem)view.getTag()).book;
        } else {
            return null;
        }
    }

    private void setupListViewListeners(final ListView lv) {
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                removeItemFromList(bookFromView(view));
                return false;
            }
        });
        if (lv instanceof ExpandableListView) {
            ((ExpandableListView)lv).setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i2, long l) {
                    BookDetailActivity.launch(getActivity(), bookFromView(view).url);
                    return true;
                }
            });
        } else {
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    BookDetailActivity.launch(getActivity(), bookFromView(view).url);
                }
            });
        }
        setSwipeRefresh(R.id.swipe_container, new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        if (!mAddedEmptyView) {
            final View empty = getActivity().getLayoutInflater().inflate(R.layout.view_empty_list, null, false);
            addContentView(empty, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            lv.setEmptyView(empty);
            mAddedEmptyView = true;
        }
    }



    private void refresh() {
        refresh(true);
    }

    private void refresh(final boolean forceRefresh) {
        setRefreshing(true);
        Book.allBooks(getActivity(), new Async.Block<List<Book>>() {
            @Override
            public void call(final List<Book> books) {
                final ListView lvBooks = (ListView) findViewById(R.id.lvBooks);
                setRefreshing(false);
                if (lvBooks != null) {
                    if (mBookListAdapter == null) {
                        mBookListAdapter = new BookListAdapter(BookListActivity.this);
                    }
                    ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(mBookListAdapter);
                    scaleInAnimationAdapter.setAbsListView(lvBooks);
                    lvBooks.setAdapter(scaleInAnimationAdapter);
                    setupListViewListeners(lvBooks);
                    mBookListAdapter.clear();
                    mBookListAdapter.addRangeToTop(books);
                    mBookListAdapter.notifyDataSetChanged();
                } else {
                    final ExpandableListView lvAuthors = (ExpandableListView) findViewById(R.id.lvAuthors);
                    setupListViewListeners(lvAuthors);
                    AuthorListActivity.populate(lvAuthors, BookListActivity.this);
                }
            }
        }, forceRefresh);
    }

    private void removeItemFromList(final Book book) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(UIUtils.getStringResource(this, R.string.alert_delete_title));
        alert.setMessage(UIUtils.getStringResource(this, R.string.alert_delete_message));
        alert.setPositiveButton(UIUtils.getStringResource(BookListActivity.this, R.string.alert_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, final int which) {
                proLib.deleteBook(book.url.substring(1, book.url.length() - 1), new Callback<Object>() {
                    @Override
                    public void success(Object o, Response response) {
                        mBookListAdapter.remove(book);
                        mBookListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d(TAG, error.getMessage());
                    }
                });
            }
        });
        alert.setNegativeButton(UIUtils.getStringResource(BookListActivity.this, R.string.alert_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}
