package com.emerson.omerrules.androidgeofencing;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class GeoFence extends GeoLocation implements Serializable,Parcelable{

    private static final long serialVersionUID = -2518143671167959230L;

    public static final String TAG = GeoFence.class.getSimpleName();


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


    private GeoFence(Parcel in) {
        double lat,lon;
        lat = in.readDouble();
        lon = in.readDouble();
        latLng = new LatLng(lat,lon);
        mName = in.readString();
        mRadius = in.readLong();
    }



    public String getName(){return mName;}

    public long getRadius(){
        return mRadius;
    }


    public boolean isWithin(GeoLocation geoLocation){
        if(geoLocation==null){return true;}
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
        Log.d(TAG,"WRITING!");
        oos.defaultWriteObject();
        oos.writeDouble(getLat());
        oos.writeDouble(getLon());
        oos.writeLong(mRadius);
        oos.writeObject(mName);
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        // default deserialization
        Log.d(TAG,"READING!");
        ois.defaultReadObject();
        double lat = ois.readDouble();
        double lon = ois.readDouble();
        latLng = new LatLng(lat,lon);
        mRadius = ois.readLong();
        mName   = (String)ois.readObject();

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latLng.latitude);
        dest.writeDouble(latLng.longitude);
        dest.writeString(mName);
        dest.writeLong(mRadius);
    }

    public static final Creator<GeoFence> CREATOR = new Creator<GeoFence>() {
        @Override
        public GeoFence createFromParcel(Parcel in) {
            return new GeoFence(in);
        }

        @Override
        public GeoFence[] newArray(int size) {
            return new GeoFence[size];
        }
    };
}
