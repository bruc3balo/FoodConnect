package com.victoria.foodconnect.pages.admin.fragments;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.pages.admin.AdminActivity.lastFragment;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.adapter.UserRvAdapter;
import com.victoria.foodconnect.databinding.FragmentUsersBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.LinkedList;
import java.util.Optional;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class UsersFragment extends Fragment {

    private FragmentUsersBinding binding;
    private UserRvAdapter userRvAdapter;
    private ArrayAdapter<String> adapter;
    private final LinkedList<String> roleList = new LinkedList<>();
    private UserViewModel userViewModel;

    private final LinkedList<Models.AppUser> allUserList = new LinkedList<>();
    private final LinkedList<Models.AppUser> userList = new LinkedList<>();

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentUsersBinding.inflate(inflater);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        AppCompatSpinner roleSpinner = binding.roleSpinner;
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, roleList);
        roleSpinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(requireActivity(), roleList.get(position), Toast.LENGTH_SHORT).show();
                filterProducts(binding.roleSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        userRvAdapter = new UserRvAdapter(this, userList);

        RecyclerView usersRv = binding.usersRv;
        usersRv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        usersRv.setAdapter(userRvAdapter);


        userRepository.getUserLive().observe(requireActivity(), appUser -> {
            if (!appUser.isPresent()) {
                Toast.makeText(requireContext(), "Failed to get user info", Toast.LENGTH_SHORT).show();
                return;
            }

            getRoles();
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        lastFragment = 1;
    }

    private void getRoles() {

        System.out.println("GET PRODUCT CATEGORY DATA");


        userViewModel.getRoles().observe(requireActivity(), jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                Toast.makeText(requireContext(), "No product categories", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonResponse response = jsonResponse.get();

            roleList.clear();
            try {
                JsonArray serviceArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.get().getData()));

                for (int i = 0; i < serviceArray.size(); i++) {

                    try {
                        System.out.println("count " + i);
                        Models.AppRole role = getObjectMapper().readValue(new JsonObject(serviceArray.getJsonObject(i).getMap()).toString(), Models.AppRole.class);
                        roleList.add(role.getName());
                        adapter.notifyDataSetChanged();
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                }

                getUsers();

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    public void getUsers() {

        System.out.println("GET MY PRODUCT DATA");

        userViewModel.getLiveAllUsers().observe(requireActivity(), jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                Toast.makeText(requireContext(), "No users", Toast.LENGTH_SHORT).show();
                return;
            }

            allUserList.clear();
            try {
                JsonArray serviceArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.get().getData()));

                for (int i = 0; i < serviceArray.size(); i++) {

                    try {
                        System.out.println("count " + i);
                        Models.AppUser appUser = getObjectMapper().readValue(new JsonObject(serviceArray.getJsonObject(i).getMap()).toString(), Models.AppUser.class);
                        allUserList.add(appUser);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                }

                filterProducts(binding.roleSpinner.getSelectedItem().toString());

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterProducts(String role) {
        userList.clear();
        adapter.notifyDataSetChanged();
        userRvAdapter.notifyDataSetChanged();
        allUserList.forEach(p -> {
            if (p.getRole().getName().equals(role)) {
                userList.add(p);
                if (!userList.isEmpty()) {
                    userRvAdapter.notifyItemInserted(userList.size() - 1);
                }
            }
        });
    }

    private void refreshWithDonors () {

    }

}