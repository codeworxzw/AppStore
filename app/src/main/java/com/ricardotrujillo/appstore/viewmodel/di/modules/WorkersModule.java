package com.ricardotrujillo.appstore.viewmodel.di.modules;

import com.ricardotrujillo.appstore.App;
import com.ricardotrujillo.appstore.viewmodel.di.scopes.AppScope;
import com.ricardotrujillo.appstore.viewmodel.worker.AnimWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.BusWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.DbWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.LogWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.MeasurementsWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.NetWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.RxWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.SharedPreferencesWorker;

import dagger.Module;
import dagger.Provides;

@Module
public class WorkersModule {

    @Provides
    @AppScope
    SharedPreferencesWorker provideSharedPreferences(){

        return new SharedPreferencesWorker();
    }

    @Provides
    @AppScope
    DbWorker provideDbWorker(){

        return new DbWorker();
    }

    @Provides
    @AppScope
    LogWorker provideLogWorker(){

        return new LogWorker();
    }

    @Provides
    @AppScope
    BusWorker provideBusWorker(){

        return new BusWorker();
    }

    @Provides
    @AppScope
    NetWorker provideNetWorker(App app) {

        return new NetWorker(app);
    }

    @Provides
    @AppScope
    MeasurementsWorker provideMeasurementsWorker(App app) {

        return new MeasurementsWorker(app);
    }

    @Provides
    @AppScope
    AnimWorker provideAnimWorker(App app) {

        return new AnimWorker(app);
    }

    @Provides
    @AppScope
    RxWorker provideRxWorker(App app) {

        return new RxWorker(app);
    }
}
