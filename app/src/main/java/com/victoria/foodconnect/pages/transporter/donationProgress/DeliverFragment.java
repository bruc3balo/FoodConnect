package com.victoria.foodconnect.pages.transporter.donationProgress;

import static android.graphics.Color.CYAN;
import static com.victoria.foodconnect.adapter.JobsRvAdapter.getFromLocation;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.drawableToBitmap;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.getLocationPermission;
import static com.victoria.foodconnect.pages.transporter.MoreActivity.getPositionFromString;
import static com.victoria.foodconnect.pages.transporter.donationProgress.DonationProgressActivity.updateDonationDistribution;
import static com.victoria.foodconnect.pages.transporter.donationProgress.StartFragment.confirmDialog;
import static com.victoria.foodconnect.pages.transporter.jobProgress.CollectingFragment.addMarkerToMap;
import static com.victoria.foodconnect.pages.transporter.jobProgress.JobActivityProgress.update;
import static com.victoria.foodconnect.service.LocationService.locationMutableLiveData;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.FragmentDeliverBinding;
import com.victoria.foodconnect.globals.directions.DirectionsViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.DistributionStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import retrofit2.Response;


public class DeliverFragment extends Fragment {

    private FragmentDeliverBinding binding;
    private Models.DonationDistribution distribution;
    private Models.Donation donation;
    private DonationProgressActivity activity;
    private GoogleMap googleMap;
    private boolean readOnly;

    private static final LatLng BOUND1 = new LatLng(-35.595209, 138.585857);
    private static final LatLng BOUND2 = new LatLng(-35.494644, 138.805927);

    public DeliverFragment() {
        // Required empty public constructor
    }

    public DeliverFragment(DonationProgressActivity activity, Models.DonationDistribution distribution, Models.Donation donation, boolean readOnly) {
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


        binding.arrived.setOnClickListener(v -> confirmDialog(requireContext(), "Have you arrived to " + donation.getBeneficiary_username().getUsername() + "?", donation, donation -> {
            deliveredProducts();
            return null;
        }));


        return binding.getRoot();
    }

    private LatLngBounds getBounds() {
        return new LatLngBounds.Builder()
                .include(BOUND1)
                .include(BOUND2)
                .build();
    }

    private void deliveredProducts() {
        binding.arrived.setEnabled(false);
        updateDonationDistribution(activity, new Models.DonorDistributionUpdateForm(distribution.getId(), DistributionStatus.ARRIVED.getCode()));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.add("Delivery").setTitle("Delivery").setIcon(android.R.drawable.ic_menu_mylocation).setOnMenuItemClickListener(item -> {
            showLocation();
            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void showLocation() {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Objects.requireNonNull(getPositionFromString(donation.getDelivery_location())), 14));
        addMarkerToMap(googleMap, getPositionFromString(donation.getDelivery_location()), donation.getDelivery_address());
    }

    private void init() {

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationButtonClickListener(() -> false);
        googleMap.setOnMyLocationClickListener(location -> getFromLocation(new LatLng(location.getLatitude(), location.getLongitude()), activity));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Objects.requireNonNull(getPositionFromString(donation.getDelivery_location())), 14));
        addMarkerToMap(googleMap, getPositionFromString(donation.getDelivery_location()), donation.getDelivery_address());

        googleMap.setOnMapLoadedCallback(() -> googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getBounds(), 14)));

        locationMutableLiveData.observe(getViewLifecycleOwner(), latLng -> {
            if (!latLng.isPresent()) {
                System.out.println("Failed to get location");
                return;
            }

            latLng.ifPresent(l -> {
                System.out.println("Location is present");
                //getDirections(l, getPositionFromString(donation.getDelivery_location()), null);
            });
        });

    }

    private void addMarkerToMap(GoogleMap googleMap, LatLng latLng, String address) {
        @SuppressLint("UseCompatLoadingForDrawables") MarkerOptions markerOptions = new MarkerOptions().position(latLng).draggable(true).title(address != null ? address : getFromLocation(latLng, requireContext()))/*.snippet("Click to confirm location")*/.icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(activity.getDrawable(R.drawable.ic_served_food))));
        googleMap.clear();
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    private void getDirections(LatLng origin, LatLng destination, List<String> waypoints) {
        new ViewModelProvider(this).get(DirectionsViewModel.class).getDirectionsLive(origin, destination, waypoints).observe(getViewLifecycleOwner(), o -> {
            if (!o.isPresent()) {
                System.out.println("FAILED TO GET DIRECTIONS");
                return;
            }

            o.ifPresent(obj -> {
                try {
                    System.out.println(obj.string() + " is body");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void drawAllPolyLines(Set<LatLng> points) {
        // Add a blue Polyline.
        googleMap.addPolyline(new PolylineOptions()
                .color(CYAN) // Line color.
                .width(3) // Line width.
                .clickable(false) //// Able to click or not.
                .addAll(points)); // all the whole list of lat lng value pairs which is retrieved by calling helper method readEncodedPolyLinePointsFromCSV.

    }
}