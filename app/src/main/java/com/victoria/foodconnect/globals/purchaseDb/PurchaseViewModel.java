package com.victoria.foodconnect.globals.purchaseDb;

import static com.google.common.net.HttpHeaders.CONNECTION;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.victoria.foodconnect.globals.GlobalRepository.application;
import static com.victoria.foodconnect.globals.GlobalRepository.purchaseApi;
import static com.victoria.foodconnect.globals.GlobalRepository.reviewApi;
import static com.victoria.foodconnect.globals.GlobalVariables.APPLICATION_JSON;
import static com.victoria.foodconnect.globals.GlobalVariables.AUTHORIZATION;
import static com.victoria.foodconnect.globals.GlobalVariables.BUYERS_ID;
import static com.victoria.foodconnect.globals.GlobalVariables.CONNECTION_VAL;
import static com.victoria.foodconnect.globals.GlobalVariables.KEEP_ALIVE;
import static com.victoria.foodconnect.globals.GlobalVariables.KEEP_ALIVE_VAL;
import static com.victoria.foodconnect.globals.GlobalVariables.SELLERS_ID;
import static com.victoria.foodconnect.utils.DataOpts.getAccessToken;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PurchaseViewModel extends AndroidViewModel {


    public PurchaseViewModel(@NonNull Application application) {
        super(application);
    }

    private MutableLiveData<Optional<Models.Remarks>> postRemark(Models.Remarks remarksForm) {
        MutableLiveData<Optional<Models.Remarks>> mutableLiveData = new MutableLiveData<>();

        reviewApi.newReview(remarksForm, getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();


                if (response.code() != 200 || jsonResponse == null || !jsonResponse.isSuccess() || jsonResponse.isHas_error() || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }


                try {
                    JsonObject reviewJson = new JsonObject(getObjectMapper().writeValueAsString(jsonResponse.getData()));
                    Models.Remarks remarks = getObjectMapper().readValue(reviewJson.toString(), Models.Remarks.class);
                    mutableLiveData.setValue(Optional.of(remarks))
                    ;
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                //save user to offline db


            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<Optional<Models.Remarks>> updateRemark(Models.Remarks remarks) {

        MutableLiveData<Optional<Models.Remarks>> mutableLiveData = new MutableLiveData<>();


        reviewApi.updateReview(remarks, getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();


                if (response.code() != 200 || jsonResponse == null || !jsonResponse.isSuccess() || jsonResponse.isHas_error() || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }


                try {
                    JsonObject reviewJson = new JsonObject(getObjectMapper().writeValueAsString(jsonResponse.getData()));
                    Models.Remarks remarks = getObjectMapper().readValue(reviewJson.toString(), Models.Remarks.class);
                    mutableLiveData.setValue(Optional.of(remarks));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                //save user to offline db


            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<Optional<Boolean>> postPurchase(Models.PurchaseCreationForm form) {
        MutableLiveData<Optional<Boolean>> mutableLiveData = new MutableLiveData<>();

        purchaseApi.newPurchase(form, getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();
                mutableLiveData.setValue(Optional.of(jsonResponse != null && jsonResponse.getData() != null && jsonResponse.isSuccess() && !jsonResponse.isHas_error()));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
                t.printStackTrace();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<Optional<Boolean>> postDonation(Models.DonationCreationForm form) {

        MutableLiveData<Optional<Boolean>> success = new MutableLiveData<>();

        purchaseApi.addDonation(form, getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                success.setValue(Optional.of(response.code() == 200));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                success.setValue(Optional.of(false));
            }
        });

        return success;
    }

    public static HashMap<String, String> getHeaderMap() {
        HashMap<String, String> headerMap = new HashMap<>();

        headerMap.put(AUTHORIZATION, getAccessToken(application));
        headerMap.put("connection", CONNECTION_VAL);
        headerMap.put("keep-alive", KEEP_ALIVE_VAL);
        headerMap.put(CONTENT_TYPE, APPLICATION_JSON);


        return headerMap;
    }

    private MutableLiveData<List<Models.Purchase>> getSellerPurchases(String sellerId) {
        MutableLiveData<List<Models.Purchase>> mutableLiveData = new MutableLiveData<>();
        List<Models.Purchase> purchaseList = new ArrayList<>();


        HashMap<String, String> params = new HashMap<>();
        params.put(SELLERS_ID, sellerId);


        purchaseApi.getAllPurchase(params, getHeaderMap()).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(new ArrayList<>());
                    return;
                }

                try {
                    JsonArray purchaseArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));

                    for (int i = 0; i < purchaseArray.size(); i++) {

                        try {
                            Models.Purchase purchase = getObjectMapper().readValue(new JsonObject(purchaseArray.getJsonObject(i).getMap()).toString(), Models.Purchase.class);

                            if (!purchase.getDeleted()) {
                                purchaseList.add(purchase);
                            }

                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                mutableLiveData.setValue(purchaseList);
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(new ArrayList<>());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<List<Models.Donation>> getDonorDonations(String donor) {
        MutableLiveData<List<Models.Donation>> mutableLiveData = new MutableLiveData<>();
        List<Models.Donation> donationList = new ArrayList<>();


        HashMap<String, String> params = new HashMap<>();
        params.put("donor", donor);

        purchaseApi.getDonation(params, getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(new ArrayList<>());
                    return;
                }

                try {
                    JsonArray purchaseArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));

                    for (int i = 0; i < purchaseArray.size(); i++) {

                        try {
                            Models.Donation donation = getObjectMapper().readValue(new JsonObject(purchaseArray.getJsonObject(i).getMap()).toString(), Models.Donation.class);
                            donationList.add(donation);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                mutableLiveData.setValue(donationList);
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(new ArrayList<>());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<List<Models.Purchase>> getTransporterPurchases() {
        MutableLiveData<List<Models.Purchase>> mutableLiveData = new MutableLiveData<>();
        List<Models.Purchase> purchaseList = new ArrayList<>();


        HashMap<String, String> params = new HashMap<>();


        purchaseApi.getAllPurchase(params, getHeaderMap()).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(new ArrayList<>());
                    return;
                }

                try {
                    JsonArray purchaseArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));

                    for (int i = 0; i < purchaseArray.size(); i++) {

                        try {
                            Models.Purchase purchase = getObjectMapper().readValue(new JsonObject(purchaseArray.getJsonObject(i).getMap()).toString(), Models.Purchase.class);
                            purchaseList.add(purchase);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                mutableLiveData.setValue(purchaseList);
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(new ArrayList<>());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<List<Models.Donation>> getTransporterDonations() {
        MutableLiveData<List<Models.Donation>> mutableLiveData = new MutableLiveData<>();
        List<Models.Donation> donationList = new ArrayList<>();

        HashMap<String, String> params = new HashMap<>();

        purchaseApi.getDonation(params, getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(new ArrayList<>());
                    return;
                }

                try {
                    JsonArray purchaseArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));

                    for (int i = 0; i < purchaseArray.size(); i++) {

                        try {
                            Models.Donation donation = getObjectMapper().readValue(new JsonObject(purchaseArray.getJsonObject(i).getMap()).toString(), Models.Donation.class);
                            donationList.add(donation);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                mutableLiveData.setValue(donationList);
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(new ArrayList<>());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<List<Models.Purchase>> getBuyerPurchases(String buyerId) {
        MutableLiveData<List<Models.Purchase>> mutableLiveData = new MutableLiveData<>();
        List<Models.Purchase> purchaseList = new ArrayList<>();


        HashMap<String, String> params = new HashMap<>();
        params.put(BUYERS_ID, buyerId);

        purchaseApi.getAllPurchase(params, getHeaderMap()).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(new ArrayList<>());
                    return;
                }

                try {

                    JsonArray purchaseArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));

                    for (int i = 0; i < purchaseArray.size(); i++) {

                        try {
                            Models.Purchase purchase = getObjectMapper().readValue(new JsonObject(purchaseArray.getJsonObject(i).getMap()).toString(), Models.Purchase.class);
                            purchaseList.add(purchase);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                mutableLiveData.setValue(purchaseList);
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(new ArrayList<>());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<List<Models.Donation>> getBeneficiaryDonations(String beneficiary) {
        MutableLiveData<List<Models.Donation>> mutableLiveData = new MutableLiveData<>();
        List<Models.Donation> donationList = new ArrayList<>();


        HashMap<String, String> params = new HashMap<>();
        params.put("beneficiary", beneficiary);

        purchaseApi.getDonation(params, getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(new ArrayList<>());
                    return;
                }

                try {
                    JsonArray purchaseArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));

                    for (int i = 0; i < purchaseArray.size(); i++) {

                        try {
                            Models.Donation donation = getObjectMapper().readValue(new JsonObject(purchaseArray.getJsonObject(i).getMap()).toString(), Models.Donation.class);
                            donationList.add(donation);

                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                mutableLiveData.setValue(donationList);
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(new ArrayList<>());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<List<Models.Purchase>> getAllPurchases() {
        MutableLiveData<List<Models.Purchase>> mutableLiveData = new MutableLiveData<>();
        List<Models.Purchase> purchaseList = new ArrayList<>();

        HashMap<String, String> params = new HashMap<>();

        purchaseApi.getAllPurchase(params, getHeaderMap()).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(new ArrayList<>());
                    return;
                }

                try {
                    JsonArray purchaseArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));

                    for (int i = 0; i < purchaseArray.size(); i++) {

                        try {
                            Models.Purchase purchase = getObjectMapper().readValue(new JsonObject(purchaseArray.getJsonObject(i).getMap()).toString(), Models.Purchase.class);

                            if (!purchase.getDeleted()) {
                                purchaseList.add(purchase);
                            }

                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                mutableLiveData.setValue(purchaseList);
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(new ArrayList<>());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<List<Models.Donation>> getAllDonations() {
        MutableLiveData<List<Models.Donation>> mutableLiveData = new MutableLiveData<>();
        List<Models.Donation> donationList = new ArrayList<>();

        HashMap<String, String> params = new HashMap<>();

        purchaseApi.getDonation(params, getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(new ArrayList<>());
                    return;
                }

                try {
                    JsonArray purchaseArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));

                    for (int i = 0; i < purchaseArray.size(); i++) {

                        try {
                            Models.Donation donation = getObjectMapper().readValue(new JsonObject(purchaseArray.getJsonObject(i).getMap()).toString(), Models.Donation.class);

                            if (!donation.isDeleted()) {
                                donationList.add(donation);
                            }

                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                mutableLiveData.setValue(donationList);
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(new ArrayList<>());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<Optional<Boolean>> acceptJob(Long purchaseId, String transporterUsername) {
        MutableLiveData<Optional<Boolean>> success = new MutableLiveData<>();

        purchaseApi.addDistribution(purchaseId, transporterUsername).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();
                success.setValue(Optional.of(response.code() == 200));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                success.setValue(Optional.empty());
            }
        });

        return success;
    }

    private MutableLiveData<Optional<Boolean>> acceptDonationJob(Long donationId, String transporterUsername) {
        MutableLiveData<Optional<Boolean>> success = new MutableLiveData<>();

        purchaseApi.addDonationDistribution(donationId, transporterUsername).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();
                success.setValue(Optional.of(response.code() == 200));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                success.setValue(Optional.empty());
            }
        });

        return success;
    }

    private MutableLiveData<Optional<Models.DonationDistribution>> getADonationDistribution(Long donationId) {
        MutableLiveData<Optional<Models.DonationDistribution>> mutableLiveData = new MutableLiveData<>();
        List<Models.DonationDistribution> list = new ArrayList<>();

        HashMap<String, String> params = new HashMap<>();
        params.put("donationId", String.valueOf(donationId));

        purchaseApi.getDonationDistribution(params).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();


                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                try {
                    JsonArray purchaseArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));


                    for (int i = 0; i < purchaseArray.size(); i++) {

                        try {
                            Models.DonationDistribution distributionModel = getObjectMapper().readValue(new JsonObject(purchaseArray.getJsonObject(i).getMap()).toString(), Models.DonationDistribution.class);

                            if (!distributionModel.getDeleted()) {
                                list.add(distributionModel);
                            }

                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                if (list.isEmpty()) {
                    mutableLiveData.setValue(Optional.empty());
                } else {
                    mutableLiveData.setValue(Optional.of(list.get(0)));
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
            }
        });


        return mutableLiveData;
    }


    private MutableLiveData<Optional<Models.DistributionModel>> getADistribution(Long purchaseId) {
        MutableLiveData<Optional<Models.DistributionModel>> mutableLiveData = new MutableLiveData<>();
        List<Models.DistributionModel> list = new ArrayList<>();

        HashMap<String, String> params = new HashMap<>();
        params.put("purchasesId", String.valueOf(purchaseId));

        purchaseApi.getDistribution(params).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();


                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                try {
                    JsonArray purchaseArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));


                    for (int i = 0; i < purchaseArray.size(); i++) {

                        try {
                            Models.DistributionModel distributionModel = getObjectMapper().readValue(new JsonObject(purchaseArray.getJsonObject(i).getMap()).toString(), Models.DistributionModel.class);

                            if (!distributionModel.getDeleted()) {
                                list.add(distributionModel);
                            }

                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                if (list.isEmpty()) {
                    mutableLiveData.setValue(Optional.empty());
                } else {
                    mutableLiveData.setValue(Optional.of(list.get(0)));
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
            }
        });


        return mutableLiveData;
    }

    private MutableLiveData<List<Models.DistributionModel>> getAllDistributions(HashMap<String, String> params, Boolean paid, Boolean completed) {
        MutableLiveData<List<Models.DistributionModel>> mutableLiveData = new MutableLiveData<>();
        List<Models.DistributionModel> list = new ArrayList<>();

        purchaseApi.getDistribution(params).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(new ArrayList<>());
                    return;
                }

                try {
                    JsonArray purchaseArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));

                    for (int i = 0; i < purchaseArray.size(); i++) {

                        try {
                            Models.DistributionModel distributionModel = getObjectMapper().readValue(new JsonObject(purchaseArray.getJsonObject(i).getMap()).toString(), Models.DistributionModel.class);


                            if (!distributionModel.getDeleted()) {
                                list.add(distributionModel);
                            }

                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                mutableLiveData.setValue(list);
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(new ArrayList<>());
            }
        });


        return mutableLiveData;
    }

    private MutableLiveData<Optional<Models.DistributionModel>> updateDistribution(Models.DistributionUpdateForm form) {
        MutableLiveData<Optional<Models.DistributionModel>> updatedModel = new MutableLiveData<>();

        purchaseApi.updateDistribution(form).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                try {

                    JsonResponse jsonResponse = response.body();

                    if (response.code() != 200 || jsonResponse == null || jsonResponse.isHas_error() || !jsonResponse.isSuccess() || jsonResponse.getData() == null) {
                        updatedModel.setValue(Optional.empty());
                        return;
                    }

                    JsonObject distModel = new JsonObject(getObjectMapper().writeValueAsString(jsonResponse.getData()));
                    Models.DistributionModel updatedDist = getObjectMapper().readValue(distModel.toString(), Models.DistributionModel.class);
                    updatedModel.setValue(Optional.of(updatedDist));

                } catch (JsonProcessingException e) {

                    e.printStackTrace();
                    updatedModel.setValue(Optional.empty());
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                updatedModel.setValue(Optional.empty());
            }
        });

        return updatedModel;
    }

    private MutableLiveData<Optional<Models.DonationDistribution>> updateDonationDistribution(Models.DonorDistributionUpdateForm form) {
        MutableLiveData<Optional<Models.DonationDistribution>> updatedModel = new MutableLiveData<>();

        purchaseApi.updateDonationDistribution(form).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                try {

                    JsonResponse jsonResponse = response.body();

                    if (response.code() != 200 || jsonResponse == null || jsonResponse.isHas_error() || !jsonResponse.isSuccess() || jsonResponse.getData() == null) {
                        updatedModel.setValue(Optional.empty());
                        return;
                    }

                    JsonObject distModel = new JsonObject(getObjectMapper().writeValueAsString(jsonResponse.getData()));
                    Models.DonationDistribution updatedDist = getObjectMapper().readValue(distModel.toString(), Models.DonationDistribution.class);
                    updatedModel.setValue(Optional.of(updatedDist));

                } catch (JsonProcessingException e) {

                    e.printStackTrace();
                    updatedModel.setValue(Optional.empty());
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                updatedModel.setValue(Optional.empty());
            }
        });

        return updatedModel;
    }


    //expose
    public LiveData<List<Models.Purchase>> getSellerPurchaseList(String sellerId) {
        return getSellerPurchases(sellerId);
    }

    public LiveData<List<Models.Purchase>> getTransporterJobsList() {
        return getTransporterPurchases();
    }

    public LiveData<List<Models.Purchase>> getBuyerPurchaseList(String buyerId) {
        return getBuyerPurchases(buyerId);
    }

    public LiveData<List<Models.Purchase>> getAllPurchasesLive() {
        return getAllPurchases();
    }

    public LiveData<Optional<Boolean>> postNewPurchase(Models.PurchaseCreationForm form) {
        return postPurchase(form);
    }

    public LiveData<Optional<Boolean>> acceptTransportJob(Long purchaseId, String transporterUsername) {
        return acceptJob(purchaseId, transporterUsername);
    }

    public LiveData<Optional<Boolean>> acceptDonationTransportJob(Long donationId, String transporterUsername) {
        return acceptDonationJob(donationId, transporterUsername);
    }

    public LiveData<List<Models.DistributionModel>> getDistributions(HashMap<String, String> params, Boolean paid, Boolean completed) {
        return getAllDistributions(params, paid, completed);
    }

    public LiveData<Optional<Models.DistributionModel>> updateADistribution(Models.DistributionUpdateForm form) {
        return updateDistribution(form);
    }

    public LiveData<Optional<Models.DonationDistribution>> updateADonationDistribution(Models.DonorDistributionUpdateForm form) {
        return updateDonationDistribution(form);
    }

    public LiveData<Optional<Models.DistributionModel>> getADistributionLive(Long purchaseId) {
        return getADistribution(purchaseId);
    }

    public LiveData<Optional<Models.DonationDistribution>> getADonationDistributionLive(Long donationId) {
        return getADonationDistribution(donationId);
    }


    public LiveData<Optional<Models.Remarks>> createNewRemark(Models.Remarks remarksForm) {
        return postRemark(remarksForm);
    }

    public LiveData<Optional<Models.Remarks>> updateARemark(Models.Remarks remarks) {
        return updateRemark(remarks);
    }

    public LiveData<Optional<Boolean>> postADonation(Models.DonationCreationForm form) {
        return postDonation(form);
    }

    public LiveData<List<Models.Donation>> getADonorDonations(String donor) {
        return getDonorDonations(donor);
    }

    public LiveData<List<Models.Donation>> getATransporterDonations() {
        return getTransporterDonations();
    }

    public LiveData<List<Models.Donation>> getABeneficiaryDonations(String beneficiary) {
        return getBeneficiaryDonations(beneficiary);
    }

    public LiveData<List<Models.Donation>> getAllDonationsLive() {
        return getAllDonations();
    }
}
