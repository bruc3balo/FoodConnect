package com.victoria.foodconnect.globals.purchaseDb;

import static com.victoria.foodconnect.globals.GlobalVariables.AUTHORIZATION;
import static com.victoria.foodconnect.globals.GlobalVariables.CONTENT_TYPE_ME;
import static com.victoria.foodconnect.globals.GlobalVariables.REVIEW;

import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.QueryMap;

public interface ReviewApi {

    String base = REVIEW;

    @POST(base)
    Call<JsonResponse> newReview(@Body Models.Remarks remarks, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @PUT(base)
    Call<JsonResponse> updateReview(@Body Models.Remarks remarks, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

}
