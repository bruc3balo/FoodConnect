package com.victoria.foodconnect.pages.seller.fragments;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class MyOrdersSeller extends Fragment {

    private FragmentMyOrdersBinding binding;
    private PurchaseRvAdapter adapter;
    private final LinkedList<Models.Purchase> purchaseList = new LinkedList<>();


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
        rv.setLayoutManager(new LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false));
        adapter = new PurchaseRvAdapter(this,purchaseList);
        rv.setAdapter(adapter);

        userRepository.getUserLive().observe(getViewLifecycleOwner(), appUser -> appUser.ifPresent(u-> getPurchases(u.getUsername())));

        return binding.getRoot();
    }

    private void getPurchases(String username) {
        new ViewModelProvider(this).get(PurchaseViewModel.class).getSellerPurchaseList(username).observe(getViewLifecycleOwner(), purchases -> {
            if (purchases.isEmpty()) {
                Toast.makeText(requireContext(), "No purchases", Toast.LENGTH_SHORT).show();
                return;
            }

            purchaseList.clear();
            purchaseList.addAll(purchases);
            adapter.notifyDataSetChanged();
        });
    }
}