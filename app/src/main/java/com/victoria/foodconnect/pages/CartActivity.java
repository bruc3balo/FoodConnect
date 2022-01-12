package com.victoria.foodconnect.pages;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.USERNAME;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.LocationOrder.CARTLIST;
import static com.victoria.foodconnect.pages.LocationOrder.PRODUCTLIST;
import static com.victoria.foodconnect.pages.LocationOrder.successOrder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.victoria.foodconnect.adapter.CartRvAdapter;
import com.victoria.foodconnect.databinding.ActivityCartBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.cartDb.CartViewMode;
import com.victoria.foodconnect.globals.productDb.ProductViewModel;
import com.victoria.foodconnect.models.Models;

import java.util.LinkedList;

public class CartActivity extends AppCompatActivity {

    private ActivityCartBinding cartBinding;
    private final LinkedList<Models.Product> allProducts = new LinkedList<>();
    private final LinkedList<Models.Cart> cartList = new LinkedList<>();
    private CartRvAdapter cartRvAdapter;
    private Domain.AppUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        successOrder = false;

        cartBinding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(cartBinding.getRoot());

        setSupportActionBar(cartBinding.toolbar);
        cartBinding.toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView cartRv = cartBinding.cartRv;
        cartRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        cartRvAdapter = new CartRvAdapter(this, cartList, allProducts);
        cartRv.setAdapter(cartRvAdapter);

        getAllProducts();

        getCartItems();

        Button checkout = cartBinding.checkoutButton;

        checkout.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(CartActivity.this, "No items in cart", Toast.LENGTH_SHORT).show();
                return;
            }

            if (allProducts.isEmpty()) {
                Toast.makeText(CartActivity.this, "Waiting", Toast.LENGTH_SHORT).show();
                return;
            }

            if (cartList.stream().anyMatch(i -> i.getNumberOfItems() == 0)) {
                Toast.makeText(CartActivity.this, "You cannot order 0 items", Toast.LENGTH_SHORT).show();
                return;
            }

            showLocationDialog();
        });

        userRepository.getUserLive().observe(this, appUser -> appUser.ifPresent(u -> {
            user = u;
            cartBinding.toolbar.setTitle(u.getUsername()+"'s Cart");
        }));

        setWindowColors(this);
    }

    private void showLocationDialog() {
        startActivity(new Intent(this, LocationOrder.class).putExtra(CARTLIST, cartList).putExtra(PRODUCTLIST, allProducts).putExtra(USERNAME, user.getUsername()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (successOrder) {
            finish();
        }
    }


    private void startShimmer() {
        cartBinding.shimmerViewContainer.startShimmer();
        cartBinding.shimmerViewContainer.setVisibility(View.VISIBLE);
        cartBinding.cartRv.setVisibility(View.GONE);
    }

    private void stopShimmer() {
        cartBinding.shimmerViewContainer.setVisibility(View.GONE);
        cartBinding.shimmerViewContainer.stopShimmer();
        cartBinding.cartRv.setVisibility(View.VISIBLE);
    }


    private void getAllProducts() {
        startShimmer();
        new ViewModelProvider(this).get(ProductViewModel.class).getAllGoodBuyerProducts().observe(this, products -> {
            stopShimmer();
            if (products.isEmpty()) {
                Toast.makeText(CartActivity.this, "Failed to get products", Toast.LENGTH_SHORT).show();
                return;
            }


            allProducts.clear();
            allProducts.addAll(products);
            cartRvAdapter.notifyDataSetChanged();

            cartBinding.checkoutButton.setEnabled(!allProducts.isEmpty());
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void getCartItems() {
        new ViewModelProvider(this).get(CartViewMode.class).getCartList().observe(this, carts -> {
            cartList.clear();
            cartList.addAll(carts);
            cartRvAdapter.notifyDataSetChanged();

            if (cartList.isEmpty()) {
                cartBinding.checkoutButton.setVisibility(View.GONE);
            } else {
                cartBinding.checkoutButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cartList.clear();
    }
}