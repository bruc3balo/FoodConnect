package com.victoria.foodconnect.login.fragments.create;


import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.USERS;
import static com.victoria.foodconnect.login.LoginActivity.loginPb;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.RoleDialogBinding;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.models.AppRolesEnum;
import com.victoria.foodconnect.pages.welcome.WelcomeActivity;

import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;


public class CreateFragment extends Fragment {

    private Domain.AppUser newUser;
    private final LinkedList<String> emailList = new LinkedList<>();
    private Button createB;
    private String role = "ROLE_BUYER";

    private boolean isWaiting = false;

    private boolean emailValid = false;

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

        emailAddressField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().isEmpty()) {
                    if (!emailList.isEmpty()) {
                        if (emailList.contains(charSequence.toString())) {
                            emailAddressField.setError("Email already taken");
                            emailAddressField.requestFocus();
                            emailValid = false;
                        } else {
                            emailValid = true;
                        }
                    } else {
                        emailValid = true;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        createB.setOnClickListener(view -> {
            if (emailValid) {
                if (validateForm(userNameField, namesField, emailAddressField, phoneNumberField, passwordF, cPasswordF)) {
                    showRoleDialog();
                }
            } else {
                Toast.makeText(requireContext(), "Email not valid", Toast.LENGTH_SHORT).show();
            }
        });


        populateEmailList();


        return v;
    }

    private void authNewUser(String s, String s1) {
        inProgress(loginPb, createB);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(s, s1).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                newUser.setId(Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getUser()).getUid());
                Toast.makeText(requireContext(), newUser.getUsername() + " Created", Toast.LENGTH_SHORT).show();
                saveUserDetails(newUser);
            } else {
                outProgress(loginPb, createB);
                Toast.makeText(requireContext(), Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
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

            newUser.setRole(role);
        });

        confirmRoleButton.setOnClickListener(view -> {
            if (role.equals("")) {
                Toast.makeText(requireContext(), "Pick a role", Toast.LENGTH_SHORT).show();
            } else {
                d.dismiss();
                authNewUser(newUser.getEmail_address(), newUser.getPassword());
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
        } else if (!phone.getText().toString().startsWith("+254")) {
            phone.requestFocus();
            phone.setError("Wrong phone format must start with +254");
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

            newUser = new Domain.AppUser(name.getText().toString(), username.getText().toString(), email.getText().toString(), password.getText().toString(), phone.getText().toString(), new Date().toString(), new Date().toString(), false, false, role);
            valid = true;
        }
        return valid;
    }

    private void inProgress(ProgressBar progressBar, Button button) {
        progressBar.setVisibility(View.VISIBLE);
        button.setEnabled(false);
        isWaiting = true;
    }

    private void outProgress(ProgressBar progressBar, Button button) {
        progressBar.setVisibility(View.GONE);
        button.setEnabled(true);
        isWaiting = false;
    }

    private void populateEmailList() {
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getEmailList().observe(getViewLifecycleOwner(), strings -> {
            if (!isWaiting) {
                outProgress(loginPb, createB);
                if (!strings.isEmpty()) {
                    emailList.clear();
                    emailList.addAll(strings);
                }
            }
        });
    }

    private void saveUserDetails(Domain.AppUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS).document(user.getId()).set(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(requireContext(), "User details saved", Toast.LENGTH_SHORT).show();
                userRepository.insert(newUser);
                new Handler(Looper.myLooper()).postDelayed(this::showWelcomeScreen, 2000);
            } else {
                outProgress(loginPb, createB);
                Toast.makeText(requireContext(), Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showWelcomeScreen() {
        startActivity(new Intent(requireContext(), WelcomeActivity.class));
        requireActivity().finish();
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