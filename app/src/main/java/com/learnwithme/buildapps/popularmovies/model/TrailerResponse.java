package com.learnwithme.buildapps.popularmovies.model;

import java.util.ArrayList;

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