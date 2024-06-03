package com.teaminversion.envisionbuddy;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface API {

    @GET("search?")
    Call<Response> searchModels(
            @Query("type") String type,
            @Query("q") String query
    );


    /*String BASE_URL = "https://api.sketchfab.com/v3/";
    @GET("search?")

    Call<ArrayList<JSONProcessActivity>> getResult(@Query("key") String key, @Query("keywords") String keywords);*/
}