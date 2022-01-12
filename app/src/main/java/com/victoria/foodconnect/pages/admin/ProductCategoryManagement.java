package com.victoria.foodconnect.pages.admin;

import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.adapter.ProductCategoryRvAdapter;
import com.victoria.foodconnect.databinding.ActivityProductCategoryManagmentBinding;
import com.victoria.foodconnect.globals.productDb.ProductViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.LinkedList;
import java.util.Optional;

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
        toolbar.setNavigationOnClickListener(v -> finish());

        productCategoryRvAdapter = new ProductCategoryRvAdapter(ProductCategoryManagement.this, productCategoryList);

        RecyclerView categoriesRv = binding.categoriesRv;
        categoriesRv.setLayoutManager(new LinearLayoutManager(ProductCategoryManagement.this, RecyclerView.VERTICAL, false));
        categoriesRv.setAdapter(productCategoryRvAdapter);

        getProductCategories();

        setWindowColors(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Add").setIcon(R.drawable.add).setOnMenuItemClickListener(item -> {
            showAddCategoryDialog();
            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    private void addCategory(String name) {
        new ViewModelProvider(this).get(ProductViewModel.class).createNewProductCategoryLive(name).observe(this, jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                outSpinnerProgress(binding.pb,null);
                Toast.makeText(getApplicationContext(), "Failed to create category", Toast.LENGTH_SHORT).show();
                return;
            }

            getProductCategories();
        });
    }

    private void showAddCategoryDialog() {
        Dialog d = new Dialog(this);
        d.setContentView(R.layout.single_value_layout);

        EditText name = d.findViewById(R.id.name);
        Button confirm = d.findViewById(R.id.confirm_button);
        ImageButton dismiss = d.findViewById(R.id.cancel);

        dismiss.setOnClickListener(v -> d.dismiss());
        confirm.setOnClickListener(v -> {
            if (name.getText().toString().isEmpty()) {
                name.setError("Category name required");
                name.requestFocus();
            } else {
                d.dismiss();
                addCategory(name.getText().toString());
            }
        });


        d.show();
    }

    public void getProductCategories() {
        System.out.println("GET PRODUCT CATEGORY DATA");
        new ViewModelProvider(this).get(ProductViewModel.class).getAllProductCategoriesLive().observe(this, jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                outSpinnerProgress(binding.pb,null);
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

                outSpinnerProgress(binding.pb,null);


            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }


}