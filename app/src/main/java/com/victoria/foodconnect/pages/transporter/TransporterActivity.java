package com.victoria.foodconnect.pages.transporter;

import static com.victoria.foodconnect.SplashScreen.logout;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.seller.SellerActivity.addFragmentToContainer;
import static com.victoria.foodconnect.pages.seller.SellerActivity.closeDrawer;
import static com.victoria.foodconnect.pages.seller.SellerActivity.isDrawerOpen;
import static com.victoria.foodconnect.pages.seller.SellerActivity.setNavDetails;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.ActivityTransporterBinding;
import com.victoria.foodconnect.pages.transporter.fragments.DonationJobsFragment;
import com.victoria.foodconnect.pages.transporter.fragments.JobsFragment;

public class TransporterActivity extends AppCompatActivity {

    private ActivityTransporterBinding binding;
    private boolean backPressed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransporterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_hamburger);
        toolbar.setNavigationOnClickListener(view -> binding.getRoot().openDrawer(GravityCompat.START));


        DrawerLayout drawerLayout = binding.getRoot();
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        NavigationView transporterDrawer = binding.transporterNavigation;
        View header = transporterDrawer.getHeaderView(0);

        transporterDrawer.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                default:
                case R.id.jobs:
                    binding.subText.setVisibility(View.VISIBLE);
                    binding.subText.setText("Purchase jobs");
                    addFragmentToContainer(getSupportFragmentManager(), binding.transporterDrawerFragment, new JobsFragment());
                    closeDrawer(drawerLayout);
                    break;

                case R.id.donations:
                    binding.subText.setVisibility(View.VISIBLE);
                    binding.subText.setText("Donation jobs");
                    addFragmentToContainer(getSupportFragmentManager(), binding.transporterDrawerFragment, new DonationJobsFragment());
                    closeDrawer(drawerLayout);
                    break;

            }
            return false;
        });

        userRepository.getUserLive().observe(this, appUser -> {
            if (appUser.isPresent()) {
                binding.text.setText(appUser.get().getUsername());
                setNavDetails(appUser.get(), header, TransporterActivity.this);
            }
        });

        setWindowColors(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Logout").setIcon(R.drawable.logout).setOnMenuItemClickListener(menuItem -> {
            logout(TransporterActivity.this);
            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        backPressed = false;

        if (getSupportFragmentManager().getFragments().isEmpty()) {
            binding.subText.setVisibility(View.VISIBLE);
            binding.subText.setText("Purchase jobs");
            addFragmentToContainer(getSupportFragmentManager(), binding.transporterDrawerFragment, new JobsFragment());
        }
    }

    @Override
    public void onBackPressed() {
        if (isDrawerOpen(binding.getRoot())) {
            closeDrawer(binding.getRoot());
        } else {
            if (!backPressed) {
                backPressed = true;
                Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
            } else {
                super.onBackPressed();
            }
        }
    }

}