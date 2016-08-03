package com.emerson.omerrules.androidgeofencing;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class GeoLocation{

    protected LatLng latLng;

    public GeoLocation(){}

    public GeoLocation(LatLng latLng){
        this.latLng = latLng;
    }

    public GeoLocation(double lat, double lon){
        this.latLng = new LatLng(lat,lon);
    }

    public double getLat(){return latLng.latitude;}
    public double getLon(){return latLng.longitude;}
    public LatLng getLatLng(){return  latLng;}

    public double distanceTo(GeoLocation geoLocation){
        float[] results = new float[4];
        Location.distanceBetween(this.latLng.latitude,this.latLng.longitude,geoLocation.getLat(),geoLocation.getLon(),results);
        return results[0];
    }



}
