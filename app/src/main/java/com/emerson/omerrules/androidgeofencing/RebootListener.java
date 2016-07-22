package com.emerson.omerrules.androidgeofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RebootListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeoServices.initialize(context.getApplicationContext());
        GeoServices.startAsService();
    }
}
