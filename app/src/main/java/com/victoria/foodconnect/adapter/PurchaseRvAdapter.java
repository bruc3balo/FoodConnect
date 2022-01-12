package com.victoria.foodconnect.adapter;

import static android.graphics.Color.GREEN;

import static com.victoria.foodconnect.globals.GlobalVariables.MEDIA_TYPE;
import static com.victoria.foodconnect.pages.transporter.MoreActivity.PURCHASE;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.ProgressActivity;
import com.victoria.foodconnect.utils.ConvertDate;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.stream.Collectors;

import me.relex.circleindicator.CircleIndicator3;


public class PurchaseRvAdapter extends RecyclerView.Adapter<PurchaseRvAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final Context mContext;
    private final LinkedList<Models.Purchase> list;
    private final Domain.AppUser user;

    public PurchaseRvAdapter(Context context, Domain.AppUser user, LinkedList<Models.Purchase> list) {
        this.mContext = context;
        this.list = list;
        this.user = user;

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
        Models.Purchase purchase = list.get(holder.getAdapterPosition());

        ViewPager2 productsRv = holder.productRv;
        CircleIndicator3 indicator = holder.indicator;


        if (purchase.isComplete()) {
            holder.completionCard.setCardBackgroundColor(GREEN);
        }

        if (purchase.getAssigned() != null) {
            holder.transported.setText(purchase.getAssigned());
        }

        holder.deliveryLocation.setText(purchase.getAddress());

        holder.buyer.setText(purchase.getBuyersId());
        holder.time.setText(ConvertDate.formatDateReadable(purchase.getCreated_at()));

        productsRv.setOffscreenPageLimit(3);
        productsRv.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        productsRv.requestTransform();
        productsRv.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        final JobProductListAdapter jobProductListAdapter = new JobProductListAdapter(mContext,  new LinkedList<>(purchase.getProduct().stream().filter(i -> i.getProduct().getSellersId().equals(user.getUsername())).collect(Collectors.toList())),true);
        jobProductListAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());
        productsRv.setAdapter(jobProductListAdapter);
        productsRv.setPadding(40, 0, 40, 0);
        indicator.setViewPager(productsRv);

        holder.more.setOnClickListener(v -> mContext.startActivity(new Intent(mContext, ProgressActivity.class).putExtra(PURCHASE, purchase).putExtra(MEDIA_TYPE, PURCHASE)));
        holder.total.setText(calculateTotal() + " KSH");
    }

    private BigDecimal calculateTotal() {
        final BigDecimal[] total = {new BigDecimal(0)};
        list.forEach(purchase -> purchase.getProduct().forEach(product -> {
            if (product.getProduct().getSellersId().equals(user.getUsername())) {
                total[0] = total[0].add(product.getProduct().getPrice().multiply(new BigDecimal(product.getCount())));
            }
        }));
        return total[0];
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
        TextView buyer, time, total, transported,deliveryLocation;
        ViewPager2 productRv;
        CardView completionCard;
        CircleIndicator3 indicator;
        Button more;


        ViewHolder(View itemView) {
            super(itemView);

            transported = itemView.findViewById(R.id.transported);
            more = itemView.findViewById(R.id.more);
            deliveryLocation = itemView.findViewById(R.id.deliveryLocation);
            buyer = itemView.findViewById(R.id.buyer);
            time = itemView.findViewById(R.id.time);
            total = itemView.findViewById(R.id.total);
            productRv = itemView.findViewById(R.id.productsRv);
            completionCard = itemView.findViewById(R.id.completionCard);
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