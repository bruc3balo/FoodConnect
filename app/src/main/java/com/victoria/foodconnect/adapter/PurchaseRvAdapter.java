package com.victoria.foodconnect.adapter;


import static com.victoria.foodconnect.adapter.JobsRvAdapter.getFromLocation;
import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.globals.GlobalVariables.LATITUDE;
import static com.victoria.foodconnect.globals.GlobalVariables.LONGITUDE;

import android.annotation.SuppressLint;
import android.app.Dialog;
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

import com.google.android.gms.maps.model.LatLng;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.seller.fragments.MyOrdersSeller;
import com.victoria.foodconnect.utils.ConvertDate;
import com.victoria.foodconnect.utils.DataOpts;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Function;


public class PurchaseRvAdapter extends RecyclerView.Adapter<PurchaseRvAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final MyOrdersSeller mContext;
    private final LinkedList<Models.Purchase> list;


    public PurchaseRvAdapter(MyOrdersSeller context, LinkedList<Models.Purchase> list) {
        this.mContext = context;
        this.list = list;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.purchase_item, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.Purchase purchase = list.get(position);


        holder.buyer.setText(purchase.getBuyersId());
        holder.time.setText(ConvertDate.formatDateReadable(purchase.getCreated_at()));

        RecyclerView rv = holder.productRv;
        rv.setLayoutManager(new LinearLayoutManager(mContext.requireContext(), RecyclerView.VERTICAL, false));
        ProductListAdapter adapter = new ProductListAdapter(mContext, purchase.getProduct());
        rv.setAdapter(adapter);

        holder.total.setText(calculateTotal() + " KSH");
    }

    private BigDecimal calculateTotal() {
        final BigDecimal[] total = {new BigDecimal(0)};
        list.forEach(purchase -> purchase.getProduct().forEach(product -> total[0] = total[0].add(product.getProduct().getPrice().multiply(new BigDecimal(product.getCount())))));
        return total[0];
    }

    private void showConfirmationDialog(String infoS, Function<Boolean, Void> yesFunction, Boolean currentState) {
        Dialog d = new Dialog(mContext.requireContext());
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
        TextView buyer, time,total;
        RecyclerView productRv;

        ViewHolder(View itemView) {
            super(itemView);

            buyer = itemView.findViewById(R.id.buyer);
            time = itemView.findViewById(R.id.time);
            total = itemView.findViewById(R.id.total);
            productRv = itemView.findViewById(R.id.productRv);

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