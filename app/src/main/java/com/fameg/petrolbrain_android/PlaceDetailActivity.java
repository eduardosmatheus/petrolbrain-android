package com.fameg.petrolbrain_android;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class PlaceDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        String placeId = getIntent().getStringExtra("PLACE_ID");

        PlaceDetailFetchTask task = new PlaceDetailFetchTask();
        task.execute(placeId);
    }


    private class PlaceDetailFetchTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            ProgressDialog dialog = new ProgressDialog(PlaceDetailActivity.this);
        }

        @Override
        protected JSONObject doInBackground(String... strings) {

            final String baseUrl = "https://maps.googleapis.com/maps/api/place/details/json?";

            Uri uri = Uri.parse(baseUrl)
                    .buildUpon()
                    .appendQueryParameter("placeid", strings[0])
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
    }
}
