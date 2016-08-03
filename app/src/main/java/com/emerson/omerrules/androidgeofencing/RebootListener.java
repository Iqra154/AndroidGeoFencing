package com.emerson.omerrules.androidgeofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class RebootListener extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = RebootListener.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private GeoFenceRegistrer mGeofenceRegistrer;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mGeofenceRegistrer = new GeoFenceRegistrer(context);
        if(mGoogleApiClient==null){
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG,"Connected!");
        GeoFenceParser.initialize(mContext);
        List<GeoFence> geoFenceList = new ArrayList<>();
        GeoFenceParser.getInstance().loadGeoFences(geoFenceList);
        Log.d(TAG,"Re-registering GeoFences...");
        if(geoFenceList.size()>0){mGeofenceRegistrer.registerGeoFences(mGoogleApiClient,geoFenceList);}
        NotificationHandler.getInstance().createPhoneRebootNotification(mContext);
    }



    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"Suspended!");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"Failed!");
    }
}
