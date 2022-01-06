package com.victoria.foodconnect.globals.purchaseDb;

import static com.victoria.foodconnect.globals.GlobalRepository.application;
import static com.victoria.foodconnect.globals.GlobalRepository.purchaseApi;
import static com.victoria.foodconnect.globals.GlobalVariables.APPLICATION_JSON;
import static com.victoria.foodconnect.globals.GlobalVariables.BUYERS_ID;
import static com.victoria.foodconnect.globals.GlobalVariables.SELLERS_ID;
import static com.victoria.foodconnect.globals.GlobalVariables.USERNAME;
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
import retrofit2.http.Query;

public class PurchaseViewModel extends AndroidViewModel {

    public PurchaseViewModel(@NonNull Application application) {
        super(application);
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

    private MutableLiveData<List<Models.Purchase>> getSellerPurchases(String sellerId) {
        MutableLiveData<List<Models.Purchase>> mutableLiveData = new MutableLiveData<>();
        List<Models.Purchase> purchaseList = new ArrayList<>();


        HashMap<String, String> params = new HashMap<>();
        params.put(SELLERS_ID, sellerId);

        purchaseApi.getAllPurchase(params, getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
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

    private MutableLiveData<List<Models.Purchase>> getTransporterPurchases(String username,int position) {
        MutableLiveData<List<Models.Purchase>> mutableLiveData = new MutableLiveData<>();
        List<Models.Purchase> purchaseList = new ArrayList<>();


        HashMap<String, String> params = new HashMap<>();


        purchaseApi.getAllPurchase(params, getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
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

                            switch (position) {
                                default:
                                case 0:
                                    if (!purchase.isComplete() &&  !purchase.getDeleted() &&purchase.getAssigned() == null) {
                                        purchaseList.add(purchase);
                                    }
                                    break;

                                case 1:
                                    if(!purchase.isComplete() && !purchase.getDeleted() && purchase.getAssigned() != null && purchase.getAssigned().equals(username)) {
                                        purchaseList.add(purchase);
                                    }
                                    break;

                                case 2:
                                    if (purchase.getDeleted() || purchase.isComplete()) {
                                        purchaseList.add(purchase);
                                    }
                                    break;
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

    private MutableLiveData<List<Models.Purchase>> getBuyerPurchases(String buyerId) {
        MutableLiveData<List<Models.Purchase>> mutableLiveData = new MutableLiveData<>();
        List<Models.Purchase> purchaseList = new ArrayList<>();


        HashMap<String, String> params = new HashMap<>();
        params.put(BUYERS_ID, buyerId);

        purchaseApi.getAllPurchase(params, getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
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

    private MutableLiveData<List<Models.Purchase>> getAllPurchases() {
        MutableLiveData<List<Models.Purchase>> mutableLiveData = new MutableLiveData<>();
        List<Models.Purchase> purchaseList = new ArrayList<>();

        HashMap<String, String> params = new HashMap<>();

        purchaseApi.getAllPurchase(params, getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
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

    //expose

    public LiveData<List<Models.Purchase>> getSellerPurchaseList(String sellerId) {
        return getSellerPurchases(sellerId);
    }

    public LiveData<List<Models.Purchase>> getTransporterJobsList(String username,int position) {
        return getTransporterPurchases(username,position);
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
        return acceptJob(purchaseId,transporterUsername);
    }

    public LiveData<List<Models.DistributionModel>> getDistributions(HashMap<String, String> params, Boolean paid, Boolean completed) {
        return getAllDistributions(params,paid,completed);
    }

    public LiveData<Optional<Models.DistributionModel>> updateADistribution(Models.DistributionUpdateForm form) {
        return updateDistribution(form);
    }

}