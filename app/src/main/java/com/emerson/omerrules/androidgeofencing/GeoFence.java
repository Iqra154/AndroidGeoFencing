package com.emerson.omerrules.androidgeofencing;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class GeoFence extends GeoLocation implements Serializable{

    private Listener mListener;
    private long mRadius;
    private String mName;


    public GeoFence(){}

    public GeoFence(String name,LatLng center,long radius){
        super(center);
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
    public boolean equals(Object o) {
        if(! (o instanceof GeoFence)){return  false;}
        return this.getName().equals(((GeoFence)o).getName());
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeDouble(getLat());
        oos.writeDouble(getLon());
        oos.writeLong(mRadius);
        oos.writeObject(mName);
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        // default deserialization
        ois.defaultReadObject();
        double lat = ois.readDouble();
        double lon = ois.readDouble();
        latLng = new LatLng(lat,lon);
        mRadius = ois.readLong();
        mName   = (String)ois.readObject();

    }


}
