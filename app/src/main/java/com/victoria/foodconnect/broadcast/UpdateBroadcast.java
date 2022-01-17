package com.victoria.foodconnect.broadcast;


import static com.victoria.foodconnect.globals.GlobalRepository.application;
import static com.victoria.foodconnect.globals.GlobalRepository.userApi;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.APPLICATION_JSON;
import static com.victoria.foodconnect.globals.GlobalVariables.DISTRIBUTION_COLLECTION;
import static com.victoria.foodconnect.globals.GlobalVariables.DONATION;
import static com.victoria.foodconnect.globals.GlobalVariables.DONATION_DISTRIBUTION_COLLECTION;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_COLLECTION;
import static com.victoria.foodconnect.globals.GlobalVariables.ROLE;
import static com.victoria.foodconnect.globals.GlobalVariables.UPDATE;
import static com.victoria.foodconnect.globals.GlobalVariables.USERNAME;
import static com.victoria.foodconnect.globals.GlobalVariables.USER_COLLECTION;
import static com.victoria.foodconnect.pages.ProgressActivity.refreshDonationProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.refreshPurchaseProgress;
import static com.victoria.foodconnect.pages.beneficiary.fragment.OrdersFragment.refreshPurchaseBene;
import static com.victoria.foodconnect.pages.beneficiary.fragment.ReceiveDonationsFragment.refreshDonationBene;
import static com.victoria.foodconnect.pages.donor.fragments.DonationsFragment.refreshDonationDonor;
import static com.victoria.foodconnect.pages.seller.fragments.MyOrdersSeller.refreshPurchaseSeller;
import static com.victoria.foodconnect.pages.seller.fragments.MyProductsSeller.refreshProductsSeller;
import static com.victoria.foodconnect.pages.transporter.MoreActivity.PURCHASE;
import static com.victoria.foodconnect.pages.transporter.donationProgress.DonationProgressActivity.refreshJobDonationProgress;
import static com.victoria.foodconnect.pages.transporter.fragments.DonationJobsFragment.refreshDonationTrans;
import static com.victoria.foodconnect.pages.transporter.fragments.JobsFragment.refreshJobTrans;
import static com.victoria.foodconnect.pages.transporter.jobProgress.JobActivityProgress.refreshJobPurchaseProgress;
import static com.victoria.foodconnect.utils.DataOpts.getAccessToken;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.victoria.foodconnect.models.AppRolesEnum;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.DataOpts;
import com.victoria.foodconnect.utils.JsonResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UpdateBroadcast extends BroadcastReceiver {

    public static final String UPDATE_INTENT = "com.victoria.foodconnect.intent.action.UPDATE";

    //broadcast to update data

    //todo update locally

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("update message received");

        if (UPDATE_INTENT.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();

            String update = extras.getString(UPDATE);
            String role = extras.getString(ROLE);
            String username = extras.getString(USERNAME);

            System.out.println("update message received is "+update);

            switch (update) {
                default:
                    break;

                case PURCHASE:
                case DISTRIBUTION_COLLECTION:
                    refreshPurchaseProgress.postValue(Optional.of(true));
                    refreshJobPurchaseProgress.postValue(Optional.of(true));

                    if (role.equals(AppRolesEnum.ROLE_BUYER.name())) {
                        refreshPurchaseBene.postValue(Optional.of(true));
                    } else if (role.equals(AppRolesEnum.ROLE_TRANSPORTER.name())) {
                        refreshJobTrans.postValue(Optional.of(true));
                    } else if (role.equals(AppRolesEnum.ROLE_SELLER.name())) {
                        refreshPurchaseSeller.postValue(Optional.of(true));
                    } else {
                        System.out.println("role not found");
                    }
                    break;

                case USER_COLLECTION:
                    HashMap<String, String> params = new HashMap<>();
                    params.put(USERNAME, username);
                    userApi.getUser(params, getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {

                            System.out.println("=============== SUCCESS GETTING USER " + username + "===================");
                            try {
                                //extract user data
                                JsonResponse jsonResponse = response.body();


                                if (jsonResponse == null) {
                                    System.out.println("NO USER DATA " + response.code());
                                    return;
                                } else {
                                    System.out.println("USER  DAA");
                                }

                                System.out.println("=============== SUCCESS GETTING USER " + getObjectMapper().writeValueAsString(jsonResponse.getData()) + "===================");


                                if (jsonResponse.getData() == null) {
                                    return;
                                }

                                JsonObject userJson = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData())).getJsonObject(0);

                                //save user to offline db
                                Models.AppUser user = getObjectMapper().readValue(userJson.toString(), Models.AppUser.class);

                                userRepository.insert(DataOpts.getDomainUserFromModelUser(user));

                                Thread.sleep(2000);
                                System.out.println("======== ROLE INSERTED " + user.getRole().getName() + "===============");

                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                            t.printStackTrace();
                        }
                    });
                    break;

                case PRODUCT_COLLECTION:
                    if (role.equals(AppRolesEnum.ROLE_SELLER.name())) {
                        refreshProductsSeller.postValue(Optional.of(true));
                    }

                    break;

                case DONATION:
                case DONATION_DISTRIBUTION_COLLECTION:
                    refreshDonationProgress.postValue(Optional.of(true));
                    refreshJobDonationProgress.postValue(Optional.of(true));

                    if (role.equals(AppRolesEnum.ROLE_BUYER.name())) {
                        refreshDonationBene.postValue(Optional.of(true));
                    } else if (role.equals(AppRolesEnum.ROLE_TRANSPORTER.name())) {
                        refreshDonationTrans.postValue(Optional.of(true));
                    } else if (role.equals(AppRolesEnum.ROLE_DONOR.name())) {
                        refreshDonationDonor.postValue(Optional.of(true));
                    } else {
                        System.out.println("role not found");
                    }
                    break;
            }

            //System.out.println(update + " update message " + role);
        }
    }
}