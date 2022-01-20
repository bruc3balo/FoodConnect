package com.victoria.foodconnect.pages.transporter.fragments;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.pages.admin.fragments.StatsFragment.getBarSeries;
import static com.victoria.foodconnect.pages.admin.fragments.StatsFragment.getMyColors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.charts.Cartesian3d;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.TooltipPositionMode;
import com.victoria.foodconnect.databinding.ActivityTransporterPurchaseStatsBinding;
import com.victoria.foodconnect.globals.statsDb.StatsViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.beneficiary.fragment.BuyerPurchasesStats;
import com.victoria.foodconnect.utils.MyLinkedMap;

import java.util.ArrayList;
import java.util.List;

public class TransporterPurchaseStats extends AppCompatActivity {

    private ActivityTransporterPurchaseStatsBinding binding;
    private AnyChartView chart;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransporterPurchaseStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        chart = binding.chart;
        chart.setProgressBar(binding.donorPb);
        userRepository.getUserLive().observe(this, appUser -> appUser.ifPresent(u->{
            username = u.getUsername();
            populateTransporterChart(u.getUsername());
            binding.toolbar.setSubtitle(u.getUsername()+", purchase stats");
        }));


        setWindowColors(this);

    }

    private void setUpTransporterPurchasesBarChart(List<Models.SellerStats> allDonorStats, MyLinkedMap<String, String> donors) {

        inSpinnerProgress(binding.donorPb, null);

        Cartesian3d cartesian = AnyChart.bar3d();

        cartesian.animation(true);

        cartesian.padding(10d, 40d, 5d, 20d);

        binding.title.setText("Purchases, of "+username+", transported in 2022");

        cartesian.yScale().minimum(0d);
        cartesian.xAxis(0).title("Months").labels().rotation(90).padding(0d, 0d, 20d, 0d);
        cartesian.yAxis(0).title("Number of complete purchase deliveries").labels().format("{%Value}{groupsSeparator: }").rotation(45);

        List<DataEntry> seriesData = new ArrayList<>();
        allDonorStats.forEach(sellerStats -> seriesData.add(new Models.Bar3dDataEntry(sellerStats, false)));

        Set set = Set.instantiate();
        set.data(seriesData);

        donors.forEach((pid, color) -> getBarSeries(cartesian, set, pid, color));

        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 20d, 0d);

        cartesian.interactivity().hoverMode(HoverMode.SINGLE);
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT).position("right").anchor(Anchor.LEFT_CENTER).offsetX(5d).offsetY(0d).format("{%Value} purchases delivered");
        cartesian.zAspect("10%").zPadding(20d).zAngle(45d).zDistribution(true);


        chart.setChart(cartesian);
        outSpinnerProgress(binding.donorPb, null);
        setWindowColors(this);

    }

    private void populateTransporterChart(String username) {
        List<Models.SellerStats> allDonorStats = new ArrayList<>();
        MyLinkedMap<String, String> donors = new MyLinkedMap<>();


        inSpinnerProgress(binding.donorPb, null);


        new ViewModelProvider(this).get(StatsViewModel.class).getTransporterPurchasesStatsLive(2022,username).observe(this, sellerStats -> {

            if (sellerStats.isEmpty() || sellerStats.get(0).getProducts().isEmpty()) {
                Toast.makeText(TransporterPurchaseStats.this, "No data", Toast.LENGTH_SHORT).show();
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

            setUpTransporterPurchasesBarChart(allDonorStats, donors);

        });


    }

}