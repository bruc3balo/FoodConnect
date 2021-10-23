package com.victoria.foodconnect.globals.userDb;



import static com.victoria.foodconnect.globals.GlobalVariables.*;
import static com.victoria.foodconnect.models.Models.*;

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

    String baseUser = "user";
    String baseAdmin = "auth";

    //login
    @POST(API_URL + CONTEXT_URL + "login")
    Call<LoginResponse> getToken(@Body UsernameAndPasswordAuthenticationRequest request);

    //users
    @GET(baseUser + "/all")
    Call<JsonResponse> getAllUsers(@Header(AUTHORIZATION) String token);

    @GET(baseUser + "/specific")
    Call<JsonResponse> getUser(@QueryMap HashMap<String,String> parameters, @Header(AUTHORIZATION) String token);

    @GET(baseUser + "/numbers")
    Call<JsonResponse> getAllPhoneNumbers();

    @GET(baseUser + "/usernames")
    Call<JsonResponse> getAllUsernames();

    @POST(baseAdmin + "/authNewUser")
    Call<JsonResponse> authenticateUser(@Body NewUserForm newUserForm);

    @PUT(baseUser + "/update")
    Call<JsonResponse> updateUser(@Query(UID) String uid, @Header(AUTHORIZATION) String token, @Body UserUpdateForm updateForm);


    //roles
    @POST(baseAdmin + "saveRole")
    Call<JsonResponse> saveRole(@Body RoleCreationForm form, @Header(AUTHORIZATION) String token);

    @PUT(baseAdmin + "/role2user")
    Call<JsonResponse> addRoleToUser(@Body RoleToUserForm form,@Header(AUTHORIZATION) String token);

    @DELETE(baseAdmin + "/deleteUser")
    Call<JsonResponse> deleteUser(@Query(UID) String uid, @Header(AUTHORIZATION) String token);

    @PUT(baseAdmin + "/disableUser")
    Call<JsonResponse> disableUser(@Query(UID) String uid, @Header(AUTHORIZATION) String token);

    @PUT(baseAdmin + "/enableUser")
    Call<JsonResponse> enableUser(@Query(UID) String uid, @Header(AUTHORIZATION) String token);

    @POST(baseAdmin + "/changePassword")
    Call<JsonResponse> changePassword(@Query(value = EMAIL_ADDRESS) String email, @Header(AUTHORIZATION) String token);

}
