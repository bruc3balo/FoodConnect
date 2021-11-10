package com.victoria.foodconnect.pages.seller;

import static com.victoria.foodconnect.SplashScreen.logout;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.utils.DataOpts.doIHavePermission;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.ActivitySellerBinding;
import com.victoria.foodconnect.login.VerifyAccount;
import com.victoria.foodconnect.pages.seller.fragments.MyOrdersSeller;
import com.victoria.foodconnect.pages.seller.fragments.MyProductsSeller;
import com.victoria.foodconnect.utils.DataOpts;

public class SellerActivity extends AppCompatActivity {

    ActivitySellerBinding binding;
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

        userRepository.getUserLive().observe(this, appUser -> {
            if (appUser.isPresent()) {
                toolbar.setTitle(appUser.get().getUsername());
                toolbar.setSubtitle(appUser.get().getRole());
            }
        });

        NavigationView sellerDrawer = binding.sellerNavigation;

        //addFragmentToContainer(getSupportFragmentManager(), binding.sellerDrawerFragment, new MyProductsSeller());

        sellerDrawer.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                default:
                case R.id.myProducts:
                    addFragmentToContainer(getSupportFragmentManager(), binding.sellerDrawerFragment, new MyProductsSeller());
                    closeDrawer(drawerLayout);
                    break;

                case R.id.myOrders:
                    addFragmentToContainer(getSupportFragmentManager(), binding.sellerDrawerFragment, new MyOrdersSeller());
                    closeDrawer(drawerLayout);
                    break;
            }
            return false;
        });

        FloatingActionButton fab = binding.addProduct;
        fab.setOnClickListener(view -> startActivity(new Intent(SellerActivity.this,AddNewProduct.class)));

        setWindowColors(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Logout").setIcon(R.drawable.logout).setOnMenuItemClickListener(menuItem -> {
            logout(SellerActivity.this);
            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        backPressed = false;
        addFragmentToContainer(getSupportFragmentManager(), binding.sellerDrawerFragment, new MyProductsSeller());

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