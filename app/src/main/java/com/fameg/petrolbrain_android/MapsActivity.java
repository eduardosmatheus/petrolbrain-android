package com.fameg.petrolbrain_android;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.fameg.petrolbrain_android.fragments.ItemFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        ConnectionCallbacks, OnConnectionFailedListener {

    private Location loc;
    private GoogleMap meuMapa;
    private GoogleApiClient client;

    private BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(findViewById(R.id.content) != null) {
            if(savedInstanceState != null) return;
        }

        navigationView = (BottomNavigationView) findViewById(R.id.mainNav);
        navigationView.setOnNavigationItemSelectedListener(navigationListener);

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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) throws SecurityException {
        meuMapa = googleMap;
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGpsActive = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean possoVerMeuLocal = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED;

        meuMapa.setMyLocationEnabled(possoVerMeuLocal && isGpsActive);

        PetrolBrainFetchPlacesTask task = new PetrolBrainFetchPlacesTask();
        task.execute(String.valueOf(loc.getLatitude()),String.valueOf(loc.getLongitude()), "2000");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Deu pau na conexão", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) throws SecurityException {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 22);

        loc = LocationServices.FusedLocationApi.getLastLocation(client);
        if(loc != null) {
            navigationView.setSelectedItemId(R.id.nearby);
        }
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

    private final BottomNavigationView.OnNavigationItemSelectedListener navigationListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
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

    public class PetrolBrainFetchPlacesTask extends AsyncTask<String, Void, JSONObject> {

        private final ProgressDialog dialog = new ProgressDialog(MapsActivity.this);

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Carregando locais próximos....");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... strings) {

            final String latitude = strings[0];
            final String longitude = strings[1];
            final String radius = strings[2];

            HttpsURLConnection conn = null;
            BufferedReader reader = null;

            final String nearbySearch = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
            Uri uri = Uri.parse(nearbySearch)
                    .buildUpon()
                    .appendQueryParameter("key", "AIzaSyD81NQ74zxczdfejdiegET7wtaPQIUmogE")
                    .appendQueryParameter("location", latitude+","+longitude)
                    .appendQueryParameter("radius", radius)
                    .appendQueryParameter("type", "gas_station")
                    .build();

            try {
                URL url = new URL(uri.toString());
                conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                InputStream in = conn.getInputStream();
                if(in == null) return null;

                reader = new BufferedReader(new InputStreamReader(in));

                StringBuffer buffer = new StringBuffer();

                String linha;
                while((linha = reader.readLine()) != null) {
                    buffer.append(String.format("%s\n", linha));
                }
                if(buffer.length() == 0) return null;

                return new JSONObject(buffer.toString());
            } catch (IOException | JSONException e) {
                Log.e("Pau em alguma coisa.", e.toString());
            } finally {
                if(conn != null) conn.disconnect();
                if(reader != null) {
                    try { reader.close(); }
                    catch (IOException e) {
                        Log.e("Erro na stream.", "Causa: ", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try {
                JSONArray arr = result.getJSONArray("results");
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject object = arr.getJSONObject(i);
                    JSONObject placeLocation = object.getJSONObject("geometry").getJSONObject("location");
                    LatLng latLng = new LatLng(placeLocation.getDouble("lat"), placeLocation.getDouble("lng"));

                    meuMapa.addMarker(new MarkerOptions().position(latLng).title(object.getString("name")));
                    meuMapa.setOnMarkerClickListener(new PlaceDetailListener(object.getString("place_id")));
                }
            } catch (JSONException e) {
                Log.e("Deu pau nos results.", "Causa: ",e);
            }
            dialog.dismiss();
            meuMapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 15));
        }
    }

    private class PlaceDetailListener implements GoogleMap.OnMarkerClickListener {

        private final String placeId;

        public PlaceDetailListener(String placeId) {
            this.placeId = placeId;
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            Intent intent = new Intent(getApplicationContext(), PlaceDetailActivity.class);
            intent.putExtra("PLACE_ID", placeId);
            startActivity(intent);
            return false;
        }

    };
}
