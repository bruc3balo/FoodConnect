package com.victoria.foodconnect.pages.transporter.jobProgress;

import static com.victoria.foodconnect.adapter.JobsRvAdapter.getFromLocation;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.drawableToBitmap;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.getLocationPermission;
import static com.victoria.foodconnect.pages.transporter.MoreActivity.getPositionFromString;
import static com.victoria.foodconnect.pages.transporter.jobProgress.JobActivityProgress.update;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.FragmentOnTheWayBinding;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.transporter.MoreActivity;
import com.victoria.foodconnect.utils.DistributionStatus;

import java.util.Objects;
import java.util.function.Function;


public class OnTheWayFragment extends Fragment {

    private FragmentOnTheWayBinding binding;
    private GoogleMap googleMap;
    private final Models.Purchase purchase;
    private final Models.DistributionModel distribution;
    private final JobActivityProgress activity;



    public OnTheWayFragment(JobActivityProgress activity,Models.Purchase purchase, Models.DistributionModel distribution) {
        // Required empty public constructor
        this.purchase = purchase;
        this.distribution = distribution;
        this.activity = activity;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentOnTheWayBinding.inflate(inflater);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                this.googleMap = googleMap;

                getLocationPermission(activity, integer -> {
                    init();
                    return null;
                });
            });
        }


        binding.arrived.setOnClickListener(v -> confirmDialog(requireContext(), "Have you arrived to "+purchase.getBuyersId() + "?", purchase -> {
            deliveredProducts();
            return null;
        }));

        return binding.getRoot();
    }

    private void deliveredProducts() {
        binding.arrived.setEnabled(false);
        update(activity, new Models.DistributionUpdateForm(distribution.getId(), DistributionStatus.ARRIVED.getCode()));
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


    private void init() {

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationButtonClickListener(() -> false);
        googleMap.setOnMyLocationClickListener(location -> Toast.makeText(requireContext(), "I am here !!! ", Toast.LENGTH_SHORT).show());
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Objects.requireNonNull(getPositionFromString(purchase.getLocation())), 14));
        addMarkerToMap(googleMap, getPositionFromString(purchase.getLocation()), purchase.getAddress());

    }

    private void addMarkerToMap(GoogleMap googleMap, LatLng latLng, String address) {
        @SuppressLint("UseCompatLoadingForDrawables") MarkerOptions markerOptions = new MarkerOptions().position(latLng).draggable(true).title(address != null ? address : getFromLocation(latLng, requireContext()))/*.snippet("Click to confirm location")*/.icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(activity.getDrawable(R.drawable.ic_served_food))));
        googleMap.clear();
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }
}