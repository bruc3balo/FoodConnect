package com.victoria.foodconnect.adapter;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.seller.fragments.MyOrdersSeller;
import com.victoria.foodconnect.utils.MyLinkedMap;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;


public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final Context mContext;
    private final LinkedList<Models.ProductCountModel> list;


    public ProductListAdapter(Context context, LinkedList<Models.ProductCountModel> list) {
        this.mContext = context;
        this.list = list;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_list_item, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.ProductCountModel entry = list.get(holder.getAdapterPosition());
        int item = entry.getCount();
        Models.Product product = entry.getProduct();


        holder.name.setText(product.getName());
        holder.price.setText(product.getPrice().toString().concat(" Ksh per "+product.getUnit()));
        holder.items.setText(String.valueOf(item));

    }

    private void showConfirmationDialog(String infoS, Function<Boolean, Void> yesFunction, Boolean currentState) {
        Dialog d = new Dialog(mContext);
        d.setContentView(R.layout.yes_no_layout);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.show();

        TextView info = d.findViewById(R.id.infoTv);
        info.setText(infoS);
        Button no = d.findViewById(R.id.noButton);
        no.setOnClickListener(v -> d.dismiss());

        Button yes = d.findViewById(R.id.yesButton);
        yes.setOnClickListener(v -> {
            yesFunction.apply(currentState);
            d.dismiss();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name, price, items;

        ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            price = itemView.findViewById(R.id.price);
            items = itemView.findViewById(R.id.items);


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