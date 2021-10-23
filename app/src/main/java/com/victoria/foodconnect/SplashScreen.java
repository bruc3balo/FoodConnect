package com.victoria.foodconnect;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.welcome.WelcomeActivity.goToNextPage;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.victoria.foodconnect.databinding.ActivitySplashScreenBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.GlobalRepository;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.login.LoginActivity;

import org.mindrot.jbcrypt.BCrypt;


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
        userRepository.deleteUserDb();
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
        if (user != null) {

            if (userRepository.getUser() == null) {
                UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
                userViewModel.getLiveUser(user.getUid()).observe(this, appUser -> {
                    if (appUser != null) {
                        userRepository.insert(appUser);
                        goToNextPage(SplashScreen.this, userRepository.getUser().getRole());
                    } else {
                        new ViewModelProvider(this).get(UserViewModel.class).getLiveUser(user.getUid()).observe(this, new Observer<Domain.AppUser>() {
                            @Override
                            public void onChanged(Domain.AppUser appUser) {
                                if (appUser != null) {
                                    userRepository.insert(appUser);
                                    goToNextPage(SplashScreen.this, userRepository.getUser().getRole());
                                }
                            }
                        });
                    }
                });
            } else {
                goToNextPage(SplashScreen.this, userRepository.getUser().getRole());
            }
        } else {
            Toast.makeText(this, "Sign in to continue", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
            goToLoginScreen();
        }
    }
}