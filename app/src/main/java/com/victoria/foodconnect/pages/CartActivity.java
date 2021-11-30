package com.victoria.foodconnect.pages;

import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.adapter.CartRvAdapter;
import com.victoria.foodconnect.databinding.ActivityCartBinding;
import com.victoria.foodconnect.globals.cartDb.CartViewMode;
import com.victoria.foodconnect.globals.productDb.ProductViewModel;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.models.Models;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class CartActivity extends AppCompatActivity {

    private ActivityCartBinding cartBinding;
    private final LinkedList<Models.Product> allProducts = new LinkedList<>();
    private final LinkedList<Models.Cart> cartList = new LinkedList<>();
    private CartRvAdapter cartRvAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

            showDialog();
        });


        setWindowColors(this);
    }

    private void showDialog() {
        Dialog d = new Dialog(this);
        d.setContentView(R.layout.checkout_layout);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView total = d.findViewById(R.id.total);
        Button order = d.findViewById(R.id.orderButton);
        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ImageButton cancel_button = d.findViewById(R.id.cancel_button);
        cancel_button.setOnClickListener(v -> d.dismiss());

        d.setOnShowListener(dialog -> total.setText(calculateTotal() + " KSH"));


        d.show();
    }

    private BigDecimal calculateTotal() {
        final BigDecimal[] total = {new BigDecimal(0)};
        cartList.forEach(c -> allProducts.stream().filter(i -> i.getId().equals(c.getProductId())).findFirst().ifPresent(value -> total[0] = total[0].add(new BigDecimal(c.getNumberOfItems()).multiply(value.getPrice()))));
        return total[0];
    }

    private void getAllProducts() {
        new ViewModelProvider(this).get(ProductViewModel.class).getAllGoodBuyerProducts().observe(this, products -> {
            if (products.isEmpty()) {
                Toast.makeText(CartActivity.this, "Failed to get products", Toast.LENGTH_SHORT).show();
                return;
            }

            allProducts.clear();
            allProducts.addAll(products);
            cartRvAdapter.notifyDataSetChanged();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void getCartItems() {
        new ViewModelProvider(this).get(CartViewMode.class).getCartList().observe(this, carts -> {
            cartList.clear();
            cartList.addAll(carts);
            cartRvAdapter.notifyDataSetChanged();

            if (carts.isEmpty()) {
                cartBinding.checkoutButton.setVisibility(View.VISIBLE);
            } else {
                cartBinding.checkoutButton.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cartList.clear();
    }
}