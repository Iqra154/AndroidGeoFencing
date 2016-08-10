package com.emerson.omerrules.androidgeofencing;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

public class GeoFenceBackgroundService extends Service implements LocationListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = GeoFenceBackgroundService.class.getSimpleName();
    private static final long   FREQUENCY = 30*1000;
    private LocationManager mManager;

    private GoogleApiClient mGoogleApiClient;
    private GeoFenceRegistrer mGeoFenceRegistrer;
    private GeofenceParser mGeoFenceParser;
    private List<GeoFence> mGeoFences;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Starting!!!");

        if(!GeofenceParser.isIsInitialized()){
            GeofenceParser.initialize(this);}
        mGeoFenceParser = GeofenceParser.getInstance();
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
        startGPSPolling();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void startGPSPolling(){
        Log.d(TAG,"Started GPS polling for improved accuracy");
        mManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,FREQUENCY,10f,this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
        Log.d(TAG,"Destroyed!!!");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,location.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG,"Started GPS polling for improved accuracy");
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

}
