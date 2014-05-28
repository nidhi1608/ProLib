package com.prolific.prolib.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prolific.prolib.app.R;
import com.prolific.prolib.net.ProLibClient;
import com.prolific.prolib.net.ProLibClient.Book;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UIUtils {
    private static final float sDeviceScale = Resources.getSystem().getDisplayMetrics().density;

    public static int p(final float dp) {
        return (int)(dp * sDeviceScale);
    }

    public static String getStringResource(final Context context, final int id) {
        return context.getResources().getString(id);
    }

    public static void populateListItem(final ViewHolder.BookItem holder, final Book book, final Context context, final LayoutInflater inflator, final boolean showAuthor) {
        holder.tvTitle.setText(book.title);
        if(showAuthor) {
            holder.tvAuthor.setText(book.author);
        } else {
            holder.tvAuthor.setVisibility(View.GONE);
        }

        Drawable res = book.lastCheckedOut == null ? context.getResources().getDrawable(R.drawable.ic_available) : context.getResources().getDrawable(R.drawable.ic_unavailable);
        holder.ivAvailability.setImageDrawable(res);
        populateCategories(inflator, holder.llCategories, book);

    }

    public static void populateCategories(final LayoutInflater inflator, final LinearLayout llCategories, final Book book) {
        llCategories.removeAllViews();
        if (book.categories != null && book.categories.length() > 0) {
            List<String> categoryList = Arrays.asList(book.categories.split(","));
            if (categoryList != null && categoryList.size() > 0) {
                for (int i = 0; i <= categoryList.size() - 1; i++) {
                    TextView tvCategory = (TextView) inflator.inflate(R.layout.category_textview_item, null);
                    tvCategory.setText(categoryList.get(i).trim());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, UIUtils.p(4), 0);
                    tvCategory.setLayoutParams(params);
                    llCategories.addView(tvCategory);
                }
            }
        }
    }

    public static boolean isTextOnly(EditText etView) {
        Pattern ps = Pattern.compile("^[a-zA-Z ]+$");
        Matcher ms = ps.matcher(etView.getText().toString().trim());
        return ms.matches();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public static void showError(final Context context, final int title, final int message, final Runnable completion) {
        showError(context, UIUtils.getStringResource(context, title), UIUtils.getStringResource(context, message), completion);
    }

    public static void showError(final Context context, final CharSequence title, final CharSequence message, final Runnable completion) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setPositiveButton(UIUtils.getStringResource(context, R.string.alert_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, final int which) {
                if (completion != null) {
                    completion.run();
                }
            }
        });
        alert.show();
    }
}
