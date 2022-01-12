package com.victoria.foodconnect.pages.donor;

import static com.victoria.foodconnect.SplashScreen.logout;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.globals.GlobalVariables.MEDIA_TYPE;
import static com.victoria.foodconnect.globals.GlobalVariables.PROFILE_PICTURE;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.STORAGE_PERMISSION_CODE;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.storagePermissions;
import static com.victoria.foodconnect.utils.DataOpts.doIHavePermission;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.makeramen.roundedimageview.RoundedImageView;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.ActivityDonorProfileBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.seller.AddNewProduct;
import com.victoria.foodconnect.service.UploadPictureService;
import com.victoria.foodconnect.utils.DataOpts;


public class DonorProfile extends AppCompatActivity {

    private ActivityDonorProfileBinding binding;
    private RoundedImageView profilePic;
    private ActivityResultLauncher<String> getProfilePictureContent;
    private Domain.AppUser user;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDonorProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getProfilePictureContent = registerForActivityResult(new ActivityResultContracts.GetContent(), this::postProfilePicture);


        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        profilePic = binding.donorDp;
        ImageButton changeDp = binding.changeDp;
        changeDp.setOnClickListener(v -> getProfileImage());

        userRepository.getUserLive().observe(this, appUser -> {
            if (!appUser.isPresent()) {
                Toast.makeText(DonorProfile.this, "Failed to get user data", Toast.LENGTH_SHORT).show();
                return;
            }

            user = appUser.get();

            if (!user.getProfile_picture().equals(HY)) {
                Glide.with(DonorProfile.this).load(user.getProfile_picture()).into(profilePic);
            } else {
                Glide.with(DonorProfile.this).load(getDrawable(R.drawable.ic_image)).into(profilePic);
            }

            if (appUser.get().getProfile_picture().equals(HY)) {
                binding.donorStatus.setTextColor(Color.RED);
                binding.donorStatus.setText("You need to upload your picture to be verified .\n Click here to upload your picture");
            } else {
                if (appUser.get().isVerified()) {
                    binding.donorStatus.setTextColor(Color.GREEN);
                    binding.donorStatus.setText("Verified");
                } else {
                    binding.donorStatus.setTextColor(Color.DKGRAY);
                    binding.donorStatus.setText("Pending verification");
                }
            }

            toolbar.setTitle(user.getUsername()+"'s profile");
            toolbar.setSubtitle(user.getNames());

        });

        binding.logoutButton.setOnClickListener(v -> logout(DonorProfile.this));

        setWindowColors(this);


    }

    private void postProfilePicture(Uri uri) {
        if (uri != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    profilePic.setImageURI(uri);
                    changeProfilePicture(new Models.UserUpdateForm(uri.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 1000);
        } else {
            Toast.makeText(this, "Profile image request empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void changeProfilePicture(Models.UserUpdateForm form) {
        startService(new Intent(DonorProfile.this, UploadPictureService.class).putExtra(PROFILE_PICTURE, form).putExtra(MEDIA_TYPE, PROFILE_PICTURE));
    }

    private void getProfileImage() {
        if (doIHavePermission(storagePermissions[0], DonorProfile.this) && doIHavePermission(storagePermissions[1], DonorProfile.this)) {
            getProfilePictureContent.launch("image/*");
        } else {
            DataOpts.requestPermissions(this, storagePermissions, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(DonorProfile.this, "Storage Permission Granted. You May proceed Now", Toast.LENGTH_SHORT).show();
                getProfileImage();
            } else {
                Toast.makeText(DonorProfile.this, "Storage Permission Denied. If you denied this you need to allow from settings", Toast.LENGTH_SHORT).show();
            }
        }
    }

}