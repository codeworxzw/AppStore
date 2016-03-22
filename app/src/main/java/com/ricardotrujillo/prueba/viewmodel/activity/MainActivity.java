package com.ricardotrujillo.prueba.viewmodel.activity;

import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.gson.Gson;
import com.ricardotrujillo.prueba.App;
import com.ricardotrujillo.prueba.R;
import com.ricardotrujillo.prueba.databinding.ActivityMainBinding;
import com.ricardotrujillo.prueba.model.Store;
import com.ricardotrujillo.prueba.model.StoreManager;
import com.ricardotrujillo.prueba.viewmodel.Constants;
import com.ricardotrujillo.prueba.viewmodel.comparator.IgnoreCaseComparator;
import com.ricardotrujillo.prueba.viewmodel.event.FetchedStoreDataEvent;
import com.ricardotrujillo.prueba.viewmodel.event.RecyclerCellEvent;
import com.ricardotrujillo.prueba.viewmodel.worker.BusWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.DbWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.LogWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.MeasurementsWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.NetWorker;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {

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

    ActivityMainBinding binding;

    ArrayAdapter<String> adapter;

    ActionBarDrawerToggle drawerToggle;

    ArrayList<String> categories = new ArrayList<>();

    boolean clickedOnItem = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inject();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        measurementsWorker.setScreenHeight(this);

        initTransition();

        addDrawerItems();

        if (measurementsWorker.setScreenOrientation(this)) {

            setupToolBar(true);

            setupDrawer();

        } else {

            setupToolBar(false);
        }

        checkForLoadedData();
    }

    @Override
    public void onResume() {
        super.onResume();

        busWorker.register(this);

        initCategoriesList();
    }


    void setupToolBar(boolean portrait) {

        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(getString(R.string.app_name));

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

    void checkForLoadedData() {

        if (storeManager.getStore() == null) {

            checkForConnectivity();

        } else {

            busWorker.getBus().post(new FetchedStoreDataEvent());
        }
    }

    void checkForConnectivity() {

        netWorker.isNetworkAvailable(this, new NetWorker.ConnectionStatusListener() {

            @Override
            public void onResult(boolean connected) {

                if (connected) {

                    getData(Constants.URL);

                } else {

                    getSavedData();
                }
            }
        });
    }

    void getSavedData() {

        Store store = (Store) dbWorker.getObject(this);

        if (store != null) {

            storeManager.addStore(store);

            busWorker.getBus().post(new FetchedStoreDataEvent());
        }
    }

    void getData(String url) {

        netWorker.get(this, url, new NetWorker.Listener() {

            @Override
            public void onDataRetrieved(String result) {

                Store store = new Gson().fromJson(result.replace(Constants.STRING_TO_ERASE, Constants.NEW_STRING), Store.class);

                store.feed.fillOriginalEntry(store.feed.entry);

                storeManager.addStore(store);

                dbWorker.saveObject(MainActivity.this, store);

                busWorker.getBus().post(new FetchedStoreDataEvent());
            }
        });
    }

    @Subscribe
    public void recievedMessage(FetchedStoreDataEvent event) {

        initCategoriesList();
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

            adapter.notifyDataSetChanged();
        }
    }

    private void addDrawerItems() {

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, categories);

        binding.navList.setAdapter(adapter);

        binding.navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (binding.drawerLayout != null)
                    binding.drawerLayout.closeDrawer(GravityCompat.START);

                busWorker.getBus().post(new RecyclerCellEvent(categories.get(position)));

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

    private void setupDrawer() {

        drawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerOpened(View drawerView) {

                super.onDrawerOpened(drawerView);

                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(getString(R.string.categories));

                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {

                super.onDrawerClosed(view);

                if (getSupportActionBar() != null && !clickedOnItem)
                    getSupportActionBar().setTitle(getString(R.string.app_name));

                invalidateOptionsMenu();
            }
        };

        drawerToggle.setDrawerIndicatorEnabled(true);

        binding.drawerLayout.addDrawerListener(drawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (drawerToggle != null) drawerToggle.syncState();
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
}
