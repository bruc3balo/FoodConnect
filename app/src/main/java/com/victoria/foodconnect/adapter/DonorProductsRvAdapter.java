package com.victoria.foodconnect.adapter;


import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.makeramen.roundedimageview.RoundedImageView;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.donor.AddItemDonor;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;


public class DonorProductsRvAdapter extends RecyclerView.Adapter<DonorProductsRvAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final AddItemDonor mContext;
    public static int itemPosition = 0;
    public static final LinkedList<Models.DonorItem> donorItems = new LinkedList<>();

    private final ActivityResultLauncher<String> launcher;
    public static final MutableLiveData<List<Models.DonorItem>> donorMutableLiveData = new MutableLiveData<>();


    public DonorProductsRvAdapter(AddItemDonor context, ActivityResultLauncher<String> launcher) {
        this.mContext = context;
        this.launcher = launcher;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.donor_item_page, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Models.DonorItem donorItem = donorItems.get(holder.getAdapterPosition());
        itemPosition = position;

        donorMutableLiveData.setValue(donorItems);
        System.out.println("Position is "+itemPosition);

        try {
            System.out.println(position + " :-> " + getObjectMapper().writeValueAsString(donorItem));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        RoundedImageView itemImage = holder.itemImage;
        EditText name = holder.name, description = holder.description;
        ImageButton removeImage = holder.removeImage, addImage = holder.addImage;
        TextView nameTv = holder.nameTv, descriptionTv = holder.descriptionTv;
        FloatingActionButton nameFab = holder.nameFab, descriptionFab = holder.descriptionFab;

        name.setText(donorItem.getName());
        nameTv.setText(donorItem.getName());

        description.setText(donorItem.getDescription());
        descriptionTv.setText(donorItem.getDescription());


        setDescriptionState(description,descriptionFab,descriptionTv,donorItem);
        setNameState(name,nameFab,nameTv,donorItem);

        if (donorItem.getImage() != null) {
            Glide.with(mContext).load(Uri.parse(donorItem.getImage())).into(itemImage);
        } else {
            itemImage.setImageURI(null);
            itemImage.setImageResource(android.R.color.transparent);
        }

        addImage.setOnClickListener(v -> {
            itemPosition = position;
            launcher.launch("image/*");
        });

        removeImage.setOnClickListener(v -> showConfirmationDialog("Do you want to remove this image ?", zero -> {
            itemPosition = position;
            donorItem.setImage(null);
            notifyDataSetChanged();
            return null;
        }));


    }

    private void setDescriptionState(EditText field, FloatingActionButton button, TextView tv, Models.DonorItem donorItem) {
        if (tv.getVisibility() == View.VISIBLE && field.getVisibility() == View.GONE) { //to editing
            button.setImageResource(R.drawable.ic_brush);

            field.setOnFocusChangeListener(null);

            button.setOnClickListener(v -> {
                String descriptionString = donorItem.getDescription() != null ? donorItem.getDescription() : "";

                tv.setVisibility(View.GONE);
                field.setVisibility(View.VISIBLE);

                field.setText(descriptionString);
                tv.setText(descriptionString);
                button.setImageResource(R.drawable.tick);
                notifyDataSetChanged();
            });

        } else if (tv.getVisibility() == View.GONE && field.getVisibility() == View.VISIBLE) { //to saving
            button.setImageResource(R.drawable.tick);

            field.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    String descriptionString = field.getText().toString().isEmpty() ? null : field.getText().toString();

                    tv.setVisibility(View.VISIBLE);
                    field.setVisibility(View.GONE);

                    button.setImageResource(R.drawable.ic_brush);
                    donorItem.setDescription(descriptionString);
                   // notifyDataSetChanged();
                }
            });

            button.setOnClickListener(v -> {
                String descriptionString = field.getText().toString().isEmpty() ? null : field.getText().toString();

                tv.setVisibility(View.VISIBLE);
                field.setVisibility(View.GONE);

                button.setImageResource(R.drawable.ic_brush);
                donorItem.setDescription(descriptionString);
                notifyDataSetChanged();

            });
        }
    }

    private void setNameState(EditText field, FloatingActionButton button, TextView tv, Models.DonorItem donorItem) {
        if (tv.getVisibility() == View.VISIBLE && field.getVisibility() == View.GONE) { //to editing

            button.setImageResource(R.drawable.ic_brush);

            field.setOnFocusChangeListener(null);

            button.setOnClickListener(v -> {
                String nameString = donorItem.getName() != null ? donorItem.getName() : "";

                tv.setVisibility(View.GONE);
                field.setVisibility(View.VISIBLE);

                field.setText(nameString);
                tv.setText(nameString);
                button.setImageResource(R.drawable.tick);
                notifyDataSetChanged();
            });

        } else if (tv.getVisibility() == View.GONE && field.getVisibility() == View.VISIBLE) { //to view and save

            button.setImageResource(R.drawable.tick);


            field.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    String nameString = field.getText().toString().isEmpty() ? null : field.getText().toString();

                    tv.setVisibility(View.VISIBLE);
                    field.setVisibility(View.GONE);

                    field.setText(nameString);
                    tv.setText(nameString);
                    button.setImageResource(R.drawable.ic_brush);
                    donorItem.setName(nameString);
                    //notifyDataSetChanged();
                }
            });

            button.setOnClickListener(v -> {
                String nameString = field.getText().toString().isEmpty() ? null : field.getText().toString();

                tv.setVisibility(View.VISIBLE);
                field.setVisibility(View.GONE);

                field.setText(nameString);
                tv.setText(nameString);
                button.setImageResource(R.drawable.ic_brush);
                donorItem.setName(nameString);
                notifyDataSetChanged();
            });
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
        return donorItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RoundedImageView itemImage;
        EditText name, description;
        ImageButton removeImage, addImage;
        TextView nameTv, descriptionTv;
        FloatingActionButton nameFab, descriptionFab;

        ViewHolder(View itemView) {
            super(itemView);

            itemImage = itemView.findViewById(R.id.itemImage);
            name = itemView.findViewById(R.id.name);
            nameTv = itemView.findViewById(R.id.nameTv);
            description = itemView.findViewById(R.id.description);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            removeImage = itemView.findViewById(R.id.removeImage);

            addImage = itemView.findViewById(R.id.addImage);
            nameFab = itemView.findViewById(R.id.nameFab);
            descriptionFab = itemView.findViewById(R.id.descriptionFab);

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