package com.victoria.foodconnect.globals.purchaseDb;

import static com.google.common.net.HttpHeaders.CONNECTION;
import static com.victoria.foodconnect.globals.GlobalVariables.AUTHORIZATION;
import static com.victoria.foodconnect.globals.GlobalVariables.CONTENT_TYPE_ME;
import static com.victoria.foodconnect.globals.GlobalVariables.ID;
import static com.victoria.foodconnect.globals.GlobalVariables.KEEP_ALIVE;
import static com.victoria.foodconnect.globals.GlobalVariables.USERNAME;

import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface PurchaseApi {

    String base = "purchase";
    String donation = "donation";
    String distribution = "distribution";
    String donation_distribution = "distribution/donation";

    //purchase
    @GET(base)
    Call<JsonResponse> getAllPurchase(@QueryMap HashMap<String, String> params, @HeaderMap HashMap<String,String> headers);

    @POST(base)
    Call<JsonResponse> newPurchase(@Body Models.PurchaseCreationForm form, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);


    //dist
    @POST(distribution)
    Call<JsonResponse> addDistribution(@Query(ID) Long purchaseId, @Query(USERNAME) String transporterUsername);

    @GET(distribution)
    Call<JsonResponse> getDistribution(@QueryMap HashMap<String, String> params);

    @PUT(distribution)
    Call<JsonResponse> updateDistribution(@Body Models.DistributionUpdateForm form);

    //donation

    @POST(donation)
    Call<JsonResponse> addDonation(@Body Models.DonationCreationForm form, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @GET(donation)
    Call<JsonResponse> getDonation(@QueryMap HashMap<String, String> params, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    //donation distribution
    @POST(donation_distribution)
    Call<JsonResponse> addDonationDistribution(@Query(ID) Long donationId, @Query(USERNAME) String transporterUsername);

    @GET(donation_distribution)
    Call<JsonResponse> getDonationDistribution(@QueryMap HashMap<String, String> params);

    @PUT(donation_distribution)
    Call<JsonResponse> updateDonationDistribution(@Body Models.DonorDistributionUpdateForm form);


}
