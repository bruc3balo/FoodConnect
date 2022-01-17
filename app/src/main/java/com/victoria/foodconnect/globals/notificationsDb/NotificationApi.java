package com.victoria.foodconnect.globals.notificationsDb;

import static com.victoria.foodconnect.globals.GlobalVariables.AUTHORIZATION;
import static com.victoria.foodconnect.globals.GlobalVariables.ID;
import static com.victoria.foodconnect.globals.GlobalVariables.USERNAME;

import com.victoria.foodconnect.utils.JsonResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface NotificationApi {

    String base = "notification";

    @GET("notification")
    Call<JsonResponse> getMyNotifications(@Query(USERNAME) String username, @Header(AUTHORIZATION) String header);

    @PUT("notification")
    Call<JsonResponse> updateNotifications(@Query(USERNAME) String username, @Query(ID) Long id, @Header(AUTHORIZATION) String header);
}
