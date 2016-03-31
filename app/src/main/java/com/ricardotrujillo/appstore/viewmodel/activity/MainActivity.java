package com.ricardotrujillo.appstore.viewmodel.activity;

import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.ricardotrujillo.appstore.App;
import com.ricardotrujillo.appstore.R;
import com.ricardotrujillo.appstore.databinding.ActivityMainBinding;
import com.ricardotrujillo.appstore.model.Store;
import com.ricardotrujillo.appstore.model.StoreManager;
import com.ricardotrujillo.appstore.viewmodel.Constants;
import com.ricardotrujillo.appstore.viewmodel.comparator.IgnoreCaseComparator;
import com.ricardotrujillo.appstore.viewmodel.event.Events;
import com.ricardotrujillo.appstore.viewmodel.event.RecyclerCellEvent;
import com.ricardotrujillo.appstore.viewmodel.worker.AnimWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.BusWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.DbWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.LogWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.MeasurementsWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.NetWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.RxBusWorker;

import java.util.ArrayList;

import javax.inject.Inject;

import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    @Inject
    NetWorker netWorker;
    @Inject
    LogWorker logWorker;
    @Inject
    DbWorker dbWorker;
    @Inject
    StoreManager storeManager;
    @Inject
    MeasurementsWorker measurementsWorker;
    @Inject
    BusWorker busWorker;
    @Inject
    AnimWorker animWorker;
    @Inject
    RxBusWorker rxBusWorker;

    ActivityMainBinding binding;

    ArrayAdapter<String> adapter;

    ActionBarDrawerToggle drawerToggle;

    ArrayList<String> categories = new ArrayList<>();

    CompositeSubscription rxSubscriptions;

    boolean clickedOnItem = false;
    boolean dismissedSplash = false;

    Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        inject();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        measurementsWorker.setScreenHeight(this);

        setOrientation();

        initTransition();

        addDrawerItems();

        shouldShowSplash();

        //busWorker.getBus().post(new RequestStoreEvent());

        rxBusWorker.send(new Events.RequestStoreEvent(Constants.MAIN_ACTIVITY));
    }

    @Override
    public void onResume() {
        super.onResume();

        busWorker.register(this);

        setUpRxObservers();

        initCategoriesList();

        checkForNetwork();

        setOrientation();
    }

    @Override
    public void onPause() {
        super.onPause();

        busWorker.unRegister(this);

        rxSubscriptions.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);

        final MenuItem item = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(this);

        return true;
    }

    void checkForNetwork() {

        rxBusWorker.send(new Events.ConnectivityStatusRequest(Constants.MAIN_ACTIVITY));
    }

    void setOrientation() {

        if (measurementsWorker.setScreenOrientation(this)) {

            setupToolBar(true);

            setUpDrawer();

        } else {

            setupToolBar(false);
        }
    }

    void setupToolBar(boolean portrait) {

        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() != null) {

            if (storeManager.getFilter() != null) {

                getSupportActionBar().setTitle(storeManager.getFilter() + " " + getString(R.string.apps));

            } else {

                getSupportActionBar().setTitle(getString(R.string.app_name));
            }
        }

        if (portrait) {

            if (getSupportActionBar() != null) {

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
            }
        }
    }

    void initTransition() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            getWindow().setSharedElementExitTransition(TransitionInflater.from(this).inflateTransition(R.transition.entry_transition));
        }
    }

    void setUpRxObservers() {

        rxSubscriptions = new CompositeSubscription();

        ConnectableObservable<Object> tapEventEmitter = rxBusWorker.toObserverable().publish();

        rxSubscriptions.add(tapEventEmitter.subscribe(new Action1<Object>() {
            @Override
            public void call(Object event) {

                if (event instanceof Events.RxFetchedStoreDataEvent) {

                    logWorker.log("RxFetchedStoreDataEvent MainActivity");

                    initCategories();

                } else if (event instanceof Events.ConnectivityStatusResponse) {

                    Events.ConnectivityStatusResponse e = (Events.ConnectivityStatusResponse) event;

                    if (e.getClassType() == Constants.MAIN_ACTIVITY) {

                        logWorker.log("ConnectivityStatusResponse MainActivity");

                        showSnackBar((Events.ConnectivityStatusResponse) event);
                    }
                }
            }
        }));

        rxSubscriptions.add(tapEventEmitter.connect());
    }

    void initCategories() {

        initCategoriesList();

        if (!dismissedSplash) {

            dismissedSplash = true;

            dismissShowSplash();
        }
    }

    void showSnackBar(Events.ConnectivityStatusResponse e) {

        if (!e.isConnected()) {

            snackbar = Snackbar
                    .make(binding.getRoot(), getString(R.string.no_connectivity), Snackbar.LENGTH_INDEFINITE);

            snackbar.show();

        } else {

            if (snackbar != null) snackbar.dismiss();
        }
    }

    void dismissShowSplash() {

        animWorker.dismissSplash(binding.splashRootRelative);
    }

    void shouldShowSplash() {

        if (storeManager.getStore() != null) {

            binding.splashRootRelative.setVisibility(View.GONE);
        }
    }

    void initCategoriesList() {

        if (categories.size() == 0 && storeManager.getStore() != null) {

            for (Store.Feed.Entry entry : storeManager.getStore().feed.originalEntry) {

                if (!categories.contains(entry.category.attributes.label))
                    categories.add(entry.category.attributes.label);
            }

            IgnoreCaseComparator icc = new IgnoreCaseComparator();

            java.util.Collections.sort(categories, icc);

            categories.add(0, getString(R.string.all_apps));

            adapter.notifyDataSetChanged();

        } else {

            adapter.notifyDataSetChanged();//
        }
    }

    private void addDrawerItems() {

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);

        binding.navList.setAdapter(adapter);

        binding.navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (binding.drawerLayout != null)
                    binding.drawerLayout.closeDrawer(GravityCompat.START);

                storeManager.setFilter(categories.get(position));

                busWorker.getBus().post(new RecyclerCellEvent(categories.get(position), getString(R.string.category)));

                if (!categories.get(position).equals(getString(R.string.all_apps))) {

                    clickedOnItem = true;

                    if (getSupportActionBar() != null)
                        getSupportActionBar().setTitle(categories.get(position) + " " + getString(R.string.apps));

                } else {

                    clickedOnItem = false;

                    if (getSupportActionBar() != null)
                        getSupportActionBar().setTitle(getString(R.string.app_name));
                }
            }
        });
    }

    private void setUpDrawer() {

        drawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(getString(R.string.categories));

                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                if (getSupportActionBar() != null) {

                    getSupportActionBar().setTitle(clickedOnItem ? storeManager.getFilter() + " " + getString(R.string.apps) : getString(R.string.app_name));
                }

                invalidateOptionsMenu();
            }
        };

        drawerToggle.setDrawerIndicatorEnabled(true);

        if (binding.drawerLayout != null) binding.drawerLayout.addDrawerListener(drawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (binding.drawerLayout != null) drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void inject() {

        ((App) getApplication()).getAppComponent().inject(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(getString(R.string.app_name));

        busWorker.getBus().post(new RecyclerCellEvent(newText, getString(R.string.name)));

        return false;
    }
}
