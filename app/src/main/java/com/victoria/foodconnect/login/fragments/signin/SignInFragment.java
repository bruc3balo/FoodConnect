package com.victoria.foodconnect.login.fragments.signin;


import static com.victoria.foodconnect.globals.GlobalRepository.application;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.ACCESS_TOKEN;
import static com.victoria.foodconnect.globals.GlobalVariables.PASSWORD;
import static com.victoria.foodconnect.globals.GlobalVariables.UID;
import static com.victoria.foodconnect.globals.GlobalVariables.USERNAME;
import static com.victoria.foodconnect.globals.GlobalVariables.USER_COLLECTION;
import static com.victoria.foodconnect.login.LoginActivity.loginPb;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.utils.DataOpts.editSp;
import static com.victoria.foodconnect.utils.DataOpts.getDomainUserFromModelUser;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;
import static com.victoria.foodconnect.utils.DataOpts.getSp;
import static com.victoria.foodconnect.utils.DataOpts.proceed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.models.Models.LoginResponse;
import com.victoria.foodconnect.models.Models.UsernameAndPasswordAuthenticationRequest;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.vertx.core.json.JsonObject;


public class SignInFragment extends Fragment {

    private Button signInB;
    private UsernameAndPasswordAuthenticationRequest request;

    private final FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> updateUi(firebaseAuth.getCurrentUser());
    private UserViewModel userViewModel;

    public SignInFragment() {
        // Required empty public constructor
    }

    public static SignInFragment newInstance() {
        return new SignInFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_sign_in, container, false);
        signInB = v.findViewById(R.id.signInB);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        EditText username = v.findViewById(R.id.usernameF), pass = v.findViewById(R.id.passwordF);

        signInB.setOnClickListener(v1 -> {
            if (validateForm(username, pass)) {
                loginUserToAPi();
            }
        });


