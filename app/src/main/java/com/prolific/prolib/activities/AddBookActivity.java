package com.prolific.prolib.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.andreabaccega.widget.FormEditText;
import com.prolific.prolib.app.ProLibApp;
import com.prolific.prolib.app.R;
import com.prolific.prolib.helpers.Async;
import com.prolific.prolib.helpers.UIUtils;
import com.prolific.prolib.net.ProLibClient;
import com.prolific.prolib.net.ProLibClient.Book;

public class AddBookActivity extends AppActivity {
    private FormEditText etTitle;
    private FormEditText etAuthor;
    private FormEditText etPublisher;
    private FormEditText etCategories;
    private TextView tvError;
    private FormEditText[] mAllFields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        etTitle = (FormEditText) findViewById(R.id.etTitle);
        etAuthor = (FormEditText) findViewById(R.id.etAuthor);
        etPublisher = (FormEditText) findViewById(R.id.etPublisher);
        etCategories = (FormEditText) findViewById(R.id.etCategories);
        tvError = (TextView) findViewById(R.id.tvError);
        FormEditText[] allFields = {etAuthor, etCategories, etTitle, etPublisher};
        mAllFields = allFields;
        for (final FormEditText field : mAllFields) {
            field.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean didGetFocus) {
                    if (didGetFocus) {
                    } else {
                        boolean valid = field.testValidity();
                        if (!valid) {
                            field.startAnimation(AnimationUtils.loadAnimation(AddBookActivity.this, R.anim.shake));
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        confirmBack();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                confirmBack();
                break;
        }
        return true;
    }

    public void addBook(View view) {
        onClickNext(view);
    }

    public void onClickNext(View v) {
        boolean allValid = true;
        for (FormEditText field : mAllFields) {
            boolean valid = field.testValidity();
            if (!valid) {
                field.startAnimation(AnimationUtils.loadAnimation(AddBookActivity.this, R.anim.shake));
            }
            allValid = valid && allValid;
        }
        if (allValid) {
            saveBook();
            Intent intent = getIntent();
            setResult(BookListActivity.REQUEST_CODE);
            finish();
        }
    }

    public void onDonePress(final MenuItem mi) {
        confirmBack();
    }

    private void confirmBack() {
        if (TextUtils.getTrimmedLength(etAuthor.getText().toString()) > 0 ||
                TextUtils.getTrimmedLength(etCategories.getText().toString()) > 0 ||
                TextUtils.getTrimmedLength(etTitle.getText().toString()) > 0 ||
                TextUtils.getTrimmedLength(etPublisher.getText().toString()) > 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(AddBookActivity.this);
            alert.setTitle(UIUtils.getStringResource(this, R.string.alert_cancel_title));
            alert.setMessage(UIUtils.getStringResource(this, R.string.alert_cancel_message));
            alert.setPositiveButton(UIUtils.getStringResource(AddBookActivity.this, R.string.alert_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, final int which) {
                    finish();
                }
            });
            alert.setNegativeButton(UIUtils.getStringResource(AddBookActivity.this, R.string.alert_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
        } else {
            finish();
        }
    }

    private void saveBook() {
        final String author = etAuthor.getText().toString();
        final String categories = etCategories.getText().toString();
        final String title = etTitle.getText().toString();
        final String publisher = etPublisher.getText().toString();
        Async.dispatch(new Runnable() {
            @Override
            public void run() {
                final ProLibClient.ProLib proLib = ProLibApp.getProLib();
                Book book = proLib.addBook(new Book(title, author, categories, null, null, publisher, null));
                if (book != null) {
                    Book.addBook(book);
                }
            }
        });
    }
}
