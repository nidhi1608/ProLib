package com.prolific.prolib.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andreabaccega.formedittextvalidator.AlphaValidator;
import com.andreabaccega.widget.FormEditText;
import com.prolific.prolib.app.ProLibApp;
import com.prolific.prolib.app.R;
import com.prolific.prolib.helpers.UIUtils;
import com.prolific.prolib.net.ProLibClient.Book;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BookDetailActivity extends AppActivity {

    private static final String INTENT_EXTRA_BOOK_URL = "bookUrl";
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        sDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static void launch(final Context context, final String bookUrl) {
        final Intent intent = new Intent(context, BookDetailActivity.class);
        intent.putExtra(INTENT_EXTRA_BOOK_URL, bookUrl);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new BookDetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.miShare) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onShareBook(final MenuItem mi) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        final StringBuilder builder = new StringBuilder();
        builder.append(toShareIntentString());
        share.putExtra(Intent.EXTRA_TEXT, builder.toString());
        startActivity(Intent.createChooser(share, getResources().getString(R.string.share_task_list)));
    }

    private String toShareIntentString() {

        final StringBuilder builder = new StringBuilder();
        Book book = Book.getBook(getActivity().getIntent().getStringExtra(INTENT_EXTRA_BOOK_URL));
        if (book != null) {
            builder.append("Book Title: " + book.title);
            builder.append("\n\n");
            builder.append("Author: " + book.author);
            builder.append("\n\n");
            builder.append("Categories: " + book.categories);
            builder.append("\n\n");
            builder.append("Publisher: " + book.publisher);
            builder.append("\n\n");
            if (book.lastCheckedOut != null) {
                    builder.append("Last Checkeout: " + formatLastCheckout(book));
            }
        }
        return builder.toString();
    }

    private Date safeParse(final String dateString) {
        try {
            return sDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    private String formatLastCheckout(Book book) {
        String dateString = "";
        String formattedString = "";
        if (book.lastCheckedOut != null) {
            final Date date = safeParse(book.lastCheckedOut);
            dateString = DateUtils.formatDateTime(getActivity(), date.getTime(),
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE);
            formattedString = book.lastCheckedOutBy + " @ " + dateString;
        }
        return formattedString;
    }

    public static class BookDetailFragment extends Fragment {
        private static final String TAG = "BookDetailFragment";

        public BookDetailFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_book_detail, container, false);
            populateView(rootView, Book.getBook(getActivity().getIntent().getStringExtra(INTENT_EXTRA_BOOK_URL)));
            return rootView;
        }

        public String formatLastCheckout(Book book) {
            String dateString = "";
            if (book.lastCheckedOut != null) {
                final Date date = safeParse(book.lastCheckedOut);
                dateString = DateUtils.formatDateTime(getActivity(), date.getTime(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE);
            }
            String formattedString = book.lastCheckedOutBy
                    + "<small><i> @ " + dateString
                    + "</i></small>";
            return formattedString;
        }

        private Date safeParse(final String dateString) {
            try {
                return sDateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return new Date();
        }

        private void setLastCheckedOut(final TextView tvCheckout, final TextView checkoutStatus, final Book book) {
            if (book.lastCheckedOutBy == null || book.lastCheckedOutBy.length() == 0) {
                tvCheckout.setText(null);
                checkoutStatus.setText(getActivity().getString(R.string.available));
                checkoutStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                tvCheckout.setVisibility(View.GONE);
                return;
            }
            checkoutStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            checkoutStatus.setText(getActivity().getString(R.string.last_checkout));
            tvCheckout.setText(Html.fromHtml(formatLastCheckout(book)));
            tvCheckout.setVisibility(View.VISIBLE);
        }

        private void populateView(final View rootView, final Book book) {
            if (book == null) {
                // This book doesn't exist, show an error; this should never happen.
                UIUtils.showError(getActivity(), R.string.alert_book_not_found, R.string.alert_book_not_found_message, new Runnable() {
                    @Override
                    public void run() {
                        getActivity().finish();
                    }
                });
            } else {
                final TextView title = (TextView) rootView.findViewById(R.id.tvTitle);
                final TextView author = (TextView) rootView.findViewById(R.id.tvAuthor);
                final TextView publisher = (TextView) rootView.findViewById(R.id.tvPublisher);
                final TextView lastCheckedOut = (TextView) rootView.findViewById(R.id.tvCheckedout);
                final TextView lastCheckedOutTitle = (TextView) rootView.findViewById(R.id.tvCheckedoutTitle);
                title.setText(book.title);
                author.setText(book.author);
                publisher.setText(book.publisher);
                setLastCheckedOut(lastCheckedOut, lastCheckedOutTitle, book);
                final LinearLayout llCategories = (LinearLayout) rootView.findViewById(R.id.llCategories);
                UIUtils.populateCategories(getActivity().getLayoutInflater(), llCategories, book);
                final Button checkout = (Button) rootView.findViewById(R.id.btnCheckout);
                checkout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle(getActivity().getString(R.string.alert_prompt_name));
                        // Set an EditText view to get user input.
                        final FormEditText input = new FormEditText(getActivity(), null);
                        input.addValidator(new AlphaValidator(getActivity().getString(R.string.error_author_has_number)));
                        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                        alert.setView(input);
                        alert.setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (input.testValidity()) {
                                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                                    progressDialog.setIndeterminate(true);
                                    progressDialog.show();
                                    final String name = input.getText().toString().trim();
                                    ProLibApp.getProLib().checkoutBook(book.parseId(), name, new Callback<Book>() {
                                        @Override
                                        public void success(Book book, Response response) {
                                            Book.upsertBook(book);
                                            setLastCheckedOut(lastCheckedOut, lastCheckedOutTitle, book);
                                            progressDialog.dismiss();
                                        }

                                        @Override
                                        public void failure(RetrofitError error) {
                                            progressDialog.dismiss();
                                            UIUtils.showError(getActivity(), getActivity().getString(R.string.error_completing_request), error.getLocalizedMessage(), null);
                                        }
                                    });
                                }
                            }
                        });
                        alert.setNegativeButton(R.string.alert_cancel, null);
                        alert.show();
                    }
                });
            }
        }
    }
}
