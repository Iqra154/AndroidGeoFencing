package com.emerson.omerrules.androidgeofencing;

import android.app.Application;

public class GeoFenceApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        GeoServices.initialize(this);
    }
}
