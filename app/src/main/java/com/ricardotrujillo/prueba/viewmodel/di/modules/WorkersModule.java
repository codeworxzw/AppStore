package com.ricardotrujillo.prueba.viewmodel.di.modules;

import com.ricardotrujillo.prueba.App;
import com.ricardotrujillo.prueba.viewmodel.di.scopes.AppScope;
import com.ricardotrujillo.prueba.viewmodel.worker.AnimWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.BusWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.DbWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.LogWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.MeasurementsWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.NetWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.SharedPreferencesWorker;

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
}
