package com.victoria.foodconnect.pages.transporter.jobProgress;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.FragmentReviewBinding;
import com.victoria.foodconnect.models.Models;

import java.util.Optional;

public class ReviewFragment extends Fragment {

    private FragmentReviewBinding binding;
    private final Models.DistributionModel distribution;
    private final Models.Remarks optionalRemarks;


    public ReviewFragment(Models.DistributionModel distribution) {
        // Required empty public constructor
        this.distribution = distribution;
        this.optionalRemarks = distribution.getRemarks();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentReviewBinding.inflate(inflater);



        return binding.getRoot();
    }
}