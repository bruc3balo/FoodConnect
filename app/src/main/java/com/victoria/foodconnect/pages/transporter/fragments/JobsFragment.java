package com.victoria.foodconnect.pages.transporter.fragments;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.annotation.SuppressLint;
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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.victoria.foodconnect.adapter.JobProgressPagerAdapter;
import com.victoria.foodconnect.adapter.JobsRvAdapter;
import com.victoria.foodconnect.adapter.LoginPagerAdapter;
import com.victoria.foodconnect.databinding.FragmentJobsBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.purchaseDb.PurchaseViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pagerTransformers.DepthPageTransformer;

import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

public class JobsFragment extends Fragment {

    private FragmentJobsBinding binding;
    private JobsRvAdapter jobsRvAdapter;
    private final LinkedList<Models.Purchase> purchaseList = new LinkedList<>();
    private final LinkedList<Models.Purchase> allPurchaseList = new LinkedList<>();
    public static final String[] jobCatArray = new String[]{"Requests", "In progress", "Completed"};
    public static final String[] readJobCatArray = new String[]{"Pending", "In progress", "Completed"};
    public static final String[] ordersArray = new String[]{"Progress", "Successful", "Failed"};
    private Domain.AppUser user;
    private int pos;
    public static MutableLiveData<Optional<Boolean>> refreshJobTrans = new MutableLiveData<>();


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
            jobsRvAdapter = new JobsRvAdapter(requireActivity(), purchaseList,user,false);
            jobs.setAdapter(jobsRvAdapter);

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

            getPurchases();
        }));

        return binding.getRoot();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getPurchases() {
        inSpinnerProgress(binding.pb,null);
        purchaseList.clear();
        jobsRvAdapter.notifyDataSetChanged();
        binding.jobCategory.setEnabled(false);

        new ViewModelProvider(this).get(PurchaseViewModel.class).getTransporterJobsList().observe(getViewLifecycleOwner(), purchases -> {
            outSpinnerProgress(binding.pb,null);
            binding.jobCategory.setEnabled(true);

            if (purchases.isEmpty()) {

                Toast.makeText(requireContext(), "No jobs", Toast.LENGTH_SHORT).show();
                return;
            }

            allPurchaseList.clear();
            allPurchaseList.addAll(purchases);
            filterList();
        });

    }

    private void filterList() {
        switch (pos) {

            //request
            default:
            case 0:
                purchaseList.clear();
                purchaseList.addAll(allPurchaseList.stream().filter(purchase -> !purchase.isComplete() && !purchase.getDeleted() && purchase.getAssigned() == null).collect(Collectors.toList()));
                jobsRvAdapter.notifyDataSetChanged();

                break;

            //in progress
            case 1:
                purchaseList.clear();

                purchaseList.addAll(allPurchaseList.stream().filter(purchase -> !purchase.isComplete() && !purchase.getDeleted() && purchase.getAssigned() != null && purchase.getAssigned().equals(user.getUsername())).collect(Collectors.toList()));
                jobsRvAdapter.notifyDataSetChanged();

                break;

            //completed
            case 2:
                purchaseList.clear();

                purchaseList.addAll(allPurchaseList.stream().filter(purchase -> purchase.getDeleted() || purchase.isComplete() && purchase.getAssigned() != null).collect(Collectors.toList()));
                jobsRvAdapter.notifyDataSetChanged();

                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (user != null) {
            getPurchases();
        }
    }
}

