package com.victoria.foodconnect.globals.statsDb;

import com.victoria.foodconnect.utils.JsonResponse;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface StatsApi {

    //todo stats api for purchase
    // transporter stats


    String base = "stats";

    @GET(base + "/seller")
    Call<JsonResponse> getSellerStats(@QueryMap HashMap<String, String> params, @HeaderMap HashMap<String, String> headers);

    @GET(base + "/all_seller")
    Call<JsonResponse> getAllSellerStats(@Query(value = "year") Integer year, @HeaderMap HashMap<String, String> headers);

    @GET(base + "/donor")
    Call<JsonResponse> getDonorStats(@QueryMap HashMap<String, String> params, @HeaderMap HashMap<String, String> headers);

    @GET(base + "/all_donor")
    Call<JsonResponse> getAllDonorStats(@Query(value = "year") Integer year, @HeaderMap HashMap<String, String> headers);

    @GET(base + "/all_bene_donations")
    Call<JsonResponse> getAllBeneficiaryDonationsStats(@QueryMap HashMap<String, String> params, @HeaderMap HashMap<String, String> headers);

    @GET(base + "/all_bene_purchase")
    Call<JsonResponse> getAllBeneficiaryPurchaseStats(@QueryMap HashMap<String, String> params, @HeaderMap HashMap<String, String> headers);

    @GET(base + "/all_trans_donations")
    Call<JsonResponse> getAllTransporterDonationsStats(@QueryMap HashMap<String, String> params, @HeaderMap HashMap<String, String> headers);

    @GET(base + "/all_trans_purchases")
    Call<JsonResponse> getAllTransporterPurchasesStats(@QueryMap HashMap<String, String> params, @HeaderMap HashMap<String, String> headers);

}