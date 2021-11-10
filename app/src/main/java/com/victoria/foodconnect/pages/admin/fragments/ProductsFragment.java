package com.victoria.foodconnect.pages.admin.fragments;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.adapter.ProductRvAdapter;
import com.victoria.foodconnect.databinding.FragmentProductsBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.productDb.ProductViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.admin.ProductCategoryManagement;

import java.util.LinkedList;
import java.util.Optional;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class ProductsFragment extends Fragment {

    private FragmentProductsBinding binding;
    private ArrayAdapter<String> adapter;
    private final LinkedList<String> categoryList = new LinkedList<>();
    private ProductViewModel productViewModel;
    private ProductRvAdapter productRvAdapter;
    private final LinkedList<Models.Product> productList = new LinkedList<>();
    private final LinkedList<Models.Product> allProductList = new LinkedList<>();

    public ProductsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentProductsBinding.inflate(inflater);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        AppCompatSpinner productCategorySpinner = binding.productCategorySpinner;
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, categoryList);
        productCategorySpinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        productCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(requireActivity(), categoryList.get(position), Toast.LENGTH_SHORT).show();
                filterProducts(binding.productCategorySpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.editProductCategory.setOnClickListener(v -> startActivity(new Intent(requireContext(), ProductCategoryManagement.class)));


        RecyclerView productsRv = binding.productsRv;
        productsRv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        productRvAdapter = new ProductRvAdapter(requireContext(), productList);
        productsRv.setAdapter(productRvAdapter);

        userRepository.getUserLive().observe(requireActivity(), new Observer<Optional<Domain.AppUser>>() {
            @Override
            public void onChanged(Optional<Domain.AppUser> appUser) {
                if (!appUser.isPresent()) {
                    Toast.makeText(requireContext(), "Failed to get usre info", Toast.LENGTH_SHORT).show();
                    return;
                }

                getProductCategories(appUser.get().getUsername());
            }
        });


        return binding.getRoot();
    }

    private void getProductCategories(String username) {

        System.out.println("GET PRODUCT CATEGORY DATA");


        productViewModel.getAllProductCategoriesLive().observe(requireActivity(), jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                Toast.makeText(requireContext(), "No product categories", Toast.LENGTH_SHORT).show();
                return;
            }

            categoryList.clear();
            try {
                JsonArray serviceArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.get().getData()));

                for (int i = 0; i < serviceArray.size(); i++) {

                    try {
                        System.out.println("count " + i);
                        Models.ProductCategory productCategory = getObjectMapper().readValue(new JsonObject(serviceArray.getJsonObject(i).getMap()).toString(), Models.ProductCategory.class);
                        categoryList.add(productCategory.getName());
                        adapter.notifyDataSetChanged();

                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                }

                getProducts(username);

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    private void getProducts(String username) {

        System.out.println("GET MY PRODUCT DATA");


        productViewModel.getAllSellerProductsLive(username).observe(requireActivity(), jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                Toast.makeText(requireContext(), "No products", Toast.LENGTH_SHORT).show();
                return;
            }

            productList.clear();
            try {
                JsonArray serviceArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.get().getData()));

                for (int i = 0; i < serviceArray.size(); i++) {

                    try {
                        System.out.println("count " + i);
                        Models.Product product = getObjectMapper().readValue(new JsonObject(serviceArray.getJsonObject(i).getMap()).toString(), Models.Product.class);
                        allProductList.add(product);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                }

                filterProducts(binding.productCategorySpinner.getSelectedItem().toString());

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterProducts(String productCategory) {
        productList.clear();
        productRvAdapter.notifyDataSetChanged();
        allProductList.forEach(p -> {
            if (p.getProduct_category().getName().equals(productCategory)) {
                productList.add(p);
                if (!productList.isEmpty()) {
                    productRvAdapter.notifyItemInserted(productList.size() - 1);
                }
            }
        });
    }


}