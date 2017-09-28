package com.learnwithme.buildapps.popularmovies.ui.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.learnwithme.buildapps.popularmovies.BuildConfig;
import com.learnwithme.buildapps.popularmovies.R;
import com.learnwithme.buildapps.popularmovies.api.APIManager;
import com.learnwithme.buildapps.popularmovies.api.APIRequester;
import com.learnwithme.buildapps.popularmovies.app.GlobalAppState;
import com.learnwithme.buildapps.popularmovies.data.FavouriteHelper;
import com.learnwithme.buildapps.popularmovies.listener.DBUpdationListener;
import com.learnwithme.buildapps.popularmovies.model.Language;
import com.learnwithme.buildapps.popularmovies.model.Movie;
import com.learnwithme.buildapps.popularmovies.model.Review;
import com.learnwithme.buildapps.popularmovies.model.ReviewResponse;
import com.learnwithme.buildapps.popularmovies.model.Trailer;
import com.learnwithme.buildapps.popularmovies.model.TrailerResponse;
import com.learnwithme.buildapps.popularmovies.ui.activity.MovieActivity;
import com.learnwithme.buildapps.popularmovies.utils.AlertDialogUtil;
import com.learnwithme.buildapps.popularmovies.utils.ConnectionUtils;
import com.learnwithme.buildapps.popularmovies.utils.ImageUtils;
import com.learnwithme.buildapps.popularmovies.utils.ManageFavMovies;
import com.learnwithme.buildapps.popularmovies.utils.ProgressBarUtil;
import com.squareup.picasso.Callback;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.learnwithme.buildapps.popularmovies.data.FavouriteHelper.ADD_TO_FAVORITE;
import static com.learnwithme.buildapps.popularmovies.utils.Constants.MOVIE_DETAIL;

public class MovieDetailFragment extends Fragment implements View.OnClickListener, DBUpdationListener {
    private static final String TAG = MovieActivity.class.getSimpleName();

    @BindView(R.id.coordinate_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolBar;
    @BindView(R.id.iv_backdrop)
    ImageView mIvBackDrop;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tv_release)
    TextView mTvReleaseDate;
    @BindView(R.id.tv_rating)
    TextView mTvRating;
    @BindView(R.id.tv_movie_overview)
    TextView mTvOverview;
    @BindView(R.id.iv_poster)
    ImageView mIvPoster;
    @BindView(R.id.fab_favorite)
    FloatingActionButton mButtonFavorite;
    @BindColor(R.color.colorPrimaryDark)
    int primaryDark;
    @BindView(R.id.fab_trailer)
    ImageView mButtonTrailer;
    @BindView(R.id.fab_share)
    FloatingActionButton mButtonShare;
    @BindView(R.id.review_layout0)
    CardView mReviewLayout0;
    @BindView(R.id.review_layout1)
    CardView mReviewLayout1;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindString(R.string.no_internet_connection)
    String noInternetConnection;
    @BindString(R.string.something_went_wrong)
    String somethingWentWrong;
    @BindString(R.string.movie_trailers_dialog_title)
    String movieTrailerDialogTitle;
    @BindString(R.string.no_internet_connection_to_show_reviews)
    String noInternetConnectionToShowReviews;

