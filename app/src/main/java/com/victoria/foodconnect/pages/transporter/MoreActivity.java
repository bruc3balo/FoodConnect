package com.victoria.foodconnect.pages.transporter;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.globals.GlobalVariables.LATITUDE;
import static com.victoria.foodconnect.globals.GlobalVariables.LONGITUDE;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.drawableToBitmap;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.getLocationPermission;
import static com.victoria.foodconnect.utils.DataOpts.getBoldSpannable;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.adapter.JobProductListAdapter;
import com.victoria.foodconnect.databinding.ActivityMoreBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.purchaseDb.PurchaseViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.DataOpts;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class MoreActivity extends AppCompatActivity {

    private ActivityMoreBinding binding;
    private Models.Purchase purchase;
    private Domain.AppUser user;
    public static final String PURCHASE = "purchase";
    private GoogleMap gMap;
    private JobProductListAdapter jobProductListAdapter;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        outSpinnerProgress(binding.pb,null);



        purchase = (Models.Purchase) getIntent().getExtras().get(PURCHASE);
        userRepository.getUserLive().observe(this, appUser -> appUser.ifPresent(u -> user = u));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                gMap = googleMap;

                getLocationPermission(MoreActivity.this, integer -> {
                    init();
                    return null;
                });
            });
        }

        Button accept = binding.accept;
        accept.setOnClickListener(v -> confirmDialog(this, "Do you want to accept this job", purchase -> {
            confirmJob();
            return null;
        }));

        binding.buyer.setText(getBoldSpannable("The beneficiary is ", purchase.getBuyersId()));
        binding.locationTv.setText(getBoldSpannable("The delivery location is ", purchase.getAddress()));
        binding.locationTv.setOnClickListener(v -> addMarkerToMap(gMap, getPositionFromString(purchase.getLocation()), purchase.getAddress(),this));

        ViewPager2 productsRv = binding.productsRv;
        jobProductListAdapter = new JobProductListAdapter(MoreActivity.this, purchase.getProduct(),false);
        productsRv.setAdapter(jobProductListAdapter);
        productsRv.setOffscreenPageLimit(3);
        productsRv.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        productsRv.requestTransform();
        productsRv.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        jobProductListAdapter.registerAdapterDataObserver(binding.indicator.getAdapterDataObserver());
        jobProductListAdapter.setClickListener((view, position) -> {
            final Models.ProductCountModel entry = purchase.getProduct().get(position);
            final int item = entry.getCount();
            final Models.Product product = entry.getProduct();

            if (product.getLocation().equals(HY)) {
                Toast.makeText(MoreActivity.this, "Location not given", Toast.LENGTH_SHORT).show();
                return;
            } else if (getPositionFromString(product.getLocation()) == null) {
                Toast.makeText(MoreActivity.this, "Location invalid", Toast.LENGTH_SHORT).show();
                return;
            } else if (getFromLocation(this,Objects.requireNonNull(getPositionFromString(product.getLocation()))) == null) {
                Toast.makeText(MoreActivity.this, "Location not found", Toast.LENGTH_SHORT).show();
                return;
            } else {
                addMarkerToMap(gMap, getPositionFromString(product.getLocation()), null,this);
            }
        });
        productsRv.setPadding(20,0,20,0);
        binding.indicator.setViewPager(productsRv);

        setWindowColors(this);

    }

    private void confirmJob() {
        inSpinnerProgress(binding.pb,null);
        new ViewModelProvider(this).get(PurchaseViewModel.class).acceptTransportJob(purchase.getId(), user.getUsername()).observe(this, success -> {
            outSpinnerProgress(binding.pb,null);

            if (!success.isPresent()) {
                Toast.makeText(getApplicationContext(), "Failed to accept job. Try again later", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!success.get()) {
                Toast.makeText(getApplicationContext(), "Error accepting job. Try again later", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(getApplicationContext(), "Success posting job", Toast.LENGTH_SHORT).show();
            finish();
        });
        //todo fix lag or finish
    }

    private void confirmDialog(Context context, String info, Function<Models.Purchase, Void> function) {
        Dialog d = new Dialog(context);
        d.setContentView(R.layout.yes_no_layout);
        TextView infov = d.findViewById(R.id.infoTv);
        infov.setText(info);
        Button no = d.findViewById(R.id.noButton);
        no.setOnClickListener(v -> d.dismiss());

        Button yes = d.findViewById(R.id.yesButton);
        yes.setOnClickListener(v -> {
            function.apply(purchase);
            d.dismiss();
        });

        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.show();
    }

    private void addMarkerToMap(GoogleMap googleMap, LatLng latLng, String address,Context context) {
        @SuppressLint("UseCompatLoadingForDrawables") MarkerOptions markerOptions = new MarkerOptions().position(latLng).draggable(true).title(address != null ? address : getFromLocation(context,latLng))/*.snippet("Click to confirm location")*/.icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(getDrawable(R.drawable.ic_served_food))));
        googleMap.clear();
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    public static String getFromMarker(Context context,Marker marker) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
            Address add = addresses.get(0);
            Toast.makeText(context, add.getAddressLine(0), Toast.LENGTH_SHORT).show();

            return add.getAddressLine(0);
        } catch (IOException | IllegalArgumentException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public static String getFromLocation(Context context,LatLng latLng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.isEmpty()) {
                return null;
            }
            Address add = addresses.get(0);
            return add.getAddressLine(0);
        } catch (IOException | IllegalArgumentException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void init() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        gMap.setMyLocationEnabled(true);
        gMap.setOnMyLocationButtonClickListener(() -> false);
        gMap.setOnMyLocationClickListener(location -> getFromLocation(this,new LatLng(location.getLatitude(),location.getLongitude())));
        gMap.getUiSettings().setZoomControlsEnabled(true);
        gMap.getUiSettings().setZoomGesturesEnabled(true);


        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Objects.requireNonNull(getPositionFromString(purchase.getLocation())), 17));
        addMarkerToMap(gMap, getPositionFromString(purchase.getLocation()), purchase.getAddress(),this);
        // googleMap.clear();
        // googleMap.addMarker(new MarkerOptions().position(temp));

    }

    public static LatLng getPositionFromString(String location) {
        try {
            LinkedHashMap<String, String> map = DataOpts.getMapFromString(location);
            double lat = Double.parseDouble(Objects.requireNonNull(map.get(LATITUDE)));
            double longi = Double.parseDouble(Objects.requireNonNull(map.get(LONGITUDE)));
            return new LatLng(lat, longi);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}