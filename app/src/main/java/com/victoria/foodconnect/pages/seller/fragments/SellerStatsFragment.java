package com.victoria.foodconnect.pages.seller.fragments;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;

import static com.victoria.foodconnect.pages.admin.fragments.StatsFragment.getBarSeries;
import static com.victoria.foodconnect.pages.admin.fragments.StatsFragment.getMyColors;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.charts.Cartesian3d;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.TooltipPositionMode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.FragmentSellerStatsBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.statsDb.StatsViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.MyLinkedMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;


public class SellerStatsFragment extends Fragment {

    private FragmentSellerStatsBinding binding;
    private AnyChartView anyChartView;
    private final MyLinkedMap<String, String> productIds = new MyLinkedMap<>();
    private final List<Models.SellerStats> allSellerStats = new ArrayList<>();


    public SellerStatsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentSellerStatsBinding.inflate(inflater);
        anyChartView = binding.anychart;
        anyChartView.setProgressBar(binding.pb);

        userRepository.getUserLive().observe(getViewLifecycleOwner(), appUser -> appUser.ifPresent(u -> getSellerStats(u.getUsername())));

        return binding.getRoot();
    }

    private void getSellerStats(String username) {
        new ViewModelProvider(this).get(StatsViewModel.class).getSellerStatsLive(2022, username).observe(getViewLifecycleOwner(), sellerStats -> {

            if (sellerStats.isEmpty() || sellerStats.get(0).getProducts().isEmpty()) {
                Toast.makeText(requireContext(), "No data", Toast.LENGTH_SHORT).show();
                //anyChartView.clear();
                outSpinnerProgress(binding.pb, null);
                binding.noData.setVisibility(View.VISIBLE);
                return;
            }

            binding.noData.setVisibility(View.GONE);

            allSellerStats.addAll(sellerStats);

            try {
                System.out.println("Data is "+getObjectMapper().writeValueAsString(sellerStats));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            allSellerStats.get(0).getProducts().keySet().forEach(pid -> {
                for (String color : getMyColors()) {
                    if (!productIds.containsValue(color)) {
                        productIds.put(pid, color);
                        break;
                    }
                }
            });

            setUpBarChart();

        });
    }

    private void setUpBarChart() {
        inSpinnerProgress(binding.pb, null);

        Cartesian3d cartesian = AnyChart.bar3d();

        cartesian.animation(true);

        cartesian.padding(10d, 40d, 5d, 20d);

        cartesian.title("Seller products for " + 2022);
        cartesian.yScale().minimum(0d);
        cartesian.xAxis(0).labels().rotation(-90d).padding(0d, 0d, 20d, 0d);
        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }");
        cartesian.yAxis(0).title("Number of items sold in 2020");

        List<DataEntry> seriesData = new ArrayList<>();
        allSellerStats.forEach(sellerStats -> seriesData.add(new Models.Bar3dDataEntry(sellerStats,true)));

        Set set = Set.instantiate();
        set.data(seriesData);

        productIds.forEach((pid, color) -> getBarSeries(cartesian, set, pid, color));


        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 20d, 0d);

        cartesian.interactivity().hoverMode(HoverMode.SINGLE);
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT).position("right").anchor(Anchor.LEFT_CENTER).offsetX(5d).offsetY(0d).format("{%Value} items sold");
        cartesian.zAspect("10%").zPadding(20d).zAngle(45d).zDistribution(true);


        anyChartView.setChart(cartesian);
        outSpinnerProgress(binding.pb, null);

    }

}