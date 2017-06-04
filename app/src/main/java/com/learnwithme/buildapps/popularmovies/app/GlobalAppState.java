package com.learnwithme.buildapps.popularmovies.app;

import android.app.Application;
import android.util.Log;

import com.learnwithme.buildapps.popularmovies.api.APIManager;

/**
 * Created by Nithin on 15/05/2017.
 */

public class GlobalAppState extends Application {
    private static final String TAG = GlobalAppState.class.getSimpleName();
    private APIManager mAPIManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "App Started");
        initApp();
    }

    private void initApp() {
        mAPIManager = APIManager.getInstance(GlobalAppState.this);
        mAPIManager.init();
    }

    public synchronized APIManager getAPIManager() {
        return mAPIManager;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if(mAPIManager != null) {
            mAPIManager.terminate();
        }
    }
}