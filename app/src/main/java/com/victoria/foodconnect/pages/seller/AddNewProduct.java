package com.victoria.foodconnect.pages.seller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.victoria.foodconnect.databinding.ActivityAddNewProductBinding;

public class AddNewProduct extends AppCompatActivity {

    ActivityAddNewProductBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddNewProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


    }
}