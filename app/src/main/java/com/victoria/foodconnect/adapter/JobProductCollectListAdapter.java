package com.victoria.foodconnect.adapter;


import static com.victoria.foodconnect.adapter.JobsRvAdapter.getFromLocation;
import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.globals.GlobalVariables.LATITUDE;
import static com.victoria.foodconnect.globals.GlobalVariables.LONGITUDE;
import static com.victoria.foodconnect.pages.transporter.jobProgress.JobActivityProgress.update;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.databinding.ActionDialogBinding;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.transporter.jobProgress.JobActivityProgress;
import com.victoria.foodconnect.utils.DataOpts;
import com.victoria.foodconnect.utils.MyLinkedMap;
import com.victoria.foodconnect.utils.ProductStatus;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public class JobProductCollectListAdapter extends RecyclerView.Adapter<JobProductCollectListAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final JobActivityProgress mContext;
    private final MyLinkedMap<String, Integer> list;
    private final List<Models.Product> productList;
    private final Long distId;


    public JobProductCollectListAdapter(JobActivityProgress context, Models.DistributionModel distributionModel, List<Models.Product> productList) {
        this.mContext = context;
        this.list = distributionModel.getProduct_status();
        this.distId = distributionModel.getId();
        this.productList = productList;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_job_item_status, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        int status = list.getValue(position);
        Optional<Models.Product> productOptional = productList.stream().filter(i -> i.getId().equals(list.getKey(position))).findFirst();
        productOptional.ifPresent(product -> {

            if (!product.getLocation().equals(HY)) {
                LinkedHashMap<String, String> map = DataOpts.getMapFromString(product.getLocation());
                LatLng productLocation = new LatLng(Double.parseDouble(Objects.requireNonNull(map.get(LATITUDE))), Double.parseDouble(Objects.requireNonNull(map.get(LONGITUDE))));
                String location = getFromLocation(productLocation, mContext);
                holder.productLocation.setText(location);
            } else {
                holder.productLocation.setVisibility(View.GONE);
            }


            holder.productTitle.setText(product.getName());
            holder.productItems.setText(status + " items");


            holder.seller.setText("Seller : " + product.getSellersId());
            Glide.with(mContext).load(product.getImage()).into(holder.productImage);


            Optional<ProductStatus> optionalProductStatus = Arrays.stream(ProductStatus.values()).filter(i->i.getCode() == status).findFirst();
            optionalProductStatus.ifPresent(s->{
                switch (s.getCode()) {
                    default:
                    case 0:
                        holder.status.setImageResource(R.color.white);
                        break;

                    case 1:
                        holder.status.setImageResource(R.drawable.ic_go);
                        break;

                    case 2:
                        holder.status.setImageResource(R.drawable.x);
                        break;

                    case 3:
                        holder.status.setImageResource(R.drawable.tick);
                        break;
                }
            });

            holder.action.setOnClickListener(v -> actionDialog(product.getId()));

        });

    }

    private void actionDialog(String pid) {
        Dialog d = new Dialog(mContext);
        ActionDialogBinding binding = ActionDialogBinding.inflate(LayoutInflater.from(mContext));
        d.setContentView(binding.getRoot());
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ImageButton go = binding.go,x = binding.fail,tick = binding.finish;

        go.setOnClickListener(v -> {
            d.dismiss();
            Map<String, Integer> product_status = new HashMap<>();
            product_status.put(pid, ProductStatus.ON_THE_WAY.getCode());
            update(mContext, new Models.DistributionUpdateForm(distId,product_status));
        });

        x.setOnClickListener(v -> {
            d.dismiss();
            Map<String, Integer> product_status = new HashMap<>();
            product_status.put(pid, ProductStatus.FAILED.getCode());
            update(mContext, new Models.DistributionUpdateForm(distId,product_status));
        });

        tick.setOnClickListener(v -> {
            d.dismiss();
            Map<String, Integer> product_status = new HashMap<>();
            product_status.put(pid, ProductStatus.COLLECTED.getCode());
            update(mContext, new Models.DistributionUpdateForm(distId,product_status));
        });

        d.show();
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView productTitle, productItems, productLocation, seller;
        ImageView productImage;
        Button action;
        ImageButton status;

        ViewHolder(View itemView) {
            super(itemView);

            productTitle = itemView.findViewById(R.id.productTitle);
            productItems = itemView.findViewById(R.id.productItems);
            productLocation = itemView.findViewById(R.id.productLocation);
            seller = itemView.findViewById(R.id.seller);
            productImage = itemView.findViewById(R.id.productImage);
            status = itemView.findViewById(R.id.status);
            action = itemView.findViewById(R.id.action);

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