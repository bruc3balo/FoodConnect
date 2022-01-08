package com.victoria.foodconnect.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayout;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.transporter.jobProgress.ArrivedFragment;
import com.victoria.foodconnect.pages.transporter.jobProgress.CollectingFragment;
import com.victoria.foodconnect.pages.transporter.jobProgress.AcceptedFragment;
import com.victoria.foodconnect.pages.transporter.jobProgress.CompleteFragment;
import com.victoria.foodconnect.pages.transporter.jobProgress.DnfFragment;
import com.victoria.foodconnect.pages.transporter.jobProgress.JobActivityProgress;
import com.victoria.foodconnect.pages.transporter.jobProgress.OnTheWayFragment;
import com.victoria.foodconnect.utils.DistributionStatus;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class JobProgressPagerAdapter extends FragmentStateAdapter {


    private final Models.Purchase purchase;
    private final Models.DistributionModel distribution;
    private final JobActivityProgress activity;

    public JobProgressPagerAdapter(JobActivityProgress activity,@NonNull FragmentManager fm, Lifecycle lifecycle, Models.Purchase purchase, Models.DistributionModel distribution) {
        super(fm, lifecycle);
        this.purchase = purchase;
        this.distribution = distribution;
        this.activity = activity;
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            default:
            case 0:
                return new AcceptedFragment(activity,purchase, distribution);

            case 1:
                return new CollectingFragment(activity,purchase, distribution);

            case 2:
                return new OnTheWayFragment(activity,purchase, distribution);

                //todo arrive bug
            case 3:
                return new ArrivedFragment(activity,purchase, distribution);

            case 4:
                return new DnfFragment(activity,purchase,distribution,false);

            case 5:
                return new DnfFragment(activity,purchase,distribution,true);
        }

    }


    @Override
    public int getItemCount() {
        return DistributionStatus.values().length;
    }



}


