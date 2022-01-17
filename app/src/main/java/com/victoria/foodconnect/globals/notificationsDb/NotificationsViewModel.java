package com.victoria.foodconnect.globals.notificationsDb;

import static com.victoria.foodconnect.globals.GlobalRepository.application;
import static com.victoria.foodconnect.globals.GlobalRepository.notificationApi;
import static com.victoria.foodconnect.utils.DataOpts.getAccessToken;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.models.Models.NotificationModels;
import com.victoria.foodconnect.utils.DataOpts;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.LinkedHashSet;
import java.util.Optional;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsViewModel extends AndroidViewModel {

    public NotificationsViewModel(@NonNull Application application) {
        super(application);
    }

    //get notifications
    private MutableLiveData<LinkedHashSet<NotificationModels>> getAllMyNotifications(String username) {
        MutableLiveData<LinkedHashSet<NotificationModels>> mutableLiveData = new MutableLiveData<>();
        LinkedHashSet<NotificationModels> notificationModelsList = new LinkedHashSet<>();

        notificationApi.getMyNotifications(username, DataOpts.getAccessToken(application)).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {

                JsonResponse jsonResponse = response.body();

                if (response.code() != 200 || jsonResponse == null || jsonResponse.isHas_error() || !jsonResponse.isSuccess() || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(new LinkedHashSet<>());
                    return;
                }

                try {
                    JsonArray users = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));
                    users.forEach(u -> {
                        try {
                            NotificationModels models = getObjectMapper().readValue(u.toString(), NotificationModels.class);
                            notificationModelsList.add(models);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    });
                    mutableLiveData.setValue(notificationModelsList);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    mutableLiveData.setValue(new LinkedHashSet<>());
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
                mutableLiveData.setValue(new LinkedHashSet<>());
            }
        });

        return mutableLiveData;
    }

    //set notification to seen
    private MutableLiveData<Optional<NotificationModels>> updateNotification(String username, Long id) {
        MutableLiveData<Optional<NotificationModels>> mutableLiveData = new MutableLiveData<>();
        notificationApi.updateNotifications(username, id, getAccessToken(application)).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {

                JsonResponse jsonResponse = response.body();

                if (response.code() != 200 || jsonResponse == null || jsonResponse.isHas_error() || !jsonResponse.isSuccess() || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                try {
                    NotificationModels notification = getObjectMapper().readValue(new JsonObject(getObjectMapper().writeValueAsString(jsonResponse.getData())).toString(), NotificationModels.class);
                    mutableLiveData.setValue(Optional.of(notification));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    mutableLiveData.setValue(Optional.empty());
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
            }
        });

        return mutableLiveData;
    }

    public LiveData<Optional<NotificationModels>> updateNotificationLiveData(String username, Long id) {
        return updateNotification(username, id);
    }

    public LiveData<LinkedHashSet<NotificationModels>> getAllMyNotificationsLiveData(String username) {
        System.out.println("Getting notifications");
        return getAllMyNotifications(username);
    }
}
