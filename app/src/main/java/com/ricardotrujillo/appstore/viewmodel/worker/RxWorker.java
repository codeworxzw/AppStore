package com.ricardotrujillo.appstore.viewmodel.worker;

import android.util.Log;

import com.ricardotrujillo.appstore.App;
import com.ricardotrujillo.appstore.R;
import com.ricardotrujillo.appstore.viewmodel.Constants;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class RxWorker {

    App app;

    @Inject
    NetWorker netWorker;

    Subscription stringSubscription;
    Subscription integerArraySubscription;
    Subscription appsSubscription;

    Observable<String> stringObservable;
    Observable<Integer> integerArrayObservable;
    Observable<String> appsObservable;
    Observable<String> fetchFreeApps = Observable.create(new Observable.OnSubscribe<String>() {
        @Override
        public void call(final Subscriber<? super String> subscriber) {
            try {
                netWorker.get(app, Constants.FREE_URL, new NetWorker.Listener() {

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
    });
    Observable<String> fetchPaidApps = Observable.create(new Observable.OnSubscribe<String>() {
        @Override
        public void call(final Subscriber<? super String> subscriber) {
            try {
                netWorker.get(app, Constants.PAID_URL, new NetWorker.Listener() {

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
    });

    @Inject
    public RxWorker(App app) {

        this.app = app;

        inject();
    }

    public void initObservables() {

        initStringObservable(Observable.just("Hello"));

        initIntegerObservable(Observable.from(new Integer[]{1, 2, 3, 4, 5, 6}) //rxWorker.initIntegerObservable(Observable.from(new Integer[]{1, 2, 3, 4, 5, 6}));
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

        initAppsObservable(Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                try {
                    netWorker.get(app, Constants.PAID_URL, new NetWorker.Listener() {

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

    void inject() {

        app.getAppComponent().inject(this);
    }

    public void fetchApps() {

        fetchFreeApps.subscribeOn(Schedulers.newThread());
        fetchPaidApps.subscribeOn(Schedulers.newThread());

        Observable.zip(
                fetchFreeApps,
                fetchPaidApps, new Func2<String, String, String>() {
                    @Override
                    public String call(String free, String paid) {
                        // Do something with the results of both threads

                        Log.d(app.getString(R.string.log_tag), "Both reults: " + free.length() + "\n" + paid.length());

                        return free + "\n" + paid;
                    }
                })
                //.observeOn(AndroidSchedulers.mainThread()) check if this works
                .subscribe();
    }

    public void fetchFreeApps() {

        fetchFreeApps
                .subscribeOn(Schedulers.newThread()) // Create a new Thread
                .observeOn(AndroidSchedulers.mainThread()) // Use the UI thread
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {

                        Log.d(app.getString(R.string.log_tag), "pepe " + String.valueOf(s.length()));
                    }
                });
    }

    public void fetchPaidApps() {

        fetchPaidApps
                .subscribeOn(Schedulers.newThread()) // Create a new Thread
                .observeOn(AndroidSchedulers.mainThread()) // Use the UI thread
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {

                        Log.d(app.getString(R.string.log_tag), "pipo " + String.valueOf(s.length()));
                    }
                });
    }

    public void initStringObservable(Observable<String> observable) {

        stringObservable = observable;
    }

    public void initIntegerObservable(Observable<Integer> observable) {

        integerArrayObservable = observable;
    }

    public void initAppsObservable(Observable<String> observable) {

        appsObservable = observable;
    }

    public Action1<String> getStringObserver() {

        return new Action1<String>() {
            @Override
            public void call(String s) {

                Log.d(app.getString(R.string.log_tag), s);
            }
        };
    }

    public Action1<Integer> getIntegerArrayObserver() {

        return new Action1<Integer>() {
            @Override
            public void call(Integer i) {

                Log.d(app.getString(R.string.log_tag), String.valueOf(i));
            }
        };
    }

    public void subscribeToString(Action1<String> action1) {

        stringSubscription = stringObservable.subscribe(action1);
    }

    public void subscribeToIntArray(Action1<Integer> action1) {

        integerArraySubscription = integerArrayObservable.subscribe(action1);
    }

    public void subscribeToApps(Action1<String> action1) {

        appsSubscription = appsObservable.subscribe(action1);
    }

    public void unSubscribeFromString() {

        stringSubscription.unsubscribe();
    }

    public void unSubscribeFromInteger() {

        integerArraySubscription.unsubscribe();
    }

    public void unSubscribeFromApps() {

        appsSubscription.unsubscribe();
    }
}
