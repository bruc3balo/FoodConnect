package com.victoria.foodconnect.pages.admin.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.FragmentProductsBinding;


public class ProductsFragment extends Fragment {

    FragmentProductsBinding binding;

    public ProductsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentProductsBinding.inflate(inflater);
        final View v = binding.getRoot();
        return v;
    }
}