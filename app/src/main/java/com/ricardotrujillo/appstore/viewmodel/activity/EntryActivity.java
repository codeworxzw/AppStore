package com.ricardotrujillo.appstore.viewmodel.activity;

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

import com.ricardotrujillo.appstore.App;
import com.ricardotrujillo.appstore.R;
import com.ricardotrujillo.appstore.databinding.ActivityEntryBinding;
import com.ricardotrujillo.appstore.model.Store;
import com.ricardotrujillo.appstore.model.StoreManager;
import com.ricardotrujillo.appstore.viewmodel.Constants;
import com.ricardotrujillo.appstore.viewmodel.event.Events;
import com.ricardotrujillo.appstore.viewmodel.interfaces.CustomCallback;
import com.ricardotrujillo.appstore.viewmodel.worker.AnimWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.BusWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.LogWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.MeasurementsWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.NetWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.RxBusWorker;
import com.squareup.picasso.Callback;

import javax.inject.Inject;

import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import rx.subscriptions.CompositeSubscription;

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
    @Inject
    RxBusWorker rxBusWorker;

    int position = -1;
    boolean shouldAnimateSharedView = true;
    CompositeSubscription rxSubscriptions;
    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;
    private boolean isAnimatingAvatar = false;
    private boolean revealedImage = false;

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

                getEntry();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        busWorker.register(this);

        setUpRxObservers();
    }

    @Override
    public void onPause() {
        super.onPause();

        busWorker.unRegister(this);

        rxSubscriptions.clear();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(Constants.POSITION, getIntent().getExtras().getInt(Constants.POSITION));
        savedInstanceState.putBoolean(Constants.REVEALED_IMAGE, revealedImage);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {

            position = savedInstanceState.getInt(Constants.POSITION);
            revealedImage = savedInstanceState.getBoolean(Constants.REVEALED_IMAGE);

            getEntry();
        }
    }

    @Override
    public void onBackPressed() {

        if (shouldAnimateSharedView) {

            supportFinishAfterTransition();

        } else {

            finish();
        }

        //super.onBackPressed();  // optional depending on your needs
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if (menuItem.getItemId() == android.R.id.home) {

            if (shouldAnimateSharedView) {

                supportFinishAfterTransition();

            } else {

                finish();
            }
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

        binding.toolbar.setBackgroundColor(color);

        binding.framelayoutTitle.setBackgroundColor(color);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = getWindow();

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(animWorker.alterColor(color, 0.8f));
        }
    }

    void getEntry() {

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

            binding.ivFeedCenterThumbContainer.setVisibility(View.INVISIBLE);

            binding.splashRootRelative.setVisibility(View.VISIBLE);

            shouldAnimateSharedView = false;

            rxBusWorker.send(new Events.RequestStoreEvent(Constants.ENTRY_ACTIVITY));
        }
    }

    void setUpRxObservers() {

        rxSubscriptions = new CompositeSubscription();

        ConnectableObservable<Object> tapEventEmitter = rxBusWorker.toObserverable().publish();

        rxSubscriptions.add(tapEventEmitter.subscribe(new Action1<Object>() {
            @Override
            public void call(Object event) {

                if (event instanceof Events.RxFetchedStoreDataEvent) {

                    logWorker.log("RxFetchedStoreDataEvent EntryActivity");

                    binding.ivFeedCenterThumbContainer.setVisibility(View.VISIBLE);

                    animWorker.exitReveal(binding.splashRootRelative);

                    getEntry();
                }
            }
        }));

        rxSubscriptions.add(tapEventEmitter.connect());
    }

    void initTransition() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.entry_transition));
        }
    }

    public void loadEntry(final Activity activity, final ActivityEntryBinding binding, final Store.Feed.Entry entry) {

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

                            if (!revealedImage) {

                                revealedImage = true;

                                animWorker.enterReveal(binding.ivFeedCenter);
                            }
                        }
                    });

                } else {

                    binding.ivFeedCenter.setImageBitmap(newBitmap);
                }

                if (position < 0) {

                    getPaletteColor(newBitmap);

                } else {

                    if (storeManager.getColorDrawable(entry.name.label) != null) {

                        setUpBarColor(storeManager.getColorDrawable(entry.name.label).getColor());

                    } else {

                        getPaletteColor(newBitmap);
                    }
                }
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