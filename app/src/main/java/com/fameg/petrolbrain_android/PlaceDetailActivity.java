package com.fameg.petrolbrain_android;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class PlaceDetailActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private ImageView placeImage;
    private TextView placeAddress;
    private TextView placePhone;
    private TextView placeOpeningHours;

    private GoogleApiClient client;

    String placeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        placeImage = (ImageView) findViewById(R.id.placeImage);
        placeAddress = (TextView) findViewById(R.id.placeAddress);
        placePhone = (TextView) findViewById(R.id.placePhone);
        placeOpeningHours = (TextView) findViewById(R.id.placeOpeningHours);

        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .enableAutoManage(this, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        placeId = getIntent().getStringExtra("PLACE_ID");

        PlaceDetailFetchTask task = new PlaceDetailFetchTask();
        task.execute(placeId);
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
    public void onConnected(@Nullable Bundle bundle) {
        PlacePhotoTask task = new PlacePhotoTask(placeImage.getWidth(), placeImage.getHeight());
        task.execute("ChIJrTLr-GyuEmsRBfy61i59si0");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private class PlaceDetailFetchTask extends AsyncTask<String, Void, JSONObject> {

        private final ProgressDialog dialog = new ProgressDialog(PlaceDetailActivity.this);

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Aguarde. Carregando informações do local selecionado...");
            dialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... strings) {

            final String baseUrl = "https://maps.googleapis.com/maps/api/place/details/json?";

            Uri uri = Uri.parse(baseUrl)
                    .buildUpon()
                    .appendQueryParameter("placeid", strings[0])
                    .appendQueryParameter("language", "pt-BR")
                    .appendQueryParameter("key", "AIzaSyB-Shj41rhxDcTs5GLOqoyJf79CY60MQug")
                    .build();

            HttpsURLConnection conn = null;
            BufferedReader reader = null;

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

                Log.d("Chegou os detalhes.", buffer.toString());
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
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                JSONObject result = jsonObject.getJSONObject("result");
                setTitle(result.getString("name"));
                placeAddress.setText(result.getString("formatted_address"));
                placePhone.setText(String.format("Fone: %s", result.getString("formatted_phone_number")));

                JSONArray openingHours = result.getJSONObject("opening_hours").getJSONArray("weekday_text");
                StringBuilder horariosAbertos = new StringBuilder();
                for (int i = 0; i < openingHours.length(); i++) {
                    String horario = openingHours.getString(i);
                    String formattedText = horario.substring(0, 1).toUpperCase() + horario.substring(1);
                    horariosAbertos.append(formattedText).append("\n");
                }
                placeOpeningHours.setText(horariosAbertos.toString());
            } catch (JSONException e) {
                Log.e("Deu erro.", "Causa : ", e);
            } finally {
                dialog.dismiss();
            }
        }
    }

    private class PlacePhotoTask extends AsyncTask<String, Void, AttributedPhoto> {

        private int mWidth;
        private int mHeight;

        public PlacePhotoTask(int width, int height) {
            mHeight = height;
            mWidth = width;
        }

        @Override
        protected void onPreExecute() {
            placeImage.setImageResource(R.drawable.ic_broken_image_black_24dp);
        }

        @Override
        protected AttributedPhoto doInBackground(String... strings) {
            final String placeId = strings[0];

            PlacePhotoMetadataResult result = Places.GeoDataApi
                    .getPlacePhotos(client, placeId).await();

            AttributedPhoto attributedPhoto = null;
            if (result.getStatus().isSuccess()) {
                PlacePhotoMetadataBuffer photoMetaData = result.getPhotoMetadata();
                if (photoMetaData.getCount() > 0 && !isCancelled()) {
                    PlacePhotoMetadata photo = photoMetaData.get(0);
                    CharSequence attribution = photo.getAttributions();
                    Bitmap image = photo.getScaledPhoto(client, mWidth, mHeight).await().getBitmap();
                    attributedPhoto = new AttributedPhoto(attribution, image);
                }
                photoMetaData.release();
            }
            return attributedPhoto;
        }

        @Override
        protected void onPostExecute(AttributedPhoto attributedPhoto) {
            if(attributedPhoto != null) {
                placeImage.setImageBitmap(attributedPhoto.bitmap);
            }
        }
    }

    /**
     * Holder for an image and its attribution.
     */
    class AttributedPhoto {

        public final CharSequence attribution;

        public final Bitmap bitmap;

        public AttributedPhoto(CharSequence attribution, Bitmap bitmap) {
            this.attribution = attribution;
            this.bitmap = bitmap;
        }
    }
}
