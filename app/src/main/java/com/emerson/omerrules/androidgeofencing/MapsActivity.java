package com.emerson.omerrules.androidgeofencing;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG = MapsActivity.class.getSimpleName();

    private static final int PERMISSION_LOCATION_REQUEST_CODE = 1;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;


    private long radiusInput = 0L;
    private String nameInput = "";

    private BroadcastReceiver locationListener;
    private Map<Marker, Circle> markerToCircleMap;
    private Map<Marker, GeoFence> markerToGeoFenceMap;
    private List<GeoFence> geoFences;
    private boolean hasPermissions = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geoFences = new ArrayList<>();
        markerToCircleMap = new HashMap<>();
        markerToGeoFenceMap = new HashMap<>();

        if(ApplicationManager.getInstance().get().isMarshmallow()) {
            if (!hasLocationPermissions()) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_LOCATION_REQUEST_CODE);
            } else {
                hasPermissions = true;
            }
        }

        if(mGoogleApiClient==null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mGoogleApiClient.connect();


    }

    private boolean hasLocationPermissions() {
        return !(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(getLocationListener(), new IntentFilter("geoservices"));
        if(GeoFenceParser.isIsInitialized()){
            geoFences.clear();
            GeoFenceParser.getInstance().loadGeoFences(geoFences);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(getLocationListener());
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(GeoFenceParser.isIsInitialized()){GeoFenceParser.getInstance().saveState();}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(GeoFenceParser.isIsInitialized()){GeoFenceParser.getInstance().saveState();}
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setMyLocationEnabled(true);
        if(hasPermissions){
            mMap.setMyLocationEnabled(true);
        }

        if(!GeoFenceParser.isIsInitialized()){GeoFenceParser.initialize(this);}
            GeoFenceParser.getInstance().loadGeoFences(geoFences);

            Log.d(TAG,"Loading fences from file...");
            for(GeoFence geoFence: geoFences){
                addFence(geoFence);
            }
            Log.d(TAG,"Fences fully loaded!");

    }

     private BroadcastReceiver getLocationListener(){
         if(locationListener == null){
            locationListener = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Toast.makeText(context,intent.getStringExtra("message"),Toast.LENGTH_LONG).show();
               }
            };
        }
        return locationListener;
    }

     @Override
     public void onMapClick(final LatLng latLng) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Add GeoFence?");


         final EditText radiusString = new EditText(this); radiusString.setHint("Radius...");
         radiusString.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

         final EditText nameString   = new EditText(this); nameString.setHint("Name...");

         LinearLayout linearLayout = new LinearLayout(this);
         LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
         linearLayout.setLayoutParams(layoutParams);
         linearLayout.setOrientation(LinearLayout.VERTICAL);
         linearLayout.addView(radiusString);
         linearLayout.addView(nameString);

         builder.setView(linearLayout);

         builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 if(!radiusString.getText().toString().equals("") && !nameString.getText().toString().equals("")) {
                     radiusInput = Long.parseLong(radiusString.getText().toString());
                     nameInput = nameString.getText().toString();
                     addFence(latLng);
                 }
             }
         });
         builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 radiusInput = 0;
                 dialog.cancel();
             }
         });

         builder.show();
     }




    private void addFence(GeoFence geofence){
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(geofence.getLatLng())
                .radius(geofence.getRadius())
                .strokeColor(Color.BLUE)
                .fillColor(Color.TRANSPARENT));


        Marker marker = mMap.addMarker(new MarkerOptions().position(geofence.getLatLng()).title(geofence.getName()));

        markerToCircleMap.put(marker,circle);
        markerToGeoFenceMap.put(marker,geofence);
    }

    private void addFence(LatLng latLng){
        if(radiusInput==0L){return;}
        GeoFence geoFence = new GeoFence(nameInput,latLng,radiusInput);

        Circle circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(radiusInput)
                .strokeColor(Color.BLUE)
                .fillColor(Color.TRANSPARENT));


        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(geoFence.getName()));

        markerToCircleMap.put(marker,circle);
        markerToGeoFenceMap.put(marker,geoFence);

        GeoFenceParser.getInstance().storeGeoFence(geoFence);

        Geofence googleGeofence = new Geofence.Builder()
                .setRequestId(geoFence.getName())
                .setCircularRegion(
                        geoFence.getLat(),
                        geoFence.getLon(),
                        geoFence.getRadius()
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        List<Geofence> googleGeofences = new ArrayList<>();

        googleGeofences.add(googleGeofence);
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(googleGeofences), getGeofencePendingIntent());

    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove GeoFence?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GeoFence geoFence = markerToGeoFenceMap.get(marker);
                Circle   circle   = markerToCircleMap.get(marker);
                GeoFenceParser.getInstance().removeGeoFence(geoFence);
                marker.remove();
                circle.remove();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        builder.show();


        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {


        switch (requestCode) {
            case PERMISSION_LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    mMap.setMyLocationEnabled(true);
                    hasPermissions = true;
                } else {


                }
                return;
            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> googleFences) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(googleFences);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeoFenceTransitionService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
