package com.prolific.prolib.net;

import android.content.Context;

import com.prolific.prolib.app.ProLibApp;
import com.prolific.prolib.app.R;
import com.prolific.prolib.helpers.Async;
import com.prolific.prolib.helpers.UIUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public class ProLibClient {

    public static class Book {
        public final String author;
        public final String categories;
        public final String lastCheckedOut;
        public final String lastCheckedOutBy;
        public final String publisher;
        public final String title;
        public final String url;

        public Book(String title, String author, String categories, String lastCheckedOut, String lastCheckedOutBy, String publisher, String url) {
            this.author = author;
            this.categories = categories;
            this.lastCheckedOut = lastCheckedOut;
            this.lastCheckedOutBy = lastCheckedOutBy;
            this.publisher = publisher;
            this.title = title;
            this.url = url;
        }

        public String parseId() {
            final String urlToUse = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
            return urlToUse.substring(urlToUse.lastIndexOf('/') + 1);
        }

        private static List<Book> sLibrary = null;
        private static final HashMap<String, Book> sBooks = new HashMap<String, Book>();

        public static void allBooks(final Context context, final Async.Block<List<Book>> completion, boolean forceRefresh) {
            if (sLibrary == null || forceRefresh) {
                if (!UIUtils.isNetworkAvailable(context)) {
                    UIUtils.showError(context, R.string.error_no_network, R.string.error_no_network_message, new Runnable() {
                        @Override
                        public void run() {
                            if (completion != null) {
                                completion.call(sLibrary);
                            }
                        }
                    });
                    return;
                }
                Async.dispatch(new Runnable() {
                    @Override
                    public void run() {
                        final List<ProLibClient.Book> books = ProLibApp.getProLib().listBooks();
                        Async.dispatchMain(new Runnable() {
                            @Override
                            public void run() {
                                sLibrary = books;
                                sBooks.clear();
                                for (final Book book : books) {
                                    sBooks.put(book.url, book);
                                }
                                completion.call(sLibrary);
                            }
                        });
                    }
                });
            } else {
                completion.call(sLibrary);
            }
        }

        public static Book getBook(final String url) {
            return sBooks.get(url);
        }

        public static void addBook(Book book) {
            sLibrary.add(0, book);
            upsertBook(book);
        }

        public static void upsertBook(final Book book) {
            sBooks.put(book.url, book);
        }
    }

    public interface ProLib {
        @GET("/books/")
        List<Book> listBooks();

        @POST("/books/")
        Book addBook(@Body Book book);

        @DELETE("/{url}/")
        void deleteBook(@Path("url") String url, Callback<Object> object);

        @PUT("/books/{id}/")
        @FormUrlEncoded
        void checkoutBook(@Path("id") final String id, @Field("lastCheckedOutBy") final String lastCheckedOutBy, Callback<Book> object);
    }
}
