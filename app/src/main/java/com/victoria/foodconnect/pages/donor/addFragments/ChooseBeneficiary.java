package com.victoria.foodconnect.pages.donor.addFragments;

import static android.graphics.Color.RED;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;
import static com.victoria.foodconnect.utils.SimilarityClass.alike;

import android.app.SearchManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.adapter.UserListRvAdapter;
import com.victoria.foodconnect.databinding.FragmentChooseBeneficiaryBinding;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.models.AppRolesEnum;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.donor.AddItemDonor;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ChooseBeneficiary extends Fragment {

    private FragmentChooseBeneficiaryBinding binding;
    private AddItemDonor activity;
    private final LinkedList<Models.AppUser> userList = new LinkedList<>();
    private final LinkedList<Models.AppUser> allUserList = new LinkedList<>();
    private UserListRvAdapter userListRvAdapter;

    public ChooseBeneficiary() {
        // Required empty public constructor
    }


    public ChooseBeneficiary(AddItemDonor activity) {
        this.activity = activity;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChooseBeneficiaryBinding.inflate(inflater);

        setHasOptionsMenu(true);

        RecyclerView rv = binding.beneRv;
        rv.setLayoutManager(new LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false));
        userListRvAdapter = new UserListRvAdapter(requireContext(),userList);
        rv.setAdapter(userListRvAdapter);

        getBeneficiaries();
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        SearchView searchView = new SearchView(Objects.requireNonNull(activity.getSupportActionBar()).getThemedContext());
        menu.add("Search menu").setIcon(R.drawable.ic_search_white).setIconTintList(ColorStateList.valueOf(RED)).setVisible(true).setActionView(searchView).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        searchView.setIconifiedByDefault(true);

        SearchManager searchManager = activity.getSystemService(SearchManager.class);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                new Handler().post(() -> {
                    try {
                        searchUsers(query);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                return false;
            }
        });


        super.onCreateOptionsMenu(menu, inflater);
    }

    public void getBeneficiaries() {
        inSpinnerProgress(binding.pb,null);
        new ViewModelProvider(this).get(UserViewModel.class).getLiveAllUsers().observe(getViewLifecycleOwner(), new Observer<Optional<JsonResponse>>() {
            @Override
            public void onChanged(Optional<JsonResponse> jsonResponse) {
                outSpinnerProgress(binding.pb,null);
                if (!jsonResponse.isPresent()) {
                    Toast.makeText(activity.getApplicationContext(), "Failed to get beneficiaries", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    JsonArray serviceArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.get().getData()));

                    for (int i = 0; i < serviceArray.size(); i++) {

                        try {
                            System.out.println("count " + i);
                            Models.AppUser appUser = getObjectMapper().readValue(new JsonObject(serviceArray.getJsonObject(i).getMap()).toString(), Models.AppUser.class);
                            if (appUser.getRole().getName().equals(AppRolesEnum.ROLE_BUYER.name())) {
                                allUserList.add(appUser);
                            }

                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }


                    userList.addAll(allUserList);
                    userListRvAdapter.notifyDataSetChanged();

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void searchUsers(String query) {
        userList.clear();
        userListRvAdapter.notifyDataSetChanged();

        if (query.isEmpty()) {
            userList.addAll(allUserList);
            userListRvAdapter.notifyDataSetChanged();
        } else {
            userListRvAdapter.notifyDataSetChanged();
            allUserList.forEach(user -> {
                if (alike(query,user.getUsername()) || alike(query,user.getNames())) {
                    userList.add(user);
                    userListRvAdapter.notifyDataSetChanged();
                }
            });
        }
    }

}