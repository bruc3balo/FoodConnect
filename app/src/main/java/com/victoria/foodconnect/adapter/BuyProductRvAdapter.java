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
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.beneficiary.BeneficiaryActivity;
import com.victoria.foodconnect.utils.MyLinkedMap;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import me.relex.circleindicator.CircleIndicator2;
import me.relex.circleindicator.CircleIndicator3;


public class BuyProductRvAdapter extends RecyclerView.Adapter<BuyProductRvAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final BeneficiaryActivity mContext;
    private final MyLinkedMap<String,LinkedList<Models.Product>> productLinkedList;
    private final List<Models.Cart> cartList;

    public BuyProductRvAdapter(BeneficiaryActivity context, MyLinkedMap<String,LinkedList<Models.Product>> productLinkedList,List<Models.Cart> cartList) {
        this.mContext = context;
        this.productLinkedList = productLinkedList;
        this.cartList = cartList;
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

        BuyerRvAdapter adapter = new BuyerRvAdapter(mContext,entry.getValue(),cartList);
        holder.productListRv.setAdapter(adapter);
        holder.productListRv.setLayoutManager(new LinearLayoutManager(mContext,RecyclerView.HORIZONTAL,false));
        adapter.notifyDataSetChanged();
//
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
//        pagerSnapHelper.attachToRecyclerView(holder.productListRv);
        holder.indicator2.attachToRecyclerView(holder.productListRv,pagerSnapHelper);


        /*holder.productListRv.setUserInputEnabled(true);
        holder.productListRv.setPageTransformer(new DepthPageTransformer());
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
        });*/

        // optional
        Objects.requireNonNull(holder.productListRv.getAdapter()).registerAdapterDataObserver(holder.indicator2.getAdapterDataObserver());

    }

    @Override
    public int getItemCount() {
        return productLinkedList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RecyclerView productListRv;
        TextView categoryName;
        CircleIndicator2 indicator2;

        ViewHolder(View itemView) {
            super(itemView);
            indicator2 = itemView.findViewById(R.id.productIndicator);
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