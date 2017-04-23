package com.fameg.petrolbrain_android;

import android.Manifest;
import android.graphics.Camera;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.google.android.gms.common.api.GoogleApiClient.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener {

    private GoogleMap mMap;
    private Location userLocation;

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        BottomNavigationView navigationView = (BottomNavigationView) findViewById(R.id.mainNav);
        navigationView.setOnNavigationItemSelectedListener(null);
        navigationView.setSelectedItemId(R.id.nearby);

        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API).build();
    }

    @Override
    protected void onResume() {
        if(client != null && !client.isConnected())
            client.connect();
        super.onResume();
    }

    @Override
    protected void onStop() {
        client.stopAutoManage(this);
        client.disconnect();
        super.onStop();
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Snackbar.make(getCurrentFocus(), "",Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 22);
        else {
            userLocation = LocationServices.FusedLocationApi.getLastLocation(client);
            LatLng aquiCoord = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            moveMapToSomewhere(aquiCoord, new MarkerOptions().title("Você está aqui"));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) throws SecurityException {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 22) {// TODO: Assign 22 to a variable.
            if(grantResults[0] == PERMISSION_GRANTED) {
                userLocation = LocationServices.FusedLocationApi.getLastLocation(client);
                LatLng localAtual = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                moveMapToSomewhere(localAtual, new MarkerOptions().title("Você está aqui."));
            } else finish();
        }
    }

    private void moveMapToSomewhere(LatLng somewhere, MarkerOptions options) {
        mMap.addMarker(options.position(somewhere));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(somewhere, 15));
    }
}
