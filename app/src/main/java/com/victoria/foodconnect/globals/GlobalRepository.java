package com.victoria.foodconnect.globals;

import static com.victoria.foodconnect.globals.GlobalVariables.API_URL;
import static com.victoria.foodconnect.globals.GlobalVariables.CONTEXT_URL;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.victoria.foodconnect.globals.productDb.ProductApi;
import com.victoria.foodconnect.globals.purchaseDb.PurchaseApi;
import com.victoria.foodconnect.globals.purchaseDb.ReviewApi;
import com.victoria.foodconnect.globals.userDb.UserApi;
import com.victoria.foodconnect.globals.userDb.UserRepository;
import com.victoria.foodconnect.pages.LocationOrder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;


public class GlobalRepository extends Application {

    public static UserRepository userRepository;
    public static Application application;
    @SuppressLint("StaticFieldLeak")
    public static FirebaseFirestore firebaseFirestore;
    public static UserApi userApi;
    public static ProductApi productApi;
    public static PurchaseApi purchaseApi;
    public static ReviewApi reviewApi;
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

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            //Retrofit
            Retrofit retrofit = new Retrofit.Builder().baseUrl(API_URL + CONTEXT_URL).client(client).addConverterFactory(JacksonConverterFactory.create()).build();
            userApi = retrofit.create(UserApi.class);
            productApi = retrofit.create(ProductApi.class);
            purchaseApi = retrofit.create(PurchaseApi.class);
            reviewApi = retrofit.create(ReviewApi.class);

            initialized = true;

            application.startService(new Intent(application, LocationOrder.class));


            System.out.println("======================== INITIALIZED ========================");
        }
    }
}
