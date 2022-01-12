package com.victoria.foodconnect.pages.donor;

import static android.graphics.Color.BLACK;
import static com.victoria.foodconnect.adapter.AddDonationItemsPagerAdapter.donationSages;
import static com.victoria.foodconnect.adapter.DonorProductsRvAdapter.donorItems;
import static com.victoria.foodconnect.adapter.DonorProductsRvAdapter.donorMutableLiveData;
import static com.victoria.foodconnect.adapter.DonorProductsRvAdapter.itemPosition;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.DONATION;
import static com.victoria.foodconnect.globals.GlobalVariables.MEDIA_TYPE;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.donor.addFragments.DonationProductsFragment.donorProductsRvAdapter;
import static com.victoria.foodconnect.pages.donor.addFragments.DonationProductsFragment.getSelectedImage;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.adapter.AddDonationItemsPagerAdapter;
import com.victoria.foodconnect.databinding.ActivityAddItemDonorBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.service.UploadPictureService;

import java.util.LinkedList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class AddItemDonor extends AppCompatActivity {

    private ActivityAddItemDonorBinding binding;
    private Domain.AppUser user;
    private AddDonationItemsPagerAdapter addDonationItemsPagerAdapter;
    private ActivityResultLauncher<String> launcher;
    public static Models.DonationCreationForm donationCreationForm;


    @SuppressLint("StaticFieldLeak")
    private static ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddItemDonorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        donationCreationForm = new Models.DonationCreationForm();

        pb = binding.pb;
        pb.setVisibility(View.GONE);

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        userRepository.getUserLive().observe(this, appUser -> appUser.ifPresent(u -> user = u));

        launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> getSelectedImage(uri, result -> {
            if (result != null) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        donorItems.get(itemPosition).setImage(result.toString());
                        donorProductsRvAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 1000);
            } else {
                Toast.makeText(AddItemDonor.this, "Product image request empty", Toast.LENGTH_SHORT).show();
                donorItems.get(itemPosition).setImage(null);
            }
            return null;
        }));
        binding.addDonation.setOnClickListener(v -> {
            if (validateForm()) {
                createDonation();
            }
        });

        setUpDonationPager();
        setWindowColors(this);
        outProgress();
    }

    private void setUpDonationPager() {
        ViewPager2 jobPager = binding.addItemPages;
        CircleIndicator3 indicator = binding.indicator;
        addDonationItemsPagerAdapter = new AddDonationItemsPagerAdapter(AddItemDonor.this, getSupportFragmentManager(), getLifecycle(), launcher);
        jobPager.setUserInputEnabled(false);
        jobPager.setAdapter(addDonationItemsPagerAdapter);
        //jobPager.setPageTransformer(new DepthPageTransformer());
        addDonationItemsPagerAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());
        indicator.setViewPager(jobPager);
        jobPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.toolbar.setSubtitle(donationSages[position]);
                if (jobPager.getCurrentItem() == 0) {
                    binding.next.setImageTintList(ColorStateList.valueOf(BLACK));
                    binding.previous.setImageTintList(ColorStateList.valueOf(getColor(R.color.semiGrey)));
                } else if (jobPager.getCurrentItem() == donationSages.length - 1) {
                    binding.next.setImageTintList(ColorStateList.valueOf(getColor(R.color.semiGrey)));
                    binding.previous.setImageTintList(ColorStateList.valueOf(BLACK));
                } else {
                    binding.next.setImageTintList(ColorStateList.valueOf(BLACK));
                    binding.previous.setImageTintList(ColorStateList.valueOf(BLACK));
                }
                super.onPageSelected(position);
            }
        });

        binding.previous.setOnClickListener(v -> {
            if (jobPager.getCurrentItem() == 0) {
                Toast.makeText(AddItemDonor.this, "This is the first step. Pick a beneficiary", Toast.LENGTH_SHORT).show();
            } else {
                jobPager.setCurrentItem(jobPager.getCurrentItem() - 1);
            }
        });

        binding.next.setOnClickListener(v -> {
            if (jobPager.getCurrentItem() == donationSages.length - 1) {
                Toast.makeText(AddItemDonor.this, "Click add donation when done", Toast.LENGTH_SHORT).show();

            } else {
                jobPager.setCurrentItem(jobPager.getCurrentItem() + 1);
            }
        });

    }

    private boolean validateForm() {

        donationCreationForm.getProducts().clear();

        donorItems.forEach(d -> {
            if ((d.getDescription() != null && !d.getDescription().isEmpty()) && (d.getName() != null && !d.getName().isEmpty()) && (d.getImage() != null && !d.getImage().isEmpty())) {
                donationCreationForm.getProducts().add(d);
            }
        });

        donationCreationForm.setDonor(user.getUsername());

        if (donationCreationForm.getBeneficiary() == null) {
            binding.addItemPages.setCurrentItem(0);
            Toast.makeText(AddItemDonor.this, "Pick a donor to receive the donation", Toast.LENGTH_SHORT).show();
            return false;
        } else if (donationCreationForm.getProducts().isEmpty()) {
            binding.addItemPages.setCurrentItem(1);
            Toast.makeText(AddItemDonor.this, "Add items for donation", Toast.LENGTH_SHORT).show();
            return false;
        } else if (donationCreationForm.getCollection_address() == null || donationCreationForm.getCollection_location() == null) {
            binding.addItemPages.setCurrentItem(2);
            Toast.makeText(AddItemDonor.this, "Add items collection address", Toast.LENGTH_SHORT).show();
            return false;
        } else if (donationCreationForm.getDelivery_address() == null || donationCreationForm.getDelivery_location() == null) {
            binding.addItemPages.setCurrentItem(3);
            Toast.makeText(AddItemDonor.this, "Add items delivery address", Toast.LENGTH_SHORT).show();
            return false;
        } else if (donationCreationForm.getDonor() == null) {
            finish();
            Toast.makeText(getApplicationContext(), "Failed to get your information", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void createDonation() {
        inProgress();
        startService(new Intent(AddItemDonor.this, UploadPictureService.class).putExtra(DONATION, donationCreationForm).putExtra(MEDIA_TYPE, DONATION));
        finish();
    }

    private void inProgress() {
        pb.setVisibility(View.VISIBLE);
        binding.addDonation.setEnabled(false);
    }

    private void outProgress() {
        pb.setVisibility(View.GONE);
        binding.addDonation.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        donationCreationForm = new Models.DonationCreationForm();
    }
}