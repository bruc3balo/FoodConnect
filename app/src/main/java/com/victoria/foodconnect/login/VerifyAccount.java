package com.victoria.foodconnect.login;

import static com.victoria.foodconnect.SplashScreen.logout;
import static com.victoria.foodconnect.globals.GlobalRepository.application;
import static com.victoria.foodconnect.globals.GlobalRepository.userApi;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.APPLICATION_JSON;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.utils.DataOpts.clickableLink;
import static com.victoria.foodconnect.utils.DataOpts.getAccessToken;
import static com.victoria.foodconnect.utils.DataOpts.getDomainUserFromModelUser;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;
import static com.victoria.foodconnect.utils.DataOpts.proceed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.ActivityVerifyAccountBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.Optional;

import io.vertx.core.json.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyAccount extends AppCompatActivity {

    private ActivityVerifyAccountBinding binding;
    private Domain.AppUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userRepository.getUserLive().observe(this, appUser -> {
            if (!appUser.isPresent()) {
                logout(VerifyAccount.this);
                return;
            }

            user = appUser.get();

            binding.checkStatus.setOnClickListener(view -> checkVerificationStatus());

            binding.resendButton.setOnClickListener(view -> sendPersonalVerificationEmail());

            binding.newInfoTv.setText("Check email for verification");

        });

        binding.logoutButton.setOnClickListener(view -> logout(VerifyAccount.this));

        outProgress();

        setWindowColors(this);
    }

    private void sendPersonalVerificationEmail() {
        inProgress();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String info = "Check " + user.getEmail();
                    binding.newInfoTv.setText(clickableLink(info), TextView.BufferType.SPANNABLE);
                    binding.newInfoTv.setTextColor(Color.CYAN);
                    binding.newInfoTv.setOnClickListener(tv -> Toast.makeText(VerifyAccount.this, info, Toast.LENGTH_SHORT).show());
                } else {
                    String info = "Failed to send verification email to"+ user.getEmail();
                    binding.newInfoTv.setText(clickableLink(info), TextView.BufferType.SPANNABLE);
                    Toast.makeText(VerifyAccount.this, info, Toast.LENGTH_SHORT).show();
                }
                outProgress();
            });
        } else {
            logout(VerifyAccount.this);
        }
    }

    private void resendEmailVerification() {
        if (user != null) {
            inProgress();
            userApi.verifyEmail(user.getEmail_address(), getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
                @Override
                public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                    outProgress();

                    int code = response.code();
                    JsonResponse jsonResponse = response.body();
                    if (code == 200) {

                        if (jsonResponse == null || jsonResponse.getData() == null) {

                            binding.newInfoTv.setText("Check email for link");
                            return;
                        }

                        String url = jsonResponse.getData().toString();

                        binding.newInfoTv.setText(clickableLink(url), TextView.BufferType.SPANNABLE);
                        binding.newInfoTv.setTextColor(Color.CYAN);
                        binding.newInfoTv.setOnClickListener(tv -> {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        });


                    } else {
                        binding.newInfoTv.setText("Problem sending email verification");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                    outProgress();
                    Toast.makeText(VerifyAccount.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void checkVerificationStatus() {
        if (user != null) {
            inProgress();
            userApi.verifyEmailStatus(user.getEmail_address(), getAccessToken(application), APPLICATION_JSON).enqueue(new Callback<JsonResponse>() {
                @Override
                public void onResponse(@NonNull Call<JsonResponse> call, @NonNull Response<JsonResponse> response) {
                    outProgress();

                    JsonResponse jsonResponse = response.body();

                    if (jsonResponse == null) {
                        Toast.makeText(VerifyAccount.this, "Failed to get response", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (jsonResponse.isHas_error()) {
                        Toast.makeText(VerifyAccount.this, "Error getting verification status", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (jsonResponse.getData() == null) {
                        String info = "Check email for verification or click resend";
                        binding.newInfoTv.setText(info);
                        binding.newInfoTv.setTextColor(Color.RED);
                        Toast.makeText(VerifyAccount.this,info , Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ObjectMapper mapper = getObjectMapper();

                    try {
                        JsonObject userJson = new JsonObject(mapper.writeValueAsString(jsonResponse.getData()));

                        //save user to offline db
                        Models.AppUser firebaseDbUser = mapper.readValue(userJson.toString(), Models.AppUser.class);

                        userRepository.insert(getDomainUserFromModelUser(firebaseDbUser));

                        System.out.println("USER VERIFIED STATUS IS " + firebaseDbUser.getVerified());
                        System.out.println("USER VERIFIED IS " + getObjectMapper().writeValueAsString(firebaseDbUser));

                        Thread.sleep(2000);

                        if (firebaseDbUser.getVerified()) {
                            proceed(VerifyAccount.this);
                        } else {
                            String info = "You have not verified your account";
                            binding.newInfoTv.setText(info);
                            binding.newInfoTv.setTextColor(Color.RED);
                            Toast.makeText(VerifyAccount.this, info , Toast.LENGTH_SHORT).show();
                        }
                    } catch (JsonProcessingException | InterruptedException e) {

                        if (e instanceof JsonProcessingException) {
                            Toast.makeText(VerifyAccount.this, "Problem mapping user data", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }

                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonResponse> call, @NonNull Throwable t) {
                    Toast.makeText(VerifyAccount.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.newInfoTv.setText(t.getMessage());
                    binding.newInfoTv.setTextColor(Color.RED);
                    outProgress();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkVerificationStatus();
    }

    private void inProgress() {
        binding.resendButton.setEnabled(false);
        binding.checkStatus.setEnabled(false);
        binding.logoutButton.setEnabled(false);
        inSpinnerProgress(binding.pb,null);
    }

    private void outProgress() {
        binding.resendButton.setEnabled(true);
        binding.checkStatus.setEnabled(true);
        binding.logoutButton.setEnabled(true);
        outSpinnerProgress(binding.pb,null);
    }

}