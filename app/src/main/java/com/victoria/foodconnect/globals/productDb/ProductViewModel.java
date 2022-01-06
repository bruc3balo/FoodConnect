package com.victoria.foodconnect.globals.productDb;

import static com.victoria.foodconnect.globals.GlobalRepository.application;
import static com.victoria.foodconnect.globals.GlobalRepository.productApi;
import static com.victoria.foodconnect.globals.GlobalVariables.APPLICATION_JSON;
import static com.victoria.foodconnect.globals.GlobalVariables.ID;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_CATEGORY_NAME;
import static com.victoria.foodconnect.globals.userDb.UserViewModel.refreshStaticToken;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.DataOpts;
import com.victoria.foodconnect.utils.JsonResponse;
import com.victoria.foodconnect.utils.MyLinkedMap;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductViewModel extends AndroidViewModel {

    public ProductViewModel(@NonNull Application application) {
        super(application);
    }

    private MutableLiveData<MyLinkedMap<String, LinkedList<Models.Product>>> getAllBuyingProducts() {
        MutableLiveData<MyLinkedMap<String, LinkedList<Models.Product>>> mutableLiveData = new MutableLiveData<>();
        MyLinkedMap<String, LinkedList<Models.Product>> map = new MyLinkedMap<>();


        productApi.getAllProducts(DataOpts.getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(new MyLinkedMap<>());
                    return;
                }

                try {
                    JsonArray serviceArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));
                    LinkedHashSet<String> categoryNames = new LinkedHashSet<>();

                    //populate keys
                    for (int i = 0; i < serviceArray.size(); i++) {

                        try {
                            System.out.println("count " + i);
                            Models.Product product = getObjectMapper().readValue(new JsonObject(serviceArray.getJsonObject(i).getMap()).toString(), Models.Product.class);

                            if (!product.getDeleted() && !product.getDisabled() && product.getUnitsLeft() > 0) {
                                Models.ProductCategory category = product.getProduct_category();
                                if (!category.getDeleted() && !category.getDisabled()) {
                                    categoryNames.add(product.getProduct_category().getName());
                                }

                            }

                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }

                    //populate map keys
                    categoryNames.forEach(c-> map.put(c,new LinkedList<>()));

                    //populate map values
                    for (int i = 0; i < serviceArray.size(); i++) {

                        try {
                            System.out.println("count for products" + i);
                            Models.Product product = getObjectMapper().readValue(new JsonObject(serviceArray.getJsonObject(i).getMap()).toString(), Models.Product.class);

                            if (!product.getDeleted() && !product.getDisabled() && product.getUnitsLeft() >     0) {
                                Objects.requireNonNull(map.get(product.getProduct_category().getName())).add(product);

                                try {
                                    System.out.println(product.getName()+" map is "+getObjectMapper().writeValueAsString(map));
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }

                            }

                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }



                mutableLiveData.setValue(map);

            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(new MyLinkedMap<>());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<LinkedList<Models.Product>> getAllUserBuyingProducts() {
        MutableLiveData<LinkedList<Models.Product>> mutableLiveData = new MutableLiveData<>();
        LinkedList<Models.Product> map = new LinkedList<>();


        productApi.getAllProducts(DataOpts.getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(new LinkedList<>());
                    return;
                }

                try {
                    JsonArray serviceArray = new JsonArray(getObjectMapper().writeValueAsString(jsonResponse.getData()));

                    //populate list
                    for (int i = 0; i < serviceArray.size(); i++) {

                        try {
                            System.out.println("count " + i);
                            Models.Product product = getObjectMapper().readValue(new JsonObject(serviceArray.getJsonObject(i).getMap()).toString(), Models.Product.class);

                            if (!product.getDeleted() && !product.getDisabled()) {
                                map.add(product);
                            }

                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }



                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                mutableLiveData.setValue(map);

            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(new LinkedList<>());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }


    private MutableLiveData<Optional<JsonResponse>> getAllProducts() {
        MutableLiveData<Optional<JsonResponse>> mutableLiveData = new MutableLiveData<>();

        productApi.getAllProducts(DataOpts.getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                mutableLiveData.setValue(Optional.of(jsonResponse));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<Optional<JsonResponse>> getAllSellerProducts(String username) {
        MutableLiveData<Optional<JsonResponse>> mutableLiveData = new MutableLiveData<>();

        productApi.getMySellingProducts(DataOpts.getAccessToken(application), username, APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                mutableLiveData.setValue(Optional.of(jsonResponse));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }


    private MutableLiveData<Optional<JsonResponse>> getAllProductCategories() {
        MutableLiveData<Optional<JsonResponse>> mutableLiveData = new MutableLiveData<>();

        System.out.println("GETTING PRODUCT CATEGORIES");

        productApi.getAllProductCategories(DataOpts.getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();


                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    System.out.println("NO PRODUCT CATEGORY DATA");
                    return;
                }


                System.out.println("HAS PRODUCT CATEGORY DATA");

                mutableLiveData.setValue(Optional.of(jsonResponse));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<Optional<JsonResponse>> getProductListWithCategory(String categoryName) {
        MutableLiveData<Optional<JsonResponse>> mutableLiveData = new MutableLiveData<>();

        productApi.getProductListWithCategory(categoryName, DataOpts.getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                mutableLiveData.setValue(Optional.of(jsonResponse));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<Optional<JsonResponse>> createNewProductCategory(String categoryName) {
        MutableLiveData<Optional<JsonResponse>> mutableLiveData = new MutableLiveData<>();

        productApi.createNewProductCategory(categoryName, DataOpts.getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                mutableLiveData.setValue(Optional.of(jsonResponse));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<Optional<JsonResponse>> getProductCategory(String id, String categoryName) {
        MutableLiveData<Optional<JsonResponse>> mutableLiveData = new MutableLiveData<>();

        HashMap<String, String> params = new HashMap<>();

        if (id == null && categoryName != null) {
            params.put(PRODUCT_CATEGORY_NAME, categoryName);
        } else if (id != null && categoryName == null) {
            params.put(ID, id);
        }

        productApi.getProductCategory(params, DataOpts.getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                mutableLiveData.setValue(Optional.of(jsonResponse));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<Optional<JsonResponse>> updateProductCategory(Models.ProductCategoryUpdateForm form, String categoryName) {
        MutableLiveData<Optional<JsonResponse>> mutableLiveData = new MutableLiveData<>();

        productApi.updateProductCategory(categoryName, DataOpts.getAccessToken(application), APPLICATION_JSON, form).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                mutableLiveData.setValue(Optional.of(jsonResponse));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<Optional<JsonResponse>> createNewProduct(Models.ProductCreationFrom form) {
        MutableLiveData<Optional<JsonResponse>> mutableLiveData = new MutableLiveData<>();

        productApi.createNewProduct(DataOpts.getAccessToken(application), APPLICATION_JSON, form).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                mutableLiveData.setValue(Optional.of(jsonResponse));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<Optional<JsonResponse>> getSpecificProduct(String id) {
        MutableLiveData<Optional<JsonResponse>> mutableLiveData = new MutableLiveData<>();

        productApi.getSpecificProduct(DataOpts.getAccessToken(application), APPLICATION_JSON, id).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                mutableLiveData.setValue(Optional.of(jsonResponse));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<Optional<JsonResponse>> updateProduct(Models.ProductUpdateForm form) {
        MutableLiveData<Optional<JsonResponse>> mutableLiveData = new MutableLiveData<>();

        productApi.updateProduct(DataOpts.getAccessToken(application), APPLICATION_JSON, form).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null) {
                    System.out.println("UPDATE NO BODY");
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                mutableLiveData.setValue(Optional.of(jsonResponse));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
                System.out.println("UPDATE "+t.getMessage());
            }
        });

        return mutableLiveData;
    }


    //expose

    public LiveData<Optional<JsonResponse>> getAllProductsLive() {
        return getAllProducts();
    }

    public LiveData<Optional<JsonResponse>> getAllSellerProductsLive(String username) {
        return getAllSellerProducts(username);
    }

    public LiveData<Optional<JsonResponse>> getAllProductCategoriesLive() {
        return getAllProductCategories();
    }

    public LiveData<Optional<JsonResponse>> getProductListWithCategoryLive(String categoryName) {
        return getProductListWithCategory(categoryName);
    }

    public LiveData<Optional<JsonResponse>> createNewProductCategoryLive(String categoryName) {
        return createNewProductCategory(categoryName);
    }

    public LiveData<Optional<JsonResponse>> getProductCategoryLive(String productCategoryId, String productCategoryName) {
        return getProductCategory(productCategoryId, productCategoryName);
    }

    public LiveData<Optional<JsonResponse>> updateProductCategoryLive(String productCategoryName, Models.ProductCategoryUpdateForm form) {
        return updateProductCategory(form, productCategoryName);
    }

    public LiveData<Optional<JsonResponse>> createNewProductLive(Models.ProductCreationFrom form) {
        return createNewProduct(form);
    }

    public LiveData<Optional<JsonResponse>> getSpecificProductLive(String productId) {
        return getSpecificProduct(productId);
    }

    public LiveData<Optional<JsonResponse>> updateProductLive(Models.ProductUpdateForm form) {
        return updateProduct( form);
    }

    public LiveData<MyLinkedMap<String, LinkedList<Models.Product>>>getAllBuyerProducts () {
        return getAllBuyingProducts();
    }

    public LiveData<LinkedList<Models.Product>> getAllGoodBuyerProducts(){
        return getAllUserBuyingProducts();
    }
}
