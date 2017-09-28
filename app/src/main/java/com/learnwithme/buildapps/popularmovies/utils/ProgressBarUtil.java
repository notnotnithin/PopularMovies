package com.learnwithme.buildapps.popularmovies.utils;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class ProgressBarUtil {
    private final RelativeLayout mRelativeLayout;

    public ProgressBarUtil(Context context) {
        ViewGroup layout = (ViewGroup) ((Activity) context).findViewById(android.R.id.content).getRootView();

        ProgressBar mProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyle);
        mProgressBar.setIndeterminate(true);

        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);

        mRelativeLayout = new RelativeLayout(context);

        mRelativeLayout.setGravity(Gravity.CENTER);
        mRelativeLayout.addView(mProgressBar);
        layout.addView(mRelativeLayout, params);

        hide();
    }

    public void show() {
        mRelativeLayout.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mRelativeLayout.setVisibility(View.GONE);
    }
}