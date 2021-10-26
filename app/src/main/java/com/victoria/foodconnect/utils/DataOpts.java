package com.victoria.foodconnect.utils;

import static android.content.Context.MODE_PRIVATE;

import static com.victoria.foodconnect.SplashScreen.logout;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.ACCESS_TOKEN;
import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.globals.GlobalVariables.USER_COLLECTION;
import static com.victoria.foodconnect.pages.welcome.WelcomeActivity.goToAdminPage;
import static com.victoria.foodconnect.pages.welcome.WelcomeActivity.goToBuyerPage;
import static com.victoria.foodconnect.pages.welcome.WelcomeActivity.goToDonorPage;
import static com.victoria.foodconnect.pages.welcome.WelcomeActivity.goToSellerPage;
import static com.victoria.foodconnect.pages.welcome.WelcomeActivity.goToTransporterProviderPage;
import static com.victoria.foodconnect.pages.welcome.WelcomeActivity.goToTutorialsPage;
import static com.victoria.foodconnect.pages.welcome.WelcomeActivity.showEmailVerificationDialog;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;

import androidx.lifecycle.LifecycleOwner;

import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.models.Models;

import java.util.Map;
import java.util.Objects;

public class DataOpts {

    public static JWT decodeToken(String token) {
        try {
            return new JWT(token);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper = mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper = mapper.findAndRegisterModules();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setVisibility(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        return mapper;
    }

    public static Map<String, ?> getSp(String name, Application application) {
        SharedPreferences sh = application.getSharedPreferences(name, MODE_PRIVATE);
        return sh.getAll();
    }

    public static void editSp(String name, Map<String, ?> sp, Application application) {
        SharedPreferences sh = application.getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor editor = sh.edit();

        sp.forEach((itemName, item) -> {
            if (item instanceof Boolean) {
                editor.putBoolean(itemName, (Boolean) item);
            } else if (item instanceof String) {
                editor.putString(itemName, (String) item);
            } else if (item instanceof Integer) {
                editor.putInt(itemName, (Integer) item);
            } else if (item instanceof Long) {
                editor.putLong(itemName, (Long) item);
            } else if (item instanceof Float) {
                editor.putFloat(itemName, (Float) item);
            }
        });

        System.out.println(" ==============  SP MAP " + sp + " ================ ");

        editor.apply();
    }

    public static void clearSp(String name, Application application) {
        SharedPreferences sh = application.getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor editor = sh.edit();

        editor.clear();

        System.out.println(" ==============  SP MAP CLEARED ================ ");

        editor.apply();
    }

    public static String getAccessToken(Application application) {

        return "Bearer " + Objects.requireNonNull(getSp(USER_COLLECTION, application).get(ACCESS_TOKEN)).toString();
    }

    public static Domain.AppUser getDomainUserFromModelUser(Models.AppUser user) {
        return new Domain.AppUser(user.getUid(), user.getId_number(), user.getPhone_number(), user.getBio(), user.getEmail_address(), user.getNames(), user.getUsername(), user.getRole().getName(), user.getCreated_at(), user.getUpdated_at(), user.getDeleted(), user.getDisabled(), user.getTutorial(), user.getVerified(),user.getLast_known_location(), user.getPassword());
    }


    public static void proceed(Activity activity) {
        userRepository.getUserLive().observe((LifecycleOwner) activity, appUser -> {
            if (!appUser.isPresent()) {
                System.out.println("COULD NOT PROCEED ... user is not present");
                logout(activity);
                return;
            }

            switch (appUser.get().getRole()) {
                case "ROLE_ADMIN":
                case "ROLE_ADMIN_TRAINEE":
                    goToAdminPage(activity);
                    break;

                case "ROLE_TRANSPORTER":

                    if (!appUser.get().isVerified()) {
                        showEmailVerificationDialog(activity);
                        return;
                    }

                    goToTransporterProviderPage(activity);

                    break;

                case "ROLE_DONOR":
                    if (!appUser.get().isVerified()) {
                        showEmailVerificationDialog(activity);
                        return;
                    }

                    goToDonorPage(activity);
                    break;

                default:
                case "ROLE_BUYER":
                    if (!appUser.get().isVerified()) {
                        showEmailVerificationDialog(activity);
                        return;
                    }

                    goToBuyerPage(activity);
                    break;

                case "ROLE_SELLER":
                    if (!appUser.get().isVerified()) {
                        showEmailVerificationDialog(activity);
                        return;
                    }

                    goToSellerPage(activity);
                    break;


                   /*  case "ROLE_DISTRIBUTOR":
                goToDistributorPage();
                break;*/
            }

        });
    }


}
