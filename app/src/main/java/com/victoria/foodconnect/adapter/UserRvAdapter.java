package com.victoria.foodconnect.adapter;


import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.admin.fragments.UsersFragment;
import com.victoria.foodconnect.utils.JsonResponse;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;

import io.vertx.core.json.JsonObject;
import retrofit2.Response;


public class UserRvAdapter extends RecyclerView.Adapter<UserRvAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final UsersFragment mContext;
    private final LinkedList<Models.AppUser> userLinkedList;
    private UserViewModel userViewModel;


    public UserRvAdapter(UsersFragment context, LinkedList<Models.AppUser> userLinkedList) {
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

        Models.AppUser user = userLinkedList.get(position);

        if (!user.getProfile_picture().equals(HY)) {
            Glide.with(mContext).load(user.getProfile_picture()).into(holder.userDp);
        } else {
            Glide.with(mContext).load(mContext.requireActivity().getDrawable(R.drawable.ic_give_food)).into(holder.userDp);
        }

        if (user.getNames() != null) {
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
                holder.verify.setOnClickListener(null);
            } else {
                holder.verify.setImageResource(R.drawable.x);
                holder.verify.setOnClickListener(v -> verifyDonor(user, position));
            }
        }

        if (user.getDeleted() != null) {
            holder.deleted.setOnClickListener(v -> showConfirmationDialog(user.getDeleted() ? "Restore " + user.getUsername().concat("?") : "Delete " + user.getUsername().concat(" ?"),currentStateDeleted -> {
                toggleDeleted(user.getUid(),user.getDeleted(), holder.deleted);
                return null;
            },user.getDeleted()));

            if (user.getDeleted()) {
                holder.deleted.setImageTintList(ColorStateList.valueOf(Color.RED));
            } else {
                holder.deleted.setImageTintList(ColorStateList.valueOf(Color.GREEN));
            }
        }

        if (user.getDisabled() != null) {
            holder.disabled.setOnClickListener(v -> showConfirmationDialog(user.getDisabled() ? "Enable " + user.getUsername().concat("?") : "Disable " + user.getUsername().concat(" ?"),currentStateDeleted -> {
                toggleDisabled(user.getUid(),user.getDisabled(), holder.disabled);
                return null;
            },user.getDisabled()));
            if (user.getDisabled()) {
                holder.disabled.setImageTintList(ColorStateList.valueOf(Color.RED));
            } else {
                holder.disabled.setImageTintList(ColorStateList.valueOf(Color.GREEN));
            }
        }

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

    @SuppressLint("SetTextI18n")
    private void verifyDonor(Models.AppUser user, int position) {
        if (user.getRole().getName().equals("ROLE_DONOR")) {
            Dialog d = new Dialog(mContext.requireContext());
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            d.setContentView(R.layout.yes_no_layout);
            d.show();

            TextView info = d.findViewById(R.id.infoTv);
            info.setText("Are you sure you want to verify " + user.getNames() + " as a donor");
            Button no = d.findViewById(R.id.noButton);
            no.setOnClickListener(v -> d.dismiss());
            Button yes = d.findViewById(R.id.yesButton);
            yes.setOnClickListener(v -> {
                Models.UserUpdateForm form = new Models.UserUpdateForm();
                form.setVerified(true);

                userViewModel.updateAUser(user.getUid(), form).observe(mContext, jsonResponseResponse -> {
                    if (!jsonResponseResponse.isPresent() || jsonResponseResponse.get().body() == null || !jsonResponseResponse.get().body().isSuccess() || jsonResponseResponse.get().body().isHas_error() || jsonResponseResponse.get().body().getData() == null) {
                        Toast.makeText(mContext.requireContext(), "Failed verification of " + user.getNames(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    d.dismiss();

                    JsonResponse response = jsonResponseResponse.get().body();

                    if (response == null) {
                        Toast.makeText(mContext.requireContext(), "Failed verification of " + user.getNames(), Toast.LENGTH_SHORT).show();
                        return;
                    }


                    try {
                        JsonObject userJson = new JsonObject(getObjectMapper().writeValueAsString(response.getData()));
                        Models.AppUser verifiedUser = getObjectMapper().readValue(userJson.toString(), Models.AppUser.class);

                        if (verifiedUser.getVerified()) {
                            Toast.makeText(mContext.requireContext(), "User is verified", Toast.LENGTH_SHORT).show();
                            refreshList();
                        } else {
                            Toast.makeText(mContext.requireContext(), "Failed to verify user", Toast.LENGTH_SHORT).show();
                        }

                        notifyItemChanged(position, verifiedUser);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                });
            });
        } else {
            Toast.makeText(mContext.requireContext(), "User is not a donor", Toast.LENGTH_SHORT).show();
        }

    }

    private void toggleDeleted(String uid, Boolean currentState, ImageButton button) {
        userViewModel.updateAUser(uid, new Models.UserUpdateForm(!currentState,null)).observe(mContext, jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                Toast.makeText(mContext.requireContext(), "Failed update " + uid, Toast.LENGTH_SHORT).show();
                return;
            }

            JsonResponse response = jsonResponse.get().body();

            if (response == null) {
                Toast.makeText(mContext.requireContext(), "Failed update " + uid, Toast.LENGTH_SHORT).show();
                return;
            }


            if (!response.isHas_error() && response.isSuccess() && response.getData() != null) {

                try {
                    JsonObject userJson = new JsonObject(getObjectMapper().writeValueAsString(response.getData()));
                    Models.AppUser verifiedUser = getObjectMapper().readValue(userJson.toString(), Models.AppUser.class);

                    setStatus(verifiedUser.getDeleted(), button);
                    Toast.makeText(mContext.requireContext(), verifiedUser.getDeleted() ? verifiedUser.getUsername() + " Deleted" : verifiedUser.getUsername() + " Restored", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                    refreshList();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void toggleDisabled(String uid, Boolean currentState, ImageButton button) {
        userViewModel.updateAUser(uid, new Models.UserUpdateForm(null,!currentState)).observe(mContext, jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                Toast.makeText(mContext.requireContext(), "Failed update " + uid, Toast.LENGTH_SHORT).show();
                return;
            }

            JsonResponse response = jsonResponse.get().body();

            if (response == null) {
                Toast.makeText(mContext.requireContext(), "Failed update " + uid, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!response.isHas_error() && response.isSuccess() && response.getData() != null) {

                try {
                    JsonObject userJson = new JsonObject(getObjectMapper().writeValueAsString(response.getData()));
                    Models.AppUser disabledUser = getObjectMapper().readValue(userJson.toString(), Models.AppUser.class);

                    setStatus(disabledUser.getDisabled(), button);
                    notifyDataSetChanged();
                    Toast.makeText(mContext.requireContext(), disabledUser.getDisabled() ? disabledUser.getUsername() + " Disabled" : disabledUser.getUsername() + " Enabled", Toast.LENGTH_SHORT).show();
                    refreshList();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setStatus(boolean deleted, ImageButton button) {
        if (deleted) {
            button.setImageTintList(ColorStateList.valueOf(Color.RED));
        } else {
            button.setImageTintList(ColorStateList.valueOf(Color.GREEN));
        }
    }


    private void refreshList() {
        mContext.getUsers();
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