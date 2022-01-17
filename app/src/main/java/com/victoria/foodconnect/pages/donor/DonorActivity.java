package com.victoria.foodconnect.pages.donor;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.seller.SellerActivity.addFragmentToContainer;
import static com.victoria.foodconnect.pages.seller.SellerActivity.closeDrawer;
import static com.victoria.foodconnect.pages.seller.SellerActivity.isDrawerOpen;
import static com.victoria.foodconnect.pages.seller.SellerActivity.setNavDetails;

import android.content.Intent;
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
import com.victoria.foodconnect.databinding.ActivityDonorBinding;
import com.victoria.foodconnect.pages.donor.fragments.DonationsFragment;
import com.victoria.foodconnect.pages.donor.fragments.DonorStatsFragment;
import com.victoria.foodconnect.pages.transporter.fragments.JobsFragment;

public class DonorActivity extends AppCompatActivity {

    private ActivityDonorBinding binding;
    private boolean backPressed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDonorBinding.inflate(getLayoutInflater());
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

        NavigationView donorNavigation = binding.donorNavigation;
        View header = donorNavigation.getHeaderView(0);
        donorNavigation.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                default:
                case R.id.donations:
                    binding.subText.setVisibility(View.VISIBLE);
                    binding.subText.setText("Donations");
                    addFragmentToContainer(getSupportFragmentManager(), binding.donorDrawerFragment, new DonationsFragment());
                    closeDrawer(drawerLayout);
                    break;

                    case R.id.stats:
                        binding.subText.setVisibility(View.VISIBLE);
                        binding.subText.setText("Statistics");
                        addFragmentToContainer(getSupportFragmentManager(), binding.donorDrawerFragment, new DonorStatsFragment());
                        closeDrawer(drawerLayout);
                        break;

            }
            return false;
        });

        userRepository.getUserLive().observe(this, appUser -> {
            appUser.ifPresent(user -> {
                binding.text.setText("Welcome " + user.getUsername());
                setNavDetails(appUser.get(), header, DonorActivity.this);

            });
        });

        binding.addItem.setOnClickListener(v -> goToAddItem());

        setWindowColors(this);
    }

    private void goToAddItem() {
        startActivity(new Intent(DonorActivity.this,AddItemDonor.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Profile").setIcon(R.drawable.ic_person_black).setOnMenuItemClickListener(menuItem -> {
            goToProfile();
            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    private void goToProfile() {
        startActivity(new Intent(DonorActivity.this,DonorProfile.class));
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

    @Override
    protected void onResume() {
        super.onResume();
        backPressed = false;

        if (getSupportFragmentManager().getFragments().isEmpty()) {
            binding.subText.setVisibility(View.VISIBLE);
            binding.subText.setText("Donations");
            addFragmentToContainer(getSupportFragmentManager(), binding.donorDrawerFragment, new DonationsFragment());
        }
    }
}