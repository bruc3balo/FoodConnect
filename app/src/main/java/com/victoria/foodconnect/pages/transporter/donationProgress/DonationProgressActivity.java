package com.victoria.foodconnect.pages.transporter.donationProgress;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.DONATION;
import static com.victoria.foodconnect.globals.GlobalVariables.READ_ONLY;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.pages.transporter.MoreActivity.PURCHASE;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.ybq.android.spinkit.SpinKitView;
import com.victoria.foodconnect.adapter.DonationProgressPagerAdapter;
import com.victoria.foodconnect.adapter.JobProgressPagerAdapter;
import com.victoria.foodconnect.databinding.ActivityDonationProgressBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.purchaseDb.PurchaseViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pagerTransformers.DepthPageTransformer;
import com.victoria.foodconnect.pages.transporter.jobProgress.JobActivityProgress;
import com.victoria.foodconnect.utils.DistributionStatus;

import java.util.Arrays;
import java.util.Optional;

import me.relex.circleindicator.CircleIndicator3;

public class DonationProgressActivity extends AppCompatActivity {

    private ActivityDonationProgressBinding binding;
    private Models.Donation donation;
    private Domain.AppUser user;
    private Models.DonationDistribution distributionModel;
    private DonationProgressPagerAdapter donationProgressPagerAdapter;
    private PurchaseViewModel purchaseViewModel;
    @SuppressLint("StaticFieldLeak")
    private static SpinKitView pb;
    private boolean readOnly;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDonationProgressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pb = binding.pb;

        donation = (Models.Donation) getIntent().getExtras().get(DONATION);
        readOnly = getIntent().getExtras().getBoolean(READ_ONLY);
        purchaseViewModel = new ViewModelProvider(this).get(PurchaseViewModel.class);

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        getDonationDistribution(donation.getId());

        userRepository.getUserLive().observe(this, appUser -> appUser.ifPresent(u -> user = u));

        setWindowColors(this);
    }


    private void getDonationDistribution(Long donationId) {
        donationInProgress();
        purchaseViewModel.getADonationDistributionLive(donationId).observe(this, optionalDistributionModel -> {
            donationOutProgress();
            optionalDistributionModel.ifPresent(this::setPageData);
        });
    }

    private void setUpDonationPager(Models.Donation donation, Models.DonationDistribution distribution) {
        ViewPager2 jobPager = binding.progressPages;
        CircleIndicator3 indicator = binding.indicator;
        donationProgressPagerAdapter = new DonationProgressPagerAdapter(DonationProgressActivity.this, getSupportFragmentManager(), getLifecycle(), donation, distribution, readOnly);
        jobPager.setUserInputEnabled(false);
        jobPager.setAdapter(donationProgressPagerAdapter);
        jobPager.setPageTransformer(new DepthPageTransformer());
        donationProgressPagerAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());
        indicator.setViewPager(jobPager);
    }

    public static void updateDonationDistribution(DonationProgressActivity donationProgressActivity, Models.DonorDistributionUpdateForm form) {
        donationInProgress();
        new ViewModelProvider(donationProgressActivity).get(PurchaseViewModel.class).updateADonationDistribution(form).observe(donationProgressActivity, optionalDistributionModel -> {
            donationOutProgress();
            optionalDistributionModel.ifPresent(donationProgressActivity::setPageData);
        });
    }

    public static void donationInProgress() {
        inSpinnerProgress(pb,null);
    }

    public static void donationOutProgress() {
        outSpinnerProgress(pb,null);
    }

    private void setPageData(Models.DonationDistribution distribution) {
        distributionModel = distribution;
        setUpDonationPager(donation, distribution);
        System.out.println("STATUS IS " + distribution.getStatus());
        Optional<DistributionStatus> distributionStatus = Arrays.stream(DistributionStatus.values()).filter(i -> i.getCode() == distribution.getStatus()).findFirst();
        binding.toolbar.setTitle(donation.getDelivery_address());
        distributionStatus.ifPresent(status -> binding.toolbar.setSubtitle(status.getDescription()));
        donationProgressPagerAdapter.notifyDataSetChanged();
        binding.progressPages.setCurrentItem(distribution.getStatus());


        switch (distribution.getStatus()) {

            //DistributionStatus.ACCEPTED
            default:
            case 0:


                break;

            // DistributionStatus.COLLECTING_ITEMS
            case 1:

                break;
        }
    }

}