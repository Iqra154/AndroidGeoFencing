package com.emerson.omerrules.androidgeofencing;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import java.util.Stack;

public class NotificationHandler {

    private static NotificationHandler sNotificationHandler = new NotificationHandler();

    public static NotificationHandler getInstance(){return sNotificationHandler;}

    private Stack<String> historyStack;

    public  static final String EXIT = "Exited the GeoFence:";
    public static final String ENTER = "Entered the GeoFence:";

    private int curID;

    private NotificationHandler(){
        historyStack = new Stack<>();
        curID = Integer.MIN_VALUE;
    }

    public void initialize(Context context,String action,GeoFence geoFence){
        historyStack.push(createMessage(action,geoFence.getName()));


        Intent intent = new Intent(context,MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP); //No need to create back stack since there is only 1 activity running at all times.
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Notification notification = mBuilder
                .setContentIntent(pendingIntent)
                .setContentTitle("GeoFence Update")
                .setContentText(historyStack.peek())
                .setAutoCancel(false)
                .build();



        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(getID(),notification);

    }

    private int getID(){
        return ++curID;
    }

    private String createMessage(String action,String geoFence){
        return new StringBuilder().append(action).append(" ").append(geoFence).toString();
    }

}
