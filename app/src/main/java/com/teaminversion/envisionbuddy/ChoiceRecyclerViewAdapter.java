package com.teaminversion.envisionbuddy;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ChoiceRecyclerViewAdapter extends RecyclerView.Adapter<ChoiceRecyclerViewAdapter.RecyclerViewHolder> {

    private ArrayList<String> arrayList;
    private final Context context;

    public ChoiceRecyclerViewAdapter(ArrayList<String> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ChoiceRecyclerViewAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_layout_choice, parent, false);
        return new ChoiceRecyclerViewAdapter.RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChoiceRecyclerViewAdapter.RecyclerViewHolder holder, int position) {
        holder.choiceTextView.setText(arrayList.get(position));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Choose an option")
                        .setItems(new CharSequence[]{"Models", "QNA"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        String word = arrayList.get(position);
                                        ProgressDialog progress = new ProgressDialog(context);
                                        progress.setMessage("Retrieving data");
                                        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                        progress.setIndeterminate(true);
                                        progress.setCancelable(false);
                                        progress.show();
                                        ChoiceActivity.models.clear();

                /*String token = "448afed57b9e4872b52b72e53c5ad9bf";
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://api.sketchfab.com")
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(new OkHttpClient.Builder()
                                .addInterceptor(chain -> {
                                    Request original = chain.request();
                                    Request request = original.newBuilder()
                                            .header("Authorization", token)
                                            .method(original.method(), original.body())
                                            .build();
                                    return chain.proceed(request);
                                }).build())
                        .build();*/
                                        Retrofit retrofit = new Retrofit.Builder()
                                                .baseUrl("https://api.sketchfab.com/v3/")
                                                .addConverterFactory(GsonConverterFactory.create())
                                                .build();
                                        API api = retrofit.create(API.class);
                                        Call<com.teaminversion.envisionbuddy.Response> call = api.searchModels("models",word);
                                        call.enqueue(new Callback<com.teaminversion.envisionbuddy.Response>() {
                                            @Override
                                            public void onResponse(Call<com.teaminversion.envisionbuddy.Response> call, Response<com.teaminversion.envisionbuddy.Response> response) {
                                                Log.d("API Response: ", "got api response");
                                                List<ResultsItem> models = response.body().getResults();
                                                for (ResultsItem model : models) {
                                                    if (model != null) {
                                                        Log.d("Model Name", model.getName());
                                                        Log.d("Model UID", model.getUid());
                                                        Log.d("Model EmbedURL", model.getEmbedUrl());
                                                        Map<String, String> modelInfo = new HashMap<>();
                                                        modelInfo.put("name", model.getName());
                                                        modelInfo.put("thumbnail", model.getThumbnails().getImages().get(0).getUrl());
                                                        modelInfo.put("url", model.getEmbedUrl());
                                                        ChoiceActivity.models.add(modelInfo);
                                                    }
                                                }


                        /*for (int i=0; i<searchResults.size(); i++){
                            Log.d("model info",searchResults.get(i).getDescription());
                            Log.d("model info",searchResults.get(i).getName();
                            Log.d("model info",searchResults.get(i).getUrl());
                            if (response!=null) {
                                Map<String, String> modelInfo = new HashMap<>();
                                modelInfo.put("name", searchResults.get(i).getName());
                                modelInfo.put("thumbnail", searchResults.get(i).getThumbnailUrl());
                                modelInfo.put("url", searchResults.get(i).getUrl());
                                ChoiceActivity.models.add(modelInfo);
                            }
                        }*/

                                                progress.dismiss();
                                                if (!ChoiceActivity.models.isEmpty()) {
                                                    context.startActivity(new Intent(context, ModelsActivity.class));
                                                }else{
                                                    Snackbar snackbar = Snackbar.make(v, "No 3D models found", Snackbar.LENGTH_LONG);
                                                    snackbar.show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<com.teaminversion.envisionbuddy.Response> call, Throwable t) {
                                                progress.dismiss();
                                                Snackbar snackbar = Snackbar.make(v, "Couldn't fetch data", Snackbar.LENGTH_LONG);
                                                snackbar.show();
                                            }
                                        });

                                        // Your code to fetch models

                                        break;
                                    case 1:
                                        // QNA option selected
                                        String word2 = arrayList.get(position);
                                        Intent intent = new Intent(context, playActivity.class);
                                        intent.putExtra("WORD_EXTRA", word2);
                                        Log.d("word2", word2);
                                        context.startActivity(intent);
                                        break;
                                }
                            }
                        });
                builder.show();

