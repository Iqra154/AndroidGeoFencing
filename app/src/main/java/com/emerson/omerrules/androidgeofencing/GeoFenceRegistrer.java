package com.emerson.omerrules.androidgeofencing;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class GeoFenceRegistrer {

    private static final String TAG = GeoFenceRegistrer.class.getSimpleName();

    private static PendingIntent sMostRecentPendingIntent;
    private static int sPendingIntentCode = Integer.MIN_VALUE;

    private Context mContext;

    public GeoFenceRegistrer(Context context){
        this.mContext = context;
    }

    private PendingIntent getServicePendingIntent() {
        Intent intent = new Intent(mContext, GeoFenceTransitionService.class);
        sMostRecentPendingIntent =  PendingIntent.getService(mContext, ++sPendingIntentCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return sMostRecentPendingIntent;
    }

    private PendingIntent getReceiverPendingIntent() {
        Intent intent = new Intent("com.omer.sensigeofence");
        sMostRecentPendingIntent =  PendingIntent.getBroadcast(mContext, ++sPendingIntentCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return sMostRecentPendingIntent;
    }



    public List<Geofence> convertGeoFences(List<GeoFence> geoFences){
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

    public GeofencingRequest getGeofencingRequest(List<Geofence> googleFences) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(googleFences);
        return builder.build();
    }

    private PendingIntent getMostRecentPendingIntent(){
        return sMostRecentPendingIntent;
    }

    private void clearAllGeoFences(GoogleApiClient googleApiClient){
        LocationServices.GeofencingApi.removeGeofences(googleApiClient,getMostRecentPendingIntent());
    }

    public void registerGeoFencesWithService(GoogleApiClient googleApiClient, List<GeoFence> nativeGeoFences){
        if(sMostRecentPendingIntent!=null){clearAllGeoFences(googleApiClient);}
        List googleGeofences = convertGeoFences(nativeGeoFences);
        GeofencingRequest request = getGeofencingRequest(googleGeofences);
        LocationServices.GeofencingApi.addGeofences(googleApiClient,request, getServicePendingIntent());
        Log.d(TAG,"Registering Geofences!");
    }

    public void registerGeoFencesWithReceiver(GoogleApiClient googleApiClient, List<GeoFence> nativeGeoFences){
        if(sMostRecentPendingIntent!=null){clearAllGeoFences(googleApiClient);}
        List googleGeofences = convertGeoFences(nativeGeoFences);
        GeofencingRequest request = getGeofencingRequest(googleGeofences);
        LocationServices.GeofencingApi.addGeofences(googleApiClient,request, getReceiverPendingIntent());
        Log.d(TAG,"Registering Geofences!");
    }




}
