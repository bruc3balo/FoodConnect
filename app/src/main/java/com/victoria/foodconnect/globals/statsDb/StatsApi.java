package com.victoria.foodconnect.globals.statsDb;

import com.victoria.foodconnect.utils.JsonResponse;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface StatsApi {

    String base = "stats";

    @GET(base + "/seller")
    Call<JsonResponse> getSellerStats(@QueryMap HashMap<String, String> params, @HeaderMap HashMap<String, String> headers);

    @GET(base + "/all_seller")
    Call<JsonResponse> getAllSellerStats(@Query(value = "year") Integer year, @HeaderMap HashMap<String, String> headers);

    @GET(base + "/donor")
    Call<JsonResponse> getDonorStats(@QueryMap HashMap<String, String> params, @HeaderMap HashMap<String, String> headers);

    @GET(base + "/all_donor")
    Call<JsonResponse> getAllDonorStats(@Query(value = "year") Integer year, @HeaderMap HashMap<String, String> headers);
}
