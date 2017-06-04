package com.learnwithme.buildapps.popularmovies.ui.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.learnwithme.buildapps.popularmovies.R;
import com.learnwithme.buildapps.popularmovies.ui.fragment.MovieGridFragment;

public class MovieActivity extends AppCompatActivity {
    private static final String MOVIES_GRID_FRAGMENT = "MOVIES_GRID_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        if(savedInstanceState != null) {
            return;
        } else {
            addGridIntoFragment();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_settings) {
            Intent settingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(settingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addGridIntoFragment() {
        if(!isFinishing()) {
            MovieGridFragment mMovieGridFragment = MovieGridFragment.newInstance();

            FragmentManager mFragmentManager = getSupportFragmentManager();
            mFragmentManager
                    .beginTransaction()
                    .add(R.id.movie_grid_container, mMovieGridFragment, MOVIES_GRID_FRAGMENT)
                    .commit();
        }
    }
}