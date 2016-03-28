package com.ricardotrujillo.appstore.viewmodel.di.components;

import com.ricardotrujillo.appstore.App;
import com.ricardotrujillo.appstore.model.EntryViewModel;
import com.ricardotrujillo.appstore.model.StoreManager;
import com.ricardotrujillo.appstore.viewmodel.activity.EntryActivity;
import com.ricardotrujillo.appstore.viewmodel.activity.MainActivity;
import com.ricardotrujillo.appstore.viewmodel.adapter.StoreRecyclerViewAdapter;
import com.ricardotrujillo.appstore.viewmodel.di.modules.AppModule;
import com.ricardotrujillo.appstore.viewmodel.di.modules.StoreModule;
import com.ricardotrujillo.appstore.viewmodel.di.modules.WorkersModule;
import com.ricardotrujillo.appstore.viewmodel.di.scopes.AppScope;
import com.ricardotrujillo.appstore.viewmodel.fragment.StoreFragment;
import com.ricardotrujillo.appstore.viewmodel.worker.AnimWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.MeasurementsWorker;
import com.ricardotrujillo.appstore.viewmodel.worker.NetWorker;

@AppScope
@dagger.Component(modules = {AppModule.class, StoreModule.class, WorkersModule.class})
public interface AppComponent {

    void inject(App app);

    void inject(MainActivity activity);

    void inject(EntryActivity entryActivity);

    void inject(StoreFragment fragment);

    void inject(StoreRecyclerViewAdapter storeRecyclerViewAdapter);

    void inject(EntryViewModel entryViewModel);

    void inject(NetWorker netWorker);

    void inject(AnimWorker animWorker);

    void inject(MeasurementsWorker measurementsWorker);

    void inject(StoreManager storeManager);
}
