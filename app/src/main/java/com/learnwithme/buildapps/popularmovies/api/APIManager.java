package com.learnwithme.buildapps.popularmovies.api;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.learnwithme.buildapps.popularmovies.BuildConfig;
import com.learnwithme.buildapps.popularmovies.model.MovieResponse;
import com.learnwithme.buildapps.popularmovies.model.ReviewResponse;
import com.learnwithme.buildapps.popularmovies.model.TrailerResponse;
import com.learnwithme.buildapps.popularmovies.utils.Constants;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class APIManager {
    private static final String TAG = APIManager.class.getSimpleName();
    private static APIManager mInstance;
    private Context mContext;
    private RequestQueue mRequestQueue;

    private APIManager(Context context) { mContext = context; }

    public static synchronized APIManager getInstance(Context context) {
        if(mInstance == null) {
            Log.d(TAG, "Global app instance created");
            mInstance = new APIManager(context);
        }
        return mInstance;
    }

    public void init() {
        mRequestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue() {
        if(mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return mRequestQueue;
    }

    /**
     * Add the request with tag to volley request queue
     */
    public <T> void addToRequestQueue(Request<T> request, String tag) {
        // set the default tag if tag is empty
        request.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        mRequestQueue.add(request);
    }


    public <T> void addToRequestQueue(Request<T> request) {
        request.setTag(TAG);
        mRequestQueue.add(request);
    }

    /**
     * Cancel any pending volley request associated with the {param requestTag}
     */
    public void cancelPendingRequests(String requestTag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(requestTag);
        }
    }

    /**
     * Cleanup & save anything that needs saving as app is going away.
     */
    public void terminate() {
        mRequestQueue.stop();
    }

    public void getMovies(final WeakReference<APIRequester> wRequester, String movieSortFilter,
                          int page, String language, String tag) {
        Log.v(TAG, "Api call : get movies");
        JSONObject obj = new JSONObject();
        Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.v(TAG, "Success : get movies returned a response");

                APIRequester req = null;
                if (wRequester != null) {
                    req = wRequester.get();
                }
                MovieResponse movieResponse = null;
                if (jsonObject != null && !TextUtils.isEmpty(jsonObject.toString())) {
                    Log.v(TAG, "Success : converting Json to Java Object via Gson");
                    movieResponse =
                            new Gson().fromJson(jsonObject.toString(), MovieResponse.class);
                }

                if (req != null) {
                    if (movieResponse != null) {
                        req.onSuccess(movieResponse);
                    }
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                APIRequester req = null;
                if (wRequester != null) {
                    req = wRequester.get();
                }
                if (req != null) {
                    req.onFailure(volleyError);
                }
            }
        };

        String toBeAppendedPath = null;
        if (movieSortFilter.equals(Constants.HIGHEST_RATED)) {
            toBeAppendedPath = "top_rated";
        } else if (movieSortFilter.equals(Constants.MOST_POPULAR)) {
            toBeAppendedPath = "popular";
        }

        Uri.Builder builder = Uri.parse(BuildConfig.BASE_URL).buildUpon();
        builder.appendPath("movie").
                appendPath(toBeAppendedPath).
                appendQueryParameter("page", String.valueOf(page)).
                appendQueryParameter("language", language).
                appendQueryParameter("api_key", BuildConfig.API_KEY);

        String url = builder.build().toString();

        JSONObjectRequest request = new JSONObjectRequest(Request.Method.GET,
                url, obj, responseListener, errorListener);
        addToRequestQueue(request, tag);
    }

    public void getVideoTrailers(final WeakReference<APIRequester> wRequester, int movieId, String language, String tag) {
        Log.v(TAG, "Api call : get video Trailers");
        JSONObject obj = new JSONObject();
        Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.v(TAG, "Success : get video trailers returned a response");

                APIRequester req = null;
                if (wRequester != null) {
                    req = wRequester.get();
                }
                TrailerResponse trailerResponse = null;
                if (jsonObject != null && !TextUtils.isEmpty(jsonObject.toString())) {
                    Log.v(TAG, "Success : converting Json to Java Object via Gson");
                    trailerResponse =
                            new Gson().fromJson(jsonObject.toString(), TrailerResponse.class);
                }

                if (req != null) {
                    if (trailerResponse != null) {
                        req.onSuccess(trailerResponse);
                    }
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                APIRequester req = null;
                if (wRequester != null) {
                    req = wRequester.get();
                }
                if (req != null) {
                    req.onFailure(volleyError);
                }
            }
        };


        Uri.Builder builder = Uri.parse(BuildConfig.BASE_URL).buildUpon();
        builder.appendPath("movie")
                .appendPath(String.valueOf(movieId))
                .appendPath("videos")
                .appendQueryParameter("language", language)
                .appendQueryParameter("api_key", BuildConfig.API_KEY);

        String url = builder.build().toString();

        JSONObjectRequest request = new JSONObjectRequest(Request.Method.GET,
                url, obj, responseListener, errorListener);
        addToRequestQueue(request, tag);
    }

    public void getMovieReviews(final WeakReference<APIRequester> wRequester, int movieId, String language, String tag) {
        Log.v(TAG, "Api call : get Movie Reviews");
        JSONObject obj = new JSONObject();
        Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.v(TAG, "Success : get movie reviews returned a response");

                APIRequester req = null;
                if (wRequester != null) {
                    req = wRequester.get();
                }
                ReviewResponse reviewResponse = null;
                if (jsonObject != null && !TextUtils.isEmpty(jsonObject.toString())) {
                    Log.v(TAG, "Success : converting Json to Java Object via Gson");
                    reviewResponse =
                            new Gson().fromJson(jsonObject.toString(), ReviewResponse.class);
                }

                if (req != null) {
                    if (reviewResponse != null) {
                        req.onSuccess(reviewResponse);
                    }
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                APIRequester req = null;
                if (wRequester != null) {
                    req = wRequester.get();
                }
                if (req != null) {
                    req.onFailure(volleyError);
                }
            }
        };

        Uri.Builder builder = Uri.parse(BuildConfig.BASE_URL).buildUpon();
        builder.appendPath("movie").
                appendPath(String.valueOf(movieId)).
                appendPath("reviews").
                appendQueryParameter("language", language).
                appendQueryParameter("api_key", BuildConfig.API_KEY);

        String url = builder.build().toString();

        JSONObjectRequest request = new JSONObjectRequest(Request.Method.GET,
                url, obj, responseListener, errorListener);
        addToRequestQueue(request, tag);
    }
}