package com.victoria.foodconnect.pages.admin.activities;

import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.pages.admin.fragments.StatsFragment.getBarSeries;
import static com.victoria.foodconnect.pages.admin.fragments.StatsFragment.getMyColors;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.charts.Cartesian3d;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.TooltipPositionMode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.databinding.ActivitySellerStatsBinding;
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

public class SellerStatsActivity extends AppCompatActivity {

    private ActivitySellerStatsBinding binding;
    private AnyChartView sellerCHart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellerStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        sellerCHart = binding.sellerProductsChart;
        sellerCHart.setProgressBar(binding.sellerPb);


        setWindowColors(this);

        populateSellerChart();
    }


    private void setUpSellerBarChart(List<Models.SellerStats> allSellerStats, MyLinkedMap<String, String> sellers) {
        inSpinnerProgress(binding.sellerPb, null);

        Cartesian3d cartesian = AnyChart.bar3d();

        cartesian.animation(true);

        cartesian.padding(10d, 40d, 5d, 20d);

        binding.title.setText("Seller products for 2022");

        cartesian.yScale().minimum(0);
        cartesian.xAxis(0).title("Months").labels().rotation(90).padding(0d, 0d, 20d, 0d);
        cartesian.yAxis(0).title("Number of products").labels().format("{%Value}{groupsSeparator: }").rotation(45);

        List<DataEntry> seriesData = new ArrayList<>();

        allSellerStats.forEach(sellerStats -> seriesData.add(new Models.Bar3dDataEntry(sellerStats, true)));

        Set set = Set.instantiate();
        set.data(seriesData);

        sellers.forEach((pid, color) -> getBarSeries(cartesian, set, pid, color));


        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 20d, 0d);

        cartesian.interactivity().hoverMode(HoverMode.SINGLE);
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT).position("right").anchor(Anchor.LEFT_CENTER).offsetX(5d).offsetY(0d).format("{%Value} items sold");
        cartesian.zAspect("10%").zPadding(20d).zAngle(45d).zDistribution(true);


        sellerCHart.setChart(cartesian);
        outSpinnerProgress(binding.sellerPb, null);

    }


    private void populateSellerChart() {
        List<Models.SellerStats> allSellerStats = new ArrayList<>();
        MyLinkedMap<String, String> sellers = new MyLinkedMap<>();


        inSpinnerProgress(binding.sellerPb, null);
        new ViewModelProvider(this).get(StatsViewModel.class).getAllSellerStatsLive(2022).observe(SellerStatsActivity.this, sellerStats -> {

            if (sellerStats.isEmpty() || sellerStats.get(0).getProducts().isEmpty()) {
                Toast.makeText(SellerStatsActivity.this, "No data", Toast.LENGTH_SHORT).show();
                outSpinnerProgress(binding.sellerPb, null);
                binding.sellerNoData.setVisibility(View.VISIBLE);
                return;
            }

            binding.sellerNoData.setVisibility(View.GONE);

            allSellerStats.addAll(sellerStats);
            allSellerStats.get(0).getProducts().keySet().forEach(pid -> {
                for (String color : getMyColors()) {
                    if (!sellers.containsValue(color)) {
                        sellers.put(pid, color);
                        break;
                    }
                }
            });

            setUpSellerBarChart(allSellerStats, sellers);
        });

    }


}