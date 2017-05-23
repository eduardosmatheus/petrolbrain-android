package com.fameg.petrolbrain_android;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlaceCommentsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_comments);

        String placeID = getIntent().getStringExtra("PLACE_ID");

        FetchCommentsTask task = new FetchCommentsTask();
        task.execute(placeID);
    }

    private class FetchCommentsTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            //TODO Chamar um loading na tela
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            String petrolbrainServerURI = "";
            try {
                String response = HttpRequest.get(petrolbrainServerURI).body();
                return new JSONObject(response);
            } catch (JSONException e) {
                Log.e("ERRO. ", "Causa: ", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                ListView lista = (ListView) findViewById(R.id.comentarios);

                ArrayAdapter<String> comentarios = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_expandable_list_item_1, new String[]{});

                lista.setAdapter(comentarios);
            }
            //TODO: Finalizar loading...
        }
    }
}
