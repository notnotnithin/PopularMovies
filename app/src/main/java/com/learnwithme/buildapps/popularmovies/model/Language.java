package com.learnwithme.buildapps.popularmovies.model;

public enum Language {
    LANGUAGE_EN("en"), LANGUAGE_HI("hi");

    private String value;

    Language(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}