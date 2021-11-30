package com.victoria.foodconnect.pages.beneficiary;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.victoria.foodconnect.adapter.OrderRvAdapter;
import com.victoria.foodconnect.databinding.ActivityBuyOrdersBinding;


public class BuyOrders extends AppCompatActivity {

    private ActivityBuyOrdersBinding binding;
    private OrderRvAdapter orderRvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBuyOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        RecyclerView ordersRv = binding.ordersRv;
        ordersRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        orderRvAdapter = new OrderRvAdapter(this,null);
        ordersRv.setAdapter(orderRvAdapter);

    }

}