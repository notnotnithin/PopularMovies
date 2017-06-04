package com.learnwithme.buildapps.popularmovies.listener;

/**
 * Created by Nithin on 29/05/2017.
 */

public interface DBUpdationListener {
    void onSuccess(int operationType);
    void onFailure();
}