package com.ricardotrujillo.prueba.activities;

import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionInflater;

import com.ricardotrujillo.prueba.App;
import com.ricardotrujillo.prueba.Constants;
import com.ricardotrujillo.prueba.R;
import com.ricardotrujillo.prueba.databinding.ActivityEntryBinding;
import com.ricardotrujillo.prueba.model.Store;
import com.ricardotrujillo.prueba.model.StoreManager;
import com.ricardotrujillo.prueba.workers.NetWorker;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

public class EntryActivity extends AppCompatActivity {

    @Inject
    StoreManager storeManager;

    ActivityEntryBinding binding;

    Store.Feed.Entry entry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_entry);

        inject();

        initTransition();

        if (savedInstanceState == null) {

            if (getIntent().getExtras() != null) {

                entry = storeManager.getStore().feed.entry[getIntent().getExtras().getInt(Constants.POSITION)];

                loadEntry();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(Constants.POSITION, getIntent().getExtras().getInt(Constants.POSITION));
        savedInstanceState.putString("MyString", "Welcome back to Android");
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        entry = storeManager.getStore().feed.entry[savedInstanceState.getInt(Constants.POSITION)];

        loadEntry();
    }

    @Override
    public void onBackPressed() {

        supportFinishAfterTransition();
    }

    void initTransition() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.entry_transition));
        }
    }

    void loadEntry() {

        Picasso.with(this)
                .load(entry.image[2].label)
                .networkPolicy(
                        NetWorker.isConnected(this) ?
                                NetworkPolicy.NO_CACHE : NetworkPolicy.OFFLINE)
                .noFade()
                .placeholder(R.drawable.img_feed_center_1)
                .into(binding.ivFeedCenter);
    }

    void inject() {

        ((App) getApplication()).getAppComponent().inject(this);
    }
}