//                String word = arrayList.get(position);
//                ProgressDialog progress = new ProgressDialog(context);
//                progress.setMessage("Retrieving data");
//                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//                progress.setIndeterminate(true);
//                progress.setCancelable(false);
//                progress.show();
//                ChoiceActivity.models.clear();
//
//                /*String token = "448afed57b9e4872b52b72e53c5ad9bf";
//                Retrofit retrofit = new Retrofit.Builder()
//                        .baseUrl("https://api.sketchfab.com")
//                        .addConverterFactory(GsonConverterFactory.create())
//                        .client(new OkHttpClient.Builder()
//                                .addInterceptor(chain -> {
//                                    Request original = chain.request();
//                                    Request request = original.newBuilder()
//                                            .header("Authorization", token)
//                                            .method(original.method(), original.body())
//                                            .build();
//                                    return chain.proceed(request);
//                                }).build())
//                        .build();*/
//                Retrofit retrofit = new Retrofit.Builder()
//                        .baseUrl("https://api.sketchfab.com/v3/")
//                        .addConverterFactory(GsonConverterFactory.create())
//                        .build();
//                API api = retrofit.create(API.class);
//                Call<com.teaminversion.envisionbuddy.Response> call = api.searchModels("models",word);
//                call.enqueue(new Callback<com.teaminversion.envisionbuddy.Response>() {
//                    @Override
//                    public void onResponse(Call<com.teaminversion.envisionbuddy.Response> call, Response<com.teaminversion.envisionbuddy.Response> response) {
//                            Log.d("API Response: ", "got api response");
//                            List<ResultsItem> models = response.body().getResults();
//                            for (ResultsItem model : models) {
//                                if (model != null) {
//                                    Log.d("Model Name", model.getName());
//                                    Log.d("Model UID", model.getUid());
//                                    Log.d("Model EmbedURL", model.getEmbedUrl());
//                                    Map<String, String> modelInfo = new HashMap<>();
//                                    modelInfo.put("name", model.getName());
//                                    modelInfo.put("thumbnail", model.getThumbnails().getImages().get(0).getUrl());
//                                    modelInfo.put("url", model.getEmbedUrl());
//                                    ChoiceActivity.models.add(modelInfo);
//                                }
//                            }
//
//
//                        /*for (int i=0; i<searchResults.size(); i++){
//                            Log.d("model info",searchResults.get(i).getDescription());
//                            Log.d("model info",searchResults.get(i).getName();
//                            Log.d("model info",searchResults.get(i).getUrl());
//                            if (response!=null) {
//                                Map<String, String> modelInfo = new HashMap<>();
//                                modelInfo.put("name", searchResults.get(i).getName());
//                                modelInfo.put("thumbnail", searchResults.get(i).getThumbnailUrl());
//                                modelInfo.put("url", searchResults.get(i).getUrl());
//                                ChoiceActivity.models.add(modelInfo);
//                            }
//                        }*/
//
//                        progress.dismiss();
//                        if (!ChoiceActivity.models.isEmpty()) {
//                            context.startActivity(new Intent(context, ModelsActivity.class));
//                        }else{
//                            Snackbar snackbar = Snackbar.make(v, "No 3D models found", Snackbar.LENGTH_LONG);
//                            snackbar.show();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<com.teaminversion.envisionbuddy.Response> call, Throwable t) {
//                        progress.dismiss();
//                        Snackbar snackbar = Snackbar.make(v, "Couldn't fetch data", Snackbar.LENGTH_LONG);
//                        snackbar.show();
//                    }
//                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView choiceTextView;
        private CardView cardView;

        public RecyclerViewHolder(@NonNull View view) {
            super(view);
            choiceTextView = view.findViewById(R.id.choiceTextView);
            cardView = view.findViewById(R.id.cardView);
        }
    }

}