package com.ricardotrujillo.prueba.viewmodel.di.modules;

import android.app.Application;

import com.ricardotrujillo.prueba.App;
import com.ricardotrujillo.prueba.viewmodel.di.scopes.AppScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ricardo on 3/24/16 at 1:47 PM.
 */
@Module
public class AppModule {

    App app;

    public AppModule(App application) {

        app = application;
    }

    @Provides
    @AppScope
    App providesApplication() {

        return app;
    }
}
