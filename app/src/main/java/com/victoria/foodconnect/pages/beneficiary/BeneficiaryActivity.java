package com.victoria.foodconnect.pages.beneficiary;

import static com.victoria.foodconnect.SplashScreen.logout;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.ActivityBeneficiaryBinding;

public class BeneficiaryActivity extends AppCompatActivity {

    ActivityBeneficiaryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBeneficiaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        userRepository.getUserLive().observe(this, appUser -> {
            if (appUser != null) {
                toolbar.setTitle(appUser.getUsername());
                toolbar.setSubtitle(appUser.getRole());
            }
        });

        setWindowColors(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Logout").setIcon(R.drawable.logout).setOnMenuItemClickListener(menuItem -> {
            logout(BeneficiaryActivity.this);
            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }


}