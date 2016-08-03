package com.emerson.omerrules.androidgeofencing;

import android.app.Application;
import android.os.Build;

public class GeoFenceApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationManager.initialize(this);
    }

    public boolean isMarshmallow(){
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.M;
    }




}
