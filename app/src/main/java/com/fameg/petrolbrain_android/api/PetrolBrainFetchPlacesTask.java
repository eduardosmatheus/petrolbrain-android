package com.fameg.petrolbrain_android.api;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class PetrolBrainFetchPlacesTask extends AsyncTask<String, Void, Void> {

    private JSONObject jsonResult;

    public JSONObject getJsonResult() {
        return jsonResult;
    }

    private void setJsonResult(String jsonResult) throws JSONException {
        if(!jsonResult.isEmpty())
            this.jsonResult = new JSONObject(jsonResult);
    }

    @Override
    protected Void doInBackground(String... strings) {

        HttpsURLConnection conn = null;
        BufferedReader reader = null;

        final String nearbySearch = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
        Uri uri = Uri.parse(nearbySearch)
                .buildUpon()
                .appendQueryParameter("key", "AIzaSyD81NQ74zxczdfejdiegET7wtaPQIUmogE")
                .appendQueryParameter("location", "-26.4747835,-48.9894727")
                .appendQueryParameter("radius", "10000")
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

            this.setJsonResult(buffer.toString());
        } catch (IOException | JSONException e) {
            Log.e("Pau em alguma coisa.", e.toString());
        } finally {
            if(conn != null) conn.disconnect();
            if(reader != null) {
                try { reader.close(); } catch (IOException e) {
                    Log.e("Erro na stream.", "Causa: ", e);
                }
            }
        }
        return null;
    }

}