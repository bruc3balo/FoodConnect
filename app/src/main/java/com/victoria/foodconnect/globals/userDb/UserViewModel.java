package com.victoria.foodconnect.globals.userDb;

import static com.victoria.foodconnect.globals.GlobalRepository.application;
import static com.victoria.foodconnect.globals.GlobalRepository.userApi;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.ACCESS_TOKEN;
import static com.victoria.foodconnect.globals.GlobalVariables.APPLICATION_JSON;
import static com.victoria.foodconnect.globals.GlobalVariables.UID;
import static com.victoria.foodconnect.globals.GlobalVariables.USER_COLLECTION;
import static com.victoria.foodconnect.utils.DataOpts.editSp;
import static com.victoria.foodconnect.utils.DataOpts.getAccessToken;
import static com.victoria.foodconnect.utils.DataOpts.getDomainUserFromModelUser;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;
import static com.victoria.foodconnect.utils.DataOpts.getSp;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.models.Models.LoginResponse;
import com.victoria.foodconnect.utils.JsonResponse;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.vertx.core.json.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserViewModel extends AndroidViewModel {

    public UserViewModel(@NonNull Application application) {
        super(application);
    }

    private MutableLiveData<Optional<JsonResponse>> getAllUserData() {
        MutableLiveData<Optional<JsonResponse>> userMutableLiveData = new MutableLiveData<>();


        userApi.getAllUsers(getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {

                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.getData() == null || jsonResponse.isHas_error() || !jsonResponse.isSuccess()) {
                    userMutableLiveData.setValue(Optional.empty());
                    return;
                }

                userMutableLiveData.setValue(Optional.of(jsonResponse));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                userMutableLiveData.setValue(Optional.empty());
            }
        });

        return userMutableLiveData;
    }

    private MutableLiveData<Optional<Response<JsonResponse>>> getAUserData(HashMap<String, String> params) {
        MutableLiveData<Optional<Response<JsonResponse>>> mutableLiveData = new MutableLiveData<>();

        String header = getAccessToken(application);

        System.out.println("===== header ==== " + header);

        userApi.getUser(params, header, APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                mutableLiveData.setValue(Optional.of(response));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<Optional<List<String>>> getAllEmailList() {
        MutableLiveData<Optional<List<String>>> emailMutable = new MutableLiveData<>();
        List<String> emailList = new ArrayList<>();

        userApi.getAllEmails(APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null) {
                    System.out.println("FAILED TO GET EMAIL RESPONSE");
                    emailMutable.setValue(Optional.empty());
                    return;
                }

                if (jsonResponse.isHas_error()) {
                    System.out.println("ERROR GETTING EMAIL RESPONSE");
                    emailMutable.setValue(Optional.empty());
                    return;
                }

                if (jsonResponse.getData() == null) {
                    System.out.println("ERROR GETTING EMAIL DATA");
                    emailMutable.setValue(Optional.empty());
                    return;
                }

                try {
                    List emails = getObjectMapper().readValue(new JSONArray(jsonResponse.getData().toString()).toString(), List.class);
                    emails.forEach(e -> emailList.add(e.toString()));
                    emailMutable.setValue(Optional.of(emailList));
                } catch (JsonProcessingException | JSONException e) {
                    e.printStackTrace();
                    System.out.println("ERROR MAPPING EMAIL DATA");
                    emailMutable.setValue(Optional.empty());
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                System.out.println(t.getMessage());
                t.printStackTrace();
                emailMutable.setValue(Optional.empty());
            }
        });

        return emailMutable;
    }

    private MutableLiveData<Optional<List<String>>> getAllMobileList() {
        MutableLiveData<Optional<List<String>>> numbersMutable = new MutableLiveData<>();
        List<String> mobileList = new ArrayList<>();

        userApi.getAllPhoneNumbers(APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null) {
                    System.out.println("FAILED TO GET MOBILE RESPONSE");
                    numbersMutable.setValue(Optional.empty());
                    return;
                }

                if (jsonResponse.isHas_error()) {
                    System.out.println("ERROR GETTING MOBILE RESPONSE");
                    numbersMutable.setValue(Optional.empty());
                    return;
                }

                if (jsonResponse.getData() == null) {
                    System.out.println("ERROR GETTING MOBILE DATA");
                    numbersMutable.setValue(Optional.empty());
                    return;
                }

                try {
                    List numbers = getObjectMapper().readValue(new JSONArray(jsonResponse.getData().toString()).toString(), List.class);
                    numbers.forEach(e -> mobileList.add(e.toString()));
                    numbersMutable.setValue(Optional.of(mobileList));
                } catch (JsonProcessingException | JSONException e) {
                    e.printStackTrace();
                    System.out.println("ERROR MAPPING MOBILE DATA");
                    numbersMutable.setValue(Optional.empty());
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                System.out.println(t.getMessage());
                t.printStackTrace();
                numbersMutable.setValue(Optional.empty());
            }
        });

        return numbersMutable;
    }

    private MutableLiveData<Optional<List<String>>> getAllUsernameList() {
        MutableLiveData<Optional<List<String>>> usernameMutable = new MutableLiveData<>();
        List<String> usernameList = new ArrayList<>();

        userApi.getAllUsernames(APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null) {
                    System.out.println("FAILED TO GET USERNAME RESPONSE");
                    usernameMutable.setValue(Optional.empty());
                    return;
                }

                if (jsonResponse.isHas_error()) {
                    System.out.println("ERROR GETTING USERNAME RESPONSE");
                    usernameMutable.setValue(Optional.empty());
                    return;
                }

                if (jsonResponse.getData() == null) {
                    System.out.println("ERROR GETTING USERNAME DATA");
                    usernameMutable.setValue(Optional.empty());
                    return;
                }

                try {
                    List numbers = getObjectMapper().readValue(new JSONArray(jsonResponse.getData().toString()).toString(), List.class);
                    numbers.forEach(e -> usernameList.add(e.toString()));
                    usernameMutable.setValue(Optional.of(usernameList));
                } catch (JsonProcessingException | JSONException e) {
                    e.printStackTrace();
                    System.out.println("ERROR MAPPING USERNAME DATA");
                    usernameMutable.setValue(Optional.empty());
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                System.out.println(t.getMessage());
                t.printStackTrace();
                usernameMutable.setValue(Optional.empty());
            }
        });

        return usernameMutable;
    }

    private MutableLiveData<Optional<Response<JsonResponse>>> createNewUserMutable(Models.NewUserForm form) {
        MutableLiveData<Optional<Response<JsonResponse>>> mutableLiveData = new MutableLiveData<>();

        userApi.authenticateUser(form, APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                mutableLiveData.setValue(Optional.of(response));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
                System.out.println("FAILED TO SEND REQUEST");
            }
        });

        return mutableLiveData;
    }


    private MutableLiveData<Optional<Response<JsonResponse>>> updateUserMutable(String uid, Models.UserUpdateForm form) {
        MutableLiveData<Optional<Response<JsonResponse>>> mutableLiveData = new MutableLiveData<>();

        userApi.updateUser(uid, getAccessToken(application), form, APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                mutableLiveData.setValue(Optional.of(response));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
                System.out.println("FAILED TO SEND UPDATE REQUEST");
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<Optional<Response<LoginResponse>>> getNewAccessToken(Models.UsernameAndPasswordAuthenticationRequest request) {
        MutableLiveData<Optional<Response<LoginResponse>>> mutableLiveData = new MutableLiveData<>();

        userApi.getToken(request, APPLICATION_JSON).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                mutableLiveData.setValue(Optional.of(response));
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<Boolean> checkVerification(String uid) {

        HashMap<String, String> params = new HashMap<>();
        params.put(UID, uid);

        MutableLiveData<Boolean> result = new MutableLiveData<>();

        userApi.getUser(params, getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {

                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null) {
                    result.setValue(false);
                    return;
                }

                if (jsonResponse.isHas_error()) {
                    result.setValue(false);
                    return;
                }

                if (jsonResponse.getData() == null) {
                    result.setValue(false);
                    return;
                }

                ObjectMapper mapper = getObjectMapper();

                try {
                    JsonObject userJson = new JsonObject(mapper.writeValueAsString(jsonResponse.getData()));

                    //save user to offline db
                    Models.AppUser firebaseDbUser = mapper.readValue(userJson.toString(), Models.AppUser.class);

                    userRepository.insert(getDomainUserFromModelUser(firebaseDbUser));

                    Thread.sleep(2000);

                    result.setValue(firebaseDbUser.getVerified());

                } catch (JsonProcessingException | InterruptedException e) {

                    if (e instanceof JsonProcessingException) {
                        result.setValue(false);
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                result.setValue(false);
            }
        });


        return result;
    }

    private MutableLiveData<Boolean> refreshToken() {
        MutableLiveData<Boolean> tokenRefreshed = new MutableLiveData<>();

        String token = Objects.requireNonNull(getSp(USER_COLLECTION, application).get(ACCESS_TOKEN)).toString();

        userApi.refreshAccessToken(token, APPLICATION_JSON).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {

                LoginResponse loginResponse = response.body();

                if (loginResponse == null) {
                    tokenRefreshed.setValue(false);
                    Toast.makeText(application, "Failed to refresh token", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, String> map = new HashMap<>();
                map.put(ACCESS_TOKEN, loginResponse.getAccess_token());

                editSp(USER_COLLECTION, map, application);

                tokenRefreshed.setValue(true);
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                tokenRefreshed.setValue(false);
            }
        });
        return tokenRefreshed;
    }

    public static MutableLiveData<Boolean> refreshStaticToken() {
        MutableLiveData<Boolean> tokenRefreshed = new MutableLiveData<>();

        String token = Objects.requireNonNull(getSp(USER_COLLECTION, application).get(ACCESS_TOKEN)).toString();


        try {
            userApi.refreshAccessToken(token, APPLICATION_JSON).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {


                    LoginResponse loginResponse = response.body();

                    if (loginResponse == null) {
                        tokenRefreshed.setValue(false);
                        Toast.makeText(application, "Failed to refresh token", Toast.LENGTH_SHORT).show();

                        return;
                    }

                    Map<String, String> map = new HashMap<>();
                    map.put(ACCESS_TOKEN, loginResponse.getAccess_token());


                    editSp(USER_COLLECTION, map, application);

                    tokenRefreshed.setValue(true);
                }

                @Override
                public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                    tokenRefreshed.setValue(false);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            tokenRefreshed.setValue(false);

        }
        return tokenRefreshed;
    }

    private MutableLiveData<Optional<JsonResponse>> getAllRoles() {
        MutableLiveData<Optional<JsonResponse>> mutableLiveData = new MutableLiveData<>();
        userApi.getRoles(getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {

                JsonResponse jsonResponse = response.body();

                if (jsonResponse == null || !jsonResponse.isSuccess() || jsonResponse.isHas_error() || jsonResponse.getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                mutableLiveData.setValue(Optional.of(jsonResponse));
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
            }
        });
        return mutableLiveData;
    }

    private MutableLiveData<Optional<JsonResponse>> getUserCart() {
        MutableLiveData<Optional<JsonResponse>> mutableLiveData = new MutableLiveData<>();
        userApi.getCart(getAccessToken(application), FirebaseAuth.getInstance().getUid()).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                if (response.body() == null || response.body().isHas_error() || !response.body().isSuccess() || response.body().getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                mutableLiveData.setValue(Optional.of(response.body()));

            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
            }
        });
        return mutableLiveData;
    }

    private MutableLiveData<Optional<JsonResponse>> saveCart(Models.Cart cart) {
        MutableLiveData<Optional<JsonResponse>> mutableLiveData = new MutableLiveData<>();

        System.out.println(new Gson().toJson(cart));
        userApi.saveNewCart(getAccessToken(application), cart).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                if (response.body() == null || response.body().isHas_error() || !response.body().isSuccess() || response.body().getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                mutableLiveData.setValue(Optional.of(response.body()));

            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
            }
        });
        return mutableLiveData;
    }

    private MutableLiveData<Optional<JsonResponse>> updateCart(Models.Cart cart) {
        MutableLiveData<Optional<JsonResponse>> mutableLiveData = new MutableLiveData<>();
        userApi.updateCart(getAccessToken(application), cart).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                if (response.body() == null || response.body().isHas_error() || !response.body().isSuccess() || response.body().getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                mutableLiveData.setValue(Optional.of(response.body()));

            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
            }
        });
        return mutableLiveData;
    }

    private MutableLiveData<Optional<JsonResponse>> deleteCart(String cartId) {
        MutableLiveData<Optional<JsonResponse>> mutableLiveData = new MutableLiveData<>();
        userApi.deleteCart(getAccessToken(application), cartId).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                if (response.body() == null || response.body().isHas_error() || !response.body().isSuccess() || response.body().getData() == null) {
                    mutableLiveData.setValue(Optional.empty());
                    return;
                }

                mutableLiveData.setValue(Optional.of(response.body()));

            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                mutableLiveData.setValue(Optional.empty());
            }
        });
        return mutableLiveData;
    }

    //expose
    public LiveData<Optional<List<String>>> getEmailList() {
        return getAllEmailList();
    }

    public LiveData<Optional<List<String>>> getPhoneNumbers() {
        return getAllMobileList();
    }

    public LiveData<Optional<List<String>>> getUsernames() {
        return getAllUsernameList();
    }

    public LiveData<Optional<JsonResponse>> getLiveAllUsers() {
        return getAllUserData();
    }

    public LiveData<Optional<Response<JsonResponse>>> getLiveUser(HashMap<String, String> params) {
        return getAUserData(params);
    }

    public LiveData<Optional<Response<JsonResponse>>> createNewUser(Models.NewUserForm form) {
        return createNewUserMutable(form);
    }

    public LiveData<Optional<Response<JsonResponse>>> updateAUser(String uid, Models.UserUpdateForm form) {
        return updateUserMutable(uid, form);
    }

    public LiveData<Optional<Response<LoginResponse>>> getToken(Models.UsernameAndPasswordAuthenticationRequest request) {
        return getNewAccessToken(request);
    }

    public LiveData<Boolean> getVerificationStatus(String uid) {
        return checkVerification(uid);
    }

    public LiveData<Boolean> refreshAccessToken() {
        return refreshToken();
    }

    public LiveData<Optional<JsonResponse>> getRoles() {
        return getAllRoles();
    }

    /*public LiveData<Optional<JsonResponse>> saveNewCart(Models.Cart cart) {
        return saveCart(cart);
    }


    public LiveData<Optional<JsonResponse>> updateACart(Models.Cart cart) {
        return updateCart(cart);
    }

    public LiveData<Optional<JsonResponse>> deleteACart(String cartId) {
        return deleteCart(cartId);
    }


    public LiveData<Optional<JsonResponse>> getMyCart() {
        return getUserCart();
    }
*/
}
