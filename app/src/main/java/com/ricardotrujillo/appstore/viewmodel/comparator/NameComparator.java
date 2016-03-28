package com.ricardotrujillo.appstore.viewmodel.comparator;

import com.ricardotrujillo.appstore.model.Store;

import java.util.Comparator;

/**
 * Created by ricardo on 3/21/16 at 8:13 PM.
 */
public class NameComparator implements Comparator<Store.Feed.Entry> {

    public int compare(Store.Feed.Entry strA, Store.Feed.Entry strB) {

        return strA.name.label.compareToIgnoreCase(strB.name.label);
    }
}
