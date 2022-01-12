package com.victoria.foodconnect.pages.donor.addFragments;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.RED;
import static com.victoria.foodconnect.adapter.DonorProductsRvAdapter.donorItems;
import static com.victoria.foodconnect.adapter.DonorProductsRvAdapter.itemPosition;

import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.adapter.DonorProductsRvAdapter;
import com.victoria.foodconnect.databinding.FragmentDonationProductsBinding;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.donor.AddItemDonor;

import java.util.function.Function;


public class DonationProductsFragment extends Fragment {


    private FragmentDonationProductsBinding binding;
    private AddItemDonor activity;
    public static DonorProductsRvAdapter donorProductsRvAdapter;
    private ActivityResultLauncher<String> launcher;

    public DonationProductsFragment() {
        // Required empty public constructor
    }

    public DonationProductsFragment(AddItemDonor activity,ActivityResultLauncher<String> launcher) {
        // Required empty public constructor
        this.activity = activity;
        this.launcher = launcher;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        setHasOptionsMenu(true);

        binding = FragmentDonationProductsBinding.inflate(inflater);

        ViewPager2 productsRv = binding.productsViewPager;
        donorProductsRvAdapter = new DonorProductsRvAdapter(activity, launcher);
        productsRv.setAdapter(donorProductsRvAdapter);
        productsRv.setUserInputEnabled(true);
        productsRv.setOffscreenPageLimit(3);
        productsRv.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        productsRv.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        productsRv.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                itemPosition = position;
                super.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
        productsRv.setClipToPadding(true);
        productsRv.setClipChildren(true);
        productsRv.setPadding(0, 20, 0, 20);


        return binding.getRoot();
    }


    public static void getSelectedImage(Uri uri, Function<Uri, Void> function) {
        function.apply(uri);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        menu.add("Remove Item").setIcon(R.drawable.minus).setIconTintList(ColorStateList.valueOf(RED)).setOnMenuItemClickListener(item -> {
            removeItem();
            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


        menu.add("Add Item").setIcon(R.drawable.add).setIconTintList(ColorStateList.valueOf(BLACK)).setOnMenuItemClickListener(item -> {
            addItem();
            return false;
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


        super.onCreateOptionsMenu(menu, inflater);
    }

    private void addItem() {
        donorItems.add(new Models.DonorItem());
        donorProductsRvAdapter.notifyDataSetChanged();
        binding.productsViewPager.setCurrentItem(donorItems.size() - 1);
    }

    private void removeItem() {
        if (!donorItems.isEmpty()) {
            donorItems.remove(itemPosition);
            donorProductsRvAdapter.notifyDataSetChanged();
            binding.productsViewPager.setCurrentItem(donorItems.size() - 1);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        donorItems.clear();
    }
}