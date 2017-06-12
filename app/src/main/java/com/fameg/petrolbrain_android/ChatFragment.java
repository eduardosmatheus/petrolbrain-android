package com.fameg.petrolbrain_android;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class ChatFragment extends Fragment {

    private EditText msgToBot;
    private Button btnSendMessage;
    private ListView msgsListView;

    private List<String> msgs;

    private final AIConfiguration config = new AIConfiguration("e0d2904ef02449a6803a285db207880c",
            AIConfiguration.SupportedLanguages.PortugueseBrazil,
            AIConfiguration.RecognitionEngine.System);

    private AIDataService aiDataService;

    private void log(String msgLog) {
        Log.d("ChatFragment", msgLog);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aiDataService = new AIDataService(config);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chat, container, false);

        msgs = new ArrayList<>();

        msgToBot = (EditText) view.findViewById(R.id.msgToBot);
        msgsListView = (ListView) view.findViewById(R.id.msgsListView);
        btnSendMessage = (Button) view.findViewById(R.id.btnSendMessage);
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                sendRequest();
            }
        });

        ListAdapter listAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, msgs);
        msgsListView.setAdapter(listAdapter);

        return view;
    }

    public void sendRequest() {
        btnSendMessage.setEnabled(false);
        String msg = msgToBot.getText().toString();
        addMsgToListView(msg);

        final AIRequest request = new AIRequest();
        request.setQuery(msg);

        new AsyncTask<AIRequest, Void, AIResponse>() {

            @Override
            protected AIResponse doInBackground(AIRequest... params) {
                try  {
                    AIResponse aiResponse = aiDataService.request(params[0]);
                    return aiResponse;
                } catch (AIServiceException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                super.onPostExecute(aiResponse);
                if(aiResponse != null) {
                    Result result = aiResponse.getResult();
                    String botMsg = result.getFulfillment().getSpeech();
                    addMsgToListView(botMsg);

                    msgToBot.setText("");
                    btnSendMessage.setEnabled(true);
                }
            }
        }.execute(request);

    }

    private void addMsgToListView(String msg) {
        msgs.add(msg);
        ((BaseAdapter) msgsListView.getAdapter()).notifyDataSetChanged();
        msgsListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                msgsListView.setSelection(msgsListView.getCount() - 1);
            }
        });
    }
}
