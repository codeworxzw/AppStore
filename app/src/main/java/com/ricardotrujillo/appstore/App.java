package com.ricardotrujillo.appstore;

import android.app.Application;

import com.android.volley.VolleyError;
import com.ricardotrujillo.appstore.model.StoreManager;
import com.ricardotrujillo.appstore.viewmodel.Constants;
import com.ricardotrujillo.appstore.viewmodel.di.components.AppComponent;
import com.ricardotrujillo.appstore.viewmodel.di.components.DaggerAppComponent;
import com.ricardotrujillo.appstore.viewmodel.di.modules.AppModule;
import com.ricardotrujillo.appstore.viewmodel.di.modules.StoreModule;
import com.ricardotrujillo.appstore.viewmodel.di.modules.WorkersModule;
import com.ricardotrujillo.appstore.viewmodel.event.ConnectivityStatusRequest;
import com.ricardotrujillo.appstore.viewmodel.event.Events;
import com.ricardotrujillo.appstore.viewmodel.worker.BusWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.DbWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.LogWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.NetWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.RxBusWorker;

import org.json.JSONObject;

import java.nio.charset.Charset;

import javax.inject.Inject;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

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
    RxBusWorker rxBusWorker;

    AppComponent appComponent;

    CompositeSubscription rxSubscriptions = new CompositeSubscription();
    CompositeSubscription compositeSubscription = new CompositeSubscription();

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

        setUpRxObservers();
    }

    void setUpRxObservers() {

        ConnectableObservable<Object> tapEventEmitter = rxBusWorker.toObserverable().publish();

        rxSubscriptions.add(tapEventEmitter.subscribe(new Action1<Object>() {
            @Override
            public void call(Object event) {

                if (event instanceof Events.RequestStoreEvent) {

                    Events.RequestStoreEvent e = (Events.RequestStoreEvent) event;

                    logWorker.log("RequestStoreEvent App: " + e.getClassType());

                    checkForLoadedData(e.getClassType());

                } else if (event instanceof Events.ConnectivityStatusResponse) {

                    Events.ConnectivityStatusResponse e = (Events.ConnectivityStatusResponse) event;

                    logWorker.log("ConnectivityStatusResponse App: " + e.getClassType());

                    if (e.getClassType() == Constants.MAIN_ACTIVITY ||
                            e.getClassType() == Constants.ENTRY_ACTIVITY) {

                        loadData(e);
                    }
                }
            }
        }));

        rxSubscriptions.add(tapEventEmitter.connect());
    }

    void checkForLoadedData(int classType) {

        if (storeManager.getStore() == null) {

            logWorker.log("checkForLoadedData 2");

            busWorker.post(new ConnectivityStatusRequest(classType));

        } else {

            logWorker.log("checkForLoadedData 1");

            busWorker.post(new Events.FetchedStoreDataEvent());
        }
    }

    void loadData(Events.ConnectivityStatusResponse e) {

        if (storeManager.getStore() == null) {

            if (e.isConnected()) {

                netWorker.isNetworkAvailable = true;

                getData(Constants.FREE_URL);

            } else {

                netWorker.isNetworkAvailable = false;

                getSavedData();
            }
        }
    }

    void getData(String url) {

        logWorker.log("getData");

        rxSubscriptions.add(netWorker.newGetRouteData(url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onCompleted() {

                        logWorker.log("onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {

                        VolleyError cause = (VolleyError) e.getCause();
                        String s = new String(cause.networkResponse.data, Charset.forName("UTF-8"));

                        logWorker.log("onError: " + s);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {

                        logWorker.log("onNext: " + jsonObject.toString());

                        storeManager.initStore(jsonObject.toString(), true);
                    }
                }));
    }

    void getSavedData() {

        String storeString = (String) dbWorker.getObject(this);

        if (storeString != null) {

            storeManager.initStore(storeString, false);
        }
    }

    public AppComponent getAppComponent() {

        return appComponent;
    }
}