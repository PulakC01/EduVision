package com.teaminversion.envisionbuddy;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment {

    ArrayList<Map<String, String>> searchList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        /*dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);*/
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        /*final TextView textView = root.findViewById(R.id.text_dashboard);
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/

        TextInputLayout inputLayout = root.findViewById(R.id.filledTextField);
        EditText searchText = root.findViewById(R.id.editText);

        RecyclerView searchRecyclerView = root.findViewById(R.id.searchRecyclerView);
        SearchRecyclerViewAdapter searchAdapter = new SearchRecyclerViewAdapter(searchList ,getActivity());
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(),2);
        searchRecyclerView.setLayoutManager(layoutManager);
        searchRecyclerView.setAdapter(searchAdapter);
        Button searchButton = root.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputLayout.setError(null);
                searchList.clear();
                String word = searchText.getText().toString();
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
                                searchList.add(modelInfo);
                            }
                        }
                        if (!searchList.isEmpty()) {
                            searchAdapter.notifyDataSetChanged();
                        } else {
                            inputLayout.setError("No 3D models found");
                        }
                        InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        manager.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
                    }

                    @Override
                    public void onFailure(Call<com.teaminversion.envisionbuddy.Response> call, Throwable t) {
                        inputLayout.setError("Couldn't fetch data");
                    }
                });
            }
        });
        return root;
    }
}