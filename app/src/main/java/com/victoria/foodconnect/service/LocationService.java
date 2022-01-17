package com.victoria.foodconnect.service;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static com.victoria.foodconnect.broadcast.UpdateBroadcast.UPDATE_INTENT;
import static com.victoria.foodconnect.globals.GlobalRepository.application;
import static com.victoria.foodconnect.globals.GlobalRepository.notificationApi;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.LATITUDE;
import static com.victoria.foodconnect.globals.GlobalVariables.LONGITUDE;
import static com.victoria.foodconnect.globals.GlobalVariables.ROLE;
import static com.victoria.foodconnect.globals.GlobalVariables.UPDATE;
import static com.victoria.foodconnect.globals.GlobalVariables.USERNAME;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;
import static com.victoria.foodconnect.utils.NotificationChannelClass.SYNCH_NOTIFICATION_CHANNEL;
import static com.victoria.foodconnect.utils.NotificationChannelClass.USER_NOTIFICATION_CHANNEL;
import static com.victoria.foodconnect.utils.NotificationChannelClass.getPopUri;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.HasDefaultViewModelProviderFactory;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.broadcast.UpdateBroadcast;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.notificationsDb.NotificationsViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.DataOpts;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import io.vertx.core.json.JsonArray;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationService extends LifecycleService implements ViewModelStoreOwner, HasDefaultViewModelProviderFactory {


    final ViewModelStore mViewModelStore = new ViewModelStore();
    private ViewModelProvider.Factory mFactory;
    private Domain.AppUser myUser;
    public static boolean locationServiceRunning = false;
    private NotificationManager notificationManager;
    private NotificationsViewModel notificationsViewModel;
    public static LatLng myLocation = null;
    public static MutableLiveData<Optional<LatLng>> locationMutableLiveData = new MutableLiveData<>();
    private final LinkedHashSet<Models.NotificationModels> notificationList = new LinkedHashSet<>();

    public LocationService() {

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        System.out.println("Location Service STARTED START");

        locationServiceRunning = true;
        notificationsViewModel = getDefaultViewModelProviderFactory().create(NotificationsViewModel.class);
        notificationManager = getSystemService(NotificationManager.class);


        if (userRepository != null) {
            userRepository.getUserLive().observe(this, optionalUser -> optionalUser.ifPresent(user -> {
                myUser = user;
                getDeviceLocation();
                //getAllNotifications(user.getUsername());
            }));
        }

        return START_STICKY;
    }


    private void getDeviceLocation () {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) { //check if location is allowed
            try {
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void run() {
                        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(LocationService.this);
                        final Task<Location> location = mFusedLocationProviderClient.getLastLocation();
                        location.addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Location currentLocation = task.getResult();
                                if (currentLocation != null) {
                                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                                    myLocation = latLng;
                                    locationMutableLiveData.setValue(Optional.of(latLng));
                                    LinkedHashMap<String, String> locationMap = new LinkedHashMap<>();
                                    locationMap.put(LATITUDE, String.valueOf(latLng.latitude));
                                    locationMap.put(LONGITUDE, String.valueOf(latLng.longitude));
                                }
                            }
                        });
                    }
                }, 0, 10000);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {

        }
    }

    //get notifications per 10 sec
    private void getAllNotifications(String username) {

        try {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() { notificationApi.getMyNotifications(username, DataOpts.getAccessToken(application)).enqueue(new Callback<JsonResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {

                            JsonResponse jsonResponse = response.body();

                            if (response.code() != 200 || jsonResponse == null || jsonResponse.isHas_error() || !jsonResponse.isSuccess() || jsonResponse.getData() == null) {
                                return;
                            }

                            try {
                                JsonArray users = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));
                                users.forEach(u -> {
                                    try {
                                        Models.NotificationModels models = getObjectMapper().readValue(u.toString(), Models.NotificationModels.class);
                                        notificationList.add(models);
                                        showNotification(models);
                                    } catch (JsonProcessingException e) {
                                        e.printStackTrace();
                                    }
                                });
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                            t.printStackTrace();
                        }
                    });

                }
            }, 0, 10000);
        } catch (NullPointerException ignore) {

        }
    }

    private void showNotification(Models.NotificationModels notificationModel) {
        //todo change logo
        if (notificationModel.isNotified()) {
            return;
        }

        Intent notificationIntent = new Intent(this, LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE);

        sendBroadcast(new Intent(this, UpdateBroadcast.class).setAction(UPDATE_INTENT).putExtra(USERNAME, myUser.getUsername()).putExtra(UPDATE, notificationModel.getUpdating()).putExtra(ROLE, myUser.getRole()));

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, USER_NOTIFICATION_CHANNEL)
                .setContentTitle(notificationModel.getTitle())
                .setContentText(notificationModel.getDescription())
                .setSmallIcon(R.drawable.ic_give_food)
                .setAutoCancel(true)
                .setTimeoutAfter(20000) //20 sec
                .setGroup(SYNCH_NOTIFICATION_CHANNEL)
                .setGroupSummary(true)
                .setOnlyAlertOnce(true)
                .setSubText(notificationModel.getSub_text() != null ? notificationModel.getSub_text() : notificationModel.getUid())
                .setContentIntent(pendingIntent)
                .setSound(getPopUri(getApplicationContext()).getKey(0));


        notificationManager.notify(Integer.parseInt(String.valueOf(notificationModel.getId())), notificationBuilder.build());
        updateNotification(notificationModel);
    }

    private void updateNotification(Models.NotificationModels notificationModel) {
        notificationModel.setNotified(true);
        notificationsViewModel.updateNotificationLiveData(notificationModel.getUid(), notificationModel.getId()).observe(this, notification -> notification.ifPresent(n -> {
            notificationList.remove(notificationModel);
            notificationList.add(notification.get());
        }));
    }


    @NonNull
    @Override
    public ViewModelProvider.Factory getDefaultViewModelProviderFactory() {
        return mFactory != null ? mFactory : (mFactory = new ViewModelProvider.AndroidViewModelFactory(getApplication()));
    }

    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        return mViewModelStore;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationServiceRunning = false;
    }
}