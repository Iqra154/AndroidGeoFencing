package com.emerson.omerrules.androidgeofencing;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeoFenceTransitionService extends IntentService {

    private static final String TAG = GeoFenceTransitionService.class.getSimpleName();

    public GeoFenceTransitionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.d(TAG, "Error with Geofence was detected...");
            return;
        }


        int geofenceTransition = geofencingEvent.getGeofenceTransition();


        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {


            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();


            String geofenceTransitionDetails = getGeofenceTransitionDetails(this, geofenceTransition, triggeringGeofences);
            NotificationHandler.getInstance().initialize(this,geofenceTransitionDetails);


            Log.i(TAG, geofenceTransitionDetails);
            Intent broadCastIntent = new Intent("geoservices");
            broadCastIntent.putExtra("message",geofenceTransitionDetails);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadCastIntent);

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
                    return getString(R.string.geofence_transition_entered);
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    return getString(R.string.geofence_transition_exited);
                default:
                    return getString(R.string.unknown_geofence_transition);
            }
        }
}
