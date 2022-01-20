package com.victoria.foodconnect.pages.admin.fragments;

import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;

import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
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
import com.victoria.foodconnect.adapter.StatsPageGrid;
import com.victoria.foodconnect.databinding.FragmentSellerProductStatsBinding;
import com.victoria.foodconnect.databinding.FragmentStatsBinding;
import com.victoria.foodconnect.globals.statsDb.StatsViewModel;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.models.AppRolesEnum;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.admin.activities.DonorStatsActivity;
import com.victoria.foodconnect.pages.admin.activities.SellerStatsActivity;
import com.victoria.foodconnect.utils.MyLinkedMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private GridView gridView;

    //donation


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

        gridView = binding.gridView;
        gridView.setAdapter(new StatsPageGrid());
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    goToSellerStats();
                    break;

                case 1:
                    goToDonorStats();
                    break;
            }
        });

        return binding.getRoot();
    }


    private void goToSellerStats () {
        startActivity(new Intent(requireContext(), SellerStatsActivity.class));
    }

    private void goToDonorStats () {
        startActivity(new Intent(requireContext(), DonorStatsActivity.class));
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