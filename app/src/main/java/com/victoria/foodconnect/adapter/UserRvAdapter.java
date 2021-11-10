package com.victoria.foodconnect.adapter;


import static com.victoria.foodconnect.globals.GlobalVariables.GAS;
import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.globals.GlobalVariables.LIQUID;
import static com.victoria.foodconnect.globals.GlobalVariables.SOLID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.models.Models;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;


public class UserRvAdapter extends RecyclerView.Adapter<UserRvAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final Context mContext;
    private final LinkedList<Models.AppUser> userLinkedList;


    public UserRvAdapter(Context context, LinkedList<Models.AppUser> userLinkedList) {
        this.mContext = context;
        this.userLinkedList = userLinkedList;

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

        Models.AppUser user = userLinkedList.get(position);

        if (!user.getProfile_picture().equals(HY)) {
            Glide.with(mContext).load(user.getProfile_picture()).into(holder.userDp);
        } else {
            Glide.with(mContext).load(mContext.getDrawable(R.drawable.ic_give_food)).into(holder.userDp);
        }

        if (user.getNames()!= null) {
            holder.usernameItem.setText(user.getNames());
        }

        if (user.getEmail_address() != null) {
            holder.emailItem.setText(user.getEmail_address());
        }

        if (user.getRole() != null) {
            holder.roleItem.setText(user.getRole().getName());
        }

        if (user.getVerified() != null) {
            if (user.getVerified()) {
                holder.verify.setImageResource(R.drawable.tick);
            } else {
                holder.verify.setImageResource(R.drawable.x);
            }
        }

        if (user.getDeleted() != null) {
            if (user.getDeleted()) {
                holder.deleted.setImageTintList(ColorStateList.valueOf(Color.RED));
            } else {
                holder.deleted.setImageTintList(ColorStateList.valueOf(Color.GREEN));
            }
        }

        if (user.getDisabled() != null) {
            if (user.getDisabled()) {
                holder.disabled.setImageTintList(ColorStateList.valueOf(Color.RED));
            } else {
                holder.disabled.setImageTintList(ColorStateList.valueOf(Color.GREEN));
            }
        }

    }

    @Override
    public int getItemCount() {
        return userLinkedList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RoundedImageView userDp;
        TextView usernameItem, emailItem,roleItem;
        ImageButton verify,disabled,deleted;

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