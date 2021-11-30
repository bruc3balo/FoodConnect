package com.victoria.foodconnect.adapter;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.beneficiary.BuyOrders;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.function.Function;


public class OrderRvAdapter extends RecyclerView.Adapter<OrderRvAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final BuyOrders mContext;
    private final LinkedList<Models.AppUser> userLinkedList;
    private UserViewModel userViewModel;


    public OrderRvAdapter(BuyOrders context, LinkedList<Models.AppUser> userLinkedList) {
        this.mContext = context;
        this.userLinkedList = userLinkedList;
        userViewModel = new ViewModelProvider(context).get(UserViewModel.class);
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

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
        return userLinkedList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RoundedImageView userDp;
        TextView usernameItem, emailItem, roleItem;
        ImageButton verify, disabled, deleted;

        ViewHolder(View itemView) {
            super(itemView);

            userDp = itemView.findViewById(R.id.profilePicItem);
            usernameItem = itemView.findViewById(R.id.usernameItem);
            emailItem = itemView.findViewById(R.id.emailItem);
            roleItem = itemView.findViewById(R.id.roleItem);

            verify = itemView.findViewById(R.id.verify);
            disabled = itemView.findViewById(R.id.disabled);
            deleted = itemView.findViewById(R.id.deleted);

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