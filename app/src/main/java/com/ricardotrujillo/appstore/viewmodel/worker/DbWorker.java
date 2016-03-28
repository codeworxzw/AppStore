package com.ricardotrujillo.appstore.viewmodel.worker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.ricardotrujillo.appstore.model.db.DaoMaster;
import com.ricardotrujillo.appstore.model.db.DaoSession;
import com.ricardotrujillo.appstore.model.db.Store;
import com.ricardotrujillo.appstore.model.db.StoreDao;
import com.ricardotrujillo.appstore.viewmodel.Constants;

import java.util.List;

import javax.inject.Inject;

public class DbWorker {

    @Inject
    public DbWorker() {

    }

    public void saveObject(Context context, Object object) {

        Store daoStore = new Store();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, Constants.DB_KEY, null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();

        daoStore.setObject(new Gson().toJson(object));
        daoStore.setObjectId(Constants.STORE_ID);

        StoreDao storeDao = daoSession.getStoreDao();

        storeDao.insertOrReplace(daoStore);
    }

    public Object getObject(Context context) {

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, Constants.DB_KEY, null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();

        StoreDao storeDao = daoSession.getStoreDao();
        List<Store> storeList = storeDao.loadAll();

        if (storeList.size() > 0) {

            //return new Gson().fromJson(storeList.get(0).getObject(), com.ricardotrujillo.appstore.model.Store.class);
            return storeList.get(0).getObject();
        }

        return null;
    }
}
