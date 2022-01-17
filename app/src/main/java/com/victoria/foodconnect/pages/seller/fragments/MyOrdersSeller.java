package com.victoria.foodconnect.pages.seller.fragments;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.pages.transporter.fragments.JobsFragment.ordersArray;
import static com.victoria.foodconnect.pages.transporter.fragments.JobsFragment.readJobCatArray;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
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
import com.victoria.foodconnect.adapter.PurchaseRvAdapter;
import com.victoria.foodconnect.databinding.FragmentMyOrdersBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.purchaseDb.PurchaseViewModel;
import com.victoria.foodconnect.models.Models;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class MyOrdersSeller extends Fragment {

    private FragmentMyOrdersBinding binding;
    private PurchaseRvAdapter adapter;
    private final LinkedList<Models.Purchase> purchaseList = new LinkedList<>();
    private final LinkedList<Models.Purchase> allPurchaseList = new LinkedList<>();
    private int pos;
    private Domain.AppUser appUser;
    public static MutableLiveData<Optional<Boolean>> refreshPurchaseSeller = new MutableLiveData<>();


    public MyOrdersSeller() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentMyOrdersBinding.inflate(inflater);

        RecyclerView rv = binding.purchaseRv;
        rv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));

        userRepository.getUserLive().observe(getViewLifecycleOwner(), appUser -> appUser.ifPresent(u -> {

            adapter = new PurchaseRvAdapter(requireContext(), u, purchaseList);
            rv.setAdapter(adapter);

            this.appUser = u;
            getPurchases(u.getUsername());

            AppCompatSpinner spinner = binding.ordersCategory;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, ordersArray);
            spinner.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    pos = position;
                    filterOrders(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }

            });
        }));

        return binding.getRoot();
    }

    private void getPurchases(String username) {
        inSpinnerProgress(binding.pb, null);
        new ViewModelProvider(this).get(PurchaseViewModel.class).getSellerPurchaseList(username).observe(getViewLifecycleOwner(), purchases -> {
            outSpinnerProgress(binding.pb, null);
            if (purchases.isEmpty()) {
                Toast.makeText(requireContext(), "No purchases", Toast.LENGTH_SHORT).show();
                return;
            }

            allPurchaseList.clear();
            allPurchaseList.addAll(purchases);
            filterOrders(pos);
        });
    }

    private void filterOrders(int position) {
        switch (position) {
            case 0:
                purchaseList.clear();
                adapter.notifyDataSetChanged();

                purchaseList.addAll(allPurchaseList.stream().filter(i -> i.getSuccess() == null).collect(Collectors.toList()));
                adapter.notifyDataSetChanged();
                break;

            case 1:
                purchaseList.clear();
                adapter.notifyDataSetChanged();

                purchaseList.addAll(allPurchaseList.stream().filter(i -> i.getSuccess() != null && i.getSuccess()).collect(Collectors.toList()));
                adapter.notifyDataSetChanged();
                break;

            case 2:
                purchaseList.clear();
                adapter.notifyDataSetChanged();

                purchaseList.addAll(allPurchaseList.stream().filter(i -> i.getSuccess() != null && !i.getSuccess()).collect(Collectors.toList()));
                adapter.notifyDataSetChanged();
                break;
        }
    }

    private void addRefreshListener() {
        refreshData().observe(this, refresh -> {
            if (refresh.isPresent()) {
                if (appUser != null) {
                    getPurchases(appUser.getUsername());
                }
            }
        });
    }

    private void removeListeners() {
        refreshData().removeObservers(this);
    }

    //listener for updates
    private LiveData<Optional<Boolean>> refreshData() {
        return refreshPurchaseSeller;
    }

    @Override
    public void onResume() {
        super.onResume();
        addRefreshListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeListeners();
    }
}