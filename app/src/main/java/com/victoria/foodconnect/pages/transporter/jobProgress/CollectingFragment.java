package com.victoria.foodconnect.pages.transporter.jobProgress;

import static com.victoria.foodconnect.adapter.JobsRvAdapter.getFromLocation;
import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.drawableToBitmap;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.getLocationPermission;
import static com.victoria.foodconnect.pages.transporter.MoreActivity.getPositionFromString;
import static com.victoria.foodconnect.pages.transporter.jobProgress.JobActivityProgress.update;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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
import com.victoria.foodconnect.adapter.JobProductCollectListAdapter;
import com.victoria.foodconnect.adapter.JobProductListAdapter;
import com.victoria.foodconnect.databinding.FragmentCollectingBinding;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.transporter.MoreActivity;
import com.victoria.foodconnect.pages.transporter.jobProgress.JobActivityProgress;
import com.victoria.foodconnect.utils.DistributionStatus;
import com.victoria.foodconnect.utils.ProductStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


public class CollectingFragment extends Fragment {

    private FragmentCollectingBinding binding;
    private GoogleMap googleMap;
    private final Models.Purchase purchase;
    private final Models.DistributionModel distribution;
    private final JobActivityProgress activity;
    private final boolean readOnly;


    public CollectingFragment(JobActivityProgress activity, Models.Purchase purchase, Models.DistributionModel distribution,boolean readOnly) {
        // Required empty public constructor
        this.purchase = purchase;
        this.distribution = distribution;
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
        binding = FragmentCollectingBinding.inflate(inflater);

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


        ViewPager2 viewPager2 = binding.productPages;
        JobProductCollectListAdapter jobProductCollectListAdapter = new JobProductCollectListAdapter(activity, distribution, purchase.getProduct().stream().map(Models.ProductCountModel::getProduct).collect(Collectors.toList()));
        viewPager2.setAdapter(jobProductCollectListAdapter);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager2.requestTransform();
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        jobProductCollectListAdapter.registerAdapterDataObserver(binding.indicator.getAdapterDataObserver());
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (googleMap != null) {
                    String pid = distribution.getProduct_status().getKey(position);
                    Optional<Models.Product> optionalProduct = purchase.getProduct().stream().map(Models.ProductCountModel::getProduct).filter(product -> product.getId().equals(pid)).findFirst();
                    optionalProduct.ifPresent(p -> {

                        if (p.getLocation().equals(HY) || getPositionFromString(p.getLocation()) == null || getFromLocation(Objects.requireNonNull(getPositionFromString(p.getLocation())), activity) == null) {
                            googleMap.clear();

                            Optional<ProductStatus> optionalProductStatus = Arrays.stream(ProductStatus.values()).filter(i -> i.getCode() == distribution.getProduct_status().getValue(position)).findFirst();
                            if (optionalProductStatus.isPresent()) {
                                optionalProductStatus.ifPresent(opStatus -> {
                                    if (opStatus.getCode() != ProductStatus.FAILED.getCode()) {
                                        Map<String, Integer> product_status = new HashMap<>();
                                        product_status.put(pid, ProductStatus.FAILED.getCode());
                                        update(activity, new Models.DistributionUpdateForm(distribution.getId(), product_status));
                                    }
                                });
                            } else {
                                Map<String, Integer> product_status = new HashMap<>();
                                product_status.put(pid, ProductStatus.FAILED.getCode());
                                update(activity, new Models.DistributionUpdateForm(distribution.getId(), product_status));
                            }
                        } else {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Objects.requireNonNull(getPositionFromString(p.getLocation())), 17));
                            addMarkerToMap(activity,googleMap, getPositionFromString(p.getLocation()), null);
                        }

                    });
                    // googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(,15));
                }
                super.onPageSelected(position);
            }
        });

        viewPager2.setPadding(20, 0, 20, 0);
        binding.indicator.setViewPager(viewPager2);

        binding.goToBeneficiary.setOnClickListener(v -> confirmDialog(requireContext(), "Are you done collecting the items?", purchase -> {
            goToBeneficiary();
            return null;
        }));

        return binding.getRoot();
    }

    private void goToBeneficiary() {
        binding.goToBeneficiary.setEnabled(false);
        update(activity, new Models.DistributionUpdateForm(distribution.getId(), DistributionStatus.ON_THE_WAY.getCode()));
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
        googleMap.setOnMyLocationClickListener(location -> getFromLocation(new LatLng(location.getLatitude(),location.getLongitude()),activity));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Objects.requireNonNull(getPositionFromString(purchase.getLocation())), 17));
        addMarkerToMap(activity,googleMap, getPositionFromString(purchase.getLocation()), purchase.getAddress());

    }

    public static void addMarkerToMap(Activity activity, GoogleMap googleMap, LatLng latLng, String address) {
        @SuppressLint("UseCompatLoadingForDrawables") MarkerOptions markerOptions = new MarkerOptions().position(latLng).draggable(true).title(address != null ? address : getFromLocation(latLng, activity))/*.snippet("Click to confirm location")*/.icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(activity.getDrawable(R.drawable.ic_served_food))));
        googleMap.clear();
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }
}