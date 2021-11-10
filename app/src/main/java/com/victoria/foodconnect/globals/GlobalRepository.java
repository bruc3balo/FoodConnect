package com.victoria.foodconnect.globals;

import static com.victoria.foodconnect.globals.GlobalVariables.API_URL;
import static com.victoria.foodconnect.globals.GlobalVariables.CONTEXT_URL;

import android.annotation.SuppressLint;
import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.victoria.foodconnect.globals.productDb.ProductApi;
import com.victoria.foodconnect.globals.userDb.UserApi;
import com.victoria.foodconnect.globals.userDb.UserRepository;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;


public class GlobalRepository extends Application {

    public static UserRepository userRepository;
    public static Application application;
    @SuppressLint("StaticFieldLeak")
    public static FirebaseFirestore firebaseFirestore;
    public static UserApi userApi;
    public static ProductApi productApi;
    public static boolean initialized = false;

    public GlobalRepository() {

    }

    public static void init(Application application) {
        if (!initialized) {

            //room db
            GlobalRepository.application = application;
            userRepository = new UserRepository(application);

            //firebase
            FirebaseApp.initializeApp(application);

            //Firestore
            firebaseFirestore = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings.Builder settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true);
            firebaseFirestore.setFirestoreSettings(settings.build());

            //Retrofit
            Retrofit retrofit = new Retrofit.Builder().baseUrl(API_URL + CONTEXT_URL).addConverterFactory(JacksonConverterFactory.create()).build();
            userApi = retrofit.create(UserApi.class);
            productApi = retrofit.create(ProductApi.class);

            initialized = true;

            System.out.println("======================== INITIALIZED ========================");
        }
    }
}
