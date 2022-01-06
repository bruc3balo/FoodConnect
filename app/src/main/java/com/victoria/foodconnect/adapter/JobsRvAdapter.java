package com.victoria.foodconnect.adapter;


import static android.graphics.Color.BLACK;
import static android.graphics.Color.GREEN;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.transporter.MoreActivity;
import com.victoria.foodconnect.pages.transporter.jobProgress.JobActivityProgress;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import me.relex.circleindicator.CircleIndicator3;


public class JobsRvAdapter extends RecyclerView.Adapter<JobsRvAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final Activity mContext;
    private final LinkedList<Models.Purchase> list;
    private final Domain.AppUser user;


    public JobsRvAdapter(Activity context, LinkedList<Models.Purchase> list, Domain.AppUser user) {
        this.mContext = context;
        this.list = list;
        this.user = user;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.job_item, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.Purchase purchase = list.get(position);

        TextView buyer = holder.buyer;
        TextView delivery = holder.deliveryLocation;
        ViewPager2 productsRv = holder.productsRv;
        Button more = holder.more;
        CardView card = holder.cardBg;
        CircleIndicator3 indicator = holder.indicator;

        if (purchase.getAssigned() != null && purchase.getAssigned().equals(user.getUsername())) {
            card.setCardBackgroundColor(GREEN);
        } else {
            card.setCardBackgroundColor(BLACK);
        }

        if (!purchase.isComplete() && !purchase.getDeleted() && purchase.getAssigned() == null) {
            more.setText("More");
            more.setOnClickListener(v -> goToMore(purchase));
        } else if (!purchase.isComplete() && !purchase.getDeleted() && purchase.getAssigned() != null && purchase.getAssigned().equals(user.getUsername())) {
            //progress
            more.setText("Progress");
            more.setOnClickListener(v -> goToProgress(purchase));
        } else if (purchase.getDeleted() || purchase.isComplete()) {
            //complete
            more.setText("Details");
        } else {
            //Toast.makeText(mContext, "HHMmmmmm", Toast.LENGTH_SHORT).show();
        }


        buyer.setText(getUnderlinedSpannableBuilder(purchase.getBuyersId()));
        delivery.setText("Delivery Location : " + purchase.getAddress());
        more.setOnClickListener(v -> {

            if (!purchase.isComplete() && !purchase.getDeleted() && purchase.getAssigned() == null) {
                goToMore(purchase);
            } else if (!purchase.isComplete() && !purchase.getDeleted() && purchase.getAssigned() != null && purchase.getAssigned().equals(user.getUsername())) {
                //progress
                goToProgress(purchase);
            } else if (purchase.getDeleted() || purchase.isComplete()) {
                //complete
            } else {
                //Toast.makeText(mContext, "HHMmmmmm", Toast.LENGTH_SHORT).show();
            }

        });


        productsRv.setOffscreenPageLimit(3);
        productsRv.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        productsRv.requestTransform();
        productsRv.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        final JobProductListAdapter jobProductListAdapter = new JobProductListAdapter(mContext, purchase.getProduct());
        jobProductListAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());
        productsRv.setAdapter(jobProductListAdapter);
        productsRv.setPadding(40, 0, 40, 0);
        indicator.setViewPager(productsRv);
    }

    private void goToMore(Models.Purchase purchase) {
        mContext.startActivity(new Intent(mContext, MoreActivity.class).putExtra(PURCHASE, purchase));
    }


    private void goToProgress(Models.Purchase purchase) {
        mContext.startActivity(new Intent(mContext, JobActivityProgress.class).putExtra(PURCHASE, purchase));
    }

    private void init(GoogleMap map) {

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        map.setMyLocationEnabled(true);
        map.setOnMyLocationButtonClickListener(() -> false);
        map.setOnMyLocationClickListener(location -> Toast.makeText(mContext, "I am here !!! ", Toast.LENGTH_SHORT).show());
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
    }


    private void addMarkerToMap(GoogleMap googleMap, LatLng latLng, String buyer) {
        String title = getFromLocation(latLng, mContext);
        @SuppressLint("UseCompatLoadingForDrawables") MarkerOptions markerOptions = new MarkerOptions().position(latLng).draggable(true).title(title != null ? title : "Lat : " + String.valueOf(latLng.latitude).substring(4).concat("Long : " + String.valueOf(latLng.latitude).substring(4))).snippet(buyer + " location").icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(mContext.getDrawable(R.drawable.ic_give_food))));
        googleMap.clear();
        googleMap.addMarker(markerOptions);
    }

    public static String getFromLocation(LatLng latLng, Context context) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.isEmpty()) {
                return null;
            }
            Address address = addresses.get(0);
            return address.getAddressLine(0);
        } catch (IOException | IllegalArgumentException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView buyer, deliveryLocation;
        ViewPager2 productsRv;
        Button more;
        CardView cardBg;
        CircleIndicator3 indicator;

        ViewHolder(View itemView) {
            super(itemView);

            buyer = itemView.findViewById(R.id.buyer);
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