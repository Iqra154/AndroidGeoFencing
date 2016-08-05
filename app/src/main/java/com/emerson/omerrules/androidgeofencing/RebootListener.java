package com.emerson.omerrules.androidgeofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class RebootListener extends BroadcastReceiver{

    private static final String TAG = RebootListener.class.getSimpleName();

    private Context mContext;
    private GeoFenceRegistrer mGeofenceRegistrer;
    private List<GeoFence> mGeoFences;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mGeofenceRegistrer = new GeoFenceRegistrer(context);
        mGeoFences         = new ArrayList<>();
        GeoFenceParser.initialize(mContext);
        GeoFenceParser.getInstance().loadGeoFences(mGeoFences);
        Intent serviceIntent = new Intent(mContext,GeoFenceBackgroundService.class);
        mContext.startService(serviceIntent);
        NotificationHandler.getInstance().createPhoneRebootNotification(mContext);
    }

}