        outSpinnerProgress(loginPb,signInB);
        return v;
    }

    private void loginUserToAPi() {
        inSpinnerProgress(loginPb,signInB);
        userViewModel.getToken(new UsernameAndPasswordAuthenticationRequest(request.getUsername(), request.getPassword())).observe(requireActivity(), loginResponseResponse -> {
            if (!loginResponseResponse.isPresent()) {
                Toast.makeText(requireContext(), "User doesn't exists or try again", Toast.LENGTH_SHORT).show();
                outSpinnerProgress(loginPb,signInB);
                return;
            }


            LoginResponse response = loginResponseResponse.get().body();

            if (loginResponseResponse.get().code() == 403) {
                Toast.makeText(requireContext(), "User doesn't exist", Toast.LENGTH_SHORT).show();
                outSpinnerProgress(loginPb,signInB);
                return;
            } else if (loginResponseResponse.get().code() == 401) {
                Toast.makeText(requireContext(), "Forbidden", Toast.LENGTH_SHORT).show();
                outSpinnerProgress(loginPb,signInB);
                return;
            }

            if (response == null) {
                Toast.makeText(requireContext(), "Failed to get access", Toast.LENGTH_SHORT).show();
                outSpinnerProgress(loginPb,signInB);
                return;
            }

            Map<String, String> credentials = new HashMap<>();
            credentials.put(ACCESS_TOKEN, response.getAccess_token());
            //credentials.put(REFRESH_TOKEN, response.getRefresh_token());
            credentials.put(PASSWORD, request.getPassword());
            credentials.put(USERNAME, request.getUsername());

            System.out.println(response.getAccess_token());

            editSp(USER_COLLECTION, credentials, application);

            getUserDetailsToSignInUserWithFirebase(request.getUsername());
        });


    }


    private void getUserDetailsToSignInUserWithFirebase(String username) {
        HashMap<String, String> params = new HashMap<>();
        params.put(USERNAME, username);

        userViewModel.getLiveUser(params).observe(getViewLifecycleOwner(), jsonResponseResponse -> {

            if (!jsonResponseResponse.isPresent()) {
                outSpinnerProgress(loginPb,signInB);
                return;
            }

            if (jsonResponseResponse.get().code() == 403) {
                Toast.makeText(requireContext(), "Not allowed", Toast.LENGTH_SHORT).show();
                outSpinnerProgress(loginPb,signInB);
                return;
            } else if (jsonResponseResponse.get().code() == 401) {
                Toast.makeText(requireContext(), "Forbidden", Toast.LENGTH_SHORT).show();
                outSpinnerProgress(loginPb,signInB);
                return;
            }

            JsonResponse response = jsonResponseResponse.get().body();

            if (response == null) {


                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                outSpinnerProgress(loginPb,signInB);
                return;
            }

            if (response.isHas_error()) {
                Toast.makeText(requireContext(), response.getApi_code_description(), Toast.LENGTH_SHORT).show();
                outSpinnerProgress(loginPb,signInB);
                return;
            }

            if (response.getData() == null) {
                Toast.makeText(requireContext(), "No user data", Toast.LENGTH_SHORT).show();
                outSpinnerProgress(loginPb,signInB);
                return;
            }

            ObjectMapper mapper = getObjectMapper();

            try {
                JsonObject userJson = new JsonObject(mapper.writeValueAsString(response.getData()));

                //save user to offline db
                Models.AppUser firebaseDbUser = mapper.readValue(userJson.toString(), Models.AppUser.class);

                signInUser(firebaseDbUser.getEmail_address(), Objects.requireNonNull(getSp(USER_COLLECTION, application).get(PASSWORD)).toString());

            } catch (JsonProcessingException e) {
                outSpinnerProgress(loginPb,signInB);
                Toast.makeText(requireContext(), "Problem mapping user data", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        });


    }

    public boolean validateForm(EditText usernameF, EditText passwordF) {
        boolean valid = false;
        if (usernameF.getText().toString().isEmpty()) {
            usernameF.setError("required");
            usernameF.requestFocus();
        } else if (passwordF.getText().toString().isEmpty()) {
            passwordF.setError("required");
            passwordF.requestFocus();
        } else {
            request = new UsernameAndPasswordAuthenticationRequest(usernameF.getText().toString(), passwordF.getText().toString());
            valid = true;
        }
        return valid;
    }

   

    private void signInUser(String username, String password) {
        inSpinnerProgress(loginPb,signInB);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(username, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(requireContext(), "User signed in", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                outSpinnerProgress(loginPb,signInB);
            }
        });
    }

    private void updateUi(FirebaseUser user) {

        //get user data by username
        //login with email & password


        if (user != null) {


            HashMap<String, String> params = new HashMap<>();
            params.put(UID, user.getUid());

            userViewModel.getLiveUser(params).observe(getViewLifecycleOwner(), jsonResponseResponse -> {

                if (!jsonResponseResponse.isPresent()) {
                    outSpinnerProgress(loginPb,signInB);
                    return;
                }

                JsonResponse response = jsonResponseResponse.get().body();

                if (response == null) {
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    outSpinnerProgress(loginPb,signInB);
                    return;
                }

                if (response.isHas_error()) {
                    Toast.makeText(requireContext(), response.getApi_code_description(), Toast.LENGTH_SHORT).show();
                    outSpinnerProgress(loginPb,signInB);
                    return;
                }

                if (response.getData() == null) {
                    Toast.makeText(requireContext(), "No user data", Toast.LENGTH_SHORT).show();
                    outSpinnerProgress(loginPb,signInB);
                    return;
                }

                ObjectMapper mapper = getObjectMapper();

                try {
                    JsonObject userJson = new JsonObject(mapper.writeValueAsString(response.getData()));

                    //save user to offline db
                    Models.AppUser firebaseDbUser = mapper.readValue(userJson.toString(), Models.AppUser.class);

                    userRepository.insert(getDomainUserFromModelUser(firebaseDbUser));

                    Thread.sleep(2000);

                    proceed(requireActivity());

                } catch (JsonProcessingException | InterruptedException e) {
                    outSpinnerProgress(loginPb,signInB);
                    if (e instanceof JsonProcessingException) {
                        Toast.makeText(requireContext(), "Problem mapping user data", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                    e.printStackTrace();
                }

            });

        } else {
            outSpinnerProgress(loginPb,signInB);
            Toast.makeText(requireContext(), "Sign in to continue", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }


}