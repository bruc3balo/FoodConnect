package com.victoria.foodconnect.pages.transporter.fragments;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.pages.transporter.fragments.JobsFragment.jobCatArray;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.adapter.DonationRvAdapter;
import com.victoria.foodconnect.adapter.JobsRvAdapter;
import com.victoria.foodconnect.databinding.FragmentDonationJobsBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.purchaseDb.PurchaseViewModel;
import com.victoria.foodconnect.models.Models;

import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;


public class DonationJobsFragment extends Fragment {

    private FragmentDonationJobsBinding binding;
    private DonationRvAdapter donationRvAdapter;
    private final LinkedList<Models.Donation> donationList = new LinkedList<>();
    private final LinkedList<Models.Donation> allDonationList = new LinkedList<>();
    private Domain.AppUser user;
    private int pos;
    public static MutableLiveData<Optional<Boolean>> refreshDonationTrans = new MutableLiveData<>();



    public DonationJobsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDonationJobsBinding.inflate(inflater);

        userRepository.getUserLive().observe(requireActivity(), appUser -> appUser.ifPresent(u -> {
            user = u;
            RecyclerView jobs = binding.jobs;
            jobs.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
            donationRvAdapter = new DonationRvAdapter(requireActivity(), donationList, user, false);
            jobs.setAdapter(donationRvAdapter);

            AppCompatSpinner spinner = binding.jobCategory;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, jobCatArray);
            spinner.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    pos = position;
                    filterList();

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            getDonations();

        }));

        return binding.getRoot();
    }

    private void getDonations() {
        inSpinnerProgress(binding.pb, null);
        donationList.clear();
        donationRvAdapter.notifyDataSetChanged();
        binding.jobCategory.setEnabled(false);

        new ViewModelProvider(this).get(PurchaseViewModel.class).getATransporterDonations().observe(getViewLifecycleOwner(), donations -> {
            outSpinnerProgress(binding.pb, null);
            binding.jobCategory.setEnabled(true);

            if (donations.isEmpty()) {

                Toast.makeText(requireContext(), "No donations", Toast.LENGTH_SHORT).show();
                return;
            }

            allDonationList.clear();
            allDonationList.addAll(donations);
            filterList();
        });

    }

    private void filterList() {
        switch (pos) {

            //request
            default:
            case 0:

                donationList.clear();
                donationRvAdapter.notifyDataSetChanged();

                donationList.addAll(allDonationList.stream().filter(donation -> !donation.isComplete() && !donation.isDeleted() && donation.getAssigned() == null).collect(Collectors.toList()));
                donationRvAdapter.notifyDataSetChanged();

                break;

            //in progress
            case 1:
                donationList.clear();
                donationRvAdapter.notifyDataSetChanged();

                donationList.addAll(allDonationList.stream().filter(donation -> !donation.isComplete() && !donation.isDeleted() && donation.getAssigned() != null && donation.getAssigned().equals(user.getUsername())).collect(Collectors.toList()));
                donationRvAdapter.notifyDataSetChanged();

                break;

            //completed
            case 2:
                donationList.clear();
                donationRvAdapter.notifyDataSetChanged();

                donationList.addAll(allDonationList.stream().filter(donation -> donation.isDeleted() || donation.isComplete() && donation.getAssigned() != null).collect(Collectors.toList()));
                donationRvAdapter.notifyDataSetChanged();
                break;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (user != null) {
            getDonations();
        }
    }
}