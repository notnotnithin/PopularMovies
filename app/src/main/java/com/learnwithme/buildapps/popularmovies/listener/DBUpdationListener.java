package com.learnwithme.buildapps.popularmovies.listener;

public interface DBUpdationListener {
    void onSuccess(int operationType);
    void onFailure();
}