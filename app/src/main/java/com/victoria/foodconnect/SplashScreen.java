package com.victoria.foodconnect;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.UID;
import static com.victoria.foodconnect.login.LoginActivity.loginPb;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.welcome.WelcomeActivity.goToNextPage;
import static com.victoria.foodconnect.utils.DataOpts.getDomainUserFromModelUser;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;
import static com.victoria.foodconnect.utils.DataOpts.proceed;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.victoria.foodconnect.databinding.ActivitySplashScreenBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.GlobalRepository;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.login.LoginActivity;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.JsonResponse;

import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Optional;

import io.vertx.core.json.JsonObject;
import retrofit2.Response;


@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    private ActivitySplashScreenBinding splashScreenBinding;
    private final FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> updateUi(firebaseAuth.getCurrentUser());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalRepository.init(getApplication());

        splashScreenBinding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(splashScreenBinding.getRoot());

        showPb();


        setWindowColors(this);

    }


    private void goToLoginScreen() {
        startActivity(new Intent(SplashScreen.this, LoginActivity.class));
        finish();
    }

    public static void logout(Activity activity) {
        FirebaseAuth.getInstance().signOut();
        userRepository.deleteAppUserDb();
        activity.startActivity(new Intent(activity, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        activity.finish();
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
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
        new Handler(Looper.myLooper()).postDelayed(() -> FirebaseAuth.getInstance().removeAuthStateListener(authStateListener), 3000);
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
            goToLoginScreen();
            return;
        }

        userRepository.getUserLive().observe(this, appUser -> {
            if (!appUser.isPresent()) { //if user no data -> get user data

                HashMap<String,String> params = new HashMap<>();
                params.put(UID,user.getUid());

                new ViewModelProvider(SplashScreen.this).get(UserViewModel.class).getLiveUser(params).observe(SplashScreen.this, jsonResponseResponse -> {
                    if (!jsonResponseResponse.isPresent()) {
                        logout(SplashScreen.this);
                        return;
                    }

                    JsonResponse response = jsonResponseResponse.get().body();

                    if (response == null) {
                        logout(SplashScreen.this);
                        return;
                    }

                    if (response.isHas_error()) {
                        logout(SplashScreen.this);
                        return;
                    }

                    if (response.getData() == null) {
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