package com.victoria.foodconnect.pages.transporter.jobProgress;

import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.utils.DataOpts.getStringFromMap;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.victoria.foodconnect.databinding.FragmentReviewBinding;
import com.victoria.foodconnect.globals.purchaseDb.PurchaseViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.transporter.donationProgress.DonationProgressActivity;
import com.victoria.foodconnect.utils.DataOpts;

import java.util.LinkedHashMap;
import java.util.Map;


public class ReviewFragment extends Fragment {

    private FragmentReviewBinding binding;

    //purchase
    private Models.Purchase purchase;
    private Models.DistributionModel distribution;
    private JobActivityProgress activity;


    //donation
    private Models.Donation donation;
    private Models.DonationDistribution donationDistribution;
    private DonationProgressActivity donationActivity;

    //seller
    private Activity context;

    private PurchaseViewModel purchaseViewModel;
    private String role;


    public ReviewFragment(JobActivityProgress activity, Models.Purchase purchase, Models.DistributionModel distribution) {
        // Required empty public constructor
        this.purchase = purchase;
        this.distribution = distribution;
        this.activity = activity;
    }

    public ReviewFragment(DonationProgressActivity donationActivity, Models.Donation donation, Models.DonationDistribution donationDistribution) {
        // Required empty public constructor
        this.donationDistribution = donationDistribution;
        this.donation = donation;
        this.donationActivity = donationActivity;
    }

    public ReviewFragment(Activity context, Models.Purchase purchase, Models.DistributionModel distribution) {
        // Required empty public constructor
        this.distribution = distribution;
        this.purchase = purchase;
        this.context = context;
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

        setEditFieldAnimation(binding.donorField, binding.sendDonor);
        setEditFieldAnimation(binding.transporterField, binding.sendTransporter);
        setEditFieldAnimation(binding.beneficiaryField, binding.sendBene);

        if (context != null) {
            userRepository.getUserLive().observe((LifecycleOwner) context, appUser -> appUser.ifPresent(u -> setLayout(u.getRole(), distribution != null ? distribution.getRemarks() : donationDistribution.getRemarks())));

        } else {
            userRepository.getUserLive().observe(activity != null ? activity : donationActivity, appUser -> appUser.ifPresent(u -> setLayout(u.getRole(), distribution != null ? distribution.getRemarks() : donationDistribution.getRemarks())));

        }
        return binding.getRoot();
    }

