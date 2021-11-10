package com.victoria.foodconnect.pages.donor;

import static com.victoria.foodconnect.SplashScreen.logout;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.ActivityDonorBinding;

public class DonorActivity extends AppCompatActivity {

    private ActivityDonorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDonorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        userRepository.getUserLive().observe(this, appUser -> {
            if (appUser.isPresent()) {
                toolbar.setTitle(appUser.get().getUsername());
                toolbar.setSubtitle(appUser.get().getRole());

                if (appUser.get().getProfile_picture().equals(HY)) {
                    binding.donorStatus.setTextColor(Color.RED);
                    binding.donorStatus.setText("You need to upload your picture to be verified .\n Click here to upload your picture");
                    binding.donorStatus.setOnClickListener(v -> goToProfile());
                } else {
                    if (appUser.get().isVerified()) {
                        binding.donorStatus.setTextColor(Color.GREEN);
                        binding.donorStatus.setText("Verified");
                    } else {
                        binding.donorStatus.setTextColor(Color.DKGRAY);
                        binding.donorStatus.setText("Pending verification");
                    }
                }

            }
        });

        setWindowColors(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Logout").setIcon(R.drawable.logout).setOnMenuItemClickListener(menuItem -> {
            logout(DonorActivity.this);
            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add("Profile").setIcon(R.drawable.ic_person).setOnMenuItemClickListener(menuItem -> {
            goToProfile();
            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    private void goToProfile() {
        startActivity(new Intent(DonorActivity.this,DonorProfile.class));
    }
}