package com.learnwithme.buildapps.popularmovies.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.learnwithme.buildapps.popularmovies.R;
import com.learnwithme.buildapps.popularmovies.model.Movie;
import com.learnwithme.buildapps.popularmovies.ui.activity.MovieActivity;
import com.learnwithme.buildapps.popularmovies.ui.activity.MovieDetailActivity;
import com.learnwithme.buildapps.popularmovies.ui.fragment.MovieDetailFragment;
import com.learnwithme.buildapps.popularmovies.utils.ImageUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.learnwithme.buildapps.popularmovies.ui.activity.MovieDetailActivity.MOVIES_DETAILS_FRAGMENT;
import static com.learnwithme.buildapps.popularmovies.utils.Constants.MOVIE_DETAIL;

/**
 * Created by Nithin on 15/05/2017.
 */

public class GridMoviesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static Context mContext;
    private static List<Movie> mMoviesList;
    private int LAST_ANIMATED_ITEM_POSITION = -1;
    private boolean mTwoPane;

    public GridMoviesAdapter(Context context, List<Movie> movieList, boolean twoPane) {
        mContext = context;
        mMoviesList = movieList;
        mTwoPane = twoPane;
    }

    public GridMoviesAdapter(Context context, List<Movie> movieList) {
        mContext = context;
        mMoviesList = movieList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_movie_item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MovieViewHolder cusHolder = (MovieViewHolder) holder;
        String completePosterPath = mMoviesList.get(position).getPosterPath();
        ImageUtils
                .with(mContext)
                .load(completePosterPath)
                .placeholder(R.mipmap.placeholder)
                .error(R.mipmap.placeholder)
                .into(cusHolder.mIVThumbNail);
        cusHolder.mIVThumbNail.setVisibility(View.VISIBLE);
        setAnimation(cusHolder.mCardView, position);
    }

    @Override
    public int getItemCount() {
        return mMoviesList.size();
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > LAST_ANIMATED_ITEM_POSITION) {
            //Animation using xml
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.translate_left);
            viewToAnimate.startAnimation(animation);
            LAST_ANIMATED_ITEM_POSITION = position;
        }
    }

    /**
     * In order to avoid the view being reused, the animation is being cleared when the view detaches.
     */
    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        ((MovieViewHolder) holder).mCardView.clearAnimation();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        @BindView(R.id.card_view)
        CardView mCardView;
        @BindView(R.id.iv_thumbnail)
        ImageView mIVThumbNail;

        public MovieViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.mIVThumbNail.setOnClickListener(this);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int itemPosition = getAdapterPosition();
            if (itemPosition != RecyclerView.NO_POSITION) {
                Movie movie = mMoviesList.get(itemPosition);

                switch (view.getId()) {
                    case R.id.iv_thumbnail:
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(MOVIE_DETAIL, movie);

                        if (mTwoPane) {
                            addDetailFragmentForTwoPane(bundle);
                        } else {
                            Intent movieDetailIntent = new Intent(mContext, MovieDetailActivity.class);
                            movieDetailIntent.putExtras(bundle);
                            mContext.startActivity(movieDetailIntent);
                        }
                        break;

                    default:
                        Toast.makeText(mContext, "You clicked on : " +
                                movie.getTitle() + " movie", Toast.LENGTH_SHORT).show();
                }
            }
        }

        public void addDetailFragmentForTwoPane(Bundle bundle) {
            MovieDetailFragment detailFragment = MovieDetailFragment.newInstance(bundle);
            FragmentManager fragmentManager = ((MovieActivity) mContext).getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.support.design.R.anim.abc_grow_fade_in_from_bottom,
                            android.support.design.R.anim.abc_shrink_fade_out_from_bottom)
                    .replace(R.id.detail_container, detailFragment, MOVIES_DETAILS_FRAGMENT)
                    .commit();
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            return true;
        }
    }
}