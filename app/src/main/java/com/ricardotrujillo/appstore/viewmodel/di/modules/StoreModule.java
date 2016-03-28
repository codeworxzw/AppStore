package com.ricardotrujillo.appstore.viewmodel.di.modules;

import com.ricardotrujillo.appstore.App;
import com.ricardotrujillo.appstore.model.StoreManager;
import com.ricardotrujillo.appstore.viewmodel.di.scopes.AppScope;

import dagger.Module;
import dagger.Provides;

@Module
public class StoreModule {

    @Provides
    @AppScope
    StoreManager provideStoreManager(App app) {

        //return new StoreManager(app);
        return new StoreManager(app);
    }
}
