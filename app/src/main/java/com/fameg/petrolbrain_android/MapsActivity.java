package com.fameg.petrolbrain_android;

import android.Manifest;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.widget.Toast;

import com.fameg.petrolbrain_android.fragments.ItemFragment;
import com.fameg.petrolbrain_android.fragments.dummy.DummyContent;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        ConnectionCallbacks, OnConnectionFailedListener, ItemFragment.OnListFragmentInteractionListener {

    private GoogleMap meuMapa;
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(findViewById(R.id.content) != null) {
            if(savedInstanceState != null) return;
        }

        BottomNavigationView navigationView = (BottomNavigationView) findViewById(R.id.mainNav);
        navigationView.setOnNavigationItemSelectedListener(navigationListener);
        navigationView.setSelectedItemId(R.id.nearby);

        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API).build();
    }

    @Override
    protected void onResume() {
        if(client != null && !client.isConnected()) {
            client.connect();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        client.stopAutoManage(this);
        client.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) throws SecurityException {
        meuMapa = googleMap;
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGpsActive = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean possoVerMeuLocal = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED;
        meuMapa.setMyLocationEnabled(possoVerMeuLocal && isGpsActive);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Deu pau na conexão", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) throws SecurityException {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 22);
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) throws SecurityException {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 22) {// TODO: Assign 22 to a variable.
            if(grantResults[0] == PERMISSION_GRANTED) {
                Location userLocation = LocationServices.FusedLocationApi.getLastLocation(client);
                LatLng localAtual = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                moveMapToSomewhere(localAtual, new MarkerOptions().title("Você está aqui."));
            } else finish();
        }
    }

    private void moveMapToSomewhere(LatLng somewhere, MarkerOptions options) {
        meuMapa.addMarker(options.position(somewhere));
        meuMapa.moveCamera(CameraUpdateFactory.newLatLngZoom(somewhere, 15));
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nearby : {
                    SupportMapFragment mapFragment = SupportMapFragment.newInstance();
                    replaceContent(mapFragment);
                    mapFragment.getMapAsync(MapsActivity.this);
                    return true;
                }

                case R.id.availablePlaces: {
                    //TODO: Listar os postos disponíveis na minha base de dados.
                    replaceContent(new ItemFragment());
                    return true;
                }
                case R.id.myProfile: {
                    return true;
                }
            }
            return false;
        }
    };

    private void replaceContent(Fragment mapFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, mapFragment).commit();

    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {
        Toast.makeText(this, item.toString(), Toast.LENGTH_SHORT).show();
    }
}