    private ManageFavMovies mManageFavMovies;
    private Movie mMovie;
    private ProgressBarUtil mProgressBar;
    private APIManager mAPIManager;
    private int mViewId;
    private Activity mActivity;
    private boolean mTwoPane;
    private ArrayList<Review> mMovieReviewList;
    private APIRequester mVideoTrailerRequester = new APIRequester() {

        @Override
        public void onFailure(Throwable error) {
            if (!isAdded()) {
                return;
            }

            mProgressBar.hide();
            Log.v(TAG, "Failure : video trailer onFailure");
            ConnectionUtils.showSnackbar(mCoordinatorLayout, noInternetConnection);
        }

        @Override
        public void onSuccess(Object respObj) {
            if (!isAdded()) {
                return;
            }

            Log.v(TAG, "Success : video trailer data : " + new Gson().toJson(respObj).toString());
            final TrailerResponse response = (TrailerResponse) respObj;

            if (response != null && response.getResults() != null && response.getResults().size() > 0) {
                final List<Trailer> videoTrailerList = response.getResults();

                int noOfTrailers = videoTrailerList.size();
                String[] trailerNames = new String[noOfTrailers];
                for (int i = 0; i < noOfTrailers; i++) {
                    trailerNames[i] = videoTrailerList.get(i).getName();
                }
                switch (mViewId) {
                    case R.id.fab_trailer:
                        mProgressBar.hide();
                        AlertDialogUtil.createSingleChoiceItemsAlert(mActivity, movieTrailerDialogTitle,
                                trailerNames, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        playVideoTrailer(videoTrailerList.get(which).getKey());
                                        dialog.dismiss();
                                    }
                                });
                        break;
                    case R.id.fab_share:
                        shareVideoTrailer(videoTrailerList.get(0).getKey());
                        break;

                }
            } else {
                mProgressBar.hide();
            }


        }
    };
    private APIRequester mMovieReviewRequester = new APIRequester() {

        @Override
        public void onFailure(Throwable error) {
            if (!isAdded()) {
                return;
            }

            mProgressBar.hide();
            Log.v(TAG, "Failure : movie Reviews onFailure");
            ConnectionUtils.showSnackbar(mCoordinatorLayout, noInternetConnectionToShowReviews);
        }

        @Override
        public void onSuccess(Object respObj) {
            if (!isAdded()) {
                return;
            }

            mProgressBar.hide();
            Log.v(TAG, "Success : movie reviews data : " + new Gson().toJson(respObj).toString());
            final ReviewResponse response = (ReviewResponse) respObj;


            if (response != null && response.getResults() != null && response.getResults().size() > 0) {
                mMovieReviewList = response.getResults();

                int noOfReviews = mMovieReviewList.size();

                if (noOfReviews >= 2) {
                    //We can fill two reviews
                    displayReviewLayout(0, mMovieReviewList.get(0));
                    displayReviewLayout(1, mMovieReviewList.get(1));
                } else {
                    //We can fill only one review
                    displayReviewLayout(0, mMovieReviewList.get(0));
                }
            }
        }
    };

    public MovieDetailFragment() {
        // Required empty public constructor
    }

    public static MovieDetailFragment newInstance(Bundle args) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Bundle arg = getArguments();
            mMovie = arg.getParcelable(MOVIE_DETAIL);

            Log.d(TAG, "Received movie from getArguments() :  " + mMovie.toString());
        }
        mActivity = getActivity();
        mProgressBar = new ProgressBarUtil(mActivity);

        AppCompatActivity activity = (AppCompatActivity) mActivity;
        // activity.setSupportActionBar(mToolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        GlobalAppState app = ((GlobalAppState) mActivity.getApplication());
        mAPIManager = app.getAPIManager();
        mManageFavMovies = ManageFavMovies.getInstance(mActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, view);

        if (getActivity().findViewById(R.id.detail_container) != null) {
            mTwoPane = true;
        }
        setStatusBarColor(primaryDark);
        mCollapsingToolBar.setTitle(mMovie.getOriginalTitle());
        mCollapsingToolBar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        mTvTitle.setText(mMovie.getOriginalTitle());

        String sourceDateStr = mMovie.getReleaseDate();
        SimpleDateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        Date sourceDate = null;
        try {
            sourceDate = sourceDateFormat.parse(sourceDateStr);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }

        SimpleDateFormat finalDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        String finalDateStr = finalDateFormat.format(sourceDate);

        mTvReleaseDate.setText(finalDateStr);
        mTvRating.setText(String.valueOf(mMovie.getVoteAverage()));
        mTvOverview.setText(mMovie.getOverview());

        ImageUtils.with(mActivity).load(mMovie.getPosterPath()).fit()
                .placeholder(R.mipmap.placeholder)
                .error(R.mipmap.placeholder)
                .into(mIvPoster);

        ImageUtils.with(mActivity).load(mMovie.getBackdropPath()).error(R.mipmap.placeholder).
            into(mIvBackDrop, new Callback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "applyPalette mTwoPane : " + mTwoPane);
                    if (!mTwoPane && isAdded()) {
                        Bitmap bitmap = ((BitmapDrawable) mIvBackDrop.getDrawable()).getBitmap();
                        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                            public void onGenerated(Palette palette) {
                                applyPalette(palette);
                            }
                        });
                    }
                }

                @Override
                public void onError() { }
            });

        mButtonFavorite.setOnClickListener(this);
        mButtonTrailer.setOnClickListener(this);
        mButtonShare.setOnClickListener(this);

        //If fav movie exists in db, fill the heart
        //otherwise, keep it empty
        if (mManageFavMovies.getBoolean(mMovie.getId())) {
            mButtonFavorite.setImageResource(R.mipmap.heart_filled);
        } else {
            mButtonFavorite.setImageResource(R.mipmap.heart_empty);
        }

        return view;
    }

    private void applyPalette(Palette palette) {
        int primaryDark = getResources().getColor(R.color.colorPrimaryDark);
        int primary = getResources().getColor(R.color.colorPrimary);
        mCollapsingToolBar.setContentScrimColor(palette.getMutedColor(primary));
        mCollapsingToolBar.setStatusBarScrimColor(palette.getDarkMutedColor(primaryDark));
        setStatusBarColor(palette.getDarkMutedColor(primaryDark));
    }

    private void setStatusBarColor(int darkMutedColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = mActivity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(darkMutedColor);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //If your Fragment is added to existing activity
        //you should call the SnackBar's method into the onActivityCreated()
        // method of the Fragment.
        if (ConnectionUtils.isNetworkAvailable(mActivity)) {
            mProgressBar.show();
            Log.v(TAG, "Calling : get movie reviews api");
            mAPIManager.getMovieReviews(
                    new WeakReference<APIRequester>(mMovieReviewRequester), mMovie.getId(),
                    Language.LANGUAGE_EN.getValue(), TAG);

        } else {
            ConnectionUtils.showSnackbar(mCoordinatorLayout, noInternetConnectionToShowReviews);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_favorite:
                //Update favorite movie database accordingly
                //If movie exists in fav db, delete it, Otherwise save it in db
                FavouriteHelper favouriteMovieDBTask = new FavouriteHelper(mActivity, mMovie, this);
                favouriteMovieDBTask.execute();
                break;

            case R.id.fab_trailer:
                mViewId = R.id.fab_trailer;
                if (ConnectionUtils.isNetworkAvailable(mActivity)) {
                    mProgressBar.show();
                    Log.v(TAG, "Calling : get video trailer api");
                    mAPIManager.getVideoTrailers(
                            new WeakReference<APIRequester>(mVideoTrailerRequester), mMovie.getId(),
                            Language.LANGUAGE_EN.getValue(), TAG);

                } else {
                    ConnectionUtils.showSnackbar(mCoordinatorLayout, noInternetConnection);
                }

                break;

            case R.id.fab_share:
                mViewId = R.id.fab_share;
                if (ConnectionUtils.isNetworkAvailable(mActivity)) {
                    mProgressBar.show();
                    Log.v(TAG, "Calling : get video trailer api to share the first trailer");
                    mAPIManager.getVideoTrailers(
                            new WeakReference<APIRequester>(mVideoTrailerRequester), mMovie.getId(),
                            Language.LANGUAGE_EN.getValue(), TAG);

                } else {
                    ConnectionUtils.showSnackbar(mCoordinatorLayout, noInternetConnection);
                }
                break;

        }
    }

    @Override
    public void onSuccess(final int operationType) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String operation;
                if (operationType == ADD_TO_FAVORITE) {
                    operation = "added to favorite";
                    mButtonFavorite.setImageResource(R.mipmap.heart_filled);
                    mManageFavMovies.putBoolean(mMovie.getId(), true);
                } else {
                    operation = "removed from favorite";
                    mButtonFavorite.setImageResource(R.mipmap.heart_empty);
                    mManageFavMovies.putBoolean(mMovie.getId(), false);
                }

                ConnectionUtils.showSnackbar(mCoordinatorLayout, mMovie.getTitle() + " " + operation);
            }
        });
    }

    @Override
    public void onFailure() {
        ConnectionUtils.showSnackbar(mCoordinatorLayout, mMovie.getTitle() + " " + somethingWentWrong);
    }

    private void playVideoTrailer(String key) {
        Uri videoUri = Uri.parse(BuildConfig.BASE_URL_VIDEO + key);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(videoUri);

        //We only start the activity if it resolves successfully
        if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't play video trailer for key: " + key);
        }
    }

    private void shareVideoTrailer(String key) {
        String videoExtraText = BuildConfig.BASE_URL_VIDEO + key;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        //This flag help you in returning to your app after any app handled the share intent
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intent.EXTRA_TEXT, videoExtraText);

        Intent shareIntent = Intent.createChooser(intent, "Share trailer via");


        // We only start the activity if it resolves successfully
        if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
            mProgressBar.hide();
            startActivity(shareIntent);
        } else {
            mProgressBar.hide();
            Log.d(TAG, "Couldn't share Video Trailer for key: " + key);
        }
    }

    private void displayReviewLayout(int position, Review movieReview) {
        CardView reviewLayout = null;
        if (position == 0) {
            reviewLayout = mReviewLayout0;
            reviewLayout.findViewById(R.id.tv_reviews_text).setVisibility(View.VISIBLE);
            reviewLayout.findViewById(R.id.line_reviews_heading).setVisibility(View.VISIBLE);
        } else if (position == 1) {
            reviewLayout = mReviewLayout1;
        }

        if (reviewLayout != null) {
            reviewLayout.setVisibility(View.VISIBLE);
            String author = movieReview.getAuthor();
            String content = movieReview.getContent();

            ((TextView) reviewLayout.findViewById(R.id.tv_review_author)).setText(author);
            ((TextView) reviewLayout.findViewById(R.id.tv_review_content)).setText(content);
        }
    }
}