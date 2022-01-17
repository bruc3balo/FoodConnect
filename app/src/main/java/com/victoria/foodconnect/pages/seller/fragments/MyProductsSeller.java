package com.victoria.foodconnect.pages.seller.fragments;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.ID;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_COLLECTION;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;
import static com.victoria.foodconnect.utils.SimilarityClass.alike;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.adapter.ProductRvAdapter;
import com.victoria.foodconnect.databinding.FragmentMyProductsSellerBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.productDb.ProductViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.beneficiary.BeneficiaryActivity;
import com.victoria.foodconnect.pages.seller.ManageProduct;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class MyProductsSeller extends Fragment {

    private FragmentMyProductsSellerBinding binding;
    private ArrayAdapter<String> adapter;
    private final LinkedList<String> categoryList = new LinkedList<>();
    private ProductRvAdapter productRvAdapter;
    private ProductViewModel productViewModel;
    private final LinkedList<Models.Product> productList = new LinkedList<>();
    private final LinkedList<Models.Product> allProductList = new LinkedList<>();

    private Domain.AppUser user;
    private boolean requested = false;
    public static MutableLiveData<Optional<Boolean>> refreshProductsSeller = new MutableLiveData<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        binding = FragmentMyProductsSellerBinding.inflate(inflater);
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

        RecyclerView productsRv = binding.productsRv;
        productsRv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        productRvAdapter = new ProductRvAdapter(requireContext(), productList);
        productsRv.setAdapter(productRvAdapter);
        productRvAdapter.setClickListener((view, position) -> startActivity(new Intent(requireContext(), ManageProduct.class).putExtra(PRODUCT_COLLECTION, productList.get(position))));

        userRepository.getUserLive().observe(getViewLifecycleOwner(), appUser -> {
            if (appUser.isPresent()) {
                user = appUser.get();
                if (!requested) {
                    getProductCategories();
                }

            } else {
                Toast.makeText(requireContext(), "Failed to get user", Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchViewItem = menu.findItem(R.id.app_bar_search);
        final SearchView searchView = (SearchView) searchViewItem.getActionView();
        searchView.setQueryHint("Search by product name");

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> Toast.makeText(requireContext(), "" + hasFocus, Toast.LENGTH_SHORT).show());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                new Handler().post(() -> {
                    try {
                        searchProducts(query);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                return false;
            }
        });

        searchView.setOnSearchClickListener(v -> searchProducts(""));

        searchView.setOnCloseListener(() -> {
            searchProducts("");
            return false;
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void searchProducts(String query) {
        productList.clear();
        productRvAdapter.notifyDataSetChanged();

        if (query.isEmpty()) {
            productList.clear();
            productList.addAll(allProductList);
            productRvAdapter.notifyDataSetChanged();
        } else {
            productList.clear();
            productRvAdapter.notifyDataSetChanged();
            allProductList.forEach(p -> {
                if (alike(query, p.getName())) {
                    System.out.println(p.getProduct_category().getName() + " : " + p.getName());
                    productList.add(p);
                    productRvAdapter.notifyDataSetChanged();
                }
            });
        }
    }



    private void getProductCategories() {
        requested = true;
        inSpinnerProgress(binding.pb,null);
        System.out.println("GET PRODUCT CATEGORY DATA");
        productViewModel.getAllProductCategoriesLive().observe(requireActivity(), jsonResponse -> {

            if (!jsonResponse.isPresent()) {
                outSpinnerProgress(binding.pb,null);
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

                getProducts();

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    private void getProducts() {

        System.out.println("GET PRODUCT DATA");

        productViewModel.getAllSellerProductsLive(user.getUsername()).observe(requireActivity(), jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                outSpinnerProgress(binding.pb,null);
                Toast.makeText(requireContext(), "No products", Toast.LENGTH_SHORT).show();
                return;
            }

            allProductList.clear();

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


                outSpinnerProgress(binding.pb,null);


                filterProducts(binding.productCategorySpinner.getSelectedItem().toString());

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")

    //todo change buyer to beneficiary
    private void filterProducts(String productCategory) {
        productList.clear();
        productRvAdapter.notifyDataSetChanged();

        allProductList.forEach(p -> {
            if (p.getProduct_category().getName().equals(productCategory)) {
                productList.add(p);
                productRvAdapter.notifyDataSetChanged();
            }
        });
    }




    private void addRefreshListener() {
        refreshData().observe(this, refresh -> {
            if (refresh.isPresent()) {
                if (user != null) {
                    getProductCategories();
                }
            }
        });
    }

    private void removeListeners() {
        refreshData().removeObservers(this);
    }

    //listener for updates
    private LiveData<Optional<Boolean>> refreshData() {
        return refreshProductsSeller;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (user != null) {
            getProductCategories();
        }

        addRefreshListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeListeners();
    }
}