    private void setLayout(String role, Models.Remarks remarks) {
        this.role = role;

        EditText donorField = binding.donorField, transField = binding.transporterField, beneField = binding.beneficiaryField;
        ImageButton sendDonor = binding.sendDonor, sendTrans = binding.sendTransporter, sendBene = binding.sendBene;
        RatingBar donorRating = binding.donorRating, transRating = binding.transRating, beneRating = binding.beneRating;
        TextView donorReview = binding.donorReview, transReview = binding.transporterReview, beneReview = binding.beneficiaryReview;

        //defaults
        // 1. Fields
        // 1. Fields
        donorField.setVisibility(View.GONE);
        transField.setVisibility(View.GONE);
        beneField.setVisibility(View.GONE);

        // 2. Rating
        donorRating.setVisibility(View.GONE);
        transRating.setVisibility(View.GONE);
        beneRating.setVisibility(View.GONE);

        // 3. Review
        donorReview.setVisibility(View.GONE);
        transReview.setVisibility(View.GONE);
        beneReview.setVisibility(View.GONE);

        binding.donorReviewLayout.setVisibility(distribution != null ? View.GONE : View.VISIBLE);

        if (remarks == null) {

            System.out.println("REMARKS NULL");

            switch (role) {

                case "ROLE_TRANSPORTER":
                    transField.setVisibility(View.VISIBLE);
                    transField.setEnabled(true);
                    transRating.setVisibility(View.VISIBLE);
                    sendTrans.setOnClickListener(v -> {
                        if (transField.getText().toString().isEmpty()) {
                            transField.setError("Cannot send empty review");
                            transField.requestFocus();
                        } else if (transField.getText().toString().equals(HY)) {
                            transField.setError("Invalid review");
                            transField.requestFocus();
                        } else {
                            Models.Remarks newRemark = new Models.Remarks();
                            newRemark.setTransporter(makeReview(transField.getText().toString(), null));

                            postReview(newRemark);
                            transField.setText("");
                        }
                    });
                    transRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                        Models.Remarks newRemark = new Models.Remarks();
                        newRemark.setTransporter(makeReview(null, rating));
                        ratingBar.setIsIndicator(true);
                        postReview(newRemark);
                    });
                    break;

                case "ROLE_DONOR":
                    donorField.setVisibility(View.VISIBLE);
                    donorField.setEnabled(true);
                    donorRating.setVisibility(View.VISIBLE);
                    sendDonor.setOnClickListener(v -> {
                        if (donorField.getText().toString().isEmpty()) {
                            donorField.setError("Cannot send empty review");
                            donorField.requestFocus();
                        } else if (donorField.getText().toString().equals(HY)) {
                            donorField.setError("Invalid review");
                            donorField.requestFocus();
                        } else {
                            Models.Remarks newRemark = new Models.Remarks();
                            newRemark.setDonor(makeReview(donorField.getText().toString(), null));
                            postReview(newRemark);
                            donorField.setText("");
                        }
                    });
                    donorRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                        Models.Remarks newRemark = new Models.Remarks();
                        newRemark.setDonor(makeReview(null, rating));
                        ratingBar.setIsIndicator(true);
                        postReview(newRemark);
                    });
                    break;

                case "ROLE_BUYER":
                    beneField.setVisibility(View.VISIBLE);
                    beneField.setEnabled(true);
                    beneRating.setVisibility(View.VISIBLE);
                    sendBene.setOnClickListener(v -> {
                        if (beneField.getText().toString().isEmpty()) {
                            beneField.setError("Cannot send empty review");
                            beneField.requestFocus();
                        } else if (beneField.getText().toString().equals(HY)) {
                            beneField.setError("Invalid review");
                            beneField.requestFocus();
                        } else {
                            Models.Remarks newRemark = new Models.Remarks();
                            newRemark.setBeneficiary(makeReview(beneField.getText().toString(), null));
                            postReview(newRemark);
                            beneField.setText("");
                        }
                    });
                    beneRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                        Models.Remarks newRemark = new Models.Remarks();
                        newRemark.setBeneficiary(makeReview(null, rating));
                        ratingBar.setIsIndicator(true);
                        postReview(newRemark);
                    });
                    break;
            }
        }

        if (remarks != null) {

            System.out.println("REMARKS NOT NULL");

            //set data
            if (remarks.getDonor() != null) {

                System.out.println("REMARKS DONOR NOT NULL");


                String review = getReviewFromString(remarks.getDonor());
                Float rating = getRatingFromString(remarks.getDonor());

                if (review != null && !review.equals(HY)) {
                    donorReview.setVisibility(View.VISIBLE);
                    donorReview.setText(review);
                }

                if (rating != null) {
                    donorRating.setVisibility(View.VISIBLE);
                    donorRating.setRating(rating);
                    donorRating.setIsIndicator(true);
                }

            } else {
                System.out.println("REMARKS DONOR NULL");
            }

            if (remarks.getBeneficiary() != null) {

                System.out.println("REMARKS BENEFICIARY NOT NULL");


                String review = getReviewFromString(remarks.getBeneficiary());
                Float rating = getRatingFromString(remarks.getBeneficiary());

                if (review != null && !review.equals(HY)) {
                    beneReview.setVisibility(View.VISIBLE);
                    beneReview.setText(review);
                }

                if (rating != null) {
                    beneRating.setVisibility(View.VISIBLE);
                    beneRating.setRating(rating);
                    beneRating.setIsIndicator(true);
                }

            } else {
                System.out.println("REMARKS BENEFICIARY NULL");
            }

            if (remarks.getTransporter() != null) {
                System.out.println("REMARKS TRANSPORTED NOT NULL");

                String review = getReviewFromString(remarks.getTransporter());
                Float rating = getRatingFromString(remarks.getTransporter());

                if (review != null && !review.equals(HY)) {
                    transReview.setVisibility(View.VISIBLE);
                    transReview.setText(review);
                }

                if (rating != null) {
                    transRating.setVisibility(View.VISIBLE);
                    transRating.setRating(rating);
                    transRating.setIsIndicator(true);
                }

            } else {
                System.out.println("REMARKS TRANSPORTED NULL");
            }

            switch (role) {
                case "ROLE_TRANSPORTER":
                    transRating.setVisibility(View.VISIBLE);

                    if (remarks.getTransporter() != null) {
                        String review = getReviewFromString(remarks.getTransporter());
                        Float rating = getRatingFromString(remarks.getTransporter());

                        if (review == null) {
                            transReview.setVisibility(View.GONE);
                            transField.setVisibility(View.VISIBLE);
                            transField.setEnabled(true);
                            sendTrans.setOnClickListener(v ->   {
                                if (transField.getText().toString().isEmpty()) {
                                    transField.setError("Cannot send empty review");
                                    transField.requestFocus();
                                } else if (transField.getText().toString().equals(HY)) {
                                    transField.setError("Invalid review");
                                    transField.requestFocus();
                                } else {
                                    remarks.setTransporter(makeReview(transField.getText().toString(), rating));
                                    postReview(remarks);
                                    transField.setText("");
                                }
                            });
                        } else {
                            transReview.setVisibility(View.VISIBLE);
                            transReview.setText(review);
                            transField.setVisibility(View.GONE);
                            transField.setEnabled(false);
                        }

                        if (rating == null) {
                            transRating.setIsIndicator(false);
                            transRating.setOnRatingBarChangeListener((ratingBar, ratingT, fromUser) -> {
                                remarks.setTransporter(makeReview(review, ratingT));
                                ratingBar.setIsIndicator(true);
                                postReview(remarks);
                            });
                        } else {
                            transRating.setIsIndicator(true);
                            transRating.setRating(rating);
                        }

                    }

                    if (remarks.getTransporter() == null) {
                        transField.setVisibility(View.VISIBLE);
                        transField.setEnabled(true);
                        sendTrans.setOnClickListener(v -> {
                            if (transField.getText().toString().isEmpty()) {
                                transField.setError("Cannot send empty review");
                                transField.requestFocus();
                            } else if (transField.getText().toString().equals(HY)) {
                                transField.setError("Invalid review");
                                transField.requestFocus();
                            } else {
                                remarks.setTransporter(makeReview(transField.getText().toString(), null));
                                postReview(remarks);
                                transField.setText("");
                            }
                        });

                        transRating.setIsIndicator(false);
                        transRating.setVisibility(View.VISIBLE);
                        transRating.setOnRatingBarChangeListener((ratingBar, ratingT, fromUser) -> {
                            remarks.setTransporter(makeReview(null, ratingT));
                            ratingBar.setIsIndicator(true);

                            postReview(remarks);
                        });
                    }

                    break;

                case "ROLE_DONOR":

                    donorRating.setVisibility(View.VISIBLE);

                    if (remarks.getDonor() != null) {
                        String review = getReviewFromString(remarks.getDonor());
                        Float rating = getRatingFromString(remarks.getDonor());

                        if (review == null) {
                            donorReview.setVisibility(View.GONE);
                            donorField.setVisibility(View.VISIBLE);
                            donorField.setEnabled(true);
                            sendDonor.setOnClickListener(v -> {
                                if (donorField.getText().toString().isEmpty()) {
                                    donorField.setError("Cannot send empty review");
                                    donorField.requestFocus();
                                } else if (donorField.getText().toString().equals(HY)) {
                                    donorField.setError("Invalid review");
                                    donorField.requestFocus();
                                } else {
                                    remarks.setDonor(makeReview(donorField.getText().toString(), rating));
                                    postReview(remarks);
                                    donorField.setText("");
                                }
                            });
                        } else {
                            donorReview.setVisibility(View.VISIBLE);
                            donorReview.setText(review);
                            donorField.setVisibility(View.GONE);
                            donorField.setEnabled(false);
                        }

                        if (rating == null) {
                            donorRating.setIsIndicator(false);
                            donorRating.setOnRatingBarChangeListener((ratingBar, ratingT, fromUser) -> {
                                remarks.setDonor(makeReview(review, ratingT));
                                ratingBar.setIsIndicator(true);
                                postReview(remarks);
                            });
                        } else {
                            donorRating.setIsIndicator(true);
                            donorRating.setRating(rating);
                        }

                    }


                    if (remarks.getDonor() == null) {
                        donorField.setVisibility(View.VISIBLE);
                        donorField.setEnabled(true);
                        sendDonor.setOnClickListener(v -> {
                            if (donorField.getText().toString().isEmpty()) {
                                donorField.setError("Cannot send empty review");
                                donorField.requestFocus();
                            } else if (donorField.getText().toString().equals(HY)) {
                                donorField.setError("Invalid review");
                                donorField.requestFocus();
                            } else {
                                remarks.setDonor(makeReview(donorField.getText().toString(), null));
                                postReview(remarks);
                                donorField.setText("");
                            }
                        });

                        donorRating.setIsIndicator(false);
                        donorRating.setVisibility(View.VISIBLE);
                        donorRating.setOnRatingBarChangeListener((ratingBar, ratingT, fromUser) -> {
                            remarks.setDonor(makeReview(null, ratingT));
                            ratingBar.setIsIndicator(true);
                            postReview(remarks);
                        });
                    }

                    break;

                case "ROLE_BUYER":

                    beneRating.setVisibility(View.VISIBLE);


                    if (remarks.getBeneficiary() != null) {
                        String review = getReviewFromString(remarks.getBeneficiary());
                        Float rating = getRatingFromString(remarks.getBeneficiary());

                        if (review == null) {
                            System.out.println("REVIEW REVIEW IS NOT THERE");
                            beneReview.setVisibility(View.GONE);
                            beneField.setVisibility(View.VISIBLE);
                            beneField.setEnabled(true);
                            sendBene.setOnClickListener(v -> {
                                if (beneField.getText().toString().isEmpty()) {
                                    beneField.setError("Cannot send empty review");
                                    beneField.requestFocus();
                                } else if (beneField.getText().toString().equals(HY)) {
                                    beneField.setError("Invalid review");
                                    beneField.requestFocus();
                                } else {
                                    remarks.setBeneficiary(makeReview(beneField.getText().toString(), rating));
                                    postReview(remarks);
                                    beneField.setText("");
                                }
                            });
                        } else {
                            System.out.println("REVIEW REVIEW IS THERE");
                            beneReview.setVisibility(View.VISIBLE);
                            beneReview.setText(review);
                            beneField.setVisibility(View.GONE);
                            beneField.setEnabled(false);

                        }

                        if (rating == null) {
                            System.out.println("REVIEW RATING IS NOT THERE");
                            beneRating.setIsIndicator(false);
                            beneRating.setOnRatingBarChangeListener((ratingBar, ratingT, fromUser) -> {
                                remarks.setBeneficiary(makeReview(review, ratingT));
                                ratingBar.setIsIndicator(true);
                                postReview(remarks);
                            });
                        } else {
                            System.out.println("REVIEW RATING IS THERE");
                            beneRating.setIsIndicator(true);
                            beneRating.setRating(rating);
                        }

                    }


                    if (remarks.getBeneficiary() == null) {
                        beneField.setVisibility(View.VISIBLE);
                        beneField.setEnabled(true);
                        sendBene.setOnClickListener(v -> {
                            if (beneField.getText().toString().isEmpty()) {
                                beneField.setError("Cannot send empty review");
                                beneField.requestFocus();
                            } else if (beneField.getText().toString().equals(HY)) {
                                beneField.setError("Invalid review");
                                beneField.requestFocus();
                            } else {
                                remarks.setBeneficiary(makeReview(beneField.getText().toString(), null));
                                postReview(remarks);
                                beneField.setText("");
                            }
                        });

                        beneRating.setIsIndicator(false);
                        beneRating.setVisibility(View.VISIBLE);
                        beneRating.setOnRatingBarChangeListener((ratingBar, ratingT, fromUser) -> {
                            remarks.setBeneficiary(makeReview(null, ratingT));
                            ratingBar.setIsIndicator(true);
                            postReview(remarks);
                        });
                    }

                    break;
            }
        }
    }

    private void createReview(Models.Remarks remarks) {
        inProgress();
        purchaseViewModel.createNewRemark(remarks).observe(getViewLifecycleOwner(), optionalRemarks -> {
            outProgress();
            if (!optionalRemarks.isPresent()) {
                Toast.makeText(activity != null ? activity : donationActivity, "Failed to create review", Toast.LENGTH_SHORT).show();
                return;
            }

            optionalRemarks.ifPresent(u -> {
                distribution.setRemarks(u);
                setLayout(role, u);
                Toast.makeText(activity != null ? activity : donationActivity, "Review posted", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void postReview(Models.Remarks remarks) {
        if (distribution != null) {
            postPurchaseReview(remarks);
        } else {
            postDonationReview(remarks);
        }
    }

    private void postPurchaseReview(Models.Remarks remarks) {
        if (hasRemarks()) {
            updateReview(remarks);
        } else {
            remarks.setDistribution_id(distribution.getId());
            createReview(remarks);
        }
    }

    private void postDonationReview(Models.Remarks remarks) {
        if (hasDonationRemarks()) {
            updateDonationReview(remarks);
        } else {
            remarks.setDonation_distribution_id(donationDistribution.getId());
            createDonationReview(remarks);
        }
    }

    private void createDonationReview(Models.Remarks remarks) {
        inProgress();
        purchaseViewModel.createNewRemark(remarks).observe(getViewLifecycleOwner(), optionalRemarks -> {
            outProgress();
            if (!optionalRemarks.isPresent()) {
                Toast.makeText(activity != null ? activity : donationActivity, "Failed to create review", Toast.LENGTH_SHORT).show();
                return;
            }

            optionalRemarks.ifPresent(u -> {
                donationDistribution.setRemarks(u);
                setLayout(role, u);
                Toast.makeText(activity != null ? activity : donationActivity, "Review posted", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateDonationReview(Models.Remarks remarks) {
        inProgress();
        purchaseViewModel.updateARemark(remarks).observe(getViewLifecycleOwner(), optionalRemarks -> {
            outProgress();
            if (!optionalRemarks.isPresent()) {
                Toast.makeText(activity != null ? activity : donationActivity, "Failed to post review", Toast.LENGTH_SHORT).show();
                return;
            }

            optionalRemarks.ifPresent(u -> {
                donationDistribution.setRemarks(u);
                setLayout(role, u);
                Toast.makeText(activity != null ? activity : donationActivity, "Review posted", Toast.LENGTH_SHORT).show();
            });

        });
    }

    private void updateReview(Models.Remarks remarks) {
        inProgress();
        purchaseViewModel.updateARemark(remarks).observe(getViewLifecycleOwner(), optionalRemarks -> {
            outProgress();
            if (!optionalRemarks.isPresent()) {
                Toast.makeText(activity != null ? activity : donationActivity, "Failed to post review", Toast.LENGTH_SHORT).show();
                return;
            }

            optionalRemarks.ifPresent(u -> {
                distribution.setRemarks(u);
                setLayout(role, u);
                Toast.makeText(activity != null ? activity : donationActivity, "Review posted", Toast.LENGTH_SHORT).show();
            });

        });
    }

    private void inProgress() {
        binding.pb.setVisibility(View.VISIBLE);
        binding.sendBene.setEnabled(false);
        binding.sendDonor.setEnabled(false);
        binding.sendTransporter.setEnabled(false);
    }

    private void outProgress() {
        binding.pb.setVisibility(View.GONE);
        binding.sendBene.setEnabled(true);
        binding.sendDonor.setEnabled(true);
        binding.sendTransporter.setEnabled(true);
    }

    public static String getReviewFromString(String s) {
        LinkedHashMap<String, String> reviewMap = DataOpts.getMapFromString(s);
        Map.Entry<String, String> reviewEntry = reviewMap.entrySet().stream().findFirst().orElse(null);
        return reviewEntry != null ? reviewEntry.getValue() : HY;
    }

    public static Float getRatingFromString(String s) {
        LinkedHashMap<String, String> reviewMap = DataOpts.getMapFromString(s);
        Map.Entry<String, String> reviewEntry = reviewMap.entrySet().stream().findFirst().orElse(null);
        String ratingString = reviewEntry != null ? reviewEntry.getKey() : HY;

        return ratingString != null && !ratingString.equals(HY) ? Float.valueOf(ratingString) : null;
    }

    private boolean hasRemarks() {
        return distribution.getRemarks() != null;
    }

    private boolean hasDonationRemarks() {
        return donationDistribution.getRemarks() != null;
    }

    private String makeReview(String text, Float rating) {
        LinkedHashMap<String, String> review = new LinkedHashMap<>();
        review.put(rating != null ? String.valueOf(rating) : HY, text != null ? text : HY);
        return getStringFromMap(review);
    }

    public static void setEditFieldAnimation(final EditText field, final ImageButton button) {

        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().isEmpty() || s.toString().equals("")) {
                    button.setVisibility(View.GONE);
                } else {
                    button.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty() || s.toString().equals("")) {
                    button.setVisibility(View.GONE);
                } else {
                    button.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty() || s.toString().equals("")) {
                    button.setVisibility(View.GONE);
                } else {
                    button.setVisibility(View.VISIBLE);
                }
            }
        });


    }

}