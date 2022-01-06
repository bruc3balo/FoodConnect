package com.victoria.foodconnect.pages;

import static com.victoria.foodconnect.globals.GlobalVariables.LATITUDE;
import static com.victoria.foodconnect.globals.GlobalVariables.LONGITUDE;
import static com.victoria.foodconnect.globals.GlobalVariables.USERNAME;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.drawableToBitmap;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.getLocationPermission;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;
import static com.victoria.foodconnect.utils.DataOpts.getStringFromMap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.ActivityLocationOrderBinding;
import com.victoria.foodconnect.globals.purchaseDb.PurchaseViewModel;
import com.victoria.foodconnect.models.Models;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class LocationOrder extends AppCompatActivity {
    
    private ActivityLocationOrderBinding binding;
    private GoogleMap googleMap;
    private String location,address;
    public static boolean successOrder = false;
    private  ArrayList<Models.Product> allProducts = new ArrayList<>();
    private ArrayList<Models.Cart> cartList = new ArrayList<>();
    public static final String CARTLIST = "c";
    public static final String PRODUCTLIST = "p";
    private final ArrayList<Address> placesList = new ArrayList<>();
    private Geocoder geocoder;
    private String username;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        geocoder = new Geocoder(this);


        final Bundle extras = getIntent().getExtras();
        allProducts = (ArrayList<Models.Product>) extras.get(PRODUCTLIST);
        cartList = (ArrayList<Models.Cart>) extras.get(CARTLIST);
        username = extras.getString(USERNAME);

        successOrder = false;

        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;

            getLocationPermission(this, integer -> {
                init();
                return null;
            });

        });


        SearchView locationSearch = binding.locationSearch;
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        locationSearch.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        locationSearch.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                System.out.println("FOCUS");
            } else {
                System.out.println("NO FOCUS");
            }
        });

        locationSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new Handler().post(() -> {
                    System.out.println("CHANGE {" + query + "}");
                    if (!query.isEmpty()) {
                        getSuggestions(query, locationSearch);
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {

                return false;
            }
        });
        locationSearch.setOnSearchClickListener(v -> System.out.println("OPEN"));

        locationSearch.setOnCloseListener(() -> {
            System.out.println("CLOSE");
            return false;
        });


        Button confirm = binding.confirm;
        confirm.setOnClickListener(v -> {
            if (location != null && address != null) {
                showDialog();
            } else {
                Toast.makeText(LocationOrder.this, "Pick location or search", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton x = binding.cancel;
        x.setOnClickListener(v -> finish());

        setWindowColors(this);
    }

    private BigDecimal calculateTotal() {
        final BigDecimal[] total = {new BigDecimal(0)};
        cartList.forEach(c -> allProducts.stream().filter(i -> i.getId().equals(c.getProductId())).findFirst().ifPresent(value -> total[0] = total[0].add(new BigDecimal(c.getNumberOfItems()).multiply(value.getPrice()))));
        return total[0];
    }

    private String getFromMarker(Marker marker) {
        Geocoder geocoder = new Geocoder(LocationOrder.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
            if (addresses.isEmpty()) {
                return null;
            }
            Address add = addresses.get(0);
            Snackbar.make(findViewById(R.id.content), add.getAddressLine(0), Snackbar.LENGTH_LONG).show();
            LatLng latLng = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
            LinkedHashMap<String, String> locationMap = new LinkedHashMap<>();
            locationMap.put(LATITUDE, String.valueOf(latLng.latitude));
            locationMap.put(LONGITUDE, String.valueOf(latLng.latitude));
            location = getStringFromMap(locationMap);
            binding.locationTv.setText("Delivery location : " + add.getAddressLine(0));
            address = add.getAddressLine(0);
            return add.getAddressLine(0);
        } catch (IOException | IllegalArgumentException e) {
            Toast.makeText(LocationOrder.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private String getFromLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(LocationOrder.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.isEmpty()) {
                return null;
            }
            Address add = addresses.get(0);
            LinkedHashMap<String, String> locationMap = new LinkedHashMap<>();
            locationMap.put(LATITUDE, String.valueOf(latLng.latitude));
            locationMap.put(LONGITUDE, String.valueOf(latLng.latitude));
            location = getStringFromMap(locationMap);address = add.getAddressLine(0);
            binding.locationTv.setText("Location is "+address);
            return add.getAddressLine(0);
        } catch (IOException | IllegalArgumentException e) {
            Toast.makeText(LocationOrder.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void init() {

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

        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationButtonClickListener(() -> false);
        googleMap.setOnMyLocationClickListener(location -> Toast.makeText(this, "I am here !!! ", Toast.LENGTH_SHORT).show());
        googleMap.setOnMapClickListener(latLng -> addMarkerToMap(googleMap, latLng, null));
        googleMap.setOnMapLongClickListener(latLng -> addMarkerToMap(googleMap, latLng, null));
        googleMap.setOnMarkerClickListener(marker -> false);
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
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
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        LatLng temp = new LatLng(37.4,-1.4);

        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(temp,17));
       // googleMap.clear();
       // googleMap.addMarker(new MarkerOptions().position(temp));

    }

    @SuppressLint("SetTextI18n")
    private void showDialog() {
        Dialog d = new Dialog(this);
        d.setContentView(R.layout.checkout_layout);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView total = d.findViewById(R.id.total);
        ProgressBar pb = d.findViewById(R.id.pb);
        pb.setVisibility(View.GONE);
        Button order = d.findViewById(R.id.orderButton);
        order.setOnClickListener(v -> {
            order.setEnabled(false);
            pb.setVisibility(View.VISIBLE);

            LinkedHashMap<String, Integer> productIdList = new LinkedHashMap<>();
            cartList.forEach(c -> productIdList.put(c.getProductId(),c.getNumberOfItems()));

            Models.PurchaseCreationForm form = new Models.PurchaseCreationForm(username, productIdList);
            form.setLocation(location);
            form.setAddress(address);

            try {
                System.out.println("Products are form "+getObjectMapper().writeValueAsString(form));
                System.out.println("Products are cart "+getObjectMapper().writeValueAsString(cartList));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            new ViewModelProvider(LocationOrder.this).get(PurchaseViewModel.class).postNewPurchase(form).observe(LocationOrder.this, success -> {
                if (d.isShowing()) {
                    order.setEnabled(true);
                    pb.setVisibility(View.GONE);
                }

                if (!success.isPresent()) {
                    Toast.makeText(LocationOrder.this, "Failed to post purchase", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(LocationOrder.this, "Successfully posted", Toast.LENGTH_SHORT).show();
                d.dismiss();
                successOrder = true;
                finish();
            });
        });

        ImageButton cancel_button = d.findViewById(R.id.cancel_button);
        cancel_button.setOnClickListener(v -> d.dismiss());

        d.setOnShowListener(dialog -> total.setText(calculateTotal() + " KSH"));
        d.show();
    }

    private MutableLiveData<List<Address>> getSuggestions(String query) {
        MutableLiveData<List<Address>> suggestions = new MutableLiveData<>();

        try {
            suggestions.setValue(geocoder.getFromLocationName(query, 10));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return suggestions;
    }

    public LiveData<List<Address>> getSuggestedAddresses(String query) {
        return getSuggestions(query);
    }

    private void showSuggestions(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add("CANCEL").setTitle("CANCEL").setOnMenuItemClickListener(menuItem -> {
            menu.dismiss();
            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        placesList.forEach(p -> menu.getMenu().add(p.getAddressLine(0)).setTitle(p.getAddressLine(0)).setOnMenuItemClickListener(menuItem -> {
            String item = menuItem.getTitle().toString();
            Toast.makeText(this, item, Toast.LENGTH_SHORT).show();
            binding.locationTv.setText("Delivery location : " + item);
            Snackbar.make(binding.getRoot(), item, Snackbar.LENGTH_LONG).show();

            Optional<Address> add = placesList.stream().filter(p1 -> p1.getAddressLine(0).equals(item)).findFirst();
            if (add.isPresent()) {
                LatLng lat = new LatLng(add.get().getLatitude(), add.get().getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lat, 15));
                addMarkerToMap(googleMap, lat, add.get().getAddressLine(0));
                LinkedHashMap<String, String> locationMap = new LinkedHashMap<>();
                locationMap.put(LATITUDE, String.valueOf(lat.latitude));
                locationMap.put(LONGITUDE, String.valueOf(lat.latitude));
                location = getStringFromMap(locationMap);
                address = getFromLocation(lat);
            }

            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS));
        menu.show();
    }

    private void addMarkerToMap(GoogleMap googleMap, LatLng latLng, String address) {
        @SuppressLint("UseCompatLoadingForDrawables") MarkerOptions markerOptions = new MarkerOptions().position(latLng).draggable(true).title(address != null ? address : getFromLocation(latLng))/*.snippet("Click to confirm location")*/.icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(getDrawable(R.drawable.ic_served_food))));
        googleMap.clear();
        googleMap.addMarker(markerOptions);

    }

    private void getSuggestions(String query, View anchor) {
        if (query == null || query.isEmpty()) {
            placesList.clear();
            return;
        }

        //placesList.addAll(geocoder.getFromLocationName(query, 10));
        getSuggestedAddresses(query).observe(this, addresses -> {
            placesList.clear();
            placesList.addAll(addresses);
            System.out.println(Arrays.toString(addresses.stream().map(p -> p.getAddressLine(0)).toArray()));
        });

        showSuggestions(anchor);
    }



    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapView.onPause();

    }
}