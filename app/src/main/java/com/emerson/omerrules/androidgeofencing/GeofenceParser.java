package com.emerson.omerrules.androidgeofencing;

import android.content.Context;
import android.os.Environment;
import android.util.Log;


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

    private static final long serialVersionUID = -2518143671167959330L;

    public static final String TAG = GeofenceParser.class.getSimpleName();

    private static String storagePath;
    private static String name = GeofenceParser.class.getSimpleName();

    private static GeofenceParser sInstance;

    private static boolean isInitialized = false;

    public static void initialize(Context context, GeoLocation geoLocation){
        sInstance = new GeofenceParser(geoLocation);
        storagePath = context.getFilesDir().getAbsolutePath();
        File file = new File(storagePath,name);
        if(file.exists()){
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(file);
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
        Log.d(TAG,"Initialized!");
    }

    public static GeofenceParser getInstance(){
        if(isInitialized){
            return sInstance;
        }
        return null;
    }

    public static boolean isIsInitialized(){return isInitialized;}

    private Map<GeoFence,Boolean> geoFences;

    public GeofenceParser(){
        geoFences = new HashMap<>();
    }

    private GeofenceParser(GeoLocation geoLocation){
        geoFences = new HashMap<>();
    }

    public void loadGeoFences(Map<GeoFence,Boolean> geoFences){
        geoFences.putAll(this.geoFences);
    }

    public void removeGeoFence(GeoFence geoFence){
        geoFences.remove(geoFence);
    }

    public void storeGeoFence(GeoFence geoFence,boolean isWithin){
        geoFences.put(geoFence,isWithin);
    }


    public void saveState(){
        Log.d(TAG,"SAVED!");
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(new File(storagePath,name));
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(this);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
