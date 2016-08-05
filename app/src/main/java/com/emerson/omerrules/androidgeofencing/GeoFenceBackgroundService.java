package com.emerson.omerrules.androidgeofencing;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class GeoFenceBackgroundService extends Service implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = GeoFenceBackgroundService.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private GeoFenceRegistrer mGeoFenceRegistrer;
    private GeoFenceParser mGeoFenceParser;
    private List<GeoFence> mGeoFences;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Starting!!!");

        if(!GeoFenceParser.isIsInitialized()){GeoFenceParser.initialize(this);}
        mGeoFenceParser = GeoFenceParser.getInstance();
        mGeoFences      = new ArrayList<>();
        mGeoFenceParser.loadGeoFences(mGeoFences);
        mGeoFenceRegistrer = new GeoFenceRegistrer(this);


        if(mGoogleApiClient==null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mGoogleApiClient.connect();

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(mGeoFences.size()>0){mGeoFenceRegistrer.registerGeoFencesWithReceiver(mGoogleApiClient,mGeoFences);}
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
        Log.d(TAG,"Destroyed!!!");
    }
}
