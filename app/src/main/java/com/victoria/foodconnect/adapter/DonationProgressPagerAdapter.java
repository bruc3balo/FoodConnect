package com.victoria.foodconnect.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.transporter.donationProgress.ArrivedDonationFragment;
import com.victoria.foodconnect.pages.transporter.donationProgress.DeliverFragment;
import com.victoria.foodconnect.pages.transporter.donationProgress.DonationProgressActivity;
import com.victoria.foodconnect.pages.transporter.donationProgress.StartFragment;
import com.victoria.foodconnect.pages.transporter.donationProgress.ToCollectionFragment;
import com.victoria.foodconnect.pages.transporter.jobProgress.ReviewFragment;
import com.victoria.foodconnect.utils.DistributionStatus;

import org.jetbrains.annotations.NotNull;


public class DonationProgressPagerAdapter extends FragmentStateAdapter {


    private final Models.Donation donation;
    private final Models.DonationDistribution distribution;
    private final DonationProgressActivity activity;
    private final boolean readOnly;


    public DonationProgressPagerAdapter(DonationProgressActivity activity, @NonNull FragmentManager fm, Lifecycle lifecycle, Models.Donation donation, Models.DonationDistribution distribution, boolean readOnly) {
        super(fm, lifecycle);
        this.donation = donation;
        this.distribution = distribution;
        this.activity = activity;
        this.readOnly = readOnly;
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            default:
            case 0:
                return new StartFragment(activity, distribution, donation, readOnly);

            case 1:
                return new ToCollectionFragment(activity, distribution, donation, readOnly);

            case 2:
                return new DeliverFragment(activity, distribution, donation, readOnly);

            case 3:
                return new ArrivedDonationFragment(activity, distribution, donation, readOnly);

            case 4:
            case 5:
                return new ReviewFragment(activity, donation, distribution);

        }

    }


    @Override
    public int getItemCount() {
        return DistributionStatus.values().length;
    }


}


