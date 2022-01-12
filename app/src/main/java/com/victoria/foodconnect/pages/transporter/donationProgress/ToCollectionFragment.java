package com.victoria.foodconnect.pages.transporter.donationProgress;

import static com.victoria.foodconnect.adapter.JobsRvAdapter.getFromLocation;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.getLocationPermission;
import static com.victoria.foodconnect.pages.transporter.MoreActivity.getPositionFromString;
import static com.victoria.foodconnect.pages.transporter.donationProgress.DonationProgressActivity.donationInProgress;
import static com.victoria.foodconnect.pages.transporter.donationProgress.DonationProgressActivity.donationOutProgress;
import static com.victoria.foodconnect.pages.transporter.donationProgress.DonationProgressActivity.updateDonationDistribution;
import static com.victoria.foodconnect.pages.transporter.donationProgress.StartFragment.confirmDialog;
import static com.victoria.foodconnect.pages.transporter.jobProgress.CollectingFragment.addMarkerToMap;
import static com.victoria.foodconnect.pages.transporter.jobProgress.JobActivityProgress.update;
import static com.victoria.foodconnect.service.LocationService.myLocation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.FragmentToCollectionBinding;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.transporter.jobProgress.JobActivityProgress;
import com.victoria.foodconnect.utils.DistributionStatus;

import java.util.Objects;


public class ToCollectionFragment extends Fragment {

    private FragmentToCollectionBinding binding;
    private Models.DonationDistribution distribution;
    private Models.Donation donation;
    private DonationProgressActivity activity;
    private GoogleMap googleMap;
    private boolean readOnly;

    public ToCollectionFragment() {
        // Required empty public constructor
    }

    public ToCollectionFragment(DonationProgressActivity activity,Models.DonationDistribution distribution,Models.Donation donation,boolean readOnly) {
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
        binding = FragmentToCollectionBinding.inflate(inflater);

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

        binding.goToBeneficiary.setOnClickListener(v -> confirmDialog(requireContext(), "Are you done collecting the items?", donation,donation -> {
            goToBeneficiary();
            return null;
        }));

        return binding.getRoot();
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


        if (myLocation == null)  {
            addMarkerToMap(activity,googleMap, getPositionFromString(donation.getCollection_location()), donation.getCollection_address());
        } else {
            addMarkerToMap(activity,googleMap, myLocation, "My location");
        }

    }

    private void goToBeneficiary() {
        donationInProgress();
        binding.goToBeneficiary.setEnabled(false);
        updateDonationDistribution(activity, new Models.DonorDistributionUpdateForm(distribution.getId(), DistributionStatus.ON_THE_WAY.getCode()));
        donationOutProgress();

    }
}