package com.victoria.foodconnect.adapter;


import static com.victoria.foodconnect.globals.GlobalVariables.GAS;
import static com.victoria.foodconnect.globals.GlobalVariables.LIQUID;
import static com.victoria.foodconnect.globals.GlobalVariables.SOLID;

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
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.models.Models;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;


public class ProductRvAdapter extends RecyclerView.Adapter<ProductRvAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final Context mContext;
    private final LinkedList<Models.Product> productLinkedList;


    public ProductRvAdapter(Context context, LinkedList<Models.Product> productLinkedList) {
        this.mContext = context;
        this.productLinkedList = productLinkedList;

    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Models.Product product = productLinkedList.get(position);

        Glide.with(mContext).load(product.getImage()).into(holder.productImage);
        holder.productTitle.setText(product.getName());
        holder.productDescription.setText(product.getProduct_description());

        switch (product.getUnit()) {
            default: case SOLID:
                holder.productPrice.setText(product.getPrice().toString().concat(" KSH").concat(" per item"));
                break;

            case GAS:
                holder.productPrice.setText(product.getPrice().toString().concat(" KSH").concat(" per item"));
                break;

            case LIQUID:
                holder.productPrice.setText(product.getPrice().toString().concat(" KSH").concat(" per item"));
                break;
        }

    }

    @Override
    public int getItemCount() {
        return productLinkedList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView productImage;
        TextView productTitle, productDescription,productPrice;

        ViewHolder(View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.productImage);
            productTitle = itemView.findViewById(R.id.productTitle);
            productDescription = itemView.findViewById(R.id.productDescription);
            productPrice = itemView.findViewById(R.id.productPrice);

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