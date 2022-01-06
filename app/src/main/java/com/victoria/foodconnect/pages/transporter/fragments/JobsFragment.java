package com.victoria.foodconnect.pages.transporter.fragments;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.adapter.JobsRvAdapter;
import com.victoria.foodconnect.databinding.FragmentJobsBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.purchaseDb.PurchaseViewModel;
import com.victoria.foodconnect.models.Models;

import java.util.LinkedList;

public class JobsFragment extends Fragment {

    private FragmentJobsBinding binding;
    private JobsRvAdapter jobsRvAdapter;
    private final LinkedList<Models.Purchase> purchaseList = new LinkedList<>();
    public static final String[] jobCatArray = new String[]{"Requests", "In progress", "Completed"};
    private Domain.AppUser user;

    public JobsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentJobsBinding.inflate(inflater);

        userRepository.getUserLive().observe(requireActivity(), appUser -> appUser.ifPresent(u -> {
            user = u;
            RecyclerView jobs = binding.jobs;
            jobs.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
            jobsRvAdapter = new JobsRvAdapter(requireActivity(), purchaseList,user);
            jobs.setAdapter(jobsRvAdapter);
            jobsRvAdapter.setClickListener((view, position) -> {

            });

            AppCompatSpinner spinner = binding.jobCategory;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, jobCatArray);
            spinner.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    getPurchases(user.getUsername(), position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        }));



        return binding.getRoot();
    }

    private void getPurchases(String username, int position) {

        binding.pb.setVisibility(View.VISIBLE);
        purchaseList.clear();
        jobsRvAdapter.notifyDataSetChanged();

        new ViewModelProvider(this).get(PurchaseViewModel.class).getTransporterJobsList(username, position).observe(getViewLifecycleOwner(), purchases -> {
            binding.pb.setVisibility(View.GONE);
            if (purchases.isEmpty()) {
                Toast.makeText(requireContext(), "No jobs", Toast.LENGTH_SHORT).show();
                return;
            }

            purchaseList.addAll(purchases);
            jobsRvAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}

