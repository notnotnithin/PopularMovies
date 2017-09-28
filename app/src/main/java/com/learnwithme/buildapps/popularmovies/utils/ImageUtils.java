package com.learnwithme.buildapps.popularmovies.utils;

import android.content.Context;

import com.squareup.picasso.Picasso;

public class ImageUtils {
    private static Picasso mInstance;

    public static Picasso with(Context context) {
        if(mInstance == null) {
            mInstance = new Picasso.Builder(context.getApplicationContext()).build();
        }
        return mInstance;
    }

    private ImageUtils() {
        throw new AssertionError("No instances.");
    }
}