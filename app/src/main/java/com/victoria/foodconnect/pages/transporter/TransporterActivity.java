package com.victoria.foodconnect.pages.transporter;

import static com.victoria.foodconnect.SplashScreen.logout;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.ActivityTransporterBinding;
import com.victoria.foodconnect.pages.beneficiary.BeneficiaryActivity;

public class TransporterActivity extends AppCompatActivity {

    ActivityTransporterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransporterBinding.inflate(getLayoutInflater());
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
        menu.add("Logout").setIcon(R.drawable.logout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                logout(TransporterActivity.this);
                return false;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }
}