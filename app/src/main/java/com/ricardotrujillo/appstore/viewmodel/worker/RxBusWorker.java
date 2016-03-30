package com.ricardotrujillo.appstore.viewmodel.worker;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * courtesy: https://gist.github.com/benjchristensen/04eef9ca0851f3a5d7bf
 */
public class RxBusWorker {

    //private final PublishSubject<Object> _bus = PublishSubject.create();

    // If multiple threads are going to emit events to this
    // then it must be made thread-safe like this instead

    private Subject<Object, Object> bus;

    @Inject
    public RxBusWorker() {

        bus = new SerializedSubject<>(PublishSubject.create());
    }

    public void send(Object o) {

        bus.onNext(o);
    }

    public Observable<Object> toObserverable() {

        return bus;
    }

    public boolean hasObservers() {

        return bus.hasObservers();
    }
}