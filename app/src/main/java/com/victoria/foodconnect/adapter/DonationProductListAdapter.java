package com.victoria.foodconnect.adapter;


import static com.victoria.foodconnect.adapter.JobsRvAdapter.getFromLocation;
import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.globals.GlobalVariables.LATITUDE;
import static com.victoria.foodconnect.globals.GlobalVariables.LONGITUDE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.DataOpts;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Objects;


public class DonationProductListAdapter extends RecyclerView.Adapter<DonationProductListAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final Context mContext;
    private final LinkedList<Models.DonorItem> list;


    public DonationProductListAdapter(Context context, LinkedList<Models.DonorItem> list) {
        this.mContext = context;
        this.list = list;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_job_item, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.DonorItem item = list.get(holder.getAdapterPosition());

        holder.productLocation.setVisibility(View.GONE);
        holder.seller.setVisibility(View.GONE);
        holder.productTitle.setText("Name : "+item.getName());
        holder.productItems.setText("Description : "+item.getDescription());
        Glide.with(mContext).load(item.getImage()).into(holder.productImage);

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView productTitle, productItems, productLocation, seller;
        ImageView productImage;

        ViewHolder(View itemView) {
            super(itemView);

            productTitle = itemView.findViewById(R.id.productTitle);
            productItems = itemView.findViewById(R.id.productItems);
            productLocation = itemView.findViewById(R.id.productLocation);
            seller = itemView.findViewById(R.id.seller);
            productImage = itemView.findViewById(R.id.productImage);

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