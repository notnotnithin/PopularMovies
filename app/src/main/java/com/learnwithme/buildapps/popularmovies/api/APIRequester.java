package com.learnwithme.buildapps.popularmovies.api;

/**
 * Created by Nithin on 15/05/2017.
 */

/**
 * This interface should be implemented by clients that ask for Data from {@code APIManager}
 */
public interface APIRequester {
    /**
     * Fetch request successful.
     *
     * @param response
     */
    void onSuccess(Object response);

    /**
     * Fetch request failed.
     *
     * @param error
     */
    void onFailure(Throwable error);
}