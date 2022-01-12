package com.victoria.foodconnect.pages.seller;

import static com.victoria.foodconnect.globals.GlobalVariables.LATITUDE;
import static com.victoria.foodconnect.globals.GlobalVariables.LONGITUDE;
import static com.victoria.foodconnect.globals.GlobalVariables.MEDIA_TYPE;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_COLLECTION;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.utils.DataOpts.doIHavePermission;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;
import static com.victoria.foodconnect.utils.DataOpts.getStringFromMap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.ActivityAddNewProductBinding;
import com.victoria.foodconnect.globals.productDb.ProductViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.models.Models.ProductCreationFrom;
import com.victoria.foodconnect.service.UploadPictureService;
import com.victoria.foodconnect.utils.DataOpts;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class AddNewProduct extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_CODE = 1;
    private ActivityAddNewProductBinding binding;
    private final ArrayList<Models.ProductCategory> allProductCategories = new ArrayList<>();
    private final ProductCreationFrom newProduct = new ProductCreationFrom();
    private ArrayAdapter<String> adapter;
    private final LinkedList<String> categoryList = new LinkedList<>();


    public static final String[] storagePermissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final int STORAGE_PERMISSION_CODE = 4;

    private ActivityResultLauncher<String> launcher;
    private Uri file;
    private GoogleMap map;



    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddNewProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.addNewProductTb;
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

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
                case R.id.tonne:
                    String tonne = getString(R.string.tonne);
                    newProduct.setUnit(tonne);
                    Toast.makeText(AddNewProduct.this, tonne, Toast.LENGTH_SHORT).show();
                    break;

                case R.id.kg:
                    String kg = getString(R.string.kg);
                    newProduct.setUnit(kg);
                    Toast.makeText(AddNewProduct.this, kg, Toast.LENGTH_SHORT).show();

                    break;

                case R.id.g:
                    String g = getString(R.string.g);
                    newProduct.setUnit(g);
                    Toast.makeText(AddNewProduct.this, g, Toast.LENGTH_SHORT).show();

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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setWindowColors(this);

        populateProductCategories();

    }

    private void saveNewProduct() {
        startService(new Intent(AddNewProduct.this, UploadPictureService.class).putExtra(PRODUCT_COLLECTION, newProduct).putExtra(MEDIA_TYPE, PRODUCT_COLLECTION));
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

        inSpinnerProgress(binding.pb,null);

        new ViewModelProvider(this).get(ProductViewModel.class).getAllProductCategoriesLive().observe(this, jsonResponse -> {
            outSpinnerProgress(binding.pb,null);
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
        } else if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location works", Toast.LENGTH_SHORT).show();
                init();
            } else {
                Toast.makeText(this, "Location needed to show product info", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        getLocationPermission(this, integer -> {
            init();
            return null;
        });
    }

    public void init() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        map.setMyLocationEnabled(true);
        map.setOnMyLocationButtonClickListener(() -> false);
        map.setOnMyLocationClickListener(location -> Toast.makeText(AddNewProduct.this, "I am here !!! ", Toast.LENGTH_SHORT).show());
        map.setOnMapClickListener(latLng -> addMarkerToMap(map, latLng));
        map.setOnMapLongClickListener(latLng -> addMarkerToMap(map, latLng));
        map.setOnMarkerClickListener(marker -> false);
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NotNull Marker marker) {

            }

            @Override
            public void onMarkerDrag(@NotNull Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(@NotNull Marker marker) {
                getFromMarker(marker);
            }
        });
        map.setOnInfoWindowClickListener(this::getFromMarker);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
    }

    private void addMarkerToMap(GoogleMap googleMap, LatLng latLng) {
        @SuppressLint("UseCompatLoadingForDrawables") MarkerOptions markerOptions = new MarkerOptions().position(latLng).draggable(true).title(getFromLocation(latLng)).snippet("Click to confirm location").icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(getDrawable(R.drawable.ic_give_food))));
        googleMap.clear();
        googleMap.addMarker(markerOptions);
    }

    private String getFromMarker(Marker marker) {
        Geocoder geocoder = new Geocoder(AddNewProduct.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
            Address address = addresses.get(0);
            Snackbar.make(binding.getRoot(), address.getAddressLine(0), Snackbar.LENGTH_LONG).show();
            binding.locationTv.setText("Supply location : " + address.getAddressLine(0));
            LatLng latLng = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
            LinkedHashMap<String, String> locationMap = new LinkedHashMap<>();
            locationMap.put(LATITUDE, String.valueOf(latLng.latitude));
            locationMap.put(LONGITUDE, String.valueOf(latLng.longitude));
            String location = getStringFromMap(locationMap);
            newProduct.setLocation(location);
            return address.getAddressLine(0);
        } catch (IOException | IllegalArgumentException e) {
            Toast.makeText(AddNewProduct.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private String getFromLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(AddNewProduct.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Address address = addresses.get(0);
            Snackbar.make(binding.getRoot(), address.getAddressLine(0), Snackbar.LENGTH_LONG).show();
            binding.locationTv.setText("Supply location : " + address.getAddressLine(0));
            LinkedHashMap<String, String> locationMap = new LinkedHashMap<>();
            locationMap.put(LATITUDE, String.valueOf(latLng.latitude));
            locationMap.put(LONGITUDE, String.valueOf(latLng.longitude));
            String location = getStringFromMap(locationMap);
            newProduct.setLocation(location);

            return address.getAddressLine(0);
        } catch (IOException | IllegalArgumentException e) {
            Toast.makeText(AddNewProduct.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public static Address getAddressFromLocation(Context context, LatLng latLng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (!addresses.isEmpty()) {
                return addresses.get(0);
            } else {
                return null;
            }
        } catch (IOException | IllegalArgumentException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    public static void getLocationPermission(Activity activity, Function<Integer, Void> function) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) { //check if location is allowed
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            function.apply(0);
        }
    }

}