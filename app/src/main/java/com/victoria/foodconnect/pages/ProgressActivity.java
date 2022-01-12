package com.victoria.foodconnect.pages;

import static com.victoria.foodconnect.globals.GlobalVariables.DONATION;
import static com.victoria.foodconnect.globals.GlobalVariables.MEDIA_TYPE;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.seller.SellerActivity.addFragmentToContainer;
import static com.victoria.foodconnect.pages.transporter.MoreActivity.PURCHASE;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.WanderingCubes;
import com.victoria.foodconnect.adapter.ProgressRvAdapter;
import com.victoria.foodconnect.databinding.ActivityProgressBinding;
import com.victoria.foodconnect.globals.purchaseDb.PurchaseViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.transporter.jobProgress.ReviewFragment;
import com.victoria.foodconnect.utils.DistributionStatus;
import com.victoria.foodconnect.utils.ProgressStatus;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class ProgressActivity extends AppCompatActivity {

    private ActivityProgressBinding binding;
    private final LinkedList<Models.ProgressModel> progressList = new LinkedList<>();
    private ProgressRvAdapter progressRvAdapter;
    private PurchaseViewModel purchaseViewModel;
    public static Timer spinTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProgressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inSpinnerProgress(binding.pb, null);

        purchaseViewModel = new ViewModelProvider(this).get(PurchaseViewModel.class);

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        progressRvAdapter = new ProgressRvAdapter(this, progressList);
        RecyclerView progressRv = binding.progressRv;
        progressRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        progressRv.setAdapter(progressRvAdapter);

        switch (getIntent().getExtras().get(MEDIA_TYPE).toString()) {
            case DONATION:
                binding.toolbar.setTitle("Donation");
                populateDonation((Models.Donation) getIntent().getExtras().get(DONATION));
                break;

            case PURCHASE:
                binding.toolbar.setTitle("Purchase");
                populatePurchase((Models.Purchase) getIntent().getExtras().get(PURCHASE));
                break;
        }


        setWindowColors(this);

    }

    public static void inSpinnerProgress(SpinKitView pb, Button button) {

        if (button != null) {
            button.setEnabled(false);
        }

        Sprite wanderingCubes = new WanderingCubes();
        wanderingCubes.setVisible(true, true);
        pb.setIndeterminateDrawable(wanderingCubes);
        pb.setVisibility(View.VISIBLE);


        Integer[] colorList = new Integer[]{Color.RED, Color.BLUE, Color.BLACK};
        final int[] i = {0};

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                pb.setColor(colorList[i[0]]);
                if (i[0] + 1 != colorList.length) {
                    i[0]++;
                } else {
                    i[0] = 0;
                }
            }
        };

        spinTimer = new Timer();
        spinTimer.scheduleAtFixedRate(timerTask, 500, 500);
    }

    public static void outSpinnerProgress(SpinKitView pb, Button button) {
        if (button != null) {
            button.setEnabled(true);
        }
        pb.setVisibility(View.GONE);
       if(spinTimer != null) {
           spinTimer.cancel();
       }
    }

    private void populateDonation(Models.Donation donation) {
        Models.ProgressModel progressModel = new Models.ProgressModel(donation.getAssigned() != null ? "Assigned" : "Assigning", donation.getAssigned() != null ? ProgressStatus.COMPLETE : ProgressStatus.NEW);
        purchaseViewModel.getADonationDistributionLive(donation.getId()).observe(this, donationDistribution -> {
            if (!donationDistribution.isPresent()) {
                progressList.clear();
                progressList.add(progressModel);
                progressRvAdapter.notifyDataSetChanged();

                Arrays.stream(DistributionStatus.values()).forEach(s -> {
                    progressList.add(new Models.ProgressModel(s.getDescription(), ProgressStatus.NEW));
                    progressRvAdapter.notifyDataSetChanged();
                });

                return;
            }

            donationDistribution.ifPresent(d -> {


                progressList.clear();
                progressList.add(progressModel);
                progressRvAdapter.notifyDataSetChanged();

                Arrays.stream(DistributionStatus.values()).forEach(s -> {
                    progressList.add(new Models.ProgressModel(s.getDescription(), ProgressStatus.NEW));
                    progressRvAdapter.notifyDataSetChanged();
                });

                progressList.forEach(progress -> {
                    final DistributionStatus current = DistributionStatus.values()[d.getStatus()];
                    final Optional<DistributionStatus> progressStatus = Arrays.stream(DistributionStatus.values()).filter(i -> i.getDescription().equals(progress.getName())).findFirst();


                    progressStatus.ifPresent(ps -> {



                        System.out.println(progress.getName() + " :  current -> " + current.getCode() + " progress -> " + ps.getCode());


                        if (current.getCode() == ps.getCode()) {
                            progress.setStatus(ProgressStatus.PROGRESS);
                        } else if (current.getCode() < ps.getCode()) {
                            progress.setStatus(ProgressStatus.NEW);
                        } else {
                            progress.setStatus(ProgressStatus.COMPLETE);
                        }
                    });


                    if (d.getStatus() == 4) {
                        progressList.removeLast();
                        progressRvAdapter.notifyDataSetChanged();
                    } else if (d.getStatus() == 5) {
                        progressList.get(progressList.size() - 2).setStatus(ProgressStatus.FAILED);
                        progressList.peekLast().setStatus(ProgressStatus.COMPLETE);
                    }

                });
            });
        });
    }


    private void setUpPurchasePager(Models.Purchase purchase, Models.DistributionModel distribution) {
        addFragmentToContainer(getSupportFragmentManager(), binding.jobActivityFragment, new ReviewFragment(this, purchase, distribution));
    }


    private void populatePurchase(Models.Purchase purchase) {
        Models.ProgressModel progressModel = new Models.ProgressModel(purchase.getAssigned() != null ? "Assigned" : "Assigning", purchase.getAssigned() != null ? ProgressStatus.COMPLETE : ProgressStatus.NEW);

        purchaseViewModel.getADistributionLive(purchase.getId()).observe(this, donationDistribution -> {
            outSpinnerProgress(binding.pb, null);

            if (!donationDistribution.isPresent()) {
                progressList.clear();
                progressList.add(progressModel);
                progressRvAdapter.notifyDataSetChanged();

                Arrays.stream(DistributionStatus.values()).forEach(s -> {
                    progressList.add(new Models.ProgressModel(s.getDescription(), ProgressStatus.NEW));
                    progressRvAdapter.notifyDataSetChanged();
                });

                return;
            }

            donationDistribution.ifPresent(d -> {

                if (d.getStatus() == DistributionStatus.COMPLETE.getCode() || d.getStatus() == DistributionStatus.DNF.getCode()) {
                    setUpPurchasePager(purchase,d);
                }

                progressList.clear();
                progressList.add(progressModel);
                progressRvAdapter.notifyDataSetChanged();

                Arrays.stream(DistributionStatus.values()).forEach(s -> {
                    progressList.add(new Models.ProgressModel(s.getDescription(), ProgressStatus.NEW));
                    progressRvAdapter.notifyDataSetChanged();
                });

                progressList.forEach(progress -> {
                    final DistributionStatus current = DistributionStatus.values()[d.getStatus()];
                    final Optional<DistributionStatus> progressStatus = Arrays.stream(DistributionStatus.values()).filter(i -> i.getDescription().equals(progress.getName())).findFirst();


                    progressStatus.ifPresent(ps -> {

                        System.out.println(progress.getName() + " :  current -> " + current.getCode() + " progress -> " + ps.getCode());


                        if (current.getCode() == ps.getCode()) {
                            progress.setStatus(ProgressStatus.PROGRESS);
                        } else if (current.getCode() < ps.getCode()) {
                            progress.setStatus(ProgressStatus.NEW);
                        } else {
                            progress.setStatus(ProgressStatus.COMPLETE);
                        }
                    });

                });

                if (d.getStatus() == 4) {
                    progressList.removeLast();
                    progressRvAdapter.notifyDataSetChanged();
                } else if (d.getStatus() == 5) {
                    progressList.get(progressList.size() - 2).setStatus(ProgressStatus.FAILED);
                    progressList.peekLast().setStatus(ProgressStatus.COMPLETE);
                }

            });
        });


    }


}