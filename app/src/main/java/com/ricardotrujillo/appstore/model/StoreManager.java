package com.ricardotrujillo.appstore.model;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.google.gson.Gson;
import com.ricardotrujillo.appstore.App;
import com.ricardotrujillo.appstore.viewmodel.Constants;
import com.ricardotrujillo.appstore.viewmodel.event.FetchedStoreDataEvent;
import com.ricardotrujillo.appstore.viewmodel.worker.BusWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.DbWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.LogWorker;

import java.util.HashMap;

import javax.inject.Inject;

public class StoreManager {

    App app;

    HashMap<String, ColorDrawable> colorDrawables = new HashMap<>();
    @Inject
    DbWorker dbWorker;
    @Inject
    BusWorker busWorker;
    @Inject
    LogWorker logWorker;
    private Store store;
    private Drawable[] drawables;
    private String filter;
    private int position;

    @Inject
    public StoreManager(App app) {

        this.app = app;

        inject();
    }

    void inject() {

        app.getAppComponent().inject(this);
    }

    public void addStore(Store store) {

        this.store = store;

        drawables = new Drawable[store.feed.entry.size()];

        for (int i = 0; i < store.feed.entry.size(); i++) {

            store.feed.entry.get(i).name.entryLabel = store.feed.entry.get(i).name.label;
            store.feed.entry.get(i).name.label = (i + 1) + ". " + store.feed.entry.get(i).name.label;

            store.feed.entry.get(i).summary.label = store.feed.entry.get(i).summary.label.length() < 400 ?
                    store.feed.entry.get(i).summary.label : store.feed.entry.get(i).summary.label.substring(0, 400) + "...";
        }
    }

    public Store getStore() {

        return store;
    }

    public String getFilter() {

        return filter;
    }

    public void setFilter(String filter) {

        this.filter = filter;
    }

    public void addColorDrawable(int position, ColorDrawable colorDrawable) {

        colorDrawables.put(store.feed.entry.get(position).name.label, colorDrawable);
    }

    public ColorDrawable getColorDrawable(String name) {

        return colorDrawables.get(name);
    }

    public int getDrawablesSize() {

        return drawables.length;
    }

    public int getPosition() {

        return position;
    }

    public void setPosition(int position) {

        this.position = position;
    }

    public void nullStore() {

        store = null;
    }

    public void initStore(String result, boolean online) {

        logWorker.log("initStore 1");

        if (getStore() == null) {

            logWorker.log("initStore 2 online: " + online);

            Store store = new Gson().fromJson(result.replace(Constants.STRING_TO_ERASE, Constants.NEW_STRING), Store.class);

            store.feed.fillOriginalEntry(store.feed.entry);

            if (online) dbWorker.saveObject(app.getApplicationContext(), store);

            addStore(store);

            busWorker.getBus().post(new FetchedStoreDataEvent()); //passed position
        }
    }
}
