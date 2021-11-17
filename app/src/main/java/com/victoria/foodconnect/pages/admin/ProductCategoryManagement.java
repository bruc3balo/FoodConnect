package com.victoria.foodconnect.pages.admin;

import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.adapter.ProductCategoryRvAdapter;
import com.victoria.foodconnect.databinding.ActivityProductCategoryManagmentBinding;
import com.victoria.foodconnect.globals.productDb.ProductViewModel;
import com.victoria.foodconnect.models.Models;

import java.util.LinkedList;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ProductCategoryManagement extends AppCompatActivity {

    private ActivityProductCategoryManagmentBinding binding;
    private final LinkedList<Models.ProductCategory> productCategoryList = new LinkedList<>();

    private ProductCategoryRvAdapter productCategoryRvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProductCategoryManagmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v->finish());

        productCategoryRvAdapter = new ProductCategoryRvAdapter(ProductCategoryManagement.this, productCategoryList);

        RecyclerView categoriesRv = binding.categoriesRv;
        categoriesRv.setLayoutManager(new LinearLayoutManager(ProductCategoryManagement.this, RecyclerView.VERTICAL, false));
        categoriesRv.setAdapter(productCategoryRvAdapter);

        getProductCategories();

        setWindowColors(this);
    }

    public void getProductCategories() {
        System.out.println("GET PRODUCT CATEGORY DATA");
        new ViewModelProvider(this).get(ProductViewModel.class).getAllProductCategoriesLive().observe(this, jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                Toast.makeText(this, "No product categories", Toast.LENGTH_SHORT).show();
                return;
            }

            productCategoryList.clear();
            productCategoryRvAdapter.notifyDataSetChanged();
            try {
                JsonArray serviceArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.get().getData()));

                for (int i = 0; i < serviceArray.size(); i++) {

                    try {
                        System.out.println("count " + i);
                        Models.ProductCategory productCategory = getObjectMapper().readValue(new JsonObject(serviceArray.getJsonObject(i).getMap()).toString(), Models.ProductCategory.class);
                        productCategoryList.add(productCategory);
                        productCategoryRvAdapter.notifyItemInserted(i);

                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                }

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }


}