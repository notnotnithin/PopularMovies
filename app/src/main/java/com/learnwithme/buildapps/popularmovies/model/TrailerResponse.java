package com.learnwithme.buildapps.popularmovies.model;

import java.util.ArrayList;

/**
 * Created by Nithin on 15/05/2017.
 */

public class TrailerResponse {
    private int id;
    private ArrayList<Trailer> results;

    public int getId() {
        return id;
    }

    public ArrayList<Trailer> getResults() {
        return results;
    }
}