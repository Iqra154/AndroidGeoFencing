package com.emerson.omerrules.androidgeofencing;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

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

public class GeofenceParser implements Serializable,Parcelable {

    private static final long serialVersionUID = -2518143671167959330L;

    public static final String TAG = GeofenceParser.class.getSimpleName();

    private static String storagePath;
    private static String name = GeofenceParser.class.getSimpleName();

    private static GeofenceParser sInstance;

    private static boolean isInitialized = false;



    public static void initialize(Context context){
        sInstance = new GeofenceParser();
        storagePath = context.getFilesDir().getAbsolutePath();
        File file = new File(storagePath,name);
        if(file.exists()){
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fin);
                sInstance = (GeofenceParser) ois.readObject();

            } catch (FileNotFoundException | ClassNotFoundException | StreamCorruptedException | OptionalDataException e) {e.printStackTrace();
            } catch (IOException e) {e.printStackTrace();
            }
        }

        isInitialized = true;
        Log.d(TAG,"Initialized!");
    }

    public static void initialize(Context context,GeofenceParser parser){
        sInstance = parser;
        storagePath = context.getFilesDir().getAbsolutePath();
    }

    public static GeofenceParser getInstance(){
        if(!isInitialized){throw new NullPointerException(TAG + ": has not been initialized");}
        return sInstance;
    }

    public static boolean isIsInitialized(){return isInitialized;}

    private List<GeoFence> geoFences;

    private boolean hasUpdates;

    private GeofenceParser(){
        geoFences = new ArrayList<>();
        hasUpdates = false;
    }

    public void loadGeoFences(List<GeoFence> geoFences){
        geoFences.addAll(this.geoFences);
    }

    public void removeGeoFence(GeoFence geoFence){
        geoFences.remove(geoFence);
        hasUpdates = true;
    }

    public void storeGeoFence(GeoFence geoFence){
        geoFences.add(geoFence);
        hasUpdates = true;
    }


    public void saveState(){
        if(!hasUpdates){return;}
        Log.d(TAG,"SAVED!");
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(new File(storagePath,name));
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(this);
        } catch (FileNotFoundException e) {e.printStackTrace();} catch (IOException e) {e.printStackTrace();
        }finally {
            hasUpdates = false;
        }

    }

    public JSONObject toJSON(){
        try {return new JSONObject(new Gson().toJson(this));
        } catch (JSONException e) {e.printStackTrace();
             return null;
        }
    }

    public static GeofenceParser createFromJSON(JSONObject jsonObject){
        return new Gson().fromJson(jsonObject.toString(),GeofenceParser.class);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(geoFences.size());
        for(GeoFence g: geoFences){
            dest.writeParcelable(g,g.describeContents());
        }
    }

    public GeofenceParser(Parcel in) {
        geoFences = new ArrayList<>();
        int size = in.readInt();
        for(int i = 0; i<size;i++){
            geoFences.add(GeoFence.CREATOR.createFromParcel(in));
        }

        hasUpdates = false;
    }

    public static final Creator<GeofenceParser> CREATOR = new Creator<GeofenceParser>() {
        @Override
        public GeofenceParser createFromParcel(Parcel in) {
            return new GeofenceParser(in);
        }

        @Override
        public GeofenceParser[] newArray(int size) {
            return new GeofenceParser[size];
        }
    };
}
