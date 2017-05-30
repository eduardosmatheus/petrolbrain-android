package com.fameg.petrolbrain_android;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.fameg.petrolbrain_android.adapters.CommentAdapter;
import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class PlaceCommentsActivity extends AppCompatActivity {

    Button comentar;
    EditText conteudoDoComentario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_comments);

        final String placeID = getIntent().getStringExtra("PLACE_ID");

        comentar = (Button) findViewById(R.id.comentar);
        conteudoDoComentario = (EditText) findViewById(R.id.conteudoComentario);

        comentar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PostCommentsTask task = new PostCommentsTask();
                task.execute(placeID, conteudoDoComentario.getText().toString());
            }
        });

        FetchCommentsTask task = new FetchCommentsTask();
        task.execute(placeID);
    }

    private class FetchCommentsTask extends AsyncTask<String, Void, JSONArray> {

        private final ProgressDialog dialog = new ProgressDialog(getApplicationContext());

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Carregando comentários...");
        }

        @Override
        protected JSONArray doInBackground(String... strings) {
            String petrolbrainServerURI = String.format("https://petrolbrain-server.herokuapp.com/places/%s/comments", strings[0]);
            try {
                String response = HttpRequest.get(petrolbrainServerURI).body();
                return new JSONArray(response);
            } catch (JSONException e) {
                Log.e("ERRO. ", "Causa: ", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray results) {
            dialog.dismiss();
            if(results.length() == 0) return;
            normalizeComments(results);
        }
    }

    private void normalizeComments(JSONArray results) {
        String[] normalizedResults = new String[results.length()];
        try {
            for (int i = 0; i < results.length(); i++) {
                JSONObject obj = results.getJSONObject(i);
                normalizedResults[i] = obj.getString("content");
            }

            ListView lista = (ListView) findViewById(R.id.comentarios);
            if(normalizedResults.length != 0) {
                CommentAdapter comments = new CommentAdapter(getApplicationContext(), 0, Arrays.asList(normalizedResults));
                lista.setAdapter(comments);
            }
        } catch (JSONException je) {
            Log.e("Deu erro.", "Causa: ", je);
        }
    }

    private class PostCommentsTask extends AsyncTask<String, Void, JSONArray> {

        @Override
        protected void onPreExecute() {
            comentar.setEnabled(false);
        }

        @Override
        protected JSONArray doInBackground(String... strings) {
            String petrolbrainServerURI = String.format("https://petrolbrain-server.herokuapp.com/places/%s/comments", strings[0]);
            try {
                JSONObject payload = new JSONObject("{}");
                payload.put("placeId", strings[0]);
                payload.put("content", strings[1]);

                String response = HttpRequest
                        .post(petrolbrainServerURI)
                        .contentType("application/json")
                        .acceptJson()
                        .send(payload.toString())
                        .body();
                Log.d("Reponse: ", response);
                return new JSONArray(response);
            } catch (JSONException e) {
                Log.e("ERRO. ", "Causa: ", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray results) {
            comentar.setEnabled(true);
            normalizeComments(results);
        }
    }

    private void addMsgToListView() {
        ListView lista = (ListView) findViewById(R.id.comentarios);
        ((ArrayAdapter) lista.getAdapter()).notifyDataSetChanged();
    }

}
