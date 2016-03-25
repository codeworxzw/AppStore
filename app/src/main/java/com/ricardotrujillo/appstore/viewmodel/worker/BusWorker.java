package com.ricardotrujillo.appstore.viewmodel.worker;

import android.util.Log;

import com.squareup.otto.Bus;

import javax.inject.Inject;

public class BusWorker {

    static Bus bus;

    @Inject
    public BusWorker() {

    }

    public Bus getBus() {

        return bus;
    }

    public void register(Object object) {

        if (bus == null) {

            bus = new Bus();
        }

        try {

            bus.register(object);

        } catch (RuntimeException re) {

            Log.d("Dagger", "re: " + re.getMessage());
        }

    }

    public void unRegister(Object object) {

        if (bus == null) {

            bus = new Bus();
        }

        bus.unregister(object);
    }

    public void post(Object event) {

        bus.post(event);
    }
}
