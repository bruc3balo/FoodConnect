package com.victoria.foodconnect.pages.beneficiary.fragment;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.pages.transporter.fragments.JobsFragment.readJobCatArray;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.victoria.foodconnect.adapter.JobsRvAdapter;
import com.victoria.foodconnect.databinding.FragmentOrdersBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.purchaseDb.PurchaseViewModel;
import com.victoria.foodconnect.models.Models;

import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrdersFragment extends AppCompatActivity {

    private FragmentOrdersBinding binding;
    private int pos;
    private Domain.AppUser user;
    private final LinkedList<Models.Purchase> purchaseList = new LinkedList<>();
    private final LinkedList<Models.Purchase> allPurchaseList = new LinkedList<>();
    private JobsRvAdapter purchaseRvAdapter;
    public static MutableLiveData<Optional<Boolean>> refreshPurchaseBene = new MutableLiveData<>();


    public OrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        userRepository.getUserLive().observe(this, appUser -> appUser.ifPresent(u -> {
            user = u;
            RecyclerView jobs = binding.donationsRv;
            jobs.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            purchaseRvAdapter = new JobsRvAdapter(this, purchaseList, user, true);

            jobs.setAdapter(purchaseRvAdapter);

            binding.toolbar.setTitle(u.getUsername() + "'s Orders");

            AppCompatSpinner spinner = binding.donationCategory;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, readJobCatArray);
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

            getOrders();

        }));

        setWindowColors(this);

    }


    private void getOrders() {
        inSpinnerProgress(binding.pb, null);
        purchaseList.clear();
        purchaseRvAdapter.notifyDataSetChanged();
        binding.donationCategory.setEnabled(false);

        new ViewModelProvider(this).get(PurchaseViewModel.class).getBuyerPurchaseList(user.getUsername()).observe(this, purchases -> {
            outSpinnerProgress(binding.pb, null);
            binding.donationCategory.setEnabled(true);

            if (purchases.isEmpty()) {

                Toast.makeText(this, "No purchases", Toast.LENGTH_SHORT).show();
                return;
            }

            allPurchaseList.clear();
            allPurchaseList.addAll(purchases);
            filterList();
        });

    }

    private void filterList () {
        switch (pos) {

            //request
            default:
            case 0:
                purchaseList.clear();
                purchaseRvAdapter.notifyDataSetChanged();
                purchaseList.addAll(allPurchaseList.stream().filter(purchase -> !purchase.isComplete() && !purchase.getDeleted() && purchase.getAssigned() == null).collect(Collectors.toList()));
                purchaseRvAdapter.notifyDataSetChanged();

                break;

            //in progress
            case 1:
                purchaseList.clear();
                purchaseRvAdapter.notifyDataSetChanged();
                purchaseList.addAll(allPurchaseList.stream().filter(purchase -> !purchase.isComplete() && !purchase.getDeleted() && purchase.getAssigned() != null).collect(Collectors.toList()));
                purchaseRvAdapter.notifyDataSetChanged();
                break;

            //completed
            case 2:
                purchaseList.clear();
                purchaseRvAdapter.notifyDataSetChanged();
                purchaseList.addAll(allPurchaseList.stream().filter(purchase -> purchase.getDeleted() || purchase.isComplete() && purchase.getAssigned() != null).collect(Collectors.toList()));
                purchaseRvAdapter.notifyDataSetChanged();
                break;
        }

    }

    private void addRefreshListener() {
        refreshData().observe(this, refresh -> {
            System.out.println("REFRESH RECEIVED");
            if (refresh.isPresent()) {
                if (user != null) {
                    getOrders();
                }
            }
        });
    }

    private void removeListeners() {
        refreshData().removeObservers(this);
    }

    //listener for updates
    private LiveData<Optional<Boolean>> refreshData() {
        return refreshPurchaseBene;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (user != null) {
            getOrders();
        }

        addRefreshListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeListeners();
    }
}