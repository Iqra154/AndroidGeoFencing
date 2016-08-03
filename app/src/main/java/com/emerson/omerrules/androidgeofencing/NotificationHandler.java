package com.emerson.omerrules.androidgeofencing;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.Stack;

public class NotificationHandler {

    private static final String TAG = NotificationHandler.class.getSimpleName();

    private static NotificationHandler sNotificationHandler;

    public static NotificationHandler getInstance(){
        if(sNotificationHandler == null){
            sNotificationHandler = new NotificationHandler();
        }
        return sNotificationHandler;}

    private Stack<String> historyStack;

    public  static final String EXIT = "Exited the GeoFence:";
    public static final String ENTER = "Entered the GeoFence:";

    private int curID;

    private NotificationHandler(){
        historyStack = new Stack<>();
        curID = Integer.MIN_VALUE;
    }

    public void initialize(Context context,String message){
        historyStack.push(message);


        Intent notificationIntent = new Intent(context.getApplicationContext(), MapsActivity.class);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        stackBuilder.addParentStack(MapsActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);


        builder.setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.powered_by_google_dark))
                .setColor(Color.RED)
                .setContentTitle(message)
                .setContentText(context.getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);


        builder.setAutoCancel(true);


        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(getID(), builder.build());

    }

    private int getID(){
        return ++curID;
    }


}
