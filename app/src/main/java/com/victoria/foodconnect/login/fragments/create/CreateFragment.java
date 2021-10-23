package com.victoria.foodconnect.login.fragments.create;


import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.login.LoginActivity.loginPb;
import static com.victoria.foodconnect.utils.DataOpts.getDomainUserFromModelUser;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;
import static com.victoria.foodconnect.utils.DataOpts.proceed;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.RoleDialogBinding;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.models.AppRolesEnum;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.welcome.WelcomeActivity;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.LinkedList;

import io.vertx.core.json.JsonObject;


public class CreateFragment extends Fragment {

    private Models.NewUserForm newUserForm;
    private final LinkedList<String> emailList = new LinkedList<>();
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
        outProgress(loginPb, createB);

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

        populateEmailList();


        return v;
    }

    private void authNewUser(String s, String s1) {
        inProgress(loginPb, createB);

        new ViewModelProvider(this).get(UserViewModel.class).createNewUser(newUserForm).observe(getViewLifecycleOwner(), jsonResponseResponse ->  {

            if (!jsonResponseResponse.isPresent()) {
                outProgress(loginPb, createB);
                return;
            }

            JsonResponse response = jsonResponseResponse.get().body();

            if (response == null) {
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                outProgress(loginPb, createB);
                return;
            }

            if (response.isHas_error()) {
                Toast.makeText(requireContext(), response.getApi_code_description(), Toast.LENGTH_SHORT).show();
                outProgress(loginPb, createB);
                return;
            }

            if (response.getData() == null) {
                Toast.makeText(requireContext(), "No user data", Toast.LENGTH_SHORT).show();
                outProgress(loginPb, createB);
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
                outProgress(loginPb, createB);
                if (e instanceof JsonProcessingException) {
                    Toast.makeText(requireContext(), "Problem mapping user data", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                e.printStackTrace();
            }

        });


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
                role = AppRolesEnum.valueOf("ROLE_CERTIFIED_AUTHORITY").name();
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
                authNewUser(newUserForm.getEmail_address(), newUserForm.getPassword());
            }
        });
        neverMindButton.setOnClickListener(view -> d.dismiss());
    }

    private boolean validateForm(EditText username, EditText name, EditText email, EditText phone, EditText password, EditText cPassword) {
        boolean valid = false;
        if (username.getText().toString().isEmpty()) {
            username.setError("Required");
            username.requestFocus();
        } else if (name.getText().toString().isEmpty()) {
            name.requestFocus();
            name.setError("Required");
        } else if (email.getText().toString().isEmpty()) {
            email.requestFocus();
            email.setError("Required");
        }  else if (!email.getText().toString().contains("@")) {
            email.requestFocus();
            email.setError("Invalid email");
        }  else if (emailList.contains(email.getText().toString())) {
            email.requestFocus();
            email.setError("Email already taken");
        } else if (!phone.getText().toString().isEmpty()) {
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

    private void inProgress(ProgressBar progressBar, Button button) {
        progressBar.setVisibility(View.VISIBLE);
        button.setEnabled(false);

    }

    private void outProgress(ProgressBar progressBar, Button button) {
        progressBar.setVisibility(View.GONE);
        button.setEnabled(true);
    }

    private void populateEmailList() {
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