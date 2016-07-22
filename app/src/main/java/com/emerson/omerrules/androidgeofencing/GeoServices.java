package com.emerson.omerrules.androidgeofencing;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoServices extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static GeoServices sGeoServices;
    private static Context sContext;

    private static final String TAG = GeoServices.class.getSimpleName();

    public static void initialize(Context context) {
        sContext = context;
    }

    public static void startAsService(){
        sContext.startService( new Intent(sContext,GeoServices.class));
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("GEOSERVICES","Started");
        sGeoServices = this;

        this.geoFences = new HashMap<>();




        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        start();

        return Service.START_STICKY;
    }

    public static GeoServices getInstance() {
        return sGeoServices;
    }

    private GoogleApiClient mGoogleApiClient;

    private GeoLocation mostRecentPoint;


    private Map<GeoFence,Boolean> geoFences;


    public GeoServices() {}


    public void start() {
        mGoogleApiClient.connect();
    }


    public void stop() {
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }

        Log.d("GEOSERVICES","Changed Location");
        processLocation(location);
    }


    public GeoLocation getMostRecentPoint(){return mostRecentPoint;}


    @Override
    public void onConnected(Bundle bundle) {
        requestLocationServices();
        Log.d("GeoServices:", "Connection success");
    }

    public boolean requestLocationServices(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){return false;}
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setSmallestDisplacement(1f);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        processLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
        return true;
    }




    private void processLocation(Location location){
        GeoLocation geoLocation = new GeoLocation(location.getLatitude(),location.getLongitude());
        mostRecentPoint = geoLocation;

        /*
        if(!GeofenceParser.isIsInitialized()){
            GeofenceParser.initialize(this,mostRecentPoint);
            GeofenceParser.getInstance().loadGeoFences(geoFences);
            Log.d(TAG,"Initialized from the service");
        }*/

        for(GeoFence geoFence:geoFences.keySet()){
            boolean isWithin  = geoFence.isWithin(mostRecentPoint);
            boolean pastState = geoFences.get(geoFence);
            if(pastState == true && isWithin == false){
                NotificationHandler.getInstance().initialize(this,NotificationHandler.EXIT,geoFence);
            }
            if(pastState == false && isWithin == true){
                NotificationHandler.getInstance().initialize(this,NotificationHandler.ENTER,geoFence);
            }

        }


        Intent dataIntent = new Intent("geoservices");
        dataIntent.putExtra("lat",location.getLatitude());
        dataIntent.putExtra("lon",location.getLongitude());
        LocalBroadcastManager.getInstance(this).sendBroadcast(dataIntent);

    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.d("GeoServices:", "Connection suspended");
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("GeoServices:", "Connection failed");
        start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    }


    public boolean checkForLocationSettings(){
        LocationManager locationManager = (LocationManager)this.getSystemService(this.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            return false;
        }
        return true;
    }

    public void addGeoFence(GeoFence geoFence){
        boolean inOrOut = geoFence.isWithin(mostRecentPoint);
        Log.d("GEOSERVICES",inOrOut?"in":"out");
        geoFences.put(geoFence,inOrOut);
        GeofenceParser.getInstance().storeGeoFence(geoFence,inOrOut);
    }

    public void removeGeoFence(GeoFence geoFence){
        geoFences.remove(geoFence);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
