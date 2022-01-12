package com.victoria.foodconnect.adapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.victoria.foodconnect.pages.donor.AddItemDonor;
import com.victoria.foodconnect.pages.donor.addFragments.ChooseBeneficiary;
import com.victoria.foodconnect.pages.donor.addFragments.DonationProductsFragment;
import com.victoria.foodconnect.pages.donor.addFragments.DonorLocationFragment;

import org.jetbrains.annotations.NotNull;


public class AddDonationItemsPagerAdapter extends FragmentStateAdapter {

    private final AddItemDonor activity;
    public static final String[] donationSages = new String[]{"Choose a beneficiary", "Add donation items", "Put pick up location", "Put delivery location"};
    private final ActivityResultLauncher<String> launcher;


    public AddDonationItemsPagerAdapter(AddItemDonor activity, @NonNull FragmentManager fm, Lifecycle lifecycle, ActivityResultLauncher<String> launcher) {
        super(fm, lifecycle);
        this.activity = activity;
        this.launcher = launcher;
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            default:
            case 0:
                return new ChooseBeneficiary(activity);

            case 1:
                return new DonationProductsFragment(activity, launcher);

            case 2:
                return new DonorLocationFragment(activity, false);

            case 3:
                return new DonorLocationFragment(activity, true);
        }
    }


    @Override
    public int getItemCount() {
        return donationSages.length;
    }

}


