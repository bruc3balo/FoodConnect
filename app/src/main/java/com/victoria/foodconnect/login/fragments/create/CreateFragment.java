package com.victoria.foodconnect.login.fragments.create;


import static com.victoria.foodconnect.SplashScreen.logout;
import static com.victoria.foodconnect.globals.GlobalRepository.application;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.ACCESS_TOKEN;
import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.globals.GlobalVariables.PASSWORD;
import static com.victoria.foodconnect.globals.GlobalVariables.USERNAME;
import static com.victoria.foodconnect.globals.GlobalVariables.USER_COLLECTION;
import static com.victoria.foodconnect.login.LoginActivity.loginPb;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.utils.DataOpts.clickableLink;
import static com.victoria.foodconnect.utils.DataOpts.editSp;
import static com.victoria.foodconnect.utils.DataOpts.getDomainUserFromModelUser;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;
import static com.victoria.foodconnect.utils.DataOpts.getSp;
import static com.victoria.foodconnect.utils.DataOpts.proceed;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.RoleDialogBinding;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.login.VerifyAccount;
import com.victoria.foodconnect.models.AppRolesEnum;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.vertx.core.json.JsonObject;


public class CreateFragment extends Fragment {

    private Models.NewUserForm newUserForm;
    private final LinkedList<String> emailList = new LinkedList<>();
    private final LinkedList<String> usernameList = new LinkedList<>();
    private final LinkedList<String> mobileList = new LinkedList<>();
    private Button createB;
    private String role = HY;


    public CreateFragment() {
        // Required empty public constructor
    }

    public static CreateFragment newInstance() {
        return new CreateFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_create, container, false);

        createB = v.findViewById(R.id.registerUserButton);
        outSpinnerProgress(loginPb, createB);

        EditText namesField = v.findViewById(R.id.namesField);
        EditText userNameField = v.findViewById(R.id.usernameField);
        EditText emailAddressField = v.findViewById(R.id.emailAddressField);
        EditText passwordF = v.findViewById(R.id.passwordF);
        EditText cPasswordF = v.findViewById(R.id.cPasswordF);
        EditText phoneNumberField = v.findViewById(R.id.phoneNumberField);

        createB.setOnClickListener(view -> {

            if (validateForm(userNameField, namesField, emailAddressField, phoneNumberField, passwordF, cPasswordF)) {
                showRoleDialog();
            }

        });

        populateList();


