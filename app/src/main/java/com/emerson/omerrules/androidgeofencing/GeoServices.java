package com.emerson.omerrules.androidgeofencing;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class GeoServices implements LocationListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,ActivityCompat.OnRequestPermissionsResultCallback {

    private static GeoServices sGeoServices;

    public static void initialize(Context context){
        sGeoServices = new GeoServices(context);
    }

    public static GeoServices getInstance(){
        return sGeoServices;
    }

    private Context mContext;

    private GoogleApiClient mGoogleApiClient;

    private GeoLocation mostRecentPoint;

    private boolean running = false;


    private List<OnReceiveLocationUpdate> updateListeners = new ArrayList<>();

    private List<GeoFence> mGeoFences;



    private GeoServices(Context context) {
        this.mContext = context;
        this.mGeoFences = new ArrayList<>();


        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


    }


    public void start(){
        mGoogleApiClient.connect();
        running = true;
    }


    public void stop(){
        mGoogleApiClient.disconnect();
        running = false;
    }



    @Override
    public void onLocationChanged(Location location) {
        if(location == null){return ;}
        processLocation(location);
    }


    public GeoLocation getMostRecentPoint(){return mostRecentPoint;}


    @Override
    public void onConnected(Bundle bundle) {
        //requestLocationServices();
        running = true;
        Log.d("GeoServices:", "Connection success");
    }



    private void processLocation(Location location){
        GeoLocation geoLocation = new GeoLocation(location.getLatitude(),location.getLongitude());
        for(GeoFence geoFence:mGeoFences){
            if(geoFence.distanceTo(geoLocation)<geoFence.getRadius()){
               // NotificationHandler.getInstance().initialize(this,);
            }
        }


        for(OnReceiveLocationUpdate onReceiveLocationUpdate :updateListeners){
            onReceiveLocationUpdate.onReceiveLocationUpdate(new GeoLocation(location.getLatitude(),location.getLongitude()));
        }

    }


    @Override
    public void onConnectionSuspended(int i) {
        running = false;
        Log.d("GeoServices:", "Connection suspended");
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("GeoServices:", "Connection failed");
        running = false;
        start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    }


    public void notifyListeners(GeoLocation geoPoint){
        for(OnReceiveLocationUpdate listener:updateListeners){
            listener.onReceiveLocationUpdate(geoPoint);
        }
    }

    public void addLocationUpdateListener(OnReceiveLocationUpdate onReceiveLocationUpdate){
        updateListeners.add(onReceiveLocationUpdate);
    }


    public boolean checkForLocationSettings(){
        LocationManager locationManager = (LocationManager)mContext.getSystemService(mContext.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            return false;
        }
        return true;
    }

    public void addGeoFence(GeoFence geoFence){
        mGeoFences.add(geoFence);
    }



    public interface OnReceiveLocationUpdate{
        public void onReceiveLocationUpdate(GeoLocation geoPoint);
    }
}
