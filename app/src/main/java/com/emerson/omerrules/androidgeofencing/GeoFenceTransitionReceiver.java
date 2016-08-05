package com.emerson.omerrules.androidgeofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeoFenceTransitionReceiver extends BroadcastReceiver {

    private static final String TAG = GeoFenceTransitionReceiver.class.getSimpleName();

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"Triggered!!!");

        mContext = context;

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.d(TAG, "Error with Geofence was detected...");
            return;
        }


        int geofenceTransition = geofencingEvent.getGeofenceTransition();


        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            Bundle data = new Bundle();
            data.putDouble("lat", geofencingEvent.getTriggeringLocation().getLatitude());
            data.putDouble("lon", geofencingEvent.getTriggeringLocation().getLongitude());


            String geofenceTransitionDetails = getGeofenceTransitionDetails(context, geofenceTransition, triggeringGeofences);
            NotificationHandler.getInstance().createGeoFenceNotification(context, geofenceTransitionDetails, data);

        }

    }


    private String getGeofenceTransitionDetails(Context context, int geofenceTransition, List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return mContext.getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return mContext.getString(R.string.geofence_transition_exited);
            default:
                return mContext.getString(R.string.unknown_geofence_transition);
        }
    }
}
