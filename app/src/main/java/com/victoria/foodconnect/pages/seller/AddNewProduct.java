package com.victoria.foodconnect.pages.seller;

import static com.victoria.foodconnect.globals.GlobalVariables.MEDIA_TYPE;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_COLLECTION;
import static com.victoria.foodconnect.utils.DataOpts.doIHavePermission;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.ActivityAddNewProductBinding;
import com.victoria.foodconnect.globals.productDb.ProductViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.models.Models.ProductCreationFrom;
import com.victoria.foodconnect.service.UploadPictureService;
import com.victoria.foodconnect.utils.DataOpts;

import java.util.ArrayList;
import java.util.LinkedList;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class AddNewProduct extends AppCompatActivity {

    ActivityAddNewProductBinding binding;
    private final ArrayList<Models.ProductCategory> allProductCategories = new ArrayList<>();
    private final ProductCreationFrom newProduct = new ProductCreationFrom();
    private ArrayAdapter<String> adapter;
    private final LinkedList<String> categoryList = new LinkedList<>();


    public static final String[] storagePermissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final int STORAGE_PERMISSION_CODE = 4;

    private ActivityResultLauncher<String> launcher;
    private Uri file;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddNewProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.addNewProductTb;
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v->finish());

        //category
        AppCompatSpinner productCategorySpinner = binding.productCategorySpinner;
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categoryList);
        productCategorySpinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        productCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(AddNewProduct.this, categoryList.get(position), Toast.LENGTH_SHORT).show();
                if (categoryList.contains(productCategorySpinner.getSelectedItem().toString())) {
                    newProduct.setProduct_category_name(productCategorySpinner.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Unit
        RadioGroup unitGroup = binding.unitGroup;
        unitGroup.setOnCheckedChangeListener((radioGroup, id) -> {
            switch (id) {
                default:
                case R.id.solid:
                    String solid = getString(R.string.solid);
                    newProduct.setUnit(solid);
                    Toast.makeText(AddNewProduct.this, solid, Toast.LENGTH_SHORT).show();
                    break;

                case R.id.liquid:
                    String liquid = getString(R.string.liquid);
                    newProduct.setUnit(liquid);
                    Toast.makeText(AddNewProduct.this, liquid, Toast.LENGTH_SHORT).show();

                    break;

                case R.id.gas:
                    String gas = getString(R.string.gas);
                    newProduct.setUnit(gas);
                    Toast.makeText(AddNewProduct.this, gas, Toast.LENGTH_SHORT).show();

                    break;
            }
        });

        //Name
        EditText productName = binding.productNameField;

        //Description
        EditText productDescription = binding.productDescriptionField;

        //Price
        EditText productPrice = binding.productPriceField;

        Button add = binding.addProductButton;
        add.setOnClickListener(view -> {
            if (validateForm(productName, productDescription, productPrice)) {
                saveNewProduct();
            }
        });

        launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), this::postProductPicture);

        binding.getImage.setOnClickListener(view -> getProductImage());
        binding.removeImage.setOnClickListener(view -> {
            Glide.with(AddNewProduct.this).load(R.drawable.ic_image).into(binding.productImagesView);
            file = null;
            newProduct.setImage(null);
        });

        populateProductCategories();

    }

    private void saveNewProduct() {
        startService(new Intent(AddNewProduct.this, UploadPictureService.class).putExtra(PRODUCT_COLLECTION,newProduct).putExtra(MEDIA_TYPE, PRODUCT_COLLECTION));
        finish();
    }

    private boolean validateForm(EditText productName, EditText productDescription, EditText productPrice) {
        boolean valid = false;
        if (productName.getText().toString().isEmpty()) {
            productName.setError("Product name required");
            productName.requestFocus();
        } else if (productDescription.getText().toString().isEmpty()) {
            productDescription.setError("Product description required");
            productDescription.requestFocus();
        } else if (productPrice.getText().toString().isEmpty()) {
            productPrice.setError("Product price required");
            productPrice.requestFocus();
        } else if (newProduct.getUnit() == null) {
            Toast.makeText(AddNewProduct.this, "Pick a Unit", Toast.LENGTH_SHORT).show();
            binding.unitGroup.requestFocus();
        } else if (newProduct.getProduct_category_name() == null) {
            Toast.makeText(AddNewProduct.this, "Pick a product Category", Toast.LENGTH_SHORT).show();
        } else if (newProduct.getImage() == null) {
            Toast.makeText(AddNewProduct.this, "Get Product Image", Toast.LENGTH_SHORT).show();
            binding.productImagesView.requestFocus();
        } else {
            newProduct.setProduct_name(productName.getText().toString());
            newProduct.setProduct_price(productPrice.getText().toString());
            newProduct.setProduct_description(productDescription.getText().toString());
            valid = true;
        }

        return valid;
    }

    private void populateProductCategories() {
        System.out.println("GET PRODUCT CATEGORY DATA");


        new ViewModelProvider(this).get(ProductViewModel.class).getAllProductCategoriesLive().observe(this, jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                Toast.makeText(this, "No product categories", Toast.LENGTH_SHORT).show();
                return;
            }

            categoryList.clear();
            try {
                JsonArray serviceArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.get().getData()));

                for (int i = 0; i < serviceArray.size(); i++) {

                    try {
                        System.out.println("count " + i);
                        Models.ProductCategory productCategory = getObjectMapper().readValue(new JsonObject(serviceArray.getJsonObject(i).getMap()).toString(), Models.ProductCategory.class);
                        if (!productCategory.getDisabled() && !productCategory.getDeleted()) {
                            categoryList.add(productCategory.getName());
                            adapter.notifyDataSetChanged();
                        }

                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                }


            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }



    private void getProductImage() {
        if (doIHavePermission(storagePermissions[0], AddNewProduct.this) && doIHavePermission(storagePermissions[1], AddNewProduct.this)) {
            getProductPictureFromGallery();
        } else {
            DataOpts.requestPermissions(this, storagePermissions, STORAGE_PERMISSION_CODE);
        }
    }

    private void getProductPictureFromGallery() {
        launcher.launch("image/*");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(AddNewProduct.this, "Storage Permission Granted. You May proceed Now", Toast.LENGTH_SHORT).show();
                getProductPictureFromGallery();
            } else {
                Toast.makeText(AddNewProduct.this, "Storage Permission Denied. If you denied this you need to allow from settings", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void postProductPicture(Uri uri) {
        if (uri != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    binding.productImagesView.setImageURI(uri);
                    file = uri;
                    newProduct.setImage(uri.toString());
                    // System.out.println("Extension is " + getExtension(getFileName(uri, ProfileActivity.this)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 1000);
        } else {
            Toast.makeText(this, "Product Image request empty", Toast.LENGTH_SHORT).show();
        }
    }
}