package com.learnwithme.buildapps.popularmovies.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.learnwithme.buildapps.popularmovies.listener.DBUpdationListener;
import com.learnwithme.buildapps.popularmovies.model.Movie;

/**
 * Created by Nithin on 31/05/2017.
 */

public class FavouriteHelper extends AsyncTask<Void, Void, Void> {
    private static final String TAG = FavouriteHelper.class.getSimpleName();

    private Context mContext;
    private Movie mMovie;
    private DBUpdationListener mDBUpdateListener;

    public static final int ADD_TO_FAVORITE = 1;
    public static final int DELETE_FROM_FAV = 2;

    public FavouriteHelper(Context mContext, Movie mMovie, DBUpdationListener mDBUpdateListener) {
        this.mContext = mContext;
        this.mMovie = mMovie;
        this.mDBUpdateListener = mDBUpdateListener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        manageFavourites();
        return null;
    }

    private void manageFavourites() {
        Log.d(TAG, MovieContract.MovieEntry.CONTENT_URI.getAuthority());

        Cursor favMovieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID},
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(mMovie.getId())},
                null);

        // If the ID exists, delete it.
        if(favMovieCursor.moveToFirst()) {
            int rowsDeleted = mContext.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(mMovie.getId())});
            if(rowsDeleted > 0) {
                mDBUpdateListener.onSuccess(DELETE_FROM_FAV);
            } else {
                mDBUpdateListener.onFailure();
            }
        } else {
            // Else insert the new click using Content Resolver and the base URI.
            ContentValues values = new ContentValues();

            values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mMovie.getId());
            values.put(MovieContract.MovieEntry.COLUMN_TITLE, mMovie.getTitle());
            values.put(MovieContract.MovieEntry.COLUMN_POSTER_IMAGE, mMovie.getPosterPath());
            values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, mMovie.getOverview());
            values.put(MovieContract.MovieEntry.COLUMN_AVERAGE_RATING, mMovie.getVoteAverage());
            values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());
            values.put(MovieContract.MovieEntry.COLUMN_BACKDROP_IMAGE, mMovie.getBackdropPath());

            Uri favInserted = mContext.getContentResolver().insert(
                    MovieContract.MovieEntry.CONTENT_URI,
                    values);

            long rowID = ContentUris.parseId(favInserted);

            if(rowID > 0) {
                mDBUpdateListener.onSuccess(ADD_TO_FAVORITE);
            } else {
                mDBUpdateListener.onFailure();
            }
        }
        favMovieCursor.close();
    }
}