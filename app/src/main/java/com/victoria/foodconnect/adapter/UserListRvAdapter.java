package com.victoria.foodconnect.adapter;


import static com.victoria.foodconnect.pages.donor.AddItemDonor.donationCreationForm;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.victoria.foodconnect.R;
import com.victoria.foodconnect.models.Models;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.function.Function;


public class UserListRvAdapter extends RecyclerView.Adapter<UserListRvAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final Context mContext;
    private final LinkedList<Models.AppUser> userList;


    public UserListRvAdapter(Context context, LinkedList<Models.AppUser> userList) {
        this.mContext = context;
        this.userList = userList;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.beneficiary_item, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Models.AppUser user = userList.get(holder.getAdapterPosition());


        if (user.getNames() != null) {
            holder.names.setText(user.getNames());
        }

        if (user.getUsername() != null) {
            holder.username.setText(user.getUsername());
        }

        if (donationCreationForm != null && donationCreationForm.getBeneficiary() != null && donationCreationForm.getBeneficiary().equals(user.getUsername())) {
            holder.selected.setImageResource(R.drawable.tick);
            holder.detailsLayout.setOnClickListener(v -> showConfirmationDialog("Do you want to remove " + user.getUsername() + " as a beneficiary ?", integer -> {
                donationCreationForm.setBeneficiary(null);
                notifyDataSetChanged();
                return null;
            }));

            holder.selected.setOnClickListener(v -> showConfirmationDialog("Do you want to remove " + user.getUsername() + " as a beneficiary ?", integer -> {
                donationCreationForm.setBeneficiary(null);
                notifyDataSetChanged();
                return null;
            }));
        } else {
            holder.selected.setImageResource(R.color.white);
            holder.detailsLayout.setOnClickListener(v -> showConfirmationDialog(donationCreationForm.getBeneficiary() == null ?"Do you want to put " + user.getUsername() + " as a beneficiary ?" : "DO you want to replace "+donationCreationForm.getBeneficiary() + " to "+user.getUsername() + " as beneficiary?", integer -> {
                donationCreationForm.setBeneficiary(user.getUsername());
                notifyDataSetChanged();
                return null;
            }));

            holder.selected.setOnClickListener(v -> showConfirmationDialog(donationCreationForm.getBeneficiary() == null ?"Do you want to put " + user.getUsername() + " as a beneficiary ?" : "DO you want to replace "+donationCreationForm.getBeneficiary() + " to "+user.getUsername() + " as beneficiary?", integer -> {
                donationCreationForm.setBeneficiary(user.getUsername());
                notifyDataSetChanged();
                return null;
            }));
        }



    }

    private void showConfirmationDialog(String infoS, Function<Integer, Void> yesFunction) {
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
            yesFunction.apply(0);
            d.dismiss();
        });
    }



    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView username, names;
        ImageView selected;
        LinearLayout detailsLayout;

        ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            names = itemView.findViewById(R.id.names);
            selected = itemView.findViewById(R.id.selected);
            detailsLayout = itemView.findViewById(R.id.detailsLayout);


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