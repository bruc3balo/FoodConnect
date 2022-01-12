package com.victoria.foodconnect.login;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.adapter.LoginPagerAdapter;
import com.victoria.foodconnect.pagerTransformers.DepthPageTransformer;

public class LoginActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    public static SpinKitView loginPb;
    private ViewPager2 loginViewPager;
    private TabLayout loginTabLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginPb = findViewById(R.id.pb);
        loginViewPager = findViewById(R.id.loginViewPager);
        loginTabLayout = findViewById(R.id.loginTabLayout);

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        setUpLoginPager();

        setWindowColors(this);
    }


    public static void setWindowColors(Activity activity) {
        activity.getWindow().setStatusBarColor(Color.BLACK);
        activity.getWindow().setNavigationBarColor(Color.BLACK);
    }

    private void setUpLoginPager() {
        LoginPagerAdapter loginPagerAdapter = new LoginPagerAdapter(getSupportFragmentManager(), getLifecycle());
        loginViewPager.setUserInputEnabled(false);
        loginViewPager.setAdapter(loginPagerAdapter);
        loginViewPager.setPageTransformer(new DepthPageTransformer());
        loginTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.setText(loginPagerAdapter.getLoginTitles(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        new TabLayoutMediator(loginTabLayout, loginViewPager, true, true, (tab, position) -> {

        }).attach();
        loginPagerAdapter.setAllTabIcons(loginTabLayout);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

