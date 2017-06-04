package com.learnwithme.buildapps.popularmovies.ui.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.learnwithme.buildapps.popularmovies.R;
import com.learnwithme.buildapps.popularmovies.api.APIManager;
import com.learnwithme.buildapps.popularmovies.api.APIRequester;
import com.learnwithme.buildapps.popularmovies.app.GlobalAppState;
import com.learnwithme.buildapps.popularmovies.data.MovieContract;
import com.learnwithme.buildapps.popularmovies.model.Language;
import com.learnwithme.buildapps.popularmovies.model.Movie;
import com.learnwithme.buildapps.popularmovies.model.MovieResponse;
import com.learnwithme.buildapps.popularmovies.ui.activity.MovieActivity;
import com.learnwithme.buildapps.popularmovies.ui.activity.SettingsActivity;
import com.learnwithme.buildapps.popularmovies.ui.adapter.GridMoviesAdapter;
import com.learnwithme.buildapps.popularmovies.utils.ConnectionUtils;
import com.learnwithme.buildapps.popularmovies.utils.Constants;
import com.learnwithme.buildapps.popularmovies.utils.ProgressBarUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.learnwithme.buildapps.popularmovies.ui.activity.MovieDetailActivity.MOVIES_DETAILS_FRAGMENT;
import static com.learnwithme.buildapps.popularmovies.utils.Constants.MOVIE_DETAIL;

/**
 * Created by Nithin on 31/05/2017.
 */

public class MovieGridFragment extends Fragment implements View.OnClickListener {
    public static final String SAVE_ALL_MOVIES_LIST = "ALL_MOVIES_LIST";
    public static final String SAVE_MOVIE_FILTER_SORT = "MOVIE_FILTER_SORT";

    // If FAV_MOVIE_COLUMNS changes, these must change.
    static final int COL_MOVIE_ROW_ID = 0;
    static final int COL_MOVIE_CONDITION_ID = 1;
    static final int COL_TITLE = 2;
    static final int COL_POSTER_IMAGE = 3;
    static final int COL_OVERVIEW = 4;
    static final int COL_AVERAGE_RATING = 5;
    static final int COL_RELEASE_DATE = 6;
    static final int COL_BACKDROP_IMAGE = 7;

