package com.victoria.foodconnect.globals.userDb;

import static com.victoria.foodconnect.globals.GlobalRepository.application;
import static com.victoria.foodconnect.globals.GlobalRepository.userApi;
import static com.victoria.foodconnect.globals.GlobalVariables.EMAIL_ADDRESS;
import static com.victoria.foodconnect.globals.GlobalVariables.UID;
import static com.victoria.foodconnect.globals.GlobalVariables.USER_COLLECTION;
import static com.victoria.foodconnect.utils.DataOpts.getAccessToken;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.models.Models.LoginResponse;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserViewModel extends AndroidViewModel {

    public UserViewModel(@NonNull Application application) {
        super(application);
    }

    private MutableLiveData<Optional<Response<JsonResponse>>> getAllUserData() {
        MutableLiveData<Optional<Response<JsonResponse>>> userMutableLiveData = new MutableLiveData<>();

        String header = getAccessToken(application);

        System.out.println("===== header ==== "+header);

        userApi.getAllUsers(header).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                userMutableLiveData.setValue(Optional.of(response));
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

        System.out.println("===== header ==== "+header);

        userApi.getUser(params, header).enqueue(new Callback<JsonResponse>() {
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

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USER_COLLECTION).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot qs = task.getResult();
                for (int i = 0; i <= Objects.requireNonNull(qs).size() - 1; i++) {
                    List<DocumentSnapshot> ds = qs.getDocuments();
                    try {
                        emailList.add(Objects.requireNonNull(ds.get(i).get(EMAIL_ADDRESS)).toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                emailMutable.setValue(Optional.of(emailList));
            } else {
                System.out.println("No data");
                emailMutable.setValue(Optional.empty());
            }
        });

        return emailMutable;
    }

    private MutableLiveData<Optional<Response<JsonResponse>>> createNewUserMutable(Models.NewUserForm form) {
        MutableLiveData<Optional<Response<JsonResponse>>> mutableLiveData = new MutableLiveData<>();

        userApi.authenticateUser(form).enqueue(new Callback<JsonResponse>() {
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

    private MutableLiveData<Optional<Response<LoginResponse>>> getNewAccessToken(Models.UsernameAndPasswordAuthenticationRequest request) {
        MutableLiveData<Optional<Response<LoginResponse>>> mutableLiveData = new MutableLiveData<>();

        userApi.getToken(request).enqueue(new Callback<LoginResponse>() {
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




    //expose
    public LiveData<Optional<List<String>>> getEmailList() {
        return getAllEmailList();
    }

    public LiveData<Optional<Response<JsonResponse>>> getLiveAllUsers() {
        return getAllUserData();
    }

    public LiveData<Optional<Response<JsonResponse>>> getLiveUser(HashMap<String, String> params) {
        return getAUserData(params);
    }

    public LiveData<Optional<Response<JsonResponse>>> createNewUser(Models.NewUserForm form) {
        return createNewUserMutable(form);
    }

    public LiveData<Optional<Response<LoginResponse>>> getToken(Models.UsernameAndPasswordAuthenticationRequest request) {
        return getNewAccessToken(request);
    }

}
