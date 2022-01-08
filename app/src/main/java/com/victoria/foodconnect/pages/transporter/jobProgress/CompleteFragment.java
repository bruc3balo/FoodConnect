package com.victoria.foodconnect.pages.transporter.jobProgress;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.FragmentCompleteBinding;
import com.victoria.foodconnect.databinding.FragmentReviewBinding;
import com.victoria.foodconnect.models.Models;

public class CompleteFragment extends Fragment {

    private FragmentReviewBinding binding;
    private final Models.Purchase purchase;
    private final Models.DistributionModel distribution;
    private final JobActivityProgress activity;


    public CompleteFragment(JobActivityProgress activity,Models.Purchase purchase,Models.DistributionModel distribution) {
        // Required empty public constructor
        this.purchase = purchase;
        this.distribution = distribution;
        this.activity = activity;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentReviewBinding.inflate(inflater);

        RatingBar donorRating = binding.donorRating;
        donorRating.setIsIndicator(false);
        donorRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                donorRating.setIsIndicator(true);
                donorRating.setRating(rating);
            }
        });


        //getChildFragmentManager().beginTransaction().add(R.id.review, new ReviewFragment(distribution));

        return binding.getRoot();
    }



}