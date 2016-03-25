package com.ricardotrujillo.appstore.viewmodel.di.modules;

import com.ricardotrujillo.appstore.model.StoreManager;
import com.ricardotrujillo.appstore.viewmodel.di.scopes.AppScope;

import dagger.Module;
import dagger.Provides;

@Module
public class StoreModule {

    @Provides
    @AppScope
    StoreManager provideStoreManager() {

        return new StoreManager();
    }
}
