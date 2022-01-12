package com.victoria.foodconnect.adapter;


import static android.graphics.Color.BLACK;
import static android.graphics.Color.GREEN;
import static com.victoria.foodconnect.globals.GlobalVariables.DONATION;
import static com.victoria.foodconnect.globals.GlobalVariables.MEDIA_TYPE;
import static com.victoria.foodconnect.globals.GlobalVariables.READ_ONLY;
import static com.victoria.foodconnect.pages.seller.AddNewProduct.drawableToBitmap;
import static com.victoria.foodconnect.pages.transporter.MoreActivity.PURCHASE;
import static com.victoria.foodconnect.utils.DataOpts.getUnderlinedSpannableBuilder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.ProgressActivity;
import com.victoria.foodconnect.pages.transporter.MoreActivity;
import com.victoria.foodconnect.pages.transporter.MoreDonationActivity;
import com.victoria.foodconnect.pages.transporter.donationProgress.DonationProgressActivity;
import com.victoria.foodconnect.pages.transporter.jobProgress.JobActivityProgress;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import me.relex.circleindicator.CircleIndicator3;


public class DonationRvAdapter extends RecyclerView.Adapter<DonationRvAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final Activity mContext;
    private final LinkedList<Models.Donation> list;
    private final Domain.AppUser user;
    private final boolean readOnly;


    public DonationRvAdapter(Activity context, LinkedList<Models.Donation> list, Domain.AppUser user, boolean readOnly) {
        this.mContext = context;
        this.list = list;
        this.user = user;
        this.readOnly = readOnly;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.donor_item, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.Donation donation = list.get(holder.getAdapterPosition());

        TextView donor = holder.donor;
        TextView delivery = holder.deliveryLocation;
        TextView collection = holder.collectionLocation;

        ViewPager2 productsRv = holder.productsRv;
        Button more = holder.more;
        CardView card = holder.cardBg;
        CircleIndicator3 indicator = holder.indicator;


        donor.setText("Donor : " + donation.getDonor_username().getUsername());
        collection.setText("From : " + donation.getCollection_address());
        delivery.setText("To : " + donation.getDelivery_address());

        if (donation.getAssigned() != null && donation.getAssigned().equals(user.getUsername())) {
            card.setCardBackgroundColor(GREEN);
        } else {
            card.setCardBackgroundColor(BLACK);
        }

        if (!donation.isComplete() && !donation.isDeleted() && donation.getAssigned() == null) {
            more.setText(readOnly ? "Progress" : "More");
            more.setOnClickListener(v -> goToMore(donation));
        } else if (!donation.isComplete() && !donation.isDeleted() && donation.getAssigned() != null) {
            //progress
            more.setText("Progress");
            more.setOnClickListener(v -> goToProgress(donation));
        } else if (donation.isDeleted() || donation.isComplete() && donation.getAssigned() != null) {
            //complete
            more.setText("Rating");
            more.setOnClickListener(v -> goToProgress(donation));
        } else {
            //Toast.makeText(mContext, "HHMmmmmm", Toast.LENGTH_SHORT).show();
        }


        productsRv.setOffscreenPageLimit(3);
        productsRv.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        productsRv.requestTransform();
        productsRv.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        final DonationProductListAdapter donationProductListAdapter = new DonationProductListAdapter(mContext, donation.getProducts());
        donationProductListAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());
        productsRv.setAdapter(donationProductListAdapter);
        productsRv.setPadding(40, 0, 40, 0);
        indicator.setViewPager(productsRv);
    }

    private void goToMore(Models.Donation donation) {
        if (readOnly) {
            mContext.startActivity(new Intent(mContext, ProgressActivity.class).putExtra(DONATION, donation).putExtra(MEDIA_TYPE, DONATION));
        } else {
            mContext.startActivity(new Intent(mContext, MoreDonationActivity.class).putExtra(DONATION, donation).putExtra(READ_ONLY, readOnly));
        }
    }


    private void goToProgress(Models.Donation donation) {
        if (readOnly && !donation.isComplete()) {
            mContext.startActivity(new Intent(mContext, ProgressActivity.class).putExtra(DONATION, donation).putExtra(MEDIA_TYPE, DONATION));
        } else {
            mContext.startActivity(new Intent(mContext, DonationProgressActivity.class).putExtra(DONATION, donation).putExtra(READ_ONLY, readOnly));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView donor, collectionLocation, deliveryLocation;
        ViewPager2 productsRv;
        Button more;
        CardView cardBg;
        CircleIndicator3 indicator;

        ViewHolder(View itemView) {
            super(itemView);

            donor = itemView.findViewById(R.id.donorName);
            collectionLocation = itemView.findViewById(R.id.collectionLocation);
            deliveryLocation = itemView.findViewById(R.id.deliveryLocation);
            productsRv = itemView.findViewById(R.id.productsRv);
            deliveryLocation = itemView.findViewById(R.id.deliveryLocation);
            more = itemView.findViewById(R.id.more);
            cardBg = itemView.findViewById(R.id.cardBg);
            indicator = itemView.findViewById(R.id.indicator);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}