package com.emerson.omerrules.androidgeofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
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
    private Intent BACKGROUND_SERVICE;
    private static final int PERMISSION_LOCATION_REQUEST_CODE = 1;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private GeoFenceRegistrer mGeoFenceRegistrer;

    private LinearLayout mViewStack;
    private Map<Marker,TextView> mMarkerToTextVeiwMap;
    private Map<TextView,GeoFence> mViewToGeoFenceMap;


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
        setContentView(R.layout.sensi_maps_activity);
        mViewStack = (LinearLayout)findViewById(R.id.view_stack);


        BACKGROUND_SERVICE = new Intent(this,GeoFenceBackgroundService.class);

        stopService(BACKGROUND_SERVICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geoFences = new ArrayList<>();
        markerToCircleMap = new HashMap<>();
        markerToGeoFenceMap = new HashMap<>();
        mMarkerToTextVeiwMap= new HashMap<>();
        mViewToGeoFenceMap  = new HashMap<>();

            if (!hasLocationPermissions()) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_LOCATION_REQUEST_CODE);
            } else {
                hasPermissions = true;
            }


        if(mGoogleApiClient==null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mGoogleApiClient.connect();
        mGeoFenceRegistrer = new GeoFenceRegistrer(this);



    }

    private boolean hasLocationPermissions() {
        return !(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(getLocationListener(), new IntentFilter("geoservices"));
      //  loadGeoFences();
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
        if(GeofenceParser.isIsInitialized()){
            GeofenceParser.getInstance().saveState();}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(GeofenceParser.isIsInitialized()){
            GeofenceParser.getInstance().saveState();}
        mGoogleApiClient.disconnect();
        stopService(BACKGROUND_SERVICE);
        startService(BACKGROUND_SERVICE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);

        if(hasPermissions){mMap.setMyLocationEnabled(true);}

        if(getIntent().getBundleExtra("data")!=null){
            Bundle data = getIntent().getBundleExtra("data");
            double lat = data.getDouble("lat");
            double lon = data.getDouble("lon");
            float currentZoom = mMap.getCameraPosition().zoom;
            if(currentZoom<16f){currentZoom=16f;}
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon),currentZoom));
        }

            loadGeoFences();


            Log.d(TAG,"Fences fully loaded!");

    }

     private BroadcastReceiver getLocationListener(){
         if(locationListener == null){
            locationListener = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Toast.makeText(context,intent.getStringExtra("message"),Toast.LENGTH_LONG).show();
                    Bundle data = intent.getBundleExtra("data");
                    double lat = data.getDouble("lat");
                    double lon = data.getDouble("lon");
                    float currentZoom = mMap.getCameraPosition().zoom;
                    if(currentZoom<16f){currentZoom=16f;}
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon),currentZoom));
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
                     createGeoFence(latLng);
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






    private void createGeoFence(LatLng latLng){
        if(radiusInput==0L){return;}
        GeoFence geoFence = new GeoFence(nameInput,latLng,radiusInput);

        GeofenceParser.getInstance().storeGeoFence(geoFence);
        geoFences.add(geoFence);

        putGeoFenceOnMap(geoFence);

        mGeoFenceRegistrer.registerGeoFencesWithService(mGoogleApiClient,geoFences);

    }

    private void putGeoFenceOnMap(GeoFence geofence){
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(geofence.getLatLng())
                .radius(geofence.getRadius())
                .strokeColor(Color.BLUE)
                .fillColor(Color.TRANSPARENT));

        TextView fenceText = createGeoFenceTextView(geofence);

        Marker marker = mMap.addMarker(new MarkerOptions().position(geofence.getLatLng()).title(geofence.getName()));

        markerToCircleMap.put(marker,circle);
        markerToGeoFenceMap.put(marker,geofence);
        mMarkerToTextVeiwMap.put(marker,fenceText);

        mViewStack.addView(fenceText);

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
                GeofenceParser.getInstance().removeGeoFence(geoFence);
                mViewStack.removeView(mMarkerToTextVeiwMap.get(marker));
                geoFences.remove(geoFence);
                if(geoFences.size()>0){mGeoFenceRegistrer.registerGeoFencesWithService(mGoogleApiClient,geoFences);}
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
        if(geoFences.size()>0){mGeoFenceRegistrer.registerGeoFencesWithService(mGoogleApiClient,geoFences);}
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void loadGeoFences(){
        if(!GeofenceParser.isIsInitialized()){
            GeofenceParser.initialize(this);}
        geoFences.clear();
        mViewStack.removeAllViews();
        GeofenceParser.getInstance().loadGeoFences(geoFences);
        for(GeoFence g:geoFences){
            putGeoFenceOnMap(g);
        }
    }

    private TextView createGeoFenceTextView(GeoFence geoFence){
        TextView fence = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(getDp(5),getDp(5),getDp(5),getDp(5));
        fence.setLayoutParams(layoutParams);
        fence.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        fence.setTextColor(Color.GREEN);
        fence.setText(geoFence.getName());
        fence.setOnClickListener(createGeoFenceTextViewListener());
        mViewToGeoFenceMap.put(fence,geoFence);
        return fence;
    }

    private View.OnClickListener createGeoFenceTextViewListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView fenceView = (TextView)v;
                GeoFence fence = mViewToGeoFenceMap.get(fenceView);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fence.getLatLng(),16f));
            }
        };
    }

    private int getDp(int px){
        Resources r = getResources();
        int dp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                px,
                r.getDisplayMetrics()
        );
        return dp;
    }


}