    private static final String TAG = MovieGridFragment.class.getSimpleName();
    private static final String[] FAV_MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_IMAGE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_AVERAGE_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_BACKDROP_IMAGE
    };

    @BindView(R.id.movie_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.connectivity_retry_linear_layout)
    LinearLayout mConnectivityRetryLayout;
    @BindView(R.id.retry_button)
    Button retryButton;
    @BindString(R.string.top_rated_movies)
    String topRatedMovies;
    @BindString(R.string.most_popular_movies)
    String mMostPopularMovies;
    @BindString(R.string.my_favorite_movies)
    String mFavouriteMovies;
    @BindString(R.string.no_internet_connection)
    String mNoInternetConnection;
    @BindString(R.string.no_favourites)
    String mNoFavList;
    @BindString(R.string.unable_to_reach_server)
    String mServerError;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Movie> mMovieList;
    private APIManager mAPIManager;
    private ProgressBarUtil mProgressBar;
    private ActionBar mActionBar;
    private SharedPreferences mPrefs;
    private String mMovieFilterSort;
    private int mPage = 1;
    private MovieActivity mActivity;
    private boolean mTwoPane = false;
    private APIRequester mMoviesRequester = new APIRequester() {
        @Override
        public void onFailure(Throwable error) {
            if (!isAdded()) {
                return;
            }

            mProgressBar.hide();
            Log.v(TAG, "Failure : movies onFailure");

            mMovieList.clear();
            mAdapter.notifyDataSetChanged();
            ConnectionUtils.showSnackbar(mRecyclerView, mServerError);
            mConnectivityRetryLayout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSuccess(Object respObj) {
            if (!isAdded()) {
                return;
            }

            mProgressBar.hide();
            if (mConnectivityRetryLayout.getVisibility() == View.VISIBLE) {
                mConnectivityRetryLayout.setVisibility(View.GONE);
            }
            Log.v(TAG, "Success : movies data : " + new Gson().toJson(respObj).toString());
            MovieResponse response = (MovieResponse) respObj;

            if (response != null && response.getResults() != null && response.getResults().size() > 0) {
                List<Movie> movieList = response.getResults();
                for (Movie movie : movieList) {
                    mMovieList.add(movie);
                }
                mAdapter.notifyDataSetChanged();

                if (mTwoPane) {
                    Log.d(TAG, "Tablet mode as mTwoPane : " + mTwoPane);

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(MOVIE_DETAIL, mMovieList.get(0));

                    addDetailFragmentForTwoPane(bundle);
                }
            }
        }
    };

    public MovieGridFragment() {
    }

    public static MovieGridFragment newInstance() {
        MovieGridFragment fragment = new MovieGridFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Bundle arg = getArguments();
        }

        mActivity = (MovieActivity) getActivity();
        GlobalAppState app = ((GlobalAppState) mActivity.getApplication());
        mAPIManager = app.getAPIManager();

        mProgressBar = new ProgressBarUtil(mActivity);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);

        mActionBar = mActivity.getSupportActionBar();
        mActionBar.setTitle(mMostPopularMovies);

        if (savedInstanceState == null || !savedInstanceState.containsKey(SAVE_ALL_MOVIES_LIST) ||
                !savedInstanceState.containsKey(SAVE_MOVIE_FILTER_SORT)) {
            Log.d(TAG, "savedInstanceState is null");
            mMovieList = new ArrayList<Movie>();
            mMovieFilterSort = Constants.MOST_POPULAR;
        } else {
            mMovieList = savedInstanceState.getParcelableArrayList(SAVE_ALL_MOVIES_LIST);
            mMovieFilterSort = savedInstanceState.getString(SAVE_MOVIE_FILTER_SORT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(mActivity, 2);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // use a grid layout manager with two columns
            mLayoutManager = new GridLayoutManager(mActivity, 2);
        } else {
            // use a grid layout manager with three columns
            mLayoutManager = new GridLayoutManager(mActivity, 3);
        }
        mRecyclerView.setLayoutManager(mLayoutManager);

        if (getActivity().findViewById(R.id.detail_container) != null) {
            mTwoPane = true;
        }

        // specify an adapter
        mAdapter = new GridMoviesAdapter(mActivity, mMovieList, mTwoPane);
        mRecyclerView.setAdapter(mAdapter);

        retryButton.setOnClickListener(this);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    int visibleItemCount = recyclerView.getChildCount();
                    int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                    int pastVisibleItem =
                            ((GridLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                    if ((visibleItemCount + pastVisibleItem) >= totalItemCount) {
                        if (!mMovieFilterSort.equals(Constants.FAVORITE)) {

                            if (ConnectionUtils.isNetworkAvailable(mActivity)) {
                                mConnectivityRetryLayout.setVisibility(View.GONE);
                                mPage++;
                                fetchMovies(false);
                            } else {
                                if (mMovieList.isEmpty()) {
                                    ConnectionUtils.showSnackbar(mRecyclerView, mNoInternetConnection);
                                    mConnectivityRetryLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        } else {
                            ConnectionUtils.showSnackbar(mRecyclerView, mNoFavList);
                        }
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        String movieFilterSort =
                mPrefs.getString(getString(R.string.sort_by_key), getString(R.string.sort_by_default));
        setActionBarTitle(movieFilterSort);
        if (!mMovieFilterSort.equalsIgnoreCase(movieFilterSort) || mMovieList.isEmpty()) {
            mMovieFilterSort = movieFilterSort;
            fetchMovies(true);
        }
    }

    private void setActionBarTitle(String movieFilterSort) {
        if (movieFilterSort.equals(Constants.HIGHEST_RATED)) {
            mActionBar.setTitle(topRatedMovies);
        } else if (movieFilterSort.equals(Constants.MOST_POPULAR)) {
            mActionBar.setTitle(mMostPopularMovies);
        } else if (movieFilterSort.equals(Constants.FAVORITE)) {
            mActionBar.setTitle(mFavouriteMovies);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.retry_button:
                if (ConnectionUtils.isNetworkAvailable(mActivity)) {
                    mConnectivityRetryLayout.setVisibility(View.GONE);
                    fetchMovies(false);
                } else {
                    if (mMovieList.isEmpty()) {
                        ConnectionUtils.showSnackbar(mRecyclerView, mNoInternetConnection);
                        mConnectivityRetryLayout.setVisibility(View.VISIBLE);
                    }
                }
                break;
        }
    }

    private void fetchMovies(boolean refresh) {
        if (refresh) {
            mMovieList.clear();
            mAdapter.notifyDataSetChanged();
            mPage = 1;
        }
        if (mMovieFilterSort.equals(Constants.FAVORITE)) {
            new LoadFavoriteMoviesTask().execute();
        } else {
            mProgressBar.show();
            Log.v(TAG, "Calling : get top rated/popular movies api according to filter");
            mAPIManager.getMovies(
                    new WeakReference<APIRequester>(mMoviesRequester), mMovieFilterSort,
                    mPage, Language.LANGUAGE_EN.getValue(), TAG);
        }
    }

    public void addDetailFragmentForTwoPane(Bundle bundle) {
        MovieDetailFragment detailFragment = MovieDetailFragment.newInstance(bundle);
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(android.support.design.R.anim.abc_grow_fade_in_from_bottom,
                        android.support.design.R.anim.abc_shrink_fade_out_from_bottom)
                .replace(R.id.detail_container, detailFragment, MOVIES_DETAILS_FRAGMENT)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(mActivity, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMovieList != null && !mMovieList.isEmpty()) {
            outState.putParcelableArrayList(SAVE_ALL_MOVIES_LIST, mMovieList);
        }
        outState.putString(SAVE_MOVIE_FILTER_SORT, mMovieFilterSort);
    }

    private class LoadFavoriteMoviesTask extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            // Retrieve movie records from fav movie table
            mMovieList.clear();
            Uri favoriteMovieUri = MovieContract.MovieEntry.CONTENT_URI;
            Cursor favMovieCursor = mActivity.getContentResolver().query(
                    favoriteMovieUri,
                    FAV_MOVIE_COLUMNS,
                    null,
                    null,
                    null);

            if (favMovieCursor.moveToFirst()) {
                do {
                    Movie movie = new Movie(favMovieCursor.getInt(COL_MOVIE_CONDITION_ID),
                            favMovieCursor.getString(COL_TITLE),
                            favMovieCursor.getString(COL_POSTER_IMAGE),
                            favMovieCursor.getString(COL_OVERVIEW),
                            favMovieCursor.getDouble(COL_AVERAGE_RATING),
                            favMovieCursor.getString(COL_RELEASE_DATE),
                            favMovieCursor.getString(COL_BACKDROP_IMAGE)
                    );
                    mMovieList.add(movie);
                } while (favMovieCursor.moveToNext());
            }
            favMovieCursor.close();
            return mMovieList.size();
        }

        @Override
        protected void onPostExecute(Integer size) {
            super.onPostExecute(size);
            Log.d(TAG, "Favorite movie size : " + size);
            mAdapter.notifyDataSetChanged();
            if (size < 1) {
                ConnectionUtils.showSnackbar(mRecyclerView, getResources().getString(R.string.no_favorite_movie));
            }
        }
    }
}