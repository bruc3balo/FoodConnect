package com.victoria.foodconnect.pages.seller;

import static com.victoria.foodconnect.SplashScreen.logout;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.makeramen.roundedimageview.RoundedImageView;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.ActivitySellerBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.pages.seller.fragments.MyOrdersSeller;
import com.victoria.foodconnect.pages.seller.fragments.MyProductsSeller;
import com.victoria.foodconnect.pages.seller.fragments.SellerStatsFragment;

public class SellerActivity extends AppCompatActivity {

    private ActivitySellerBinding binding;
    private boolean backPressed = false;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_hamburger);
        toolbar.setNavigationOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        NavigationView sellerDrawer = binding.sellerNavigation;

        //addFragmentToContainer(getSupportFragmentManager(), binding.sellerDrawerFragment, new MyProductsSeller());

        sellerDrawer.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                default:
                case R.id.myProducts:
                    binding.subText.setVisibility(View.VISIBLE);
                    binding.subText.setText("My products");
                    addFragmentToContainer(getSupportFragmentManager(), binding.sellerDrawerFragment, new MyProductsSeller());
                    closeDrawer(drawerLayout);
                    break;

                case R.id.myOrders:
                    binding.subText.setVisibility(View.VISIBLE);
                    binding.subText.setText("My orders");
                    addFragmentToContainer(getSupportFragmentManager(), binding.sellerDrawerFragment, new MyOrdersSeller());
                    closeDrawer(drawerLayout);
                    break;

                case R.id.stats:
                    binding.subText.setVisibility(View.VISIBLE);
                    binding.subText.setText("Statistics");
                    addFragmentToContainer(getSupportFragmentManager(), binding.sellerDrawerFragment, new SellerStatsFragment());
                    closeDrawer(drawerLayout);
                    break;
            }
            return false;
        });

        View header = sellerDrawer.getHeaderView(0);
        userRepository.getUserLive().observe(this, appUser -> {
            if (appUser.isPresent()) {
                binding.text.setText("Welcome "+appUser.get().getUsername());
                setNavDetails(appUser.get(), header, SellerActivity.this);
            }
        });

        FloatingActionButton fab = binding.addProduct;
        fab.setOnClickListener(view -> startActivity(new Intent(SellerActivity.this, AddNewProduct.class)));

        setWindowColors(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Logout").setIcon(R.drawable.logout).setOnMenuItemClickListener(menuItem -> {
            logout(SellerActivity.this);
            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        backPressed = false;
        if (getSupportFragmentManager().getFragments().isEmpty()) {
            binding.subText.setVisibility(View.VISIBLE);
            binding.subText.setText("My products");
            addFragmentToContainer(getSupportFragmentManager(), binding.sellerDrawerFragment, new MyProductsSeller());
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

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void setNavDetails(Domain.AppUser appUser, View header, Activity activity) {
        TextView email = header.findViewById(R.id.emailNav);
        email.setText(appUser.getEmail_address() != null ? appUser.getEmail_address() : "Email Address");

        TextView username = header.findViewById(R.id.usernameNav);
        username.setText(appUser.getUsername() != null ? appUser.getUsername() : "Username");

        RoundedImageView prof = header.findViewById(R.id.profPicNav);
        Glide.with(activity).load(appUser.getProfile_picture().equals(HY) ? activity.getDrawable(R.drawable.ic_give_food) : appUser.getProfile_picture()).into(prof);

    }

    public static void addFragmentToContainer(FragmentManager fragmentManager, FragmentContainerView containerView, Fragment fragment) {
        if (fragmentManager.getFragments().isEmpty()) {
            fragmentManager.beginTransaction().add(containerView.getId(), fragment).commit();
        } else {
            fragmentManager.beginTransaction().replace(containerView.getId(), fragment).commit();
        }
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
    }

    public static boolean isDrawerOpen(DrawerLayout drawerLayout) {
        return drawerLayout.isDrawerOpen(GravityCompat.START);
    }

}