package com.ricardotrujillo.appstore.viewmodel.worker;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ricardotrujillo.appstore.App;
import com.ricardotrujillo.appstore.R;
import com.ricardotrujillo.appstore.viewmodel.Constants;
import com.ricardotrujillo.appstore.viewmodel.event.ConnectivityStatusRequest;
import com.ricardotrujillo.appstore.viewmodel.event.ConnectivityStatusResponse;
import com.ricardotrujillo.appstore.viewmodel.interfaces.CustomCallback;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.inject.Inject;

public class NetWorker {

    App app;
    @Inject
    BusWorker busWorker;
    private RequestQueue queue;

    @Inject
    public NetWorker(App app) {

        this.app = app;

        inject();

        busWorker.register(this);
    }

    public static boolean isConnected(Context activity) {

        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    void inject() {

        app.getAppComponent().inject(this);
    }

    @Subscribe
    public void recievedMessage(final ConnectivityStatusRequest event) {

        isNetworkAvailable(app, new NetWorker.ConnectionStatusListener() {

            @Override
            public void onResult(boolean status) {

                busWorker.post(new ConnectivityStatusResponse(status));
            }
        });
    }

    void isNetworkAvailable(Context context, final ConnectionStatusListener listener) {

        new CheckConnectivity(context, new ConnectionStatusListener() {

            @Override
            public void onResult(boolean connected) {

                listener.onResult(connected);
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void get(final Context context, final String url, final Listener listener) {

        queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {

                listener.onDataRetrieved(res);

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("Test", "onErrorResponse " + error.toString());

                get(context, url, listener);
            }
        });

        RetryPolicy policy = new DefaultRetryPolicy(Constants.SOCKET_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        stringRequest.setRetryPolicy(policy);
        stringRequest.setTag(Constants.TAG);

        queue.add(stringRequest);
    }

    public void cancelAll() {

        if (queue != null) queue.cancelAll(Constants.TAG);
    }

    public void GlideloadImageInto(ImageView view, String url, final CustomCallback callback) {

        Glide.with(app)
                .load(url)
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.img_feed_center_1)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new BitmapImageViewTarget(view) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        super.onResourceReady(bitmap, anim);

                        callback.onSuccess();
                    }
                });

    }

    public void PicassoLoadInto(ImageView view, String url, final CustomCallback callback) {

        Picasso.with(app)
                .load(url)
                .networkPolicy(
                        NetWorker.isConnected(app) ?
                                NetworkPolicy.NO_CACHE : NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.img_feed_center_1)
                .noFade()
                .into(view, new Callback() {
                    @Override
                    public void onSuccess() {

                        callback.onSuccess();
                    }

                    @Override
                    public void onError() {

                        callback.onSuccess();
                    }
                });
    }

    public interface ConnectionStatusListener {

        void onResult(boolean connected);
    }

    public interface Listener {

        void onDataRetrieved(String result);
    }

    private class CheckConnectivity extends AsyncTask<String, Boolean, Boolean> {

        Context activity;
        ConnectionStatusListener listener;

        public CheckConnectivity(Context activity, ConnectionStatusListener listener) {

            this.activity = activity;
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(String... uri) {

            if (isNetworkAvailable()) {

                try {

                    HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                    urlc.setRequestProperty("User-Agent", "Test");
                    urlc.setRequestProperty("Connection", "close");
                    urlc.setConnectTimeout(3000);
                    urlc.connect();

                    return (urlc.getResponseCode() == 200);

                } catch (IOException e) {

                }

            } else {

                return false;
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean connected) {
            super.onPostExecute(connected);

            listener.onResult(connected);
        }

        private boolean isNetworkAvailable() {

            ConnectivityManager connectivityManager = (ConnectivityManager) activity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

            return activeNetworkInfo != null;
        }
    }
}
