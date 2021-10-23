package com.victoria.foodconnect.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayout;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.login.fragments.create.CreateFragment;
import com.victoria.foodconnect.login.fragments.signin.SignInFragment;


import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class LoginPagerAdapter extends FragmentStateAdapter {


    private final String[] loginTitles = new String[]{"Sign in", "Sign up"};
    private final int[] loginIcons = new int[]{R.drawable.ic_unlock, R.drawable.ic_lock};

    public LoginPagerAdapter(@NonNull FragmentManager fm, Lifecycle lifecycle) {
        super(fm, lifecycle);
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new SignInFragment();
        } else {
            return new CreateFragment();
        }
    }

    public String getLoginTitles(int position) {
        return loginTitles[position];
    }

    public int getLoginIcons(int position) {
        return loginIcons[position];
    }

    @Override
    public int getItemCount() {
        return loginIcons.length;
    }



    public void setAllTabIcons(TabLayout tab) {
        for (int i = 0; i <= loginIcons.length - 1; i++) {
            Objects.requireNonNull(tab.getTabAt(i)).setIcon(loginIcons[i]);
            Objects.requireNonNull(tab.getTabAt(i)).setText(loginTitles[i]);
        }
    }


}


