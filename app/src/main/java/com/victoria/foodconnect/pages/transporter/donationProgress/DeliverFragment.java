package com.victoria.foodconnect.pages.transporter.donationProgress;

import static com.victoria.foodconnect.adapter.JobsRvAdapter.getFromLocation;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.drawableToBitmap;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.getLocationPermission;
import static com.victoria.foodconnect.pages.transporter.MoreActivity.getPositionFromString;
import static com.victoria.foodconnect.pages.transporter.donationProgress.DonationProgressActivity.updateDonationDistribution;
import static com.victoria.foodconnect.pages.transporter.donationProgress.StartFragment.confirmDialog;
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
import com.victoria.foodconnect.databinding.FragmentDeliverBinding;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.DistributionStatus;

import java.util.Objects;
import java.util.function.Function;


public class DeliverFragment extends Fragment {

    private FragmentDeliverBinding binding;
    private Models.DonationDistribution distribution;
    private Models.Donation donation;
    private DonationProgressActivity activity;
    private GoogleMap googleMap;
    private boolean readOnly;

    public DeliverFragment() {
        // Required empty public constructor
    }

    public DeliverFragment(DonationProgressActivity activity, Models.DonationDistribution distribution, Models.Donation donation,boolean readOnly) {
        this.distribution = distribution;
        this.donation = donation;
        this.activity = activity;
        this.readOnly = readOnly;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDeliverBinding.inflate(inflater);


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


        binding.arrived.setOnClickListener(v -> confirmDialog(requireContext(), "Have you arrived to " + donation.getBeneficiary_username().getUsername()  + "?", donation, donation -> {
            deliveredProducts();
            return null;
        }));


        return binding.getRoot();
    }

    private void deliveredProducts() {
        binding.arrived.setEnabled(false);
        updateDonationDistribution(activity, new Models.DonorDistributionUpdateForm(distribution.getId(), DistributionStatus.ARRIVED.getCode()));
    }


    private void init() {

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationButtonClickListener(() -> false);
        googleMap.setOnMyLocationClickListener(location -> getFromLocation(new LatLng(location.getLatitude(),location.getLongitude()),activity));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Objects.requireNonNull(getPositionFromString(donation.getDelivery_location())), 14));
        addMarkerToMap(googleMap, getPositionFromString(donation.getDelivery_location()), donation.getDelivery_address());

    }

    private void addMarkerToMap(GoogleMap googleMap, LatLng latLng, String address) {
        @SuppressLint("UseCompatLoadingForDrawables") MarkerOptions markerOptions = new MarkerOptions().position(latLng).draggable(true).title(address != null ? address : getFromLocation(latLng, requireContext()))/*.snippet("Click to confirm location")*/.icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(activity.getDrawable(R.drawable.ic_served_food))));
        googleMap.clear();
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }
}