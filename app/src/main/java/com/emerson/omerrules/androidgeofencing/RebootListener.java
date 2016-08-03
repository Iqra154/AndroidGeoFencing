package com.emerson.omerrules.androidgeofencing;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class RebootListener extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = RebootListener.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if(mGoogleApiClient==null){
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG,"Connected!");
        GeoFenceParser.initialize(mContext);
        List<GeoFence> geoFenceList = new ArrayList<>();
        GeoFenceParser.getInstance().loadGeoFences(geoFenceList);
        Log.d(TAG,"Re-registering GeoFences...");
        GeofencingRequest request = getGeofencingRequest(convertGeoFences(geoFenceList));
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, request, getGeofencePendingIntent());
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(mContext, GeoFenceTransitionService.class);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private List<Geofence> convertGeoFences(List<GeoFence> geoFences){
        List<Geofence> googleGeoFences = new ArrayList<>();
        for(GeoFence geoFence: geoFences){
            googleGeoFences.add(new Geofence.Builder()
                    .setRequestId(geoFence.getName())
                    .setCircularRegion(
                            geoFence.getLat(),
                            geoFence.getLon(),
                            geoFence.getRadius()
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }

        return googleGeoFences;
    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> googleFences) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(googleFences);
        return builder.build();
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
