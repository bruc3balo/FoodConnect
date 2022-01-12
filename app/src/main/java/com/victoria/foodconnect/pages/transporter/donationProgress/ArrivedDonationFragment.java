package com.victoria.foodconnect.pages.transporter.donationProgress;

import static com.victoria.foodconnect.pages.transporter.donationProgress.DonationProgressActivity.updateDonationDistribution;
import static com.victoria.foodconnect.pages.transporter.jobProgress.JobActivityProgress.update;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.FragmentArrivedBinding;
import com.victoria.foodconnect.databinding.FragmentArrivedDonationBinding;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.DistributionStatus;

import java.util.function.Function;

public class ArrivedDonationFragment extends Fragment {

    private FragmentArrivedDonationBinding binding;
    private Models.DonationDistribution distribution;
    private Models.Donation donation;
    private DonationProgressActivity activity;
    private boolean readOnly;

    public ArrivedDonationFragment() {
        // Required empty public constructor
    }

    public ArrivedDonationFragment(DonationProgressActivity activity,Models.DonationDistribution distribution,Models.Donation donation, boolean readOnly) {
        this.distribution = distribution;
        this.donation = donation;
        this.activity = activity;
        this.readOnly = readOnly;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentArrivedDonationBinding.inflate(inflater);

        binding.complete.setOnClickListener(v -> confirmDialog(activity, "Have you delivered the items ?", donation -> {
            complete();
            return null;
        }));

        binding.dnf.setOnClickListener(v -> confirmDialog(activity, "Do you want to mark the job as did not finish?", purchase -> {
            dnf();
            return null;
        }));

        return binding.getRoot();
    }

    private void confirmDialog(Context context, String info, Function<Models.Donation, Void> function) {
        Dialog d = new Dialog(context);
        d.setContentView(R.layout.yes_no_layout);
        TextView infov = d.findViewById(R.id.infoTv);
        infov.setText(info);
        Button no = d.findViewById(R.id.noButton);
        no.setOnClickListener(v -> d.dismiss());

        Button yes = d.findViewById(R.id.yesButton);
        yes.setOnClickListener(v -> {
            function.apply(donation);
            d.dismiss();
        });


        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.show();
    }

    private void complete() {
        updateDonationDistribution(activity, new Models.DonorDistributionUpdateForm(distribution.getId(), DistributionStatus.COMPLETE.getCode()));
    }

    private void dnf() {
        updateDonationDistribution(activity, new Models.DonorDistributionUpdateForm(distribution.getId(), DistributionStatus.DNF.getCode()));
    }
}