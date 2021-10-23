package com.victoria.foodconnect.pages.welcome;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.adapter.TutorialVpAdapter;
import com.victoria.foodconnect.databinding.ActivityTutorialBinding;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.admin.AdminActivity;
import com.victoria.foodconnect.pages.beneficiary.BeneficiaryActivity;
import com.victoria.foodconnect.pages.donor.DonorActivity;
import com.victoria.foodconnect.pages.seller.SellerActivity;
import com.victoria.foodconnect.pages.transporter.TransporterActivity;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator3;

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
}