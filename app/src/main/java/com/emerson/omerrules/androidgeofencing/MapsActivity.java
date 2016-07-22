package com.emerson.omerrules.androidgeofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener{

    private GoogleMap mMap;

    private long radiusInput = 0L;
    private String nameInput = "";

    private BroadcastReceiver locationListener;
    private Map<Marker,Circle> markerToCircleMap;
    private Map<Marker,GeoFence> markerToGeoFenceMap;
    private Map<GeoFence,Boolean> geoFences;

    private GeofenceParser geofenceParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        GeoServices.startAsService();

        geoFences = new HashMap<>();
        markerToCircleMap = new HashMap<>();
        markerToGeoFenceMap = new HashMap<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(getLocationListener(),new IntentFilter("geoservices"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(getLocationListener());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        geofenceParser.saveState();
    }

    private BroadcastReceiver getLocationListener(){
        if(locationListener == null){
            locationListener = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    GeoLocation geoLocation = new GeoLocation(intent.getDoubleExtra("lat",0),intent.getDoubleExtra("lon",0));
                    onReceiveLocationUpdate(geoLocation);
                }
            };
        }
        return locationListener;
    }
    
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setMyLocationEnabled(true);
    }

    public void onReceiveLocationUpdate(GeoLocation geoPoint) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(geoPoint.getLatLng(),12));
        if(!GeofenceParser.isIsInitialized()){
            GeofenceParser.initialize(geoPoint);
            geofenceParser = GeofenceParser.getInstance();
            geofenceParser.loadGeoFences(geoFences);

            for(GeoFence geoFence: geoFences.keySet()){
                addFence(geoFence);
            }

        }
        Log.d("GOOGLE MAPS","location received");
    }

    @Override
    public void onMapClick(final LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add GeoFence?");


        final EditText radiusString = new EditText(this); radiusString.setHint("Radius...");
        radiusString.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

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
                .radius(radiusInput)
                .strokeColor(Color.RED)
                .fillColor(Color.TRANSPARENT));


        Marker marker = mMap.addMarker(new MarkerOptions().position(geofence.getLatLng()).title(geofence.getName()));

        markerToCircleMap.put(marker,circle);
        markerToGeoFenceMap.put(marker,geofence);
    }

    private void addFence(LatLng latLng){
        if(radiusInput==0L){return;}
        GeoFence geoFence = new GeoFence(nameInput,latLng,radiusInput);
        GeoServices.getInstance().addGeoFence(geoFence);

        Circle circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(radiusInput)
                .strokeColor(Color.RED)
                .fillColor(Color.TRANSPARENT));


        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(geoFence.getName()));

        markerToCircleMap.put(marker,circle);
        markerToGeoFenceMap.put(marker,geoFence);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        GeoFence geoFence = markerToGeoFenceMap.get(marker);
        Circle   circle   = markerToCircleMap.get(marker);
        GeoServices.getInstance().removeGeoFence(geoFence);
        marker.remove();
        circle.remove();
        return true;
    }
}
