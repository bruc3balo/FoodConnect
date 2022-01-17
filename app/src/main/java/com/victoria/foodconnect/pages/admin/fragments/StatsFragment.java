package com.victoria.foodconnect.pages.admin.fragments;

import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;

import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.charts.Cartesian3d;
import com.anychart.core.cartesian.series.Bar3d;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.TooltipPositionMode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.databinding.FragmentSellerProductStatsBinding;
import com.victoria.foodconnect.databinding.FragmentStatsBinding;
import com.victoria.foodconnect.globals.statsDb.StatsViewModel;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.models.AppRolesEnum;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.MyLinkedMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    //seller
    private final Calendar selectedDate = Calendar.getInstance();
    private final List<Models.SellerStats> allSellerStats = new ArrayList<>();
    private AnyChartView sellerCHart, donorChart;
    private final MyLinkedMap<String, String> productIds = new MyLinkedMap<>();
    private final ArrayList<String> sellerList = new ArrayList<>();
    private static final String ALL_SELLERS = "All sellers";
    private String selectedSellerUsername = ALL_SELLERS;

    //donation
    private final MyLinkedMap<String, String> donors = new MyLinkedMap<>();
    private final ArrayList<String> donorsList = new ArrayList<>();
    private final List<Models.SellerStats> allDonorStats = new ArrayList<>();

    private static final String ALL_DONORS = "All donors";
    private String selectedDonorUsername = ALL_DONORS;

    public StatsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentStatsBinding.inflate(inflater);

        sellerCHart = binding.sellerProductsChart;
        sellerCHart.setProgressBar(binding.sellerPb);

        donorChart = binding.donationProductsChart;
        donorChart.setProgressBar(binding.donorPb);


        sellerStats();
        //donorStats();

        return binding.getRoot();
    }

    //seller
    private void sellerStats() {
        selectedSellerUsername = ALL_SELLERS;
        sellerList.add(ALL_SELLERS);

        getSellerList();

        AppCompatSpinner sellerSpinner = binding.seller;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, sellerList);
        sellerSpinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        sellerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (sellerList.isEmpty()) {
                    selectedSellerUsername = ALL_SELLERS;
                } else {
                    selectedSellerUsername = sellerList.get(position);
                }
                Toast.makeText(requireContext(), selectedSellerUsername, Toast.LENGTH_SHORT).show();
                populateSellerChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void setUpSellerBarChart() {
        inSpinnerProgress(binding.sellerPb, null);

        Cartesian3d cartesian = AnyChart.bar3d();

        cartesian.animation(true);

        cartesian.padding(10d, 40d, 5d, 20d);

        cartesian.title("Seller products for " + selectedDate.get(Calendar.YEAR));
        cartesian.yScale().minimum(0d);
        cartesian.xAxis(0).labels().rotation(-90d).padding(0d, 0d, 20d, 0d);
        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }");
        cartesian.yAxis(0).title("Number of items sold in " + selectedDate.get(Calendar.YEAR));

        List<DataEntry> seriesData = new ArrayList<>();
        allSellerStats.forEach(sellerStats -> seriesData.add(new Models.Bar3dDataEntry(sellerStats, true)));

        Set set = Set.instantiate();
        set.data(seriesData);

        productIds.forEach((pid, color) -> getBarSeries(cartesian, set, pid, color));


        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 20d, 0d);

        cartesian.interactivity().hoverMode(HoverMode.SINGLE);
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT).position("right").anchor(Anchor.LEFT_CENTER).offsetX(5d).offsetY(0d).format("{%Value} items sold");
        cartesian.zAspect("10%").zPadding(20d).zAngle(45d).zDistribution(true);


        sellerCHart.setChart(cartesian);
        outSpinnerProgress(binding.sellerPb, null);

    }

    public void getSellerList() {


        new ViewModelProvider(this).get(UserViewModel.class).getLiveAllUsers().observe(requireActivity(), jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                outSpinnerProgress(binding.sellerPb, null);
                Toast.makeText(requireContext(), "No users", Toast.LENGTH_SHORT).show();
                return;
            }

            sellerList.clear();
            try {
                JsonArray serviceArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.get().getData()));

                sellerList.add(ALL_SELLERS);
                for (int i = 0; i < serviceArray.size(); i++) {

                    try {
                        System.out.println("count " + i);
                        Models.AppUser appUser = getObjectMapper().readValue(new JsonObject(serviceArray.getJsonObject(i).getMap()).toString(), Models.AppUser.class);
                        if (appUser.getRole().getName().equals(AppRolesEnum.ROLE_SELLER.name())) {
                            sellerList.add(appUser.getUsername());
                        }

                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                }

                outSpinnerProgress(binding.sellerPb, null);


            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    private void populateSellerChart() {
        if (!allSellerStats.isEmpty()) {
            sellerCHart.clear();
        }
        allSellerStats.clear();
        productIds.clear();

        inSpinnerProgress(binding.sellerPb, null);
        if (selectedSellerUsername.equals(ALL_SELLERS)) {
            new ViewModelProvider(this).get(StatsViewModel.class).getAllSellerStatsLive(selectedDate.get(Calendar.YEAR)).observe(getViewLifecycleOwner(), sellerStats -> {

                if (sellerStats.isEmpty() || sellerStats.get(0).getProducts().isEmpty()) {
                    Toast.makeText(requireContext(), "No data", Toast.LENGTH_SHORT).show();
                    outSpinnerProgress(binding.sellerPb, null);
                    binding.sellerNoData.setVisibility(View.VISIBLE);
                    return;
                }

                binding.sellerNoData.setVisibility(View.GONE);

                allSellerStats.addAll(sellerStats);
                allSellerStats.get(0).getProducts().keySet().forEach(pid -> {
                    for (String color : getMyColors()) {
                        if (!productIds.containsValue(color)) {
                            productIds.put(pid, color);
                            break;
                        }
                    }
                });

                setUpSellerBarChart();

            });
        } else {
            new ViewModelProvider(this).get(StatsViewModel.class).getSellerStatsLive(selectedDate.get(Calendar.YEAR), selectedSellerUsername).observe(getViewLifecycleOwner(), sellerStats -> {

                if (sellerStats.isEmpty() || sellerStats.get(0).getProducts().isEmpty()) {
                    Toast.makeText(requireContext(), "No data", Toast.LENGTH_SHORT).show();
                    sellerCHart.clear();
                    outSpinnerProgress(binding.sellerPb, null);
                    binding.sellerNoData.setVisibility(View.VISIBLE);

                    return;
                }

                binding.sellerNoData.setVisibility(View.GONE);

                allSellerStats.addAll(sellerStats);
                allSellerStats.get(0).getProducts().keySet().forEach(pid -> {
                    for (String color : getMyColors()) {
                        if (!productIds.containsValue(color)) {
                            productIds.put(pid, color);
                            break;
                        }
                    }
                });

                setUpSellerBarChart();

            });
        }
    }


    //donor
    private void donorStats() {
        selectedDonorUsername = ALL_DONORS;
        donorsList.add(ALL_DONORS);

        getDonorsList();


        AppCompatSpinner sellerSpinner = binding.seller;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, donorsList);
        sellerSpinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        sellerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (donorsList.isEmpty()) {
                    selectedDonorUsername = ALL_DONORS;
                } else {
                    selectedDonorUsername = donorsList.get(position);
                }
                Toast.makeText(requireContext(), selectedDonorUsername, Toast.LENGTH_SHORT).show();
                populateDonorChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void getDonorsList() {


        new ViewModelProvider(this).get(UserViewModel.class).getLiveAllUsers().observe(requireActivity(), jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                outSpinnerProgress(binding.donorPb, null);
                Toast.makeText(requireContext(), "No users", Toast.LENGTH_SHORT).show();
                return;
            }

            donorsList.clear();
            try {
                JsonArray serviceArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.get().getData()));

                donorsList.add(ALL_DONORS);
                for (int i = 0; i < serviceArray.size(); i++) {

                    try {
                        System.out.println("count " + i);
                        Models.AppUser appUser = getObjectMapper().readValue(new JsonObject(serviceArray.getJsonObject(i).getMap()).toString(), Models.AppUser.class);
                        if (appUser.getRole().getName().equals(AppRolesEnum.ROLE_DONOR.name())) {
                            donorsList.add(appUser.getUsername());
                        }

                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                }

                outSpinnerProgress(binding.donorPb, null);


            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    private void setUpDonorBarChart() {
        inSpinnerProgress(binding.donorPb, null);

        Cartesian3d cartesian = AnyChart.bar3d();

        cartesian.animation(true);

        cartesian.padding(10d, 40d, 5d, 20d);

        cartesian.title("Donation products for 2022");
        cartesian.yScale().minimum(0d);
        cartesian.xAxis(0).labels().rotation(-90d).padding(0d, 0d, 20d, 0d);
        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }");
        cartesian.yAxis(0).title("Number of items donated in " + selectedDate.get(Calendar.YEAR));

        List<DataEntry> seriesData = new ArrayList<>();
        allDonorStats.forEach(sellerStats -> seriesData.add(new Models.Bar3dDataEntry(sellerStats, false)));

        Set set = Set.instantiate();
        set.data(seriesData);

        donors.forEach((pid, color) -> getBarSeries(cartesian, set, pid, color));


        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 20d, 0d);

        cartesian.interactivity().hoverMode(HoverMode.SINGLE);
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT).position("right").anchor(Anchor.LEFT_CENTER).offsetX(5d).offsetY(0d).format("{%Value} items sold");
        cartesian.zAspect("10%").zPadding(20d).zAngle(45d).zDistribution(true);


        donorChart.setChart(cartesian);
        outSpinnerProgress(binding.donorPb, null);

    }

    private void populateDonorChart() {
        if (!allDonorStats.isEmpty()) {
            donorChart.clear();
        }
        allDonorStats.clear();
        donorsList.clear();

        inSpinnerProgress(binding.donorPb, null);
        if (selectedDonorUsername.equals(ALL_DONORS)) {
            new ViewModelProvider(this).get(StatsViewModel.class).getAllDonorStatsLive(selectedDate.get(Calendar.YEAR)).observe(getViewLifecycleOwner(), sellerStats -> {

                if (sellerStats.isEmpty() || sellerStats.get(0).getProducts().isEmpty()) {
                    Toast.makeText(requireContext(), "No data", Toast.LENGTH_SHORT).show();
                    outSpinnerProgress(binding.donorPb, null);
                    binding.noDonorData.setVisibility(View.VISIBLE);
                    return;
                }

                binding.noDonorData.setVisibility(View.GONE);

                allDonorStats.addAll(sellerStats);
                allDonorStats.get(0).getProducts().keySet().forEach(pid -> {
                    for (String color : getMyColors()) {
                        if (!donors.containsValue(color)) {
                            donors.put(pid, color);
                            break;
                        }
                    }
                });

                setUpDonorBarChart();

            });
        } else {
            new ViewModelProvider(this).get(StatsViewModel.class).getDonorStatsLive(selectedDate.get(Calendar.YEAR), selectedDonorUsername).observe(getViewLifecycleOwner(), sellerStats -> {

                if (sellerStats.isEmpty() || sellerStats.get(0).getProducts().isEmpty()) {
                    Toast.makeText(requireContext(), "No data", Toast.LENGTH_SHORT).show();
                    donorChart.clear();
                    outSpinnerProgress(binding.donorPb, null);
                    binding.noDonorData.setVisibility(View.VISIBLE);

                    return;
                }

                binding.noDonorData.setVisibility(View.GONE);

                allDonorStats.addAll(sellerStats);
                allDonorStats.get(0).getProducts().keySet().forEach(pid -> {
                    for (String color : getMyColors()) {
                        if (!donors.containsValue(color)) {
                            donors.put(pid, color);
                            break;
                        }
                    }
                });

                setUpDonorBarChart();

            });
        }
    }


    //default
    public static String[] getMyColors() {
        return new String[]{"#3220d6", "#d81125", "#edf41d", "#46e822", "#eaea10", "#b2c1ba", "#4f5451", "#db801e", "#e01fd3"};
    }

    public static Bar3d getBarSeries(Cartesian3d bar3d, Set set, String product, String color) {
        product = product.contains(HY) ? getProductFromString(product) : product;
        String mapping = "{ x: 'x', value: '" + product + "' }";
        System.out.println("Mapping is " + mapping + " and product is " + product);
        final Bar3d bar = bar3d.bar(set.mapAs(mapping)).name(product);
        bar.color(color);
        return bar;
    }

    public static String getSingleString(String s) {
        return s.replaceAll(" ", "").trim();
    }

    public static String getProductFromString(String s) {
        return s.split("-")[1].replaceAll(" ", "").trim();
    }

}