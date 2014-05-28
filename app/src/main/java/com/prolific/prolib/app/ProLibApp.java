package com.prolific.prolib.app;

import android.app.Application;

import com.prolific.prolib.helpers.Constants;
import com.prolific.prolib.net.ProLibClient;

import retrofit.RestAdapter;

public class ProLibApp extends Application {
    private static ProLibClient.ProLib mProLib;

    @Override public void onCreate() {
        super.onCreate();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.PROLIB_SERVER)
                .build();
        mProLib = restAdapter.create(ProLibClient.ProLib.class);
    }

    public static ProLibClient.ProLib getProLib() {
        return mProLib;
    }
}
