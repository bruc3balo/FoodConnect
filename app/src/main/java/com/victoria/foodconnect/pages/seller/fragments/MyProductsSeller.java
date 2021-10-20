package com.victoria.foodconnect.pages.seller.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.FragmentMyProductsSellerBinding;


public class MyProductsSeller extends Fragment {

    FragmentMyProductsSellerBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMyProductsSellerBinding.inflate(inflater);
        final View v = binding.getRoot();



        return v;
    }
}