package com.emerson.omerrules.androidgeofencing;

import android.os.Environment;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Map;

public class GeofenceParser implements Serializable {

    public static final String TAG = GeofenceParser.class.getSimpleName();

    private static String storagePath;

    private static GeofenceParser sInstance;

    private static boolean isInitialized = false;

    public static void initialize(GeoLocation geoLocation){
        sInstance = new GeofenceParser(geoLocation);
        storagePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"\\GeoFenceParser\\data.txt";
        File file = new File(storagePath);
        if(file.exists()){
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(storagePath);
                ObjectInputStream ois = new ObjectInputStream(fin);
                sInstance = (GeofenceParser) ois.readObject();

            } catch (FileNotFoundException e) {e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        isInitialized = true;
    }

    public static GeofenceParser getInstance(){
        if(isInitialized){
            return sInstance;
        }
        return null;
    }

    public static boolean isIsInitialized(){return isInitialized;}

    private Map<GeoFence,Boolean> geoFences;

    private GeofenceParser(GeoLocation geoLocation){
        geoFences = new HashMap<>();
    }

    public void loadGeoFences(Map<GeoFence,Boolean> geoFences){
        geoFences.putAll(geoFences);
    }

    public void removeGeoFence(GeoFence geoFence){
        geoFences.remove(geoFence);
    }

    public void storeGeoFence(GeoFence geoFence,boolean isWithin){
        geoFences.put(geoFence,isWithin);
    }


    public void saveState(){
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(storagePath);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(this);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
