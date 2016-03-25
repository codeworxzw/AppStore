package com.ricardotrujillo.prueba.viewmodel.di.components;

import com.ricardotrujillo.prueba.App;
import com.ricardotrujillo.prueba.model.EntryViewModel;
import com.ricardotrujillo.prueba.viewmodel.activity.EntryActivity;
import com.ricardotrujillo.prueba.viewmodel.activity.MainActivity;
import com.ricardotrujillo.prueba.viewmodel.adapter.StoreRecyclerViewAdapter;
import com.ricardotrujillo.prueba.viewmodel.di.modules.AppModule;
import com.ricardotrujillo.prueba.viewmodel.di.modules.StoreModule;
import com.ricardotrujillo.prueba.viewmodel.di.modules.WorkersModule;
import com.ricardotrujillo.prueba.viewmodel.di.scopes.AppScope;
import com.ricardotrujillo.prueba.viewmodel.fragment.StoreFragment;
import com.ricardotrujillo.prueba.viewmodel.worker.AnimWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.MeasurementsWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.NetWorker;

@AppScope
@dagger.Component(modules = {AppModule.class, StoreModule.class, WorkersModule.class})
public interface AppComponent {

    void inject(App app);

    void inject(MainActivity activity);

    void inject(EntryActivity entryActivity);

    void inject(StoreFragment fragment);

    void inject(StoreRecyclerViewAdapter storeRecyclerViewAdapter);

    void inject(EntryViewModel entryViewModel); //a

    void inject(NetWorker netWorker);

    void inject(AnimWorker animWorker);

    void inject(MeasurementsWorker measurementsWorker);
}