        return v;
    }

    private void authNewUser() {
        inSpinnerProgress(loginPb, createB);

        new ViewModelProvider(this).get(UserViewModel.class).createNewUser(newUserForm).observe(getViewLifecycleOwner(), jsonResponseResponse -> {

            if (!jsonResponseResponse.isPresent()) {
                inSpinnerProgress(loginPb, createB);
                Toast.makeText(requireContext(), "Failed to send request", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonResponse response = jsonResponseResponse.get().body();

            if (response == null) {
                Toast.makeText(requireContext(), "Something went wrong creating account", Toast.LENGTH_SHORT).show();
                outSpinnerProgress(loginPb, createB);
                return;
            }

            if (response.isHas_error()) {
                Toast.makeText(requireContext(), response.getApi_code_description(), Toast.LENGTH_SHORT).show();
                inSpinnerProgress(loginPb, createB);
                return;
            }

            if (response.getData() == null) {
                Toast.makeText(requireContext(), "No user data", Toast.LENGTH_SHORT).show();
                inSpinnerProgress(loginPb, createB);
                return;
            }

            ObjectMapper mapper = getObjectMapper();

            try {
                JsonObject userJson = new JsonObject(mapper.writeValueAsString(response.getData()));

                //save user to offline db
                Models.AppUser firebaseDbUser = mapper.readValue(userJson.toString(), Models.AppUser.class);

                System.out.println(mapper.writeValueAsString(firebaseDbUser));

                if (firebaseDbUser == null) {
                    Toast.makeText(requireContext(), "Failed to get user data", Toast.LENGTH_SHORT).show();
                    inSpinnerProgress(loginPb, createB);
                    return;
                }

                userRepository.insert(getDomainUserFromModelUser(firebaseDbUser));

                Thread.sleep(2000);

                loginUserToAPi();

            } catch (JsonProcessingException | InterruptedException e) {
                inSpinnerProgress(loginPb, createB);
                if (e instanceof JsonProcessingException) {
                    Toast.makeText(requireContext(), "Problem mapping user data", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                e.printStackTrace();
            }

        });


    }

    private void loginUserToAPi() {
        inSpinnerProgress(loginPb,createB);
        new ViewModelProvider(this).get(UserViewModel.class).getToken(new Models.UsernameAndPasswordAuthenticationRequest(newUserForm.getUsername(), newUserForm.getPassword())).observe(requireActivity(), loginResponseResponse -> {
            if (!loginResponseResponse.isPresent()) {
                Toast.makeText(requireContext(), "User doesn't exists or try again", Toast.LENGTH_SHORT).show();
                outSpinnerProgress(loginPb,createB);
                return;
            }


            Models.LoginResponse response = loginResponseResponse.get().body();

            if (loginResponseResponse.get().code() == 403) {
                Toast.makeText(requireContext(), "User doesn't exist", Toast.LENGTH_SHORT).show();
                outSpinnerProgress(loginPb,createB);
                return;
            } else if (loginResponseResponse.get().code() == 401) {
                Toast.makeText(requireContext(), "Forbidden", Toast.LENGTH_SHORT).show();
                outSpinnerProgress(loginPb,createB);
                return;
            }

            if (response == null) {
                Toast.makeText(requireContext(), "Failed to get access", Toast.LENGTH_SHORT).show();
                outSpinnerProgress(loginPb,createB);
                return;
            }

            Map<String, String> credentials = new HashMap<>();
            credentials.put(ACCESS_TOKEN, response.getAccess_token());
            credentials.put(PASSWORD, newUserForm.getPassword());
            credentials.put(USERNAME, newUserForm.getUsername());

            editSp(USER_COLLECTION, credentials, application);

            signInUser(newUserForm.getEmail_address(), Objects.requireNonNull(getSp(USER_COLLECTION, application).get(PASSWORD)).toString());
        });

    }

    private void signInUser(String username, String password) {
        inSpinnerProgress(loginPb,createB);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(username, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendPersonalVerificationEmail();
            } else {
                Toast.makeText(requireContext(), Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                outSpinnerProgress(loginPb,createB);
            }
        });
    }


    private void sendPersonalVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Toast.makeText(requireContext(), Optional.ofNullable(task.getException() != null ? task.getException().getLocalizedMessage() : "Failed").orElse("Failed to send verification email"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Verification email sent successfully", Toast.LENGTH_SHORT).show();
                }
                proceed(requireActivity());
            });
        }
    }


    private void showRoleDialog() {
        Dialog d = new Dialog(requireContext());
        RoleDialogBinding roleDialogBinding = RoleDialogBinding.inflate(getLayoutInflater());
        d.setContentView(roleDialogBinding.getRoot());
        d.setCancelable(false);
        d.setCanceledOnTouchOutside(false);

        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button confirmRoleButton = roleDialogBinding.confirmRoleButton;
        RadioGroup roleRadioGroup = roleDialogBinding.roleRadioGroup;
        Button neverMindButton = roleDialogBinding.cancelButton;

        d.show();

        roleRadioGroup.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (checkedId == roleDialogBinding.beneficiaryRadio.getId()) {
                role = AppRolesEnum.valueOf("ROLE_BUYER").name();
            } else if (checkedId == roleDialogBinding.certifiedAuthorityRadio.getId()) {
                role = AppRolesEnum.valueOf("ROLE_DONOR").name();
            } else if (checkedId == roleDialogBinding.sellerRadio.getId()) {
                role = AppRolesEnum.valueOf("ROLE_SELLER").name();
            } else if (checkedId == roleDialogBinding.transporterRadio.getId()) {
                role = AppRolesEnum.valueOf("ROLE_TRANSPORTER").name();
            }

            newUserForm.setRole(role);
        });

        confirmRoleButton.setOnClickListener(view -> {
            if (role.equals(HY)) {
                Toast.makeText(requireContext(), "Pick a role", Toast.LENGTH_SHORT).show();
            } else {
                d.dismiss();
                authNewUser();
            }
        });
        neverMindButton.setOnClickListener(view -> d.dismiss());
    }

    private boolean validateForm(EditText username, EditText name, EditText email, EditText phone, EditText password, EditText cPassword) {
        boolean valid = false;
        if (username.getText().toString().isEmpty()) {
            username.setError("Required");
            username.requestFocus();
        } else if (usernameList.contains(username.getText().toString())) {
            username.setError("Username already taken");
            username.requestFocus();
        } else if (name.getText().toString().isEmpty()) {
            name.requestFocus();
            name.setError("Required");
        } else if (email.getText().toString().isEmpty()) {
            email.requestFocus();
            email.setError("Required");
        } else if (!email.getText().toString().contains("@")) {
            email.requestFocus();
            email.setError("Invalid email");
        } else if (emailList.contains(email.getText().toString())) {
            email.requestFocus();
            email.setError("Email already taken");
        } else if (phone.getText().toString().isEmpty()) {
            phone.requestFocus();
            phone.setError("Required");
        } else if (!phone.getText().toString().startsWith("+254")) {
            phone.requestFocus();
            phone.setError("Wrong phone format must start with +254");
            if (phone.getText().toString().length() > 5) {
                phone.setSelection(3);
            } else {
                phone.setSelection(phone.getText().toString().length());
            }
        } else if (phone.getText().toString().length() < 12) {
            phone.requestFocus();
            phone.setError("Phone is to have 12 digits");
        } else if (mobileList.contains(phone.getText().toString())) {
            phone.setError("Phone number is already registered");
            phone.requestFocus();
        } else if (password.getText().toString().length() < 6) {
            password.requestFocus();
            password.setError("Must not be less than 12 digits");
        } else if (!cPassword.getText().toString().equals(password.getText().toString())) {
            password.requestFocus();
            password.setError("Passwords don't match");
        } else {
            newUserForm = new Models.NewUserForm(name.getText().toString(), username.getText().toString(), email.getText().toString(), password.getText().toString(), phone.getText().toString(), HY, HY, role);
            valid = true;
        }
        return valid;
    }


    private void populateList() {
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getEmailList().observe(getViewLifecycleOwner(), emails -> {
            if (!emails.isPresent()) {
                return;
            }

            if (!emails.get().isEmpty()) {
                emailList.clear();
                emailList.addAll(emails.get());
            }
        });

        userViewModel.getUsernames().observe(getViewLifecycleOwner(), usernames -> {
            if (!usernames.isPresent()) {
                return;
            }

            if (!usernames.get().isEmpty()) {
                usernameList.clear();
                usernameList.addAll(usernames.get());
            }
        });

        userViewModel.getPhoneNumbers().observe(getViewLifecycleOwner(), mobile -> {
            if (!mobile.isPresent()) {
                return;
            }

            if (!mobile.get().isEmpty()) {
                mobileList.clear();
                mobileList.addAll(mobile.get());
            }
        });
    }


    public static void setAnimatedBg(ViewGroup viewGroup) {
        AnimationDrawable animationDrawable = (AnimationDrawable) viewGroup.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}