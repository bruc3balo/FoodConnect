package com.victoria.foodconnect.globals.statsDb;

import static com.victoria.foodconnect.globals.GlobalRepository.statsApi;
import static com.victoria.foodconnect.globals.GlobalVariables.USERNAME;
import static com.victoria.foodconnect.globals.purchaseDb.PurchaseViewModel.getHeaderMap;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.models.Models.SellerStats;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatsViewModel extends AndroidViewModel {

    public StatsViewModel(@NonNull Application application) {
        super(application);
    }


    private MutableLiveData<List<SellerStats>> getSellerStats(Integer year, String sellerName) {
        MutableLiveData<List<SellerStats>> mutableLiveData = new MutableLiveData<>();
        LinkedHashSet<SellerStats> statsLinkedHashSet = new LinkedHashSet<>();


        HashMap<String, String> params = new HashMap<>();
        params.put("year", String.valueOf(year));
        params.put(USERNAME, sellerName);

        statsApi.getSellerStats(params, getHeaderMap()).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || response.code() != 200 || !jsonResponse.isSuccess() || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(new ArrayList<>());
                    System.out.println("Failed to get stats");
                    return;
                }


                try {
                    JsonArray serviceArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));

                    //populate keys
                    for (int i = 0; i < serviceArray.size(); i++) {
                        try {
                            Models.SellerStats sellerStats = getObjectMapper().readValue(new JsonObject(serviceArray.getJsonObject(i).getMap()).toString(), Models.SellerStats.class);
                            statsLinkedHashSet.add(sellerStats);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }

                    mutableLiveData.setValue(new ArrayList<>(statsLinkedHashSet));

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    mutableLiveData.setValue(new ArrayList<>());

                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(new ArrayList<>());
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<List<SellerStats>> getAllSellerStats(Integer year) {
        MutableLiveData<List<SellerStats>> mutableLiveData = new MutableLiveData<>();
        LinkedHashSet<SellerStats> statsLinkedHashSet = new LinkedHashSet<>();

        statsApi.getAllSellerStats(year, getHeaderMap()).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || response.code() != 200 || !jsonResponse.isSuccess() || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(new ArrayList<>());
                    System.out.println("Failed to get stats");
                    return;
                }


                try {
                    JsonArray serviceArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));

                    //populate keys
                    for (int i = 0; i < serviceArray.size(); i++) {
                        try {
                            Models.SellerStats sellerStats = getObjectMapper().readValue(new JsonObject(serviceArray.getJsonObject(i).getMap()).toString(), Models.SellerStats.class);
                            statsLinkedHashSet.add(sellerStats);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }

                    mutableLiveData.setValue(new ArrayList<>(statsLinkedHashSet));

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    mutableLiveData.setValue(new ArrayList<>());

                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(new ArrayList<>());
                t.printStackTrace();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<List<SellerStats>> getAllDonorStats(Integer year) {
        MutableLiveData<List<SellerStats>> mutableLiveData = new MutableLiveData<>();
        LinkedHashSet<SellerStats> statsLinkedHashSet = new LinkedHashSet<>();

        statsApi.getAllDonorStats(year, getHeaderMap()).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || response.code() != 200 || !jsonResponse.isSuccess() || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(new ArrayList<>());
                    System.out.println("Failed to get stats");
                    return;
                }


                try {
                    JsonArray serviceArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));

                    //populate keys
                    for (int i = 0; i < serviceArray.size(); i++) {
                        try {
                            Models.SellerStats sellerStats = getObjectMapper().readValue(new JsonObject(serviceArray.getJsonObject(i).getMap()).toString(), Models.SellerStats.class);
                            statsLinkedHashSet.add(sellerStats);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }

                    mutableLiveData.setValue(new ArrayList<>(statsLinkedHashSet));

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    mutableLiveData.setValue(new ArrayList<>());

                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(new ArrayList<>());
                t.printStackTrace();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<List<SellerStats>> getDonorStats(Integer year, String donorName) {
        MutableLiveData<List<SellerStats>> mutableLiveData = new MutableLiveData<>();
        LinkedHashSet<SellerStats> statsLinkedHashSet = new LinkedHashSet<>();


        HashMap<String, String> params = new HashMap<>();
        params.put("year", String.valueOf(year));
        params.put(USERNAME, donorName);

        statsApi.getDonorStats(params, getHeaderMap()).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || response.code() != 200 || !jsonResponse.isSuccess() || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(new ArrayList<>());
                    System.out.println("Failed to get stats");
                    return;
                }


                try {
                    JsonArray serviceArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));

                    //populate keys
                    for (int i = 0; i < serviceArray.size(); i++) {
                        try {
                            Models.SellerStats sellerStats = getObjectMapper().readValue(new JsonObject(serviceArray.getJsonObject(i).getMap()).toString(), Models.SellerStats.class);
                            statsLinkedHashSet.add(sellerStats);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }

                    mutableLiveData.setValue(new ArrayList<>(statsLinkedHashSet));

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    mutableLiveData.setValue(new ArrayList<>());

                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(new ArrayList<>());
            }
        });

        return mutableLiveData;
    }

    //expose

    public LiveData<List<SellerStats>> getSellerStatsLive(Integer year, String sellerName) {
        return getSellerStats(year, sellerName);
    }

    public LiveData<List<SellerStats>> getAllSellerStatsLive(Integer year) {
        return getAllSellerStats(year);
    }

    public LiveData<List<SellerStats>> getDonorStatsLive(Integer year, String sellerName) {
        return getDonorStats(year, sellerName);
    }

    public LiveData<List<SellerStats>> getAllDonorStatsLive(Integer year) {
        return getAllDonorStats(year);
    }


}
