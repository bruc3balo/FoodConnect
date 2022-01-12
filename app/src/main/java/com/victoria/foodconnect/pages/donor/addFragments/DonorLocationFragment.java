package com.victoria.foodconnect.pages.donor.addFragments;

import static android.graphics.Color.RED;
import static com.victoria.foodconnect.SplashScreen.logout;
import static com.victoria.foodconnect.globals.GlobalVariables.LATITUDE;
import static com.victoria.foodconnect.globals.GlobalVariables.LONGITUDE;
import static com.victoria.foodconnect.pages.LocationOrder.getSuggestedAddresses;
import static com.victoria.foodconnect.pages.donor.AddItemDonor.donationCreationForm;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.drawableToBitmap;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.getLocationPermission;
import static com.victoria.foodconnect.pages.transporter.MoreActivity.getFromLocation;
import static com.victoria.foodconnect.service.LocationService.locationMutableLiveData;
import static com.victoria.foodconnect.service.LocationService.myLocation;
import static com.victoria.foodconnect.utils.DataOpts.getStringFromMap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.FragmentDonorLocationBinding;
import com.victoria.foodconnect.pages.LocationOrder;
import com.victoria.foodconnect.pages.beneficiary.BeneficiaryActivity;
import com.victoria.foodconnect.pages.donor.AddItemDonor;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;


public class DonorLocationFragment extends Fragment {

    private FragmentDonorLocationBinding binding;
    private AddItemDonor activity;
    private boolean delivery;
    private final ArrayList<Address> placesList = new ArrayList<>();
    private GoogleMap googleMap;
    private LatLng me;


    public DonorLocationFragment() {
        // Required empty public constructor
    }

