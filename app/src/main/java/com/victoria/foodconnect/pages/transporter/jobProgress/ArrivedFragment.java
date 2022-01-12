package com.victoria.foodconnect.pages.transporter.jobProgress;

import static com.victoria.foodconnect.pages.transporter.jobProgress.JobActivityProgress.update;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.FragmentArrivedBinding;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.DistributionStatus;

import java.util.function.Function;


public class ArrivedFragment extends Fragment {

    private FragmentArrivedBinding binding;
    private final Models.Purchase purchase;
    private final Models.DistributionModel distribution;
    private final JobActivityProgress activity;
    private final boolean readOnly;


    public ArrivedFragment(JobActivityProgress activity, Models.Purchase purchase, Models.DistributionModel distribution,boolean readOnly) {
        // Required empty public constructor
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
        binding = FragmentArrivedBinding.inflate(inflater);


        binding.complete.setOnClickListener(v -> paidDialog(activity, "Has the job been paid?"));
        binding.dnf.setOnClickListener(v -> confirmDialog(activity, "Do you want to mark the job as did not finish?", purchase -> {
            dnf();
            return null;
        }));


        return binding.getRoot();
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

    private void complete(boolean paid) {
        update(activity, new Models.DistributionUpdateForm(distribution.getId(), DistributionStatus.COMPLETE.getCode(), paid));
    }

    private void dnf() {
        update(activity, new Models.DistributionUpdateForm(distribution.getId(), DistributionStatus.DNF.getCode()));
    }

    private void paidDialog(Context context, String info) {
        Dialog d = new Dialog(context);
        d.setContentView(R.layout.check_dialog);
        TextView infov = d.findViewById(R.id.infoTv);
        infov.setText(info);

        ImageButton no = d.findViewById(R.id.noButton);
        no.setOnClickListener(v -> d.dismiss());

        final boolean[] paid = new boolean[1];

        CheckBox paidCheck = d.findViewById(R.id.paidCheck);
        paidCheck.setOnCheckedChangeListener((buttonView, isChecked) -> paid[0] = isChecked);

        Button yes = d.findViewById(R.id.yesButton);
        yes.setOnClickListener(v -> {
            confirmDialog(context, "You want to complete the job?", purchase -> {
                complete(paid[0]);
                return null;
            });
            d.dismiss();
        });

        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.show();
    }
}