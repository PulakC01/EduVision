package com.teaminversion.envisionbuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;


public class ChoiceActivity extends AppCompatActivity {

    static ArrayList<String> choiceList = new ArrayList<>();
    static ArrayList<Map<String, String>> models = new ArrayList<>();
    String result = "";
    ChoiceRecyclerViewAdapter choiceAdapter;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        RecyclerView choiceRecyclerView = findViewById(R.id.choiceRecyclerView);

        Intent intent = getIntent();
        String text = intent.getStringExtra("text");
        boolean status = intent.getBooleanExtra("status", false);
        //choiceList.clear();
        if (!status) {
            analyzeText(text);
        }

        choiceAdapter = new ChoiceRecyclerViewAdapter(choiceList,this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        choiceRecyclerView.setLayoutManager(layoutManager);
        choiceRecyclerView.setAdapter(choiceAdapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChoiceActivity.this, MainActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void analyzeText(String inputText){
        //Sample choices
        choiceList.clear();
        AnalyzeTextTask analyzeTextTask = new AnalyzeTextTask();
        try {
            String urlEncoder = URLEncoder.encode(inputText, "UTF-8");
            analyzeTextTask.execute("https://1a3b-106-208-59-224.ngrok-free.app/" + urlEncoder);
            progress = new ProgressDialog(this);
            progress.setMessage("Retrieving data");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setCancelable(false);
            progress.show();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChoiceActivity.this, MainActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public class AnalyzeTextTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                while (data != -1){
                    char current  = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                Log.i("JSONArray", s);
                JSONArray jsonArray = new JSONArray(s);
                for (int i=0; i<jsonArray.length(); i++){
                    Log.i("infomact", (String) jsonArray.get(i));
                    choiceList.add((String) jsonArray.get(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            progress.dismiss();
            if (!choiceList.isEmpty()) {
                choiceAdapter.notifyDataSetChanged();
            }else{
                Toast.makeText(ChoiceActivity.this, "No relevant words found", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ChoiceActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }
}
