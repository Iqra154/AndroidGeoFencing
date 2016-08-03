package com.emerson.omerrules.androidgeofencing;

public class ApplicationManager {

    private static final String TAG = ApplicationManager.class.getSimpleName();

    private static ApplicationManager sInstance;

    public static void initialize(GeoFenceApp app){
        if(sInstance != null){throw new RuntimeException(TAG + ": Has already been initialized.");}
        sInstance = new ApplicationManager(app);
    }

    public static ApplicationManager getInstance(){
        if(sInstance == null){throw new NullPointerException(TAG + ": No instance has been initialized.");}
        return sInstance;
    }

    private GeoFenceApp mApplication;

    private ApplicationManager(GeoFenceApp app){
        this.mApplication = app;
    }

    public GeoFenceApp get(){
        return mApplication;
    }


}
