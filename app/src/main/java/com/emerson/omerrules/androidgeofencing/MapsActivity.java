package com.emerson.omerrules.androidgeofencing;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback ,GeoServices.OnReceiveLocationUpdate,GoogleMap.OnMapClickListener{

    private GoogleMap mMap;

    public GeoServices mGeoServices;

    private long radiusInput = 0L;
    private String nameInput = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getGeoServices();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);



        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onReceiveLocationUpdate(GeoLocation geoPoint) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(geoPoint.getLatLng()));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type GeoFence Radius?");


        final EditText radiusString = new EditText(this); radiusString.setHint("Radius..."); radiusString.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL);
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
                radiusInput = Long.parseLong(radiusString.getText().toString());
                nameInput = nameString.getText().toString();
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

    private void addFence(LatLng latLng){
        if(radiusInput==0L){return;}
        GeoFence geoFence = new GeoFence(nameInput,latLng,radiusInput);
        getGeoServices().addGeoFence(geoFence);

        mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(radiusInput)
                .strokeColor(Color.RED)
                .fillColor(Color.TRANSPARENT));
    }

    private GeoServices getGeoServices(){
       if(mGeoServices == null){
           mGeoServices = GeoServices.getInstance();
           mGeoServices.addLocationUpdateListener(this);
           mGeoServices.start();
       }
        return mGeoServices;
    }
}
