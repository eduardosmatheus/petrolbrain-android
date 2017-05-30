package com.fameg.petrolbrain_android;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.fameg.petrolbrain_android.fragments.SettingsFragment;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

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

    private Map<String, String> markerIds = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsActive = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGpsActive) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Para utilizar este app, é necessário estar com a localização ativa.");
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    finish();
                }
            });
            dialog.create().show();
        }

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
    public void onMapReady(GoogleMap googleMap) throws SecurityException {
        meuMapa = googleMap;
        boolean possoVerMeuLocal = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED;

        meuMapa.setMyLocationEnabled(possoVerMeuLocal);
        meuMapa.setOnMarkerClickListener(new PlaceDetailListener());

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
                loc = LocationServices.FusedLocationApi.getLastLocation(client);
                LatLng localAtual = new LatLng(loc.getLatitude(), loc.getLongitude());
                moveMapToSomewhere(localAtual);
            } else finish();
        }
    }

    private void moveMapToSomewhere(LatLng somewhere) {
        meuMapa.addMarker(new MarkerOptions().position(somewhere));
        meuMapa.moveCamera(CameraUpdateFactory.newLatLngZoom(somewhere, 15));
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navigationListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.nearby) {
                SupportMapFragment mapFragment = SupportMapFragment.newInstance();
                replaceContent(mapFragment);
                mapFragment.getMapAsync(MapsActivity.this);
                return true;
            } else if (item.getItemId() == R.id.ajuda) {
                replaceContent(new ChatFragment());
                return true;
            } else if (item.getItemId() == R.id.mySettings) {
                replaceContent(new SettingsFragment());
                return true;
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
            try {
                Uri uri = Uri.parse(nearbySearch)
                    .buildUpon()
                    .appendQueryParameter("key", "AIzaSyD81NQ74zxczdfejdiegET7wtaPQIUmogE")
                    .appendQueryParameter("location", latitude+","+longitude)
                    .appendQueryParameter("radius", radius)
                    .appendQueryParameter("type", "gas_station")
                    .build();

                String response = HttpRequest.get(uri.toString()).body();
                return new JSONObject(response);
            } catch (JSONException e) {
                Log.e("Pau em alguma coisa.", e.toString());
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

                    String id = object.getString("place_id");
                    String placeName = object.getString("name");

                    markerIds.put(placeName, id);

                    MarkerOptions options = new MarkerOptions()
                            .position(latLng)
                            .title(placeName);
                    meuMapa.addMarker(options);
                }
            } catch (JSONException e) {
                Log.e("ERRO.", "Causa: ",e);
            }
            dialog.dismiss();
        }
    }

    private class PlaceDetailListener implements GoogleMap.OnMarkerClickListener {

        @Override
        public boolean onMarkerClick(Marker marker) {
            final String placeId = markerIds.get(marker.getTitle());

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MapsActivity.this);
            dialogBuilder.setTitle("Ver detalhes");
            dialogBuilder.setMessage("Deseja ver os detalhes do local?");
            dialogBuilder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(getApplicationContext(), PlaceDetailActivity.class);
                    intent.putExtra("PLACE_ID", placeId);
                    startActivity(intent);
                }
            });

            dialogBuilder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            dialogBuilder.create().show();
            return false;
        }

    }
}
