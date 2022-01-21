package com.victoria.foodconnect.pages.transporter.jobProgress;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.transporter.MoreActivity.PURCHASE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.victoria.foodconnect.adapter.JobProgressPagerAdapter;
import com.victoria.foodconnect.databinding.ActivityJobProgressBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.purchaseDb.PurchaseViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pagerTransformers.DepthPageTransformer;
import com.victoria.foodconnect.utils.DistributionStatus;

import java.util.Arrays;
import java.util.Optional;

import me.relex.circleindicator.CircleIndicator3;

public class JobActivityProgress extends AppCompatActivity {

    private ActivityJobProgressBinding binding;
    private Models.Purchase purchase;
    private Domain.AppUser user;
    private Models.DistributionModel distributionModel;
    private JobProgressPagerAdapter jobPagerAdapter;
    private PurchaseViewModel purchaseViewModel;
    @SuppressLint("StaticFieldLeak")
    private static ProgressBar pb;
    public static MutableLiveData<Optional<Boolean>> refreshJobPurchaseProgress = new MutableLiveData<>();
    private final MutableLiveData<Optional<Integer>> currentStatus = new MutableLiveData<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityJobProgressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pb = binding.pb;

        purchaseViewModel = new ViewModelProvider(this).get(PurchaseViewModel.class);

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        purchase = (Models.Purchase) getIntent().getExtras().get(PURCHASE);
        getDistribution(purchase.getId());

        userRepository.getUserLive().observe(this, appUser -> appUser.ifPresent(u -> user = u));


        setWindowColors(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    private void getDistribution(Long purchaseId) {
        jobInProgress();
        purchaseViewModel.getADistributionLive(purchaseId).observe(this, optionalDistributionModel -> {
            jobOutProgress();
            optionalDistributionModel.ifPresent(d -> setUpJobPager(purchase, d));
        });
    }

    private void setUpJobPager(Models.Purchase purchase, Models.DistributionModel distribution) {
        ViewPager2 jobPager = binding.progressPages;
        CircleIndicator3 indicator = binding.indicator;
        jobPagerAdapter = new JobProgressPagerAdapter(JobActivityProgress.this, getSupportFragmentManager(), getLifecycle(), purchase, distribution, false);
        jobPager.setUserInputEnabled(false);
        jobPager.setAdapter(jobPagerAdapter);
        jobPager.setPageTransformer(new DepthPageTransformer());
        jobPagerAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());
        indicator.setViewPager(jobPager);
        setPageData(distribution);
    }

    public static void update(JobActivityProgress jobActivityProgress, Models.DistributionUpdateForm form) {
        jobInProgress();
        new ViewModelProvider(jobActivityProgress).get(PurchaseViewModel.class).updateADistribution(form).observe(jobActivityProgress, optionalDistributionModel -> {
            jobOutProgress();
            optionalDistributionModel.ifPresent(jobActivityProgress::updatePage);
        });
    }

    private void updatePage (Models.DistributionModel distribution) {
        Arrays.stream(DistributionStatus.values()).filter(i -> i.getCode() == distribution.getStatus()).findFirst().ifPresent(status -> binding.toolbar.setSubtitle(status.getDescription()));
        jobPagerAdapter = new JobProgressPagerAdapter(JobActivityProgress.this, getSupportFragmentManager(), getLifecycle(), purchase, distribution, false);
        binding.progressPages.setAdapter(jobPagerAdapter);
        currentStatus.setValue(Optional.of(distribution.getStatus()));
        jobPagerAdapter.notifyDataSetChanged();

    }

    public static void jobInProgress() {
        pb.setVisibility(View.VISIBLE);
    }

    public static void jobOutProgress() {
        pb.setVisibility(View.GONE);
    }

    private void setPageData(Models.DistributionModel distribution) {
        distributionModel = distribution;
        System.out.println("STATUS IS " + distribution.getStatus());
        Optional<DistributionStatus> distributionStatus = Arrays.stream(DistributionStatus.values()).filter(i -> i.getCode() == distribution.getStatus()).findFirst();
        binding.toolbar.setTitle(purchase.getAddress());
        distributionStatus.ifPresent(status -> binding.toolbar.setSubtitle(status.getDescription()));
        jobPagerAdapter.notifyDataSetChanged();
        currentStatus.setValue(Optional.of(distribution.getStatus()));


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



    private void addRefreshListener() {
        refreshData().observe(this, refresh -> {
            if (refresh.isPresent()) {
                getDistribution(purchase.getId());
            }
        });
        observeStatus().observe(this,status ->status.ifPresent(s->binding.progressPages.setCurrentItem(s)));
    }

    private void removeListeners() {
        refreshData().removeObservers(this);
        observeStatus().removeObservers(this);
    }

    //listener for updates
    private LiveData<Optional<Boolean>> refreshData () {
        return refreshJobPurchaseProgress;
    }

    private LiveData<Optional<Integer>> observeStatus () {
        return currentStatus;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDistribution(purchase.getId());
        addRefreshListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeListeners();
    }
}