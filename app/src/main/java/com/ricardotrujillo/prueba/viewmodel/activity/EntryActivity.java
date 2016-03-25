package com.ricardotrujillo.prueba.viewmodel.activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.ricardotrujillo.prueba.App;
import com.ricardotrujillo.prueba.R;
import com.ricardotrujillo.prueba.databinding.ActivityEntryBinding;
import com.ricardotrujillo.prueba.model.Store;
import com.ricardotrujillo.prueba.model.StoreManager;
import com.ricardotrujillo.prueba.viewmodel.Constants;
import com.ricardotrujillo.prueba.viewmodel.event.FetchedStoreDataEvent;
import com.ricardotrujillo.prueba.viewmodel.event.RequestStoreEvent;
import com.ricardotrujillo.prueba.viewmodel.interfaces.CustomCallback;
import com.ricardotrujillo.prueba.viewmodel.worker.AnimWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.BusWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.LogWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.MeasurementsWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.NetWorker;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

public class EntryActivity extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener {

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;
    Store.Feed.Entry entry;
    ActivityEntryBinding binding;
    @Inject
    StoreManager storeManager;
    @Inject
    MeasurementsWorker measurementsWorker;
    @Inject
    LogWorker logWorker;
    @Inject
    BusWorker busWorker;
    @Inject
    AnimWorker animWorker;
    @Inject
    NetWorker netWorker;

    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;
    private boolean isAnimatingAvatar = false;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inject();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_entry);

        setupToolBar();

        animWorker.startAlphaAnimation(binding.textviewTitle, 0, View.INVISIBLE);

        initTransition();

        if (savedInstanceState == null) {

            if (getIntent().getExtras() != null) {

                position = getIntent().getExtras().getInt(Constants.POSITION);

                getEntry(position);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        busWorker.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        busWorker.unRegister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(Constants.POSITION, getIntent().getExtras().getInt(Constants.POSITION));
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {

            position = getIntent().getExtras().getInt(Constants.POSITION);

            getEntry(position);

            //if (entry != null && entry.paletteColor != 0) setUpBarColor(entry.paletteColor);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if (menuItem.getItemId() == android.R.id.home) {

            supportFinishAfterTransition();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {

        int maxScroll = appBarLayout.getTotalScrollRange();

        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);

        handleToolbarTitleVisibility(percentage);
    }

    void setupToolBar() {

        binding.appbar.addOnOffsetChangedListener(this);
        binding.toolbar.inflateMenu(R.menu.actions);

        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(null);
        }
    }

    private void handleToolbarTitleVisibility(float percentage) {

        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if (!mIsTheTitleVisible) {

                animWorker.startAlphaAnimation(binding.textviewTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);

                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {

                animWorker.startAlphaAnimation(binding.textviewTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);

                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {

        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {

            if (mIsTheTitleContainerVisible) {

                animWorker.startAlphaAnimation(binding.linearlayoutTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);

                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {

                animWorker.startAlphaAnimation(binding.linearlayoutTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);

                mIsTheTitleContainerVisible = true;
            }
        }
    }

    void setUpBarColor(int color) {

        binding.toolbar.setBackgroundColor(animWorker.alterColor(color, 0.9f));

        binding.framelayoutTitle.setBackgroundColor(animWorker.alterColor(color, 0.9f));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = getWindow();

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(animWorker.alterColor(color, 0.7f));
        }
    }

    void getEntry(int position) {

        if (storeManager.getStore() != null) {

            entry = storeManager.getStore().feed.entry.get(position);

            binding.setEntry(entry);
            binding.setHandlers(this);

            loadEntry(this, binding, entry);

            binding.ivFeedCenterThumbContainer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(final View v, MotionEvent event) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {

                        if (!isAnimatingAvatar) {

                            isAnimatingAvatar = true;

                            animWorker.animateAvatar(v, new Callback() {
                                @Override
                                public void onSuccess() {

                                    isAnimatingAvatar = false;
                                }

                                @Override
                                public void onError() {

                                }
                            });

                            return true;

                        }
                    }

                    return false;
                }
            });

        } else {

            busWorker.getBus().post(new RequestStoreEvent(position));
        }
    }

    @Subscribe
    public void recievedMessage(FetchedStoreDataEvent event) {

        getEntry(event.getPosition());
    }

    void initTransition() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.entry_transition));
        }
    }

    public void loadEntry(final Activity activity, final ActivityEntryBinding binding, Store.Feed.Entry entry) {

        netWorker.PicassoLoadInto(binding.ivFeedCenterThumb, entry.image[2].label, new CustomCallback() {
            @Override
            public void onSuccess() {

                final Bitmap bitmap = ((BitmapDrawable) binding.ivFeedCenterThumb.getDrawable()).getBitmap();

                Bitmap newBitmap = bitmap.copy(bitmap.getConfig(), bitmap.isMutable());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

                    binding.ivFeedCenter.setImageBitmap(animWorker.blur(activity, newBitmap, 7f));

                    binding.ivFeedCenter.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

                        @Override
                        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

                            v.removeOnLayoutChangeListener(this);

                            animWorker.enterReveal(binding.ivFeedCenter);
                        }
                    });

                } else {

                    binding.ivFeedCenter.setImageBitmap(newBitmap);
                }

                getPaletteColor(newBitmap);
            }

            @Override
            public void onError() {

            }
        });
    }

    void getPaletteColor(Bitmap myBitmap) {

        if (myBitmap != null && !myBitmap.isRecycled()) {

            Palette.from(myBitmap).generate(new Palette.PaletteAsyncListener() {

                public void onGenerated(Palette palette) {

                    setUpBarColor(animWorker.getDarkColorDrawable(palette).getColor());
                }
            });
        }
    }

    void inject() {

        ((App) getApplication()).getAppComponent().inject(this);
    }
}