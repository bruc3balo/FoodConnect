package com.victoria.foodconnect.pages.transporter.jobProgress;

import static com.victoria.foodconnect.pages.transporter.jobProgress.JobActivityProgress.update;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.FragmentAcceptedBinding;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.DistributionStatus;

import java.util.function.Function;

public class AcceptedFragment extends Fragment {

    private FragmentAcceptedBinding binding;
    private final Models.Purchase purchase;
    private final Models.DistributionModel distribution;
    private final JobActivityProgress activity;
    private final boolean readOnly;


    public AcceptedFragment(JobActivityProgress activity, Models.Purchase purchase, Models.DistributionModel distribution, boolean readOnly) {
        this.purchase = purchase;
        this.distribution = distribution;
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
        binding = FragmentAcceptedBinding.inflate(inflater);

        binding.start.setOnClickListener(v -> confirmDialog(requireContext(), "Do you start collecting the items?", purchase -> {
            startJob();
            return null;
        }));

        return binding.getRoot();
    }

    private void startJob() {
        binding.start.setEnabled(false);
        update((JobActivityProgress) requireActivity(), new Models.DistributionUpdateForm(distribution.getId(), DistributionStatus.COLLECTING_ITEMS.getCode()));
    }

    private void confirmDialog(Context context, String info, Function<Models.Purchase, Void> function) {
        Dialog d = new Dialog(context);
        d.setContentView(R.layout.yes_no_layout);
        TextView infov = d.findViewById(R.id.infoTv);
        infov.setText(info);
        Button no = d.findViewById(R.id.noButton);
        no.setOnClickListener(v -> d.dismiss());

        Button yes = d.findViewById(R.id.yesButton);
        yes.setOnClickListener(v -> {
            function.apply(purchase);
            d.dismiss();
        });

        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.show();
    }


}