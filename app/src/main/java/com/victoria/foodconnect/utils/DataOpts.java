package com.victoria.foodconnect.utils;

import static android.content.Context.MODE_PRIVATE;

import static com.victoria.foodconnect.SplashScreen.logout;
import static com.victoria.foodconnect.globals.GlobalRepository.application;
import static com.victoria.foodconnect.globals.GlobalRepository.userApi;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.ACCESS_TOKEN;
import static com.victoria.foodconnect.globals.GlobalVariables.APPLICATION_JSON;
import static com.victoria.foodconnect.globals.GlobalVariables.CONTENT_TYPE_ME;
import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.globals.GlobalVariables.KEY;
import static com.victoria.foodconnect.globals.GlobalVariables.USER_COLLECTION;
import static com.victoria.foodconnect.globals.userDb.UserViewModel.refreshStaticToken;


import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.victoria.foodconnect.databinding.VerificationLayoutBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.login.VerifyAccount;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.admin.AdminActivity;
import com.victoria.foodconnect.pages.beneficiary.BeneficiaryActivity;
import com.victoria.foodconnect.pages.donor.DonorActivity;
import com.victoria.foodconnect.pages.seller.SellerActivity;
import com.victoria.foodconnect.pages.transporter.TransporterActivity;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import io.vertx.core.json.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataOpts {

    public static JWT decodeToken (String token) {
        try {
            return new JWT(token);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ObjectMapper getObjectMapper () {
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

    public static Map<String, String> getContentType() {
        Map<String, String> contentType = new HashMap<>();
        contentType.put(CONTENT_TYPE_ME, APPLICATION_JSON);
        return contentType;
    }

    public static Domain.AppUser getDomainUserFromModelUser(Models.AppUser user) {
        return new Domain.AppUser(user.getUid(), user.getId_number(), user.getPhone_number(), user.getBio(), user.getEmail_address(), user.getNames(), user.getUsername(), user.getRole().getName(), user.getCreated_at(), user.getUpdated_at(), user.getDeleted(), user.getDisabled(), user.getTutorial(), user.getVerified(), user.getLast_known_location(), user.getPassword(),user.getProfile_picture());
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
                    refreshStaticToken().observe((LifecycleOwner) activity, success -> {
                        if (success) {
                            goToAdminPage(activity);
                        } else {
                            logout(activity);
                        }
                    });

                    break;

                case "ROLE_TRANSPORTER":

                    if (!appUser.get().isVerified()) {
                        goToVerifyPage(activity);
                        return;
                    }

                    refreshStaticToken().observe((LifecycleOwner) activity, success -> {
                        if (success) {
                            goToTransporterProviderPage(activity);
                        } else {
                            logout(activity);
                        }
                    });


                    break;

                case "ROLE_DONOR":
                    /*if (!appUser.get().isVerified()) {
                        goToVerifyPage(activity);
                        return;
                    }*/

                    refreshStaticToken().observe((LifecycleOwner) activity, success -> {
                        if (success) {
                            goToDonorPage(activity);
                        } else {
                            logout(activity);
                        }
                    });

                    break;

                default:
                case "ROLE_BUYER":
                    if (!appUser.get().isVerified()) {
                        goToVerifyPage(activity);
                        return;
                    }

                    refreshStaticToken().observe((LifecycleOwner) activity, success -> {
                        if (success) {
                            goToBuyerPage(activity);
                        } else {
                            logout(activity);
                        }
                    });

                    break;

                case "ROLE_SELLER":
                    if (!appUser.get().isVerified()) {
                        goToVerifyPage(activity);
                        return;
                    }

                    refreshStaticToken().observe((LifecycleOwner) activity, success -> {
                        if (success) {
                            goToSellerPage(activity);
                        } else {
                            logout(activity);
                        }
                    });

                    break;


                   /*  case "ROLE_DISTRIBUTOR":
                goToDistributorPage();
                break;*/
            }

        });
    }

    private static void goToVerifyPage(Activity activity) {
        activity.startActivity(new Intent(activity, VerifyAccount.class));
        activity.finish();
    }

    public static SpannableStringBuilder clickableLink(String link) {
        SpannableStringBuilder underlined = new SpannableStringBuilder(link);
        underlined.setSpan(new UnderlineSpan(), 0, link.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return underlined;
    }

    public static void goToAdminPage(Activity activity) {
        activity.startActivity(new Intent(activity, AdminActivity.class));
        activity.finish();
    }

    public static void goToBuyerPage(Activity activity) {
        activity.startActivity(new Intent(activity, BeneficiaryActivity.class));
        activity.finish();
    }

    public static void goToSellerPage(Activity activity) {
        activity.startActivity(new Intent(activity, SellerActivity.class));
        activity.finish();
    }

     /*   private void goToDistributorPage() {
        startActivity(new Intent(this, .class));
        finish();
    }*/

    public static void goToTransporterProviderPage(Activity activity) {
        activity.startActivity(new Intent(activity, TransporterActivity.class));
        activity.finish();
    }

    public static void goToDonorPage(Activity activity) {
        activity.startActivity(new Intent(activity, DonorActivity.class));
        activity.finish();
    }

    public static boolean doIHavePermission(String permission, Activity activity) {
        return ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_DENIED;
    }

    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public static String getStringFromList(LinkedList<String> specialities) {
        if (specialities.isEmpty()) {
            return "";
        } else {
            StringBuilder specString = new StringBuilder();
            for (String spec : specialities) {
                specString.append(',').append(spec);
            }
            return specString.substring(1);
        }
    }

    public static LinkedList<String> getListFromString(String specialityString) {
        if (specialityString.isEmpty() || specialityString == null) {
            return new LinkedList<>();
        } else {
            LinkedList<String> specialities = new LinkedList<>();
            String[] a = specialityString.split(",");
            Collections.addAll(specialities, a);
            return specialities;
        }
    }

    public static SpannableStringBuilder getUnderlinedSpannableBuilder(String s) {
        SpannableStringBuilder content = new SpannableStringBuilder(s);
        content.setSpan(new UnderlineSpan(), 0, s.length(), 0);
        return content;
    }

    public static String getStringFromMap(LinkedHashMap<String,String> map) {
        if (!map.isEmpty()) {
            StringBuilder rs = new StringBuilder();

            map.forEach((day,time)-> {
                String spec = day.concat(KEY).concat(time);
                rs.append(',').append(spec);
            });

            return rs.substring(1);
        } else {
            return "";
        }
    }

    public static LinkedHashMap<String,String> getMapFromString(String s) {
        if (s == null || s.isEmpty()) {
            return new LinkedHashMap<>();
        } else {
            LinkedHashMap<String,String> workingHoursMap = new LinkedHashMap<>();
            String[] a = s.split(",");

            for (String item : a) {
                String key = item.split("\\^")[0];
                String val = item.split("\\^")[1];
                workingHoursMap.put(key,val);
            }

            // Collections.addAll(specialities, a);
            return workingHoursMap;
        }
    }

    public static SpannableString getBoldSpannable(String normal, String bold) {
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        int end = normal.length() + bold.length();

        SpannableString farmNameFormatted = new SpannableString(normal.concat(bold));
        farmNameFormatted.setSpan(boldSpan, normal.length(), end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return farmNameFormatted;
    }

}
