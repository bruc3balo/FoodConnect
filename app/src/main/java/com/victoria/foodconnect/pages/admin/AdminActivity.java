package com.victoria.foodconnect.pages.admin;

import static com.victoria.foodconnect.SplashScreen.logout;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.seller.SellerActivity.addFragmentToContainer;
import static com.victoria.foodconnect.pages.seller.SellerActivity.closeDrawer;
import static com.victoria.foodconnect.pages.seller.SellerActivity.isDrawerOpen;

import android.annotation.SuppressLint;
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
import androidx.lifecycle.Observer;

import com.google.android.material.navigation.NavigationView;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.ActivityAdminBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.pages.admin.fragments.ProductsFragment;
import com.victoria.foodconnect.pages.beneficiary.BeneficiaryActivity;
import com.victoria.foodconnect.pages.seller.fragments.MyOrdersSeller;
import com.victoria.foodconnect.pages.seller.fragments.MyProductsSeller;


public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private boolean backPressed = false;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DrawerLayout drawerLayout = binding.getRoot();

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

        //addFragmentToContainer(getSupportFragmentManager(), binding.adminFragment, new ProductsFragment());

        NavigationView sellerDrawer = binding.adminNavigation;
        sellerDrawer.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                default:
                case R.id.products:
                    addFragmentToContainer(getSupportFragmentManager(), binding.adminFragment, new ProductsFragment());
                    closeDrawer(drawerLayout);
                    break;

              /*  case R.id.myOrders:
                    addFragmentToContainer(getSupportFragmentManager(), binding.adminFragment, new MyOrdersSeller());
                    closeDrawer(drawerLayout);
                    break;*/
            }
            return false;
        });

        setWindowColors(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Logout").setIcon(R.drawable.logout).setOnMenuItemClickListener(menuItem -> {
            logout(AdminActivity.this);
            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        backPressed = false;
        addFragmentToContainer(getSupportFragmentManager(), binding.adminFragment, new MyProductsSeller());

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