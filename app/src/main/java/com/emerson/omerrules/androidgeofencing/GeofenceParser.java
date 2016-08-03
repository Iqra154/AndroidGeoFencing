package com.emerson.omerrules.androidgeofencing;

import android.content.Context;
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
import java.util.ArrayList;
import java.util.List;

public class GeoFenceParser implements Serializable {

    private static final long serialVersionUID = -2518143671167959330L;

    public static final String TAG = GeoFenceParser.class.getSimpleName();

    private static String storagePath;
    private static String name = GeoFenceParser.class.getSimpleName();

    private static GeoFenceParser sInstance;

    private static boolean isInitialized = false;

    public static void initialize(Context context){
        sInstance = new GeoFenceParser();
        storagePath = context.getFilesDir().getAbsolutePath();
        File file = new File(storagePath,name);
        if(file.exists()){
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fin);
                sInstance = (GeoFenceParser) ois.readObject();

            } catch (FileNotFoundException | ClassNotFoundException | StreamCorruptedException | OptionalDataException e) {e.printStackTrace();
            } catch (IOException e) {e.printStackTrace();
            }
        }

        isInitialized = true;
        Log.d(TAG,"Initialized!");
    }

    public static GeoFenceParser getInstance(){
        if(!isInitialized){throw new NullPointerException(TAG + ": has not been initialized");}
        return sInstance;
    }

    public static boolean isIsInitialized(){return isInitialized;}

    private List<GeoFence> geoFences;

    public GeoFenceParser(){
        geoFences = new ArrayList<>();
    }

    public void loadGeoFences(List<GeoFence> geoFences){
        geoFences.addAll(this.geoFences);
    }

    public void removeGeoFence(GeoFence geoFence){
        geoFences.remove(geoFence);
    }

    public void storeGeoFence(GeoFence geoFence){
        geoFences.add(geoFence);
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
