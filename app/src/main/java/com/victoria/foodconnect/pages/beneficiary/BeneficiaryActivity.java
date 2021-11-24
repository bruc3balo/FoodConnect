package com.victoria.foodconnect.pages.beneficiary;

import static com.victoria.foodconnect.SplashScreen.logout;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.adapter.BuyProductRvAdapter;
import com.victoria.foodconnect.databinding.ActivityBeneficiaryBinding;
import com.victoria.foodconnect.globals.productDb.ProductViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pagerTransformers.DepthPageTransformer;
import com.victoria.foodconnect.pagerTransformers.ForegroundToBackgroundPageTransformer;
import com.victoria.foodconnect.pagerTransformers.ZoomInTransformer;
import com.victoria.foodconnect.pagerTransformers.ZoomOutPageTransformer;
import com.victoria.foodconnect.utils.MyLinkedMap;

import java.util.LinkedList;

public class BeneficiaryActivity extends AppCompatActivity {

    private ActivityBeneficiaryBinding binding;
    private BuyProductRvAdapter buyProductRvAdapter;
    private final MyLinkedMap<String, LinkedList<Models.Product>> productLinkedList = new MyLinkedMap<>();
    private ProductViewModel productViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBeneficiaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        userRepository.getUserLive().observe(this, appUser -> {
            if (appUser.isPresent()) {
                toolbar.setTitle(appUser.get().getUsername());
                toolbar.setSubtitle(appUser.get().getRole());
            }
        });

        ViewPager2 productsRv = binding.productsRv;
        buyProductRvAdapter = new BuyProductRvAdapter(this,productLinkedList);
        productsRv.setAdapter(buyProductRvAdapter);

        productsRv.setUserInputEnabled(true);
        productsRv.setPageTransformer(new ZoomInTransformer());
        productsRv.setOffscreenPageLimit(3);
        productsRv.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        productsRv.requestTransform();
        productsRv.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        productsRv.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                System.out.println("positionOffset : "+positionOffset + " // positionOffsetPixels : "+positionOffsetPixels);
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });


        populateProducts();

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


    @SuppressLint("NotifyDataSetChanged")
    private void populateProducts () {
        productViewModel.getAllBuyerProducts().observe(this, products -> {
            productLinkedList.clear();
            productLinkedList.putAll(products);
            buyProductRvAdapter.notifyDataSetChanged();
        });
    }


}