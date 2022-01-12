package com.victoria.foodconnect.service;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.LATITUDE;
import static com.victoria.foodconnect.globals.GlobalVariables.LONGITUDE;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.HasDefaultViewModelProviderFactory;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.victoria.foodconnect.domain.Domain;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends LifecycleService implements ViewModelStoreOwner, HasDefaultViewModelProviderFactory {


    final ViewModelStore mViewModelStore = new ViewModelStore();
    private ViewModelProvider.Factory mFactory;
    private Domain.AppUser myUser;
    public static boolean locationServiceRunning = false;

    public static LatLng myLocation = null;
    public static MutableLiveData<Optional<LatLng>> locationMutableLiveData = new MutableLiveData<>();

    public LocationService() {

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        System.out.println("Location Service STARTED START");

        locationServiceRunning = true;

        if (userRepository != null) {
            userRepository.getUserLive().observe(this, optionalUser -> optionalUser.ifPresent(user -> {
                myUser = user;
                getDeviceLocation();
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