package com.victoria.foodconnect.pages.transporter.jobProgress;

import static com.victoria.foodconnect.adapter.JobsRvAdapter.getFromLocation;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.drawableToBitmap;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.getLocationPermission;
import static com.victoria.foodconnect.pages.transporter.MoreActivity.PURCHASE;
import static com.victoria.foodconnect.pages.transporter.MoreActivity.getPositionFromString;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.ActivityJobProgressBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.models.Models;

import java.util.Objects;
import java.util.function.Function;

public class JobActivityProgress extends AppCompatActivity {

    private ActivityJobProgressBinding binding;
    private Models.Purchase purchase;
    private Domain.AppUser user;
    private GoogleMap googleMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityJobProgressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        purchase = (Models.Purchase) getIntent().getExtras().get(PURCHASE);
        userRepository.getUserLive().observe(this, appUser -> appUser.ifPresent(u -> user = u));

        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;

            getLocationPermission(this, integer -> {
                init();
                return null;
            });

        });

        Button proceed = binding.proceed;
        proceed.setOnClickListener(v -> confirmDialog(this, "Do you want to accept this job", purchase -> {
            //confirmJob();
            return null;
        }));

        setWindowColors(this);

        //todo viewpager
        //todo map
        //todo interact

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

    private void addMarkerToMap(GoogleMap googleMap, LatLng latLng, String address) {
        @SuppressLint("UseCompatLoadingForDrawables") MarkerOptions markerOptions = new MarkerOptions().position(latLng).draggable(true).title(address != null ? address : getFromLocation(latLng,this))/*.snippet("Click to confirm location")*/.icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(getDrawable(R.drawable.ic_served_food))));
        googleMap.clear();
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    private void init() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationButtonClickListener(() -> false);
        googleMap.setOnMyLocationClickListener(location -> Toast.makeText(this, "I am here !!! ", Toast.LENGTH_SHORT).show());
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Objects.requireNonNull(getPositionFromString(purchase.getLocation())), 17));
        addMarkerToMap(googleMap, getPositionFromString(purchase.getLocation()), purchase.getAddress());
        // googleMap.clear();
        // googleMap.addMarker(new MarkerOptions().position(temp));

    }

}