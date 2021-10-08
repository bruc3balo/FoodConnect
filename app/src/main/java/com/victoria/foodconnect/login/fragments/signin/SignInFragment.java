package com.victoria.foodconnect.login.fragments.signin;


import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.login.LoginActivity.loginPb;
import static com.victoria.foodconnect.pages.welcome.WelcomeActivity.goToNextPage;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.models.Models;

import java.util.Objects;


public class SignInFragment extends Fragment {

    private Button signInB;
    private Models.UsernameAndPasswordAuthenticationRequest request;

    private boolean isWaiting = false;

    private final FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> updateUi(firebaseAuth.getCurrentUser());


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

        outProgress(loginPb, signInB);


        EditText email = v.findViewById(R.id.emailF), pass = v.findViewById(R.id.passwordF);

        signInB.setOnClickListener(v1 -> {
            if (validateForm(email, pass)) {
                signInUser(request.getUsername(), request.getPassword());
            }
        });

        return v;
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
            request = new Models.UsernameAndPasswordAuthenticationRequest(usernameF.getText().toString(), passwordF.getText().toString());
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

    private void signInUser(String s, String s1) {
        inProgress(loginPb, signInB);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(s, s1).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(requireContext(), "User signed in", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                outProgress(loginPb, signInB);
            }
        });
    }

    private void updateUi(FirebaseUser user) {
        if (user != null) {
            UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
            userViewModel.getLiveUser(user.getUid()).observe(getViewLifecycleOwner(), appUser -> {
                if (appUser != null) {
                    userRepository.insert(appUser);
                    outProgress(loginPb, signInB);
                    goToNextPage(requireActivity(), userRepository.getUser().getRole());
                }
            });
        } else {
            outProgress(loginPb, signInB);
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