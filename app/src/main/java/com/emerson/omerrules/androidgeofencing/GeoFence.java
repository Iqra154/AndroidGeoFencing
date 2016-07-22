package com.emerson.omerrules.androidgeofencing;

import com.google.android.gms.maps.model.LatLng;

public class GeoFence extends GeoLocation{

    private Listener mListener;

    private GeoLocation mCenter;
    private long mRadius;
    private String mName;


    public GeoFence(String name,LatLng center,long radius){
        super(center);
        this.mCenter = new GeoLocation(center);
        this.mRadius = radius;
        this.mName = name;
    }

    public GeoFence(String name,double lat,double lng,long radius){
        this(name,new LatLng(lat,lng),radius);
    }

    private void raiseEvent(){}

    public void setListener(Listener listener){
        this.mListener = listener;
    }

    public String getName(){return mName;}

    public long getRadius(){
        return mRadius;
    }


    public interface Listener{
        int EVENT_ENTER = 0;
        int EVENT_EXIT  = 1;
        public void eventOccured(int event);
    }

    public boolean isWithin(GeoLocation geoLocation){
        return this.distanceTo(geoLocation)<this.getRadius();
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

}
