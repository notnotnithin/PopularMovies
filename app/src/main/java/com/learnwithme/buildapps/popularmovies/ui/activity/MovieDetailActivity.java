package com.learnwithme.buildapps.popularmovies.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.learnwithme.buildapps.popularmovies.R;
import com.learnwithme.buildapps.popularmovies.ui.fragment.MovieDetailFragment;

public class MovieDetailActivity extends AppCompatActivity {
    public static final String MOVIES_DETAILS_FRAGMENT = "MOVIES_DETAILS_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        if (savedInstanceState != null) {
            return;
        } else {
            addDetailFragment();
        }
    }

    public void addDetailFragment() {
        if (!isFinishing()) {
            MovieDetailFragment detailFragment = MovieDetailFragment.newInstance(getIntent().getExtras());

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.detail_container, detailFragment, MOVIES_DETAILS_FRAGMENT)
                    .commit();
        }
    }
}