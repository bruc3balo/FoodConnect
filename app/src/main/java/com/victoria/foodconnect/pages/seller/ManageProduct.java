package com.victoria.foodconnect.pages.seller;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.MEDIA_TYPE;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_COLLECTION;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_COLLECTION_UPDATE;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.STORAGE_PERMISSION_CODE;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.storagePermissions;
import static com.victoria.foodconnect.utils.DataOpts.doIHavePermission;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.databinding.ActivityManageProductBinding;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.service.UploadPictureService;
import com.victoria.foodconnect.utils.DataOpts;

import java.util.Optional;

public class ManageProduct extends AppCompatActivity {

    private ActivityManageProductBinding binding;
    private final Models.ProductUpdateForm form = new Models.ProductUpdateForm();
    private ActivityResultLauncher<String> launcher;
    private Models.Product oldProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //Name
        EditText productName = binding.productNameField;


        //Description
        EditText productDescription = binding.productDescriptionField;

        //Price
        EditText productPrice = binding.productPriceField;

        //Items
        EditText productItemsField = binding.productItemsField;

        binding.getImage.setOnClickListener(view -> getProductImage());
        launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), this::postProductPicture);

        binding.updateProductButton.setOnClickListener(v -> {
            try {
                if (validateForm(productName, productDescription, productPrice, productItemsField)) {
                    updateProduct();
                }
            } catch (Exception e) {
                Toast.makeText(ManageProduct.this, "Error validating form", Toast.LENGTH_SHORT).show();
            }
        });

        if (getIntent().getExtras() != null) {
            oldProduct = (Models.Product) getIntent().getExtras().get(PRODUCT_COLLECTION);
            form.setId(oldProduct.getId());
            binding.toolbar.setTitle(oldProduct.getName() + " update");
            binding.toolbar.setNavigationOnClickListener(v -> finish());
            productName.setText(oldProduct.getName());
            productPrice.setText(String.valueOf(oldProduct.getPrice()));
            productDescription.setText(oldProduct.getProduct_description());
            Glide.with(this).load(oldProduct.getImage()).into(binding.productImage);

            if (userRepository != null) {
                userRepository.getUserLive().observe(this, appUser -> {
                    if (appUser.isPresent()) {
                      Optional<Models.ProductAmount> amount = oldProduct.getProductAmount().stream().filter(i-> i.getSellersId().equals(appUser.get().getUsername())).findFirst();

                        try {
                            System.out.println("product "+getObjectMapper().writeValueAsString(oldProduct));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                        if (amount.isPresent()) {
                          productItemsField.setText(String.valueOf(amount.get().getUnit()));
                          System.out.println("FOUND");
                      } else {
                          productItemsField.setText("0");
                          System.out.println("NOT FOUND");
                      }
                    }
                });
            }
        }

        setWindowColors(this);
    }

    private void getProductImage() {
        if (doIHavePermission(storagePermissions[0], ManageProduct.this) && doIHavePermission(storagePermissions[1], ManageProduct.this)) {
            getProductPictureFromGallery();
        } else {
            DataOpts.requestPermissions(this, storagePermissions, STORAGE_PERMISSION_CODE);
        }
    }

    private void getProductPictureFromGallery() {
        launcher.launch("image/*");
    }

    private void postProductPicture(Uri uri) {
        if (uri != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    binding.productImage.setImageURI(uri);
                    form.setImage(uri.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 1000);
        } else {
            Toast.makeText(this, "Product Image request empty", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateForm(EditText productName, EditText productDescription, EditText productPrice, EditText items) {
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
        } else if (items.getText().toString().isEmpty()) {
            items.setError("cannot be empty");
            items.requestFocus();
            items.setText("0");
        } else {
            form.setProduct_name(productName.getText().toString());
            form.setProduct_description(productDescription.getText().toString());
            form.setProduct_price(productPrice.getText().toString());
            form.setUnitsLeft(Integer.valueOf(items.getText().toString()));
            valid = true;
        }

        return valid;
    }

    private void updateProduct() {
        startService(new Intent(ManageProduct.this, UploadPictureService.class).putExtra(PRODUCT_COLLECTION_UPDATE, form).putExtra(MEDIA_TYPE, PRODUCT_COLLECTION_UPDATE));
        finish();
    }
}