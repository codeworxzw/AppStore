package com.ricardotrujillo.appstore;

import android.app.Application;

import com.google.gson.Gson;
import com.ricardotrujillo.appstore.model.Store;
import com.ricardotrujillo.appstore.model.StoreManager;
import com.ricardotrujillo.appstore.viewmodel.Constants;
import com.ricardotrujillo.appstore.viewmodel.di.components.AppComponent;
import com.ricardotrujillo.appstore.viewmodel.di.components.DaggerAppComponent;
import com.ricardotrujillo.appstore.viewmodel.di.modules.AppModule;
import com.ricardotrujillo.appstore.viewmodel.di.modules.StoreModule;
import com.ricardotrujillo.appstore.viewmodel.di.modules.WorkersModule;
import com.ricardotrujillo.appstore.viewmodel.event.ConnectivityStatusRequest;
import com.ricardotrujillo.appstore.viewmodel.event.ConnectivityStatusResponse;
import com.ricardotrujillo.appstore.viewmodel.event.FetchedStoreDataEvent;
import com.ricardotrujillo.appstore.viewmodel.event.RequestStoreEvent;
import com.ricardotrujillo.appstore.viewmodel.worker.BusWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.DbWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.LogWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.NetWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.RxWorker;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Ricardo on 15/03/2016.
 */
public class App extends Application {

    @Inject
    NetWorker netWorker;
    @Inject
    DbWorker dbWorker;
    @Inject
    StoreManager storeManager;
    @Inject
    BusWorker busWorker;
    @Inject
    LogWorker logWorker;
    @Inject
    RxWorker rxWorker;

    Action1<String> appsAction;

    private AppComponent appComponent;

    @Inject
    public App() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        //appComponent = DaggerAppComponent.create();
        //appComponent.inject(this);

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .storeModule(new StoreModule())
                .workersModule(new WorkersModule())
                .build();

        getAppComponent().inject(this);

        busWorker.register(this);

        initObservables();

        initObservers();

        checkForLoadedData(0);

        rxWorker.subscribeToApps(appsAction);
    }

    void initObservables() {

        rxWorker.initStringObservable(Observable.just("Hello"));

        rxWorker.initIntegerObservable(Observable.from(new Integer[]{1, 2, 3, 4, 5, 6}) //rxWorker.initIntegerObservable(Observable.from(new Integer[]{1, 2, 3, 4, 5, 6}));
                .skip(0)
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return integer % 2 != 0;
                    }
                })
                .map(new Func1<Integer, Integer>() { // Input and Output are both Integer
                    @Override
                    public Integer call(Integer integer) {
                        return integer * integer;
                    }
                }));

        rxWorker.initAppsObservable(Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    netWorker.get(getApplicationContext(), Constants.PAID_URL, new NetWorker.Listener() {

                        @Override
                        public void onDataRetrieved(String result) {

                            subscriber.onNext(result); // Emit the contents of the FREE_URL
                            subscriber.onCompleted(); // Nothing more to emit
                        }
                    });
                } catch (Exception e) {
                    subscriber.onError(e); // In case there are network errors
                }
            }
        }));
    }

    void initObservers() {

        appsAction = new Action1<String>() {
            @Override
            public void call(String result) {

                logWorker.log("From App: " + String.valueOf(result.length()));

                Store store = new Gson().fromJson(result.replace(Constants.STRING_TO_ERASE, Constants.NEW_STRING), Store.class);

                store.feed.fillOriginalEntry(store.feed.entry);

                storeManager.addStore(store);

                dbWorker.saveObject(getApplicationContext(), store);

                busWorker.getBus().post(new FetchedStoreDataEvent()); //passed position
            }
        };
    }

    @Subscribe
    public void recievedMessage(RequestStoreEvent event) {

        checkForLoadedData(event.getPosition());
    }

    void checkForLoadedData(int position) {

        if (storeManager.getStore() == null) {

            busWorker.post(new ConnectivityStatusRequest(position));

        } else {

            busWorker.post(new FetchedStoreDataEvent(position));
        }
    }

    @Subscribe
    public void recievedMessage(ConnectivityStatusResponse e) {

        if (e.getPostion() >= 0) {

            if (e.isConnected()) {

                getData(e.getPostion(), Constants.FREE_URL);

            } else {

                getSavedData(e.getPostion());
            }
        }
    }

    void getSavedData(int position) {

        Store store = (Store) dbWorker.getObject(this);

        if (store != null) {

            storeManager.addStore(store);

            busWorker.getBus().post(new FetchedStoreDataEvent(position));
        }
    }

    void getData(final int position, String url) {

        rxWorker.fetchFreeApps();

        rxWorker.fetchPaidApps();

//        netWorker.get(this, url, new NetWorker.Listener() {
//
//            @Override
//            public void onDataRetrieved(String result) {
//
//                Store store = new Gson().fromJson(result.replace(Constants.STRING_TO_ERASE, Constants.NEW_STRING), Store.class);
//
//                store.feed.fillOriginalEntry(store.feed.entry);
//
//                storeManager.addStore(store);
//
//                dbWorker.saveObject(getApplicationContext(), store);
//
//                busWorker.getBus().post(new FetchedStoreDataEvent(position));
//            }
//        });
    }

    public AppComponent getAppComponent() {

        return appComponent;
    }
}