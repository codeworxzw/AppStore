package com.ricardotrujillo.appstore.model;

import android.app.Activity;
import android.databinding.BindingAdapter;
import android.widget.ImageView;

/**
 * Created by Ricardo on 16/03/2016.
 */
public class EntryViewModel {

    static Activity activity;

    public EntryViewModel(Activity act) {

        activity = act;
    }

    @BindingAdapter({"bind:imageUrl"})
    public static void loadImage(ImageView view, String imageUrl) {

        //Picasso.with(view.getContext())
        //        .load(imageUrl)
        //        .networkPolicy(
        //                NetWorker.isConnected(activity) ?
        //                        NetworkPolicy.NO_CACHE : NetworkPolicy.OFFLINE)
        //        .noFade()
        //        .into(view);
    }
}