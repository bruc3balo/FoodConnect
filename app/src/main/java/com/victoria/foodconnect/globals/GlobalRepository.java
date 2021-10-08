package com.victoria.foodconnect.globals;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.victoria.foodconnect.globals.userDb.UserRepository;


public class GlobalRepository extends Application {

    public static UserRepository userRepository;
    public static Application application;
    public static boolean initialized = false;

    public GlobalRepository() {

    }

    public static void init(Application application) {
        if (!initialized) {
            userRepository = new UserRepository(application);
            GlobalRepository.application = application;
            FirebaseApp.initializeApp(application);
        }
    }
}
