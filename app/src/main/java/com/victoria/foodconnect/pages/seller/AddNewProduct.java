package com.victoria.foodconnect.pages.seller;

import static com.victoria.foodconnect.globals.GlobalVariables.IMAGE;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_CATEGORY_NAME;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_NAME;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_PRICE;
import static com.victoria.foodconnect.globals.GlobalVariables.UNIT;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.ActivityAddNewProductBinding;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.models.Models.ProductCreationFrom;

import java.util.ArrayList;
import java.util.Objects;

public class AddNewProduct extends AppCompatActivity {

    ActivityAddNewProductBinding binding;
    private final ArrayList<Models.ProductCategory> allProductCategories = new ArrayList<>();
    private final ProductCreationFrom newProduct = new ProductCreationFrom();

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddNewProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Category
        Button productCatButton = binding.productCategoryButton;
        productCatButton.setOnClickListener(view -> {
            PopupMenu menu = new PopupMenu(AddNewProduct.this, view);
            menu.getMenu().add("CANCEL").setTitle("CANCEL").setOnMenuItemClickListener(menuItem -> {
                menu.dismiss();
                return false;
            }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            allProductCategories.forEach(p -> menu.getMenu().add(p.getName()).setTitle(p.getName()).setIcon(R.drawable.ic_back).setOnMenuItemClickListener(menuItem -> {

                String item = menuItem.getTitle().toString();
                newProduct.setProduct_name(item);
                binding.categoryChosen.setText(item);

                return false;
            }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS));
            menu.show();
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

        populateProductCategories();

    }


    private void validateForm(EditText productName, EditText productDescription, EditText productPrice) {

    }

    private void populateProductCategories() {

    }
}