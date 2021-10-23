package com.victoria.foodconnect.globals.userDb;

import static com.victoria.foodconnect.globals.GlobalVariables.EMAIL_ADDRESS;
import static com.victoria.foodconnect.globals.GlobalVariables.USER_COLLECTION;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.victoria.foodconnect.domain.Domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserViewModel extends AndroidViewModel {

    public UserViewModel(@NonNull Application application) {
        super(application);
    }

    private MutableLiveData<List<Domain.AppUser>> getAllUserData() {
        MutableLiveData<List<Domain.AppUser>> userMutableLiveData = new MutableLiveData<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USER_COLLECTION).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot qs = task.getResult();
                userMutableLiveData.setValue(Objects.requireNonNull(qs).toObjects(Domain.AppUser.class));
            } else {
                System.out.println("No data");
            }
        });

        return userMutableLiveData;
    }

    private MutableLiveData<Domain.AppUser> getAUserData(String uid) {
        MutableLiveData<Domain.AppUser> userMutableLiveData = new MutableLiveData<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USER_COLLECTION).document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Domain.AppUser user = Objects.requireNonNull(task.getResult()).toObject(Domain.AppUser.class);
                userMutableLiveData.setValue(user);
            } else {
                System.out.println("No data");
            }
        });

        return userMutableLiveData;
    }

    private MutableLiveData<List<String>> getAllEmailList() {
        MutableLiveData<List<String>> emailMutable = new MutableLiveData<>();
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
                emailMutable.setValue(emailList);
            } else {
                System.out.println("No data");
            }
        });

        return emailMutable;
    }



    //expose
    public LiveData<List<String>> getEmailList() {
        return getAllEmailList();
    }

    public LiveData<List<Domain.AppUser>> getLiveAllUsers () {
        return getAllUserData();
    }

    public LiveData<Domain.AppUser> getLiveUser(String uid) {
        return getAUserData(uid);
    }
}
