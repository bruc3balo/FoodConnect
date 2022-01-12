package com.victoria.foodconnect.pages.transporter;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.DONATION;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.drawableToBitmap;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.getLocationPermission;
import static com.victoria.foodconnect.pages.transporter.MoreActivity.getPositionFromString;
import static com.victoria.foodconnect.utils.DataOpts.getBoldSpannable;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.adapter.DonationProductListAdapter;
import com.victoria.foodconnect.databinding.ActivityMoreDonationBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.purchaseDb.PurchaseViewModel;
import com.victoria.foodconnect.models.Models;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public class MoreDonationActivity extends AppCompatActivity {

    private ActivityMoreDonationBinding binding;
    private Models.Donation donation;
    private Domain.AppUser user;
    private GoogleMap gMap;
    private DonationProductListAdapter donationProductListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMoreDonationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        donation = (Models.Donation) getIntent().getExtras().get(DONATION);

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.pb.setVisibility(View.GONE);

        binding.toolbar.setTitle("Donation");
        binding.toolbar.setSubtitle(donation.getDonor_username().getUsername());

        userRepository.getUserLive().observe(this, appUser -> appUser.ifPresent(u -> user = u));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                gMap = googleMap;

                getLocationPermission(MoreDonationActivity.this, integer -> {
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

        binding.donorName.setText(getBoldSpannable("The donor is ", donation.getDonor_username().getUsername()));
        binding.deliveryLocation.setText(getBoldSpannable("To : ", donation.getDelivery_address()));
        binding.deliveryLocation.setOnClickListener(v -> addMarkerToMap(gMap, getPositionFromString(donation.getDelivery_location()), donation.getDelivery_address(),this));

        binding.collectionLocation.setText(getBoldSpannable("From : ", donation.getCollection_address()));
        binding.collectionLocation.setOnClickListener(v -> addMarkerToMap(gMap, getPositionFromString(donation.getCollection_location()), donation.getCollection_address(),this));


        ViewPager2 productsRv = binding.productsRv;
        donationProductListAdapter = new DonationProductListAdapter(MoreDonationActivity.this, donation.getProducts());
        productsRv.setAdapter(donationProductListAdapter);
        productsRv.setOffscreenPageLimit(3);
        productsRv.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        productsRv.requestTransform();
        productsRv.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        donationProductListAdapter.registerAdapterDataObserver(binding.indicator.getAdapterDataObserver());

        productsRv.setPadding(20,0,20,0);
        binding.indicator.setViewPager(productsRv);

        setWindowColors(this);

    }

    private void confirmJob() {
        inSpinnerProgress(binding.pb,null);

        new ViewModelProvider(this).get(PurchaseViewModel.class).acceptDonationTransportJob(donation.getId(), user.getUsername()).observe(this, success -> {
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
    }

    private void confirmDialog(Context context, String info, Function<Models.Donation, Void> function) {
        Dialog d = new Dialog(context);
        d.setContentView(R.layout.yes_no_layout);
        TextView infov = d.findViewById(R.id.infoTv);
        infov.setText(info);
        Button no = d.findViewById(R.id.noButton);
        no.setOnClickListener(v -> d.dismiss());

        Button yes = d.findViewById(R.id.yesButton);
        yes.setOnClickListener(v -> {
            function.apply(donation);
            d.dismiss();
        });

        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.show();
    }

    private void addMarkerToMap(GoogleMap googleMap, LatLng latLng, String address, Context context) {
        @SuppressLint("UseCompatLoadingForDrawables") MarkerOptions markerOptions = new MarkerOptions().position(latLng).draggable(true).title(address != null ? address : getFromLocation(context,latLng))/*.snippet("Click to confirm location")*/.icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(getDrawable(R.drawable.ic_served_food))));
        googleMap.clear();
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    public static String getFromMarker(Context context, Marker marker) {
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


        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Objects.requireNonNull(getPositionFromString(donation.getCollection_location())), 17));
        addMarkerToMap(gMap, getPositionFromString(donation.getCollection_location()), donation.getCollection_address(),this);

    }
}