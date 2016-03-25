package com.ricardotrujillo.appstore.viewmodel.worker;


import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.ricardotrujillo.appstore.App;

import javax.inject.Inject;

public class MeasurementsWorker {

    private static int screenWidth = 0;
    private static int screenHeight = 0;
    App app;
    private Measurements measurements = new Measurements();

    @Inject
    public MeasurementsWorker(App app) {

        this.app = app;

        inject();
    }

    void inject() {

        app.getAppComponent().inject(this);
    }


    public int getScreenHeight() {

        return measurements.getScreenHeight();
    }

    public void setScreenHeight(Context context) {

        measurements.setScreenHeight(context);
    }

    public int getScreenH(Context c) {

        if (screenHeight == 0) {

            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenHeight = size.y;
        }

        return screenHeight;
    }

    public int getScreenW(Context c) {

        if (screenWidth == 0) {

            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
        }

        return screenWidth;
    }

    public boolean isInPortraitMode() {

        return app.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public double isTablet(DisplayMetrics dm) {

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int dens = dm.densityDpi;
        double wi = (double) width / (double) dens;
        double hi = (double) height / (double) dens;
        double x = Math.pow(wi, 2);
        double y = Math.pow(hi, 2);
        double screenInches = Math.sqrt(x + y);

        return screenInches;
    }

    public boolean isTablet(Context context) {

        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public boolean setScreenOrientation(Activity activity) {

        //if (!isTablet(activity)) {
//
        //    //activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//
        //    return true;
//
        //} else {
//
        //    //activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//
        //    return false;
        //}

        return activity.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE;
    }

    private class Measurements {

        private int rootHeight;

        public int getScreenHeight() {

            return rootHeight;
        }

        public void setScreenHeight(Context context) {

            rootHeight = getScreenH(context);
        }
    }
}
