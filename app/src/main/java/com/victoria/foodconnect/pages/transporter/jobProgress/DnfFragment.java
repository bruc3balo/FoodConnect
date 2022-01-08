package com.victoria.foodconnect.pages.transporter.jobProgress;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.utils.DataOpts.getStringFromMap;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.victoria.foodconnect.databinding.FragmentReviewBinding;

import com.victoria.foodconnect.globals.purchaseDb.PurchaseViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.DataOpts;

import java.util.LinkedHashMap;
import java.util.Map;


public class DnfFragment extends Fragment {

    private FragmentReviewBinding binding;
    private final Models.Purchase purchase;
    private final Models.DistributionModel distribution;
    private final JobActivityProgress activity;
    private PurchaseViewModel purchaseViewModel;
    private String role;
    private final boolean dnf;


    public DnfFragment(JobActivityProgress activity, Models.Purchase purchase, Models.DistributionModel distribution,boolean dnf) {
        // Required empty public constructor
        this.purchase = purchase;
        this.distribution = distribution;
        this.activity = activity;
        this.dnf = dnf;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentReviewBinding.inflate(inflater);
        purchaseViewModel = new ViewModelProvider(this).get(PurchaseViewModel.class);

        EditText donorField = binding.donorField, transField = binding.transporterField, beneField = binding.beneficiaryField;
        ImageButton sendDonor = binding.sendBene, sendTrans = binding.sendTransporter, sendBene = binding.sendBene;

        setEditFieldAnimation(donorField, sendDonor);
        setEditFieldAnimation(transField, sendTrans);
        setEditFieldAnimation(beneField, sendBene);

        userRepository.getUserLive().observe(activity, appUser -> appUser.ifPresent(u -> setLayout(u.getRole(), distribution.getRemarks())));

        return binding.getRoot();
    }


    private void setLayout(String role, Models.Remarks remarks) {
        this.role = role;
        switch (role) {
            default:
            case "ROLE_TRANSPORTER":
                binding.donorLayout.setVisibility(View.GONE);
                binding.beneLayout.setVisibility(View.GONE);

                //create
                if (remarks == null || remarks.getTransporter() == null) {

                    binding.transporterField.setEnabled(true);
                    binding.sendTransporter.setOnClickListener(v -> {
                        Models.Remarks newRemark = new Models.Remarks();
                        newRemark.setTransporter(makeReview(binding.transporterField.getText().toString(), null));
                        newRemark.setDistribution_id(distribution.getId());
                        createReview(newRemark);
                    });

                    binding.transRating.setIsIndicator(false);
                    binding.transRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                        if (fromUser) {
                            Models.Remarks newRemark = new Models.Remarks();
                            newRemark.setDistribution_id(distribution.getId());
                            newRemark.setTransporter(makeReview(null, rating));
                            createReview(newRemark);
                            ratingBar.setIsIndicator(true);
                            ratingBar.setRating(rating);
                        }
                    });

                    return;
                }

                //update
                LinkedHashMap<String, String> transporterReview = DataOpts.getMapFromString(remarks.getTransporter());
                Map.Entry<String, String> reviewEntry = transporterReview.entrySet().stream().findFirst().orElse(null);


                String val = reviewEntry != null ? reviewEntry.getKey() : HY;
                Float transRating = val != null && !val.equals(HY) ? Float.valueOf(val) : null;
                String reviewS = reviewEntry != null ? reviewEntry.getValue() : HY;


                System.out.println("Val is "+val);

