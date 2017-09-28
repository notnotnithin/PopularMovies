package com.learnwithme.buildapps.popularmovies.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ManageFavMovies {
    private static final String PREFERENCE_NAME_FAV_MOVIES = "fav_movies";
    private static ManageFavMovies instance = null;
    private final SharedPreferences sharedPreferences;

    private ManageFavMovies(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFERENCE_NAME_FAV_MOVIES, Context.MODE_PRIVATE);
    }

    public static synchronized ManageFavMovies getInstance(Context context) {
        if (instance == null)
            instance = new ManageFavMovies(context);
        return instance;
    }

    public boolean getBoolean(int key) {
        return sharedPreferences.getBoolean(String.valueOf(key), false);
    }

    public void putBoolean(int key, boolean value) {
        sharedPreferences.edit().putBoolean(String.valueOf(key), value).apply();
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}