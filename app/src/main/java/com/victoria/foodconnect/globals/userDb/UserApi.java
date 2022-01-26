package com.victoria.foodconnect.globals.userDb;


import static com.victoria.foodconnect.globals.GlobalVariables.*;
import static com.victoria.foodconnect.models.Models.*;

import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface UserApi {

    //retrofit

    String baseUser = "user";
    String baseAdmin = "auth";

    //login
    @POST(API_URL + CONTEXT_URL + "login")
    Call<LoginResponse> getToken(@Body UsernameAndPasswordAuthenticationRequest request, @Header(CONTENT_TYPE_ME) String contentType);

    @POST(baseUser + "/refresh")
    Call<LoginResponse> refreshAccessToken(@Query(ACCESS_TOKEN) String token, @Header(CONTENT_TYPE_ME) String contentType);

    //users
    @GET(baseUser + "/all")
    Call<JsonResponse> getAllUsers(@Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @GET(baseUser + "/specific")
    Call<JsonResponse> getUser(@QueryMap HashMap<String, String> parameters, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @GET(baseUser + "/numbers")
    Call<JsonResponse> getAllPhoneNumbers(@Header(CONTENT_TYPE_ME) String contentType);

    @GET(baseUser + "/usernames")
    Call<JsonResponse> getAllUsernames(@Header(CONTENT_TYPE_ME) String contentType);

    @GET(baseUser + "/emails")
    Call<JsonResponse> getAllEmails(@Header(CONTENT_TYPE_ME) String contentType);

    @POST(baseAdmin + "/authNewUser")
    Call<JsonResponse> authenticateUser(@Body NewUserForm newUserForm, @Header(CONTENT_TYPE_ME) String contentType);

    @PUT(baseUser + "/update")
    Call<JsonResponse> updateUser(@Query(UID) String uid, @Header(AUTHORIZATION) String token, @Body UserUpdateForm updateForm, @Header(CONTENT_TYPE_ME) String contentType);


    //roles

    @GET(baseUser + "/roles")
    Call<JsonResponse> getRoles(@Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @POST(baseAdmin + "/saveRole")
    Call<JsonResponse> saveRole(@Body RoleCreationForm form, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @PUT(baseAdmin + "/role2user")
    Call<JsonResponse> addRoleToUser(@Body RoleToUserForm form, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @DELETE(baseAdmin + "/deleteUser")
    Call<JsonResponse> deleteUser(@Query(UID) String uid, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @PUT(baseAdmin + "/disableUser")
    Call<JsonResponse> disableUser(@Query(UID) String uid, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @PUT(baseAdmin + "/enableUser")
    Call<JsonResponse> enableUser(@Query(UID) String uid, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @POST(baseAdmin + "/changePassword")
    Call<JsonResponse> changePassword(@Query(value = EMAIL_ADDRESS) String email, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @POST(baseAdmin + "/verify")
    Call<JsonResponse> verifyEmail(@Query(value = EMAIL_ADDRESS) String email, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @GET(baseAdmin + "/verifyStatus")
    Call<JsonResponse> verifyEmailStatus(@Query(value = EMAIL_ADDRESS) String email, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);


    //cart
    @POST(baseUser + "/cart")
    Call<JsonResponse> saveNewCart(@Header(AUTHORIZATION) String token, @Body Models.Cart cart);

    @PUT(baseUser + "/cart")
    Call<JsonResponse> updateCart(@Header(AUTHORIZATION) String token, @Body Models.Cart cart);

    @DELETE(baseUser + "/cart")
    Call<JsonResponse> deleteCart(@Header(AUTHORIZATION) String token, @Query(ID) String id);

    @GET(baseUser + "/cart")
    Call<JsonResponse> getCart(@Header(AUTHORIZATION) String token, @Query(UID) String uid);


}
