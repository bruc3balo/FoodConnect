package com.victoria.foodconnect.pages.welcome;

import static com.victoria.foodconnect.SplashScreen.logout;
import static com.victoria.foodconnect.globals.GlobalRepository.application;
import static com.victoria.foodconnect.globals.GlobalRepository.userApi;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.UID;
import static com.victoria.foodconnect.utils.DataOpts.getAccessToken;
import static com.victoria.foodconnect.utils.DataOpts.getDomainUserFromModelUser;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;
import static com.victoria.foodconnect.utils.DataOpts.proceed;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.viewpager2.widget.ViewPager2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.adapter.TutorialVpAdapter;
import com.victoria.foodconnect.databinding.ActivityTutorialBinding;
import com.victoria.foodconnect.databinding.EmailVerificationDialogBinding;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.admin.AdminActivity;
import com.victoria.foodconnect.pages.beneficiary.BeneficiaryActivity;
import com.victoria.foodconnect.pages.donor.DonorActivity;
import com.victoria.foodconnect.pages.seller.SellerActivity;
import com.victoria.foodconnect.pages.transporter.TransporterActivity;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import io.vertx.core.json.JsonObject;
import me.relex.circleindicator.CircleIndicator3;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WelcomeActivity extends AppCompatActivity {

    private ActivityTutorialBinding tutorialBinding;
    private final ArrayList<Models.TutorialModel> tutorialList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tutorialBinding = ActivityTutorialBinding.inflate(getLayoutInflater());
        setContentView(tutorialBinding.getRoot());

        populateTutorials();


        ViewPager2 tutorialViewPager = tutorialBinding.tutorialViewPager;
        TutorialVpAdapter tutorialVpAdapter = new TutorialVpAdapter(this, tutorialList);
        tutorialViewPager.setAdapter(tutorialVpAdapter);

        CircleIndicator3 indicator = tutorialBinding.tutorialIndicator;
        indicator.setViewPager(tutorialViewPager);

        // optionalA
        tutorialVpAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());

        Button next = tutorialBinding.nextButton;
        next.setOnClickListener(view -> {
            if (tutorialViewPager.getCurrentItem() != tutorialList.size() - 1) {
                tutorialViewPager.setCurrentItem(tutorialViewPager.getCurrentItem() + 1);
            } else {
                //todo update user
                goToNextPage(this, userRepository.getUser().get().getRole());
            }
        });

        setWindowColors();

    }

    private void populateTutorials() {
        Models.TutorialModel tutorialModel = new Models.TutorialModel("https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.clker.com%2Fcliparts%2F3%2Fm%2F1%2FO%2F7%2Fu%2Fsearch-icon-red-hi.png&f=1&nofb=1", "This is a solution oriented application aimed at achieving the 2nd Sustainable development goal ", "About");
        Models.TutorialModel tutorialModel1 = new Models.TutorialModel("https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fthumbs.dreamstime.com%2Fb%2Fattractive-man-sleeping-home-couch-mobile-phone-digital-tablet-pad-his-hands-young-shirt-jeans-internet-61244350.jpg&f=1&nofb=1", "It does it by linking and tracking the food distributors  within our country ", "How");
        tutorialList.add(tutorialModel);
        tutorialList.add(tutorialModel1);
    }

    private void setWindowColors() {
        getWindow().setStatusBarColor(getColor(R.color.white));
        getWindow().setNavigationBarColor(getColor(R.color.white));

    }

    public static void goToNextPage(Activity activity, String role) {
        switch (role) {
            case "ROLE_ADMIN":
            case "ROLE_ADMIN_TRAINEE":
                goToAdminPage(activity);
                break;

            case "ROLE_TRANSPORTER":
                goToTransporterProviderPage(activity);
                break;

            case "ROLE_CERTIFIED_AUTHORITY":
                goToDonorPage(activity);
                break;

          /*  case "ROLE_DISTRIBUTOR":
                goToDistributorPage();
                break;*/

            default:
            case "ROLE_BUYER":
                goToBuyerPage(activity);
                break;

            case "ROLE_SELLER":
                goToSellerPage(activity);
                break;
        }
    }

    public static void goToAdminPage(Activity activity) {
        activity.startActivity(new Intent(activity, AdminActivity.class));
        activity.finish();
    }

    public static void goToTutorialsPage(Activity activity) {
        activity.startActivity(new Intent(activity, WelcomeActivity.class));
        activity.finish();
    }

    public static void showEmailVerificationDialog(Activity activity) {
        Dialog d = new Dialog(activity);
        EmailVerificationDialogBinding binding = EmailVerificationDialogBinding.inflate(activity.getLayoutInflater());
        d.setContentView(binding.getRoot());
        d.show();
        d.setCanceledOnTouchOutside(false);
        d.setCancelable(false);

        Toolbar toolbar = binding.verificationTb;

        Button email = binding.email;
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                emailVerification(activity);
            }
        });

        ProgressBar timeout = binding.timeout;
        EditText code = binding.code;
        Button mobile = binding.mobile;
        mobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                code.setVisibility(View.VISIBLE);
                mobileVerification(activity);
            }
        });


        Button logout = binding.logout;
        logout.setOnClickListener(view -> {d.dismiss();logout(activity);});
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

    private static void mobileVerification(Activity activity) {

    }

    private static void emailVerification(Activity activity,String email) {
        userApi.verifyEmail(email,getAccessToken(application)).enqueue(new Callback<JsonResponse>() {
            @Override
            public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                Toast.makeText(activity, "Email send successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                Toast.makeText(activity, "Failed to send email verification link", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static MutableLiveData<Boolean> checkVerification(Activity activity, String uid) {

        HashMap<String, String> params = new HashMap<>();
        params.put(UID, uid);

        MutableLiveData<Boolean> result = new MutableLiveData<>();

        new ViewModelProvider((ViewModelStoreOwner) activity).get(UserViewModel.class).getLiveUser(params).observe((LifecycleOwner) activity, new Observer<Optional<Response<JsonResponse>>>() {
            @Override
            public void onChanged(Optional<Response<JsonResponse>> jsonResponseResponse) {


                if (!jsonResponseResponse.isPresent()) {
                    Toast.makeText(activity, "Failed to check verification status", Toast.LENGTH_SHORT).show();
                    return;
                }

                JsonResponse response = jsonResponseResponse.get().body();

                if (response == null) {
                    Toast.makeText(activity, jsonResponseResponse.get().message(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.isHas_error()) {
                    Toast.makeText(activity, response.getApi_code_description(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.getData() == null) {
                    Toast.makeText(activity, "Check email for link", Toast.LENGTH_SHORT).show();
                    return;
                }

                ObjectMapper mapper = getObjectMapper();

                try {
                    JsonObject userJson = new JsonObject(mapper.writeValueAsString(response.getData()));

                    //save user to offline db
                    Models.AppUser firebaseDbUser = mapper.readValue(userJson.toString(), Models.AppUser.class);

                    userRepository.insert(getDomainUserFromModelUser(firebaseDbUser));

                    Thread.sleep(2000);

                    result.setValue(true);

                } catch (JsonProcessingException | InterruptedException e) {
                    if (e instanceof JsonProcessingException) {
                        Toast.makeText(activity, "Problem mapping user data", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                    e.printStackTrace();

                    result.setValue(false);

                }

            }
        });

        return result;
    }

    private static LiveData<Boolean> getVerificationStatus (Activity activity, String uid) {
        return checkVerification(activity,uid)
    }

}