                //rating
                assert val != null;
                if (val.equals(HY)) {
                    binding.transRating.setIsIndicator(false);
                    binding.transRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                        if (fromUser) {
                            LinkedHashMap<String,String> tranR = new LinkedHashMap<>();
                            tranR.put(String.valueOf(rating),reviewS);
                            distribution.getRemarks().setTransporter(getStringFromMap(tranR));
                            updateReview(distribution.getRemarks());
                            ratingBar.setIsIndicator(true);
                            ratingBar.setRating(rating);
                        }
                    });
                } else {
                    binding.transRating.setRating(transRating);
                    binding.transRating.setIsIndicator(true);
                }

                System.out.println("ReviewS is "+reviewS);

                //text

                if (reviewS.equals(HY)) {
                    binding.transporterField.setEnabled(true);
                    binding.sendTransporter.setOnClickListener(v -> {
                        LinkedHashMap<String,String> tranR = new LinkedHashMap<>();
                        tranR.put(val,binding.transporterField.getText().toString());
                        distribution.getRemarks().setTransporter(getStringFromMap(tranR));
                        updateReview(distribution.getRemarks());
                        binding.transLayout.setVisibility(View.GONE);
                        binding.transporterReview.setVisibility(View.VISIBLE);
                        binding.transporterReview.setText(reviewS);
                    });
                } else {
                    binding.transLayout.setVisibility(View.GONE);
                    binding.transporterReview.setVisibility(View.VISIBLE);
                    binding.transporterReview.setText(reviewS);
                }

                break;

            case "ROLE_DONOR":

                binding.transLayout.setVisibility(View.GONE);
                binding.beneLayout.setVisibility(View.GONE);

                //create
                if (remarks == null || remarks.getDonor() == null) {

                    binding.donorField.setEnabled(true);

                    binding.sendDonor.setOnClickListener(v -> {
                        Models.Remarks newRemark = new Models.Remarks();
                        newRemark.setDonor(makeReview(binding.donorField.getText().toString(), null));
                        newRemark.setDistribution_id(distribution.getId());
                        createReview(newRemark);
                    });

                    binding.donorRating.setIsIndicator(false);
                    binding.donorRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                        if (fromUser) {
                            Models.Remarks newRemark = new Models.Remarks();
                            newRemark.setDistribution_id(distribution.getId());
                            newRemark.setDonor(makeReview(null, rating));
                            createReview(newRemark);
                            ratingBar.setIsIndicator(true);
                            ratingBar.setRating(rating);
                        }
                    });

                    return;
                }


                //update
                LinkedHashMap<String, String> donorReview = DataOpts.getMapFromString(remarks.getDonor());
                Map.Entry<String, String> donorReviewEntry = donorReview.entrySet().stream().findFirst().orElse(null);


                String valD = donorReviewEntry != null ? donorReviewEntry.getKey() : HY;
                Float donRating = valD != null && !valD.equals(HY) ? Float.valueOf(valD) : null;
                String reviewD = donorReviewEntry != null ? donorReviewEntry.getValue() : HY;


                System.out.println("ValD is "+valD);

                //rating
                assert valD != null;
                if (valD.equals(HY)) {
                    binding.donorRating.setIsIndicator(false);
                    binding.donorRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                        if (fromUser) {
                            LinkedHashMap<String,String> donR = new LinkedHashMap<>();
                            donR.put(String.valueOf(rating),reviewD);
                            distribution.getRemarks().setDonor(getStringFromMap(donR));
                            updateReview(distribution.getRemarks());
                            ratingBar.setIsIndicator(true);
                            ratingBar.setRating(rating);
                        }
                    });
                } else {
                    binding.donorRating.setRating(donRating);
                    binding.donorRating.setIsIndicator(true);
                }

                System.out.println("ReviewS is "+reviewD);

                //text
                if (reviewD.equals(HY)) {
                    binding.donorField.setEnabled(true);
                    binding.sendDonor.setOnClickListener(v -> {
                        LinkedHashMap<String,String> donR = new LinkedHashMap<>();
                        donR.put(valD,binding.donorField.getText().toString());
                        distribution.getRemarks().setDonor(getStringFromMap(donR));
                        updateReview(distribution.getRemarks());
                        binding.donorLayout.setVisibility(View.GONE);
                        binding.donorReview.setVisibility(View.VISIBLE);
                        binding.donorReview.setText(reviewD);
                    });
                } else {
                    binding.donorLayout.setVisibility(View.GONE);
                    binding.sendDonor.setVisibility(View.VISIBLE);
                    binding.donorReview.setText(reviewD);
                }

                break;


            case "ROLE_BUYER":
                binding.donorLayout.setVisibility(View.GONE);
                binding.transLayout.setVisibility(View.GONE);

                //create
                if (remarks == null) {

                    binding.beneficiaryField.setEnabled(true);
                    binding.sendBene.setOnClickListener(v -> {
                        Models.Remarks newRemark = new Models.Remarks();
                        newRemark.setBeneficiary(makeReview(binding.beneficiaryField.getText().toString(), null));
                        newRemark.setDistribution_id(distribution.getId());
                        createReview(newRemark);
                    });

                    binding.beneRating.setIsIndicator(false);
                    binding.beneRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                        if (fromUser) {
                            Models.Remarks newRemark = new Models.Remarks();
                            newRemark.setDistribution_id(distribution.getId());
                            newRemark.setBeneficiary(makeReview(null, rating));
                            createReview(newRemark);
                            ratingBar.setIsIndicator(true);
                            ratingBar.setRating(rating);
                        }
                    });

                    return;
                }


                //update
                LinkedHashMap<String, String> beneReview = DataOpts.getMapFromString(remarks.getBeneficiary());
                Map.Entry<String, String> beneReviewEntry = beneReview.entrySet().stream().findFirst().orElse(null);


                String valB = beneReviewEntry != null ? beneReviewEntry.getKey() : HY;
                Float benRating = valB != null && !valB.equals(HY) ? Float.valueOf(valB) : null;
                String reviewB = beneReviewEntry != null ? beneReviewEntry.getValue() : HY;


                System.out.println("ValB is "+valB);

                //rating
                assert valB != null;
                if (valB.equals(HY)) {
                    binding.beneRating.setIsIndicator(false);
                    binding.beneRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                        if (fromUser) {
                            LinkedHashMap<String,String> beneR = new LinkedHashMap<>();
                            beneR.put(String.valueOf(rating),reviewB);
                            distribution.getRemarks().setBeneficiary(getStringFromMap(beneR));
                            updateReview(distribution.getRemarks());
                            ratingBar.setIsIndicator(true);
                            ratingBar.setRating(rating);
                        }
                    });
                } else {
                    binding.beneRating.setRating(benRating);
                    binding.beneRating.setIsIndicator(true);
                }

                System.out.println("ReviewB is "+reviewB);

                //text
                if (reviewB.equals(HY)) {
                    binding.beneficiaryField.setEnabled(true);
                    binding.sendBene.setOnClickListener(v -> {
                        LinkedHashMap<String,String> benR = new LinkedHashMap<>();
                        benR.put(valB,binding.beneficiaryField.getText().toString());
                        distribution.getRemarks().setBeneficiary(getStringFromMap(benR));
                        updateReview(distribution.getRemarks());
                        binding.beneLayout.setVisibility(View.GONE);
                        binding.beneficiaryReview.setVisibility(View.VISIBLE);
                        binding.beneficiaryReview.setText(reviewB);
                    });
                } else {
                    binding.beneLayout.setVisibility(View.GONE);
                    binding.beneficiaryReview.setVisibility(View.VISIBLE);
                    binding.beneficiaryReview.setText(reviewB);
                }

                break;

        }
    }

    private void createReview(Models.Remarks remarks) {

        purchaseViewModel.createNewRemark(remarks).observe(getViewLifecycleOwner(), optionalDistributionModel -> {
            if (!optionalDistributionModel.isPresent()) {
                Toast.makeText(activity, "Failed to create review", Toast.LENGTH_SHORT).show();
                return;
            }

            optionalDistributionModel.ifPresent(u -> {
                distribution.setRemarks(u);
                setLayout(role, u);
                Toast.makeText(activity, "Review posted", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateReview(Models.Remarks remarks) {

        purchaseViewModel.updateARemark(remarks).observe(getViewLifecycleOwner(), optionalDistributionModel -> {

            if (!optionalDistributionModel.isPresent()) {
                Toast.makeText(activity, "Failed to post review", Toast.LENGTH_SHORT).show();
                return;
            }

            optionalDistributionModel.ifPresent(u -> {
                distribution.setRemarks(u);
                setLayout(role, u);
                Toast.makeText(activity, "Review posted", Toast.LENGTH_SHORT).show();
            });

        });
    }

    private boolean hasRemarks() {
        return distribution.getRemarks() != null;
    }

    private String makeReview(String text, Float rating) {
        LinkedHashMap<String, String> review = new LinkedHashMap<>();
        review.put(rating != null ? String.valueOf(rating) : HY, text != null ? text : HY);
        return getStringFromMap(review);
    }

    private void setEditFieldAnimation(final EditText field, final ImageButton button) {

        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().isEmpty()) {
                    button.setVisibility(View.GONE);
                } else {
                    button.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    button.setVisibility(View.GONE);
                } else {
                    button.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    button.setVisibility(View.GONE);
                } else {
                    button.setVisibility(View.VISIBLE);
                }
            }
        });


    }

}