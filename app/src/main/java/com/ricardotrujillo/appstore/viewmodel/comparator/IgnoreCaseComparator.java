package com.ricardotrujillo.appstore.viewmodel.comparator;

import java.util.Comparator;

/**
 * Created by ricardo on 3/21/16 at 8:13 PM.
 */
public class IgnoreCaseComparator implements Comparator<String> {

    public int compare(String strA, String strB) {

        return strA.compareToIgnoreCase(strB);
    }
}
