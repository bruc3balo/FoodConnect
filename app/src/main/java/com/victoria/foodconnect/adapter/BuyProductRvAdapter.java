package com.victoria.foodconnect.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pagerTransformers.DepthPageTransformer;
import com.victoria.foodconnect.pagerTransformers.ForegroundToBackgroundPageTransformer;
import com.victoria.foodconnect.pagerTransformers.ZoomInTransformer;
import com.victoria.foodconnect.pagerTransformers.ZoomOutPageTransformer;
import com.victoria.foodconnect.utils.MyLinkedMap;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Map;


public class BuyProductRvAdapter extends RecyclerView.Adapter<BuyProductRvAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final Context mContext;
    private final MyLinkedMap<String,LinkedList<Models.Product>> productLinkedList;

    public BuyProductRvAdapter(Context context, MyLinkedMap<String,LinkedList<Models.Product>> productLinkedList) {
        this.mContext = context;
        this.productLinkedList = productLinkedList;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_sell_item, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Map.Entry<String,LinkedList<Models.Product>> entry = productLinkedList.getEntry(position);
        holder.categoryName.setText(entry.getKey().concat(" ( ").concat(String.valueOf(entry.getValue().size())).concat(" )"));

        holder.productListRv.setAdapter(new BuyerRvAdapter(mContext,entry.getValue()));

        holder.productListRv.setUserInputEnabled(true);
        holder.productListRv.setPageTransformer(new ZoomOutPageTransformer());
        holder.productListRv.setOffscreenPageLimit(3);
        holder.productListRv.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        holder.productListRv.requestTransform();
        holder.productListRv.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        holder.productListRv.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                System.out.println("positionOffset : "+positionOffset + " // positionOffsetPixels : "+positionOffsetPixels);
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });



    }

    @Override
    public int getItemCount() {
        return productLinkedList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ViewPager2 productListRv;
        TextView categoryName;

        ViewHolder(View itemView) {
            super(itemView);

            productListRv = itemView.findViewById(R.id.productsRvList);
            categoryName = itemView.findViewById(R.id.categoryName);


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