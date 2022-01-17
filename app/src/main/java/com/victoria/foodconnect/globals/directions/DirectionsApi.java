package com.victoria.foodconnect.globals.directions;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface DirectionsApi {

    @GET("directions/json")
    Call<ResponseBody> getDirections(@QueryMap HashMap<String, String> params);

}