    public DonorLocationFragment(AddItemDonor activity, boolean delivery) {
        this.activity = activity;
        this.delivery = delivery;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDonorLocationBinding.inflate(inflater);
        setHasOptionsMenu(true);


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

        if (delivery) {
            setDelivery();
        } else {
            setCollection();
        }


        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        SearchView searchView = new SearchView(Objects.requireNonNull(activity.getSupportActionBar()).getThemedContext());
        menu.add("Search menu").setIcon(R.drawable.ic_search_white).setIconTintList(ColorStateList.valueOf(RED)).setVisible(true).setActionView(searchView).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        searchView.setIconifiedByDefault(true);

        SearchManager searchManager = activity.getSystemService(SearchManager.class);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new Handler().post(() -> {
                    System.out.println("CHANGE {" + query + "}");
                    if (!query.isEmpty()) {
                        getQuerySuggestions(query, binding.anchor);
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setDelivery() {
        binding.locationTv.setText("Delivery location");
    }

    private void setCollection() {
        binding.locationTv.setText("Collection location");
    }

    private void init() {

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationButtonClickListener(() -> false);
        googleMap.setOnMyLocationClickListener(location -> getFromLocation(new LatLng(location.getLatitude(),location.getLongitude())));
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

        if (myLocation != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,15));
        }


        getMyLocation();
    }



    private void getMyLocation() {
        locationMutableLiveData.observe(activity, myPosition -> myPosition.ifPresent(p -> me = p));
    }

    private void getQuerySuggestions(String query, View anchor) {
        if (query == null || query.isEmpty()) {
            placesList.clear();
            return;
        }

        //placesList.addAll(geocoder.getFromLocationName(query, 10));
        getSuggestedAddresses(activity, query).observe(this, addresses -> {
            placesList.clear();
            placesList.addAll(addresses);
            System.out.println(Arrays.toString(addresses.stream().map(p -> p.getAddressLine(0)).toArray()));
        });

        showSuggestions(anchor);
    }

    private void showSuggestions(View anchor) {
        PopupMenu menu = new PopupMenu(activity, anchor);
        menu.getMenu().add("CANCEL").setTitle("CANCEL").setOnMenuItemClickListener(menuItem -> {
            menu.dismiss();
            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        placesList.forEach(p -> menu.getMenu().add(p.getAddressLine(0)).setTitle(p.getAddressLine(0)).setOnMenuItemClickListener(menuItem -> {
            String item = menuItem.getTitle().toString();
            Toast.makeText(activity, item, Toast.LENGTH_SHORT).show();
            binding.locationTv.setText(delivery ? "Delivery location : " + item : "Collection location : " + item);
            Snackbar.make(binding.getRoot(), item, Snackbar.LENGTH_LONG).show();

            Optional<Address> add = placesList.stream().filter(p1 -> p1.getAddressLine(0).equals(item)).findFirst();
            if (add.isPresent()) {
                LatLng lat = new LatLng(add.get().getLatitude(), add.get().getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lat, 15));
                addMarkerToMap(googleMap, lat, add.get().getAddressLine(0));
                LinkedHashMap<String, String> locationMap = new LinkedHashMap<>();
                locationMap.put(LATITUDE, String.valueOf(lat.latitude));
                locationMap.put(LONGITUDE, String.valueOf(lat.longitude));
                String location = getStringFromMap(locationMap);
                String address = getFromLocation(lat);

                if (delivery) {
                    donationCreationForm.setDelivery_location(location);
                    donationCreationForm.setDelivery_address(address);
                } else {
                    donationCreationForm.setCollection_location(location);
                    donationCreationForm.setCollection_address(address);
                }
            }

            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS));
        menu.show();
    }

    private void addMarkerToMap(GoogleMap googleMap, LatLng latLng, String address) {
        binding.locationPB.setVisibility(View.VISIBLE);
        @SuppressLint("UseCompatLoadingForDrawables") MarkerOptions markerOptions = new MarkerOptions().position(latLng).draggable(true).title(address != null ? address : getFromLocation(latLng))/*.snippet("Click to confirm location")*/.icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(activity.getDrawable(R.drawable.ic_served_food))));
        googleMap.clear();
        googleMap.addMarker(markerOptions);
        binding.locationPB.setVisibility(View.GONE);
    }

    private String getFromMarker(Marker marker) {
        binding.locationPB.setVisibility(View.VISIBLE);
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
            if (addresses.isEmpty()) {
                return null;
            }
            Address add = addresses.get(0);
            Snackbar.make(activity.findViewById(R.id.content), add.getAddressLine(0), Snackbar.LENGTH_LONG).show();
            LatLng latLng = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
            LinkedHashMap<String, String> locationMap = new LinkedHashMap<>();
            locationMap.put(LATITUDE, String.valueOf(latLng.latitude));
            locationMap.put(LONGITUDE, String.valueOf(latLng.longitude));
            String location = getStringFromMap(locationMap);

            String address = add.getAddressLine(0);
            binding.locationTv.setText(delivery ? "Delivery location : " + address : "Collection location : " + address);

            if (delivery) {
                donationCreationForm.setDelivery_location(location);
                donationCreationForm.setDelivery_address(address);
            } else {
                donationCreationForm.setCollection_location(location);
                donationCreationForm.setCollection_address(address);
            }

            binding.locationPB.setVisibility(View.GONE);

            return add.getAddressLine(0);
        } catch (IOException | IllegalArgumentException e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
            LinkedHashMap<String, String> locationMap = new LinkedHashMap<>();
            locationMap.put(LATITUDE, String.valueOf(latLng.latitude));
            locationMap.put(LONGITUDE, String.valueOf(latLng.longitude));
            String location = getStringFromMap(locationMap);
            String address = location;

            if (delivery) {
                donationCreationForm.setDelivery_location(location);
                donationCreationForm.setDelivery_address(address);
            } else {
                donationCreationForm.setCollection_location(location);
                donationCreationForm.setCollection_address(address);
            }

            binding.locationTv.setText(delivery ? "Delivery location : " + address : "Collection location : " + address);

            return null;
        }
    }

    private String getFromLocation(LatLng latLng) {
        binding.locationPB.setVisibility(View.VISIBLE);
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.isEmpty()) {
                return null;
            }
            Address add = addresses.get(0);
            LinkedHashMap<String, String> locationMap = new LinkedHashMap<>();
            locationMap.put(LATITUDE, String.valueOf(latLng.latitude));
            locationMap.put(LONGITUDE, String.valueOf(latLng.longitude));
            String location = getStringFromMap(locationMap);
            String address = add.getAddressLine(0);
            binding.locationTv.setText(delivery ? "Delivery location : " + address : "Collection location : " + address);

            if (delivery) {
                donationCreationForm.setDelivery_location(location);
                donationCreationForm.setDelivery_address(address);
            } else {
                donationCreationForm.setCollection_location(location);
                donationCreationForm.setCollection_address(address);
            }

            binding.locationPB.setVisibility(View.GONE);
            return add.getAddressLine(0);
        } catch (IOException | IllegalArgumentException e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();

            LinkedHashMap<String, String> locationMap = new LinkedHashMap<>();
            locationMap.put(LATITUDE, String.valueOf(latLng.latitude));
            locationMap.put(LONGITUDE, String.valueOf(latLng.longitude));
            String location = getStringFromMap(locationMap);
            String address = location;
            binding.locationTv.setText(delivery ? "Delivery location : " + address : "Collection location : " + address);

            if (delivery) {
                donationCreationForm.setDelivery_location(location);
                donationCreationForm.setDelivery_address(address);
            } else {
                donationCreationForm.setCollection_location(location);
                donationCreationForm.setCollection_address(address);
            }

            return null;
        }
    }
}