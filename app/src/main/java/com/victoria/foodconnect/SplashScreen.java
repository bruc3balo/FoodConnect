package com.victoria.foodconnect;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.UID;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.LOCATION_PERMISSION_CODE;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.STORAGE_PERMISSION_CODE;
import static com.victoria.foodconnect.utils.DataOpts.getDomainUserFromModelUser;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;
import static com.victoria.foodconnect.utils.DataOpts.proceed;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.victoria.foodconnect.databinding.ActivitySplashScreenBinding;
import com.victoria.foodconnect.globals.GlobalRepository;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.login.LoginActivity;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.service.LocationService;
import com.victoria.foodconnect.utils.JsonResponse;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import io.vertx.core.json.JsonObject;


@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    private ActivitySplashScreenBinding splashScreenBinding;
    private final FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> updateUi(firebaseAuth.getCurrentUser());
    private boolean locationGranted = false;
    private boolean storageGranted = false;
    private final ActivityResultLauncher<Intent> gpsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent intent = result.getData();
            // Handle the Intent
            System.out.println("app result received");
            askPermissions();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalRepository.init(getApplication());

        splashScreenBinding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(splashScreenBinding.getRoot());

        showPb();


        setWindowColors(this);

    }

    private void askPermissions() {
        System.out.println("app asking permission");
        if (!locationGranted) {
            getLocationPermission();
        } else if (!storageGranted) {
            getStoragePermission();
        } else if (!isMapsEnabled()) {
            getGpsPermission();
        } else if (isMapsEnabled()) {
            System.out.println("app gps permission granted");
            FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
            new Handler(Looper.myLooper()).postDelayed(() -> FirebaseAuth.getInstance().removeAuthStateListener(authStateListener), 3000);
        } else {
            FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
            new Handler(Looper.myLooper()).postDelayed(() -> FirebaseAuth.getInstance().removeAuthStateListener(authStateListener), 3000);
        }
    }

    private void getGpsPermission() {
        Dialog d = new Dialog(this);
        d.setContentView(R.layout.new_info_layout);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView infoTv = d.findViewById(R.id.newInfoTv);
        infoTv.setText("This application requires GPS to work properly, you need enable it. Flip the switch");
        Button dismiss = d.findViewById(R.id.dismissButton);
        dismiss.setOnClickListener(v -> d.dismiss());
        d.setOnDismissListener(dialog -> getGpsResult());
        d.show();

    }

    private void getGpsResult() {
        Intent enableGps = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        gpsLauncher.launch(enableGps);
    }

    private boolean isMapsEnabled() {
        System.out.println("app gps permission");
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    private void getLocationPermission() {
        System.out.println("app location permission");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) { //check if location is allowed
            ActivityCompat.requestPermissions(SplashScreen.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            locationGranted = true;
            System.out.println("app location granted");
            askPermissions();
        }
    }

    private void getStoragePermission() {
        System.out.println("app storage permission");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        } else {
            storageGranted = true;
            System.out.println("app storage permission granted");
            askPermissions();
        }
    }


    public static void logout(Activity activity) {
        System.out.println("Logging out user");
        FirebaseAuth.getInstance().signOut();
        userRepository.deleteAppUserDb();
        activity.stopService(new Intent(activity, LocationService.class));
        activity.startActivity(new Intent(activity, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        activity.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case LOCATION_PERMISSION_CODE:
                locationGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                askPermissions();

                break;

            case STORAGE_PERMISSION_CODE:
                storageGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                askPermissions();
                break;

        }
    }


    private void showPb() {
        splashScreenBinding.splashScreenPb.setVisibility(View.VISIBLE);
    }

    private void hidePb() {
        splashScreenBinding.splashScreenPb.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        askPermissions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }

    private void updateUi(FirebaseUser user) {

        if (user == null) {
            Toast.makeText(this, "Sign in to continue", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
            logout(SplashScreen.this);
            return;
        }


        userRepository.getUserLive().observe(this, appUser -> {
            if (!appUser.isPresent()) { //if user no data -> get user data

                HashMap<String, String> params = new HashMap<>();
                params.put(UID, user.getUid());


                final UserViewModel userViewModel = new ViewModelProvider(SplashScreen.this).get(UserViewModel.class);
                userViewModel.getLiveUser(params).observe(SplashScreen.this, jsonResponseResponse -> {
                    if (!jsonResponseResponse.isPresent()) {
                        logout(SplashScreen.this);
                        return;
                    }

                    JsonResponse response = jsonResponseResponse.get().body();

                    if (response == null || response.isHas_error() || response.getData() == null) {
                        logout(SplashScreen.this);
                        return;
                    }

                    ObjectMapper mapper = getObjectMapper();

                    try {
                        JsonObject userJson = new JsonObject(mapper.writeValueAsString(response.getData()));

                        //save user to offline db
                        Models.AppUser firebaseDbUser = mapper.readValue(userJson.toString(), Models.AppUser.class);

                        userRepository.insert(getDomainUserFromModelUser(firebaseDbUser));

                        Thread.sleep(2000);

                        proceed(SplashScreen.this);

                    } catch (JsonProcessingException | InterruptedException e) {

                        if (e instanceof JsonProcessingException) {
                            Toast.makeText(SplashScreen.this, "Problem mapping user data", Toast.LENGTH_SHORT).show();
                            logout(SplashScreen.this);

                            e.printStackTrace();
                        }

                    }
                });


                return;
            }

            proceed(SplashScreen.this);

        });

    }
}