package com.victoria.foodconnect.adapter;


import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.makeramen.roundedimageview.RoundedImageView;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.admin.fragments.UsersFragment;
import com.victoria.foodconnect.utils.ProgressStatus;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;


public class ProgressRvAdapter extends RecyclerView.Adapter<ProgressRvAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final Activity mContext;
    private final LinkedList<Models.ProgressModel> list;

    public static final int BOTTOM_NODE = 2;
    public static final int MIDDLE_NODE = 1;
    public static final int TOP_NODE = 0;
    private int VT = 0;


    public ProgressRvAdapter(Activity context, LinkedList<Models.ProgressModel> list) {
        this.mContext = context;
        this.list = list;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        switch (viewType) {
            default:
            case TOP_NODE:
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.top_tree_item, parent, false));

            case MIDDLE_NODE:
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.multi_tree_item, parent, false));

            case BOTTOM_NODE:
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_tree_item, parent, false));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.ProgressModel progressModel = list.get(holder.getAdapterPosition());
        holder.title.setText(progressModel.getName());
        setStatus(progressModel, holder);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setStatus(Models.ProgressModel progressModel, ViewHolder holder) {
        switch (progressModel.getStatus()) {
            case NEW:
                switch (VT) {
                    default:
                    case TOP_NODE:
                        Glide.with(mContext).load(R.drawable.ic_circle).into(holder.image);
                        holder.image.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.darker_gray)));
                        holder.image.setImageTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));
                        holder.image.setBackground(mContext.getDrawable(R.drawable.ic_circle_button_white));
                        holder.to.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.darker_gray)));

                        break;

                    case MIDDLE_NODE:
                        Glide.with(mContext).load(R.drawable.ic_circle).into(holder.image);
                        holder.image.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.darker_gray)));
                        holder.image.setImageTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.darker_gray)));
                        holder.image.setBackground(mContext.getDrawable(R.drawable.ic_circle_button_white));
                        holder.to.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.darker_gray)));
                        holder.from.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.darker_gray)));

                        break;

                    case BOTTOM_NODE:
                        Glide.with(mContext).load(R.drawable.ic_circle).into(holder.image);
                        holder.image.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.darker_gray)));
                        holder.image.setImageTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.darker_gray)));
                        holder.image.setBackground(mContext.getDrawable(R.drawable.ic_circle_button_white));
                        holder.from.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.darker_gray)));
                        break;
                }
                break;

            case PROGRESS:
                switch (VT) {
                    default:
                    case TOP_NODE:
                        Glide.with(mContext).load(R.drawable.ic_circle).into(holder.image);
                        holder.image.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));
                        holder.image.setImageTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));
                        holder.image.setBackground(mContext.getDrawable(R.drawable.ic_circle_button_white));
                        holder.to.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));

                        break;

                    case MIDDLE_NODE:
                        Glide.with(mContext).load(R.drawable.ic_circle).into(holder.image);
                        holder.image.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));
                        holder.image.setImageTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));
                        holder.image.setBackground(mContext.getDrawable(R.drawable.ic_circle_button_white));
                        holder.to.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));
                        holder.from.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));

                        break;

                    case BOTTOM_NODE:

                        Glide.with(mContext).load(R.drawable.ic_circle).into(holder.image);
                        holder.image.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));
                        holder.image.setImageTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));
                        holder.image.setBackground(mContext.getDrawable(R.drawable.ic_circle_button_white));
                        holder.from.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));


                        break;
                }
                break;

            case COMPLETE:

                switch (VT) {
                    default:
                    case TOP_NODE:
                        Glide.with(mContext).load(R.drawable.tick).into(holder.image);
                        holder.image.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));
                        holder.image.setImageTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));
                        holder.image.setBackground(mContext.getDrawable(R.drawable.ic_circle_button_white));
                        holder.to.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));

                        break;

                    case MIDDLE_NODE:
                        Glide.with(mContext).load(R.drawable.tick).into(holder.image);
                        holder.image.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));
                        holder.image.setImageTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));
                        holder.image.setBackground(mContext.getDrawable(R.drawable.ic_circle_button_white));
                        holder.to.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));
                        holder.from.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));

                        break;

                    case BOTTOM_NODE:
                        Glide.with(mContext).load(R.drawable.tick).into(holder.image);
                        holder.image.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));
                        holder.image.setImageTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));
                        holder.image.setBackground(mContext.getDrawable(R.drawable.ic_circle_button_white));
                        holder.from.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_dark)));

                        break;
                }

                break;

            case FAILED:
                switch (VT) {
                    default:
                    case TOP_NODE:
                        Glide.with(mContext).load(R.drawable.x).into(holder.image);
                        holder.image.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_red_dark)));
                        holder.image.setImageTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.darker_gray)));
                        holder.image.setBackground(mContext.getDrawable(R.drawable.ic_circle_button_white));
                        holder.to.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_red_dark)));

                        break;

                    case MIDDLE_NODE:

                        Glide.with(mContext).load(R.drawable.x).into(holder.image);
                        holder.image.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_red_dark)));
                        holder.image.setImageTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.darker_gray)));
                        holder.image.setBackground(mContext.getDrawable(R.drawable.ic_circle_button_white));
                        holder.to.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_red_dark)));
                        holder.from.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_light)));

                        break;

                    case BOTTOM_NODE:
                        Glide.with(mContext).load(R.drawable.x).into(holder.image);
                        holder.image.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_red_dark)));
                        holder.image.setImageTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.darker_gray)));
                        holder.image.setBackground(mContext.getDrawable(R.drawable.ic_circle_button_white));
                        holder.from.setBackgroundTintList(ColorStateList.valueOf(mContext.getColor(android.R.color.holo_green_light)));
                        break;
                }

                break;
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            VT = TOP_NODE;
        } else if (position == list.size() - 1) {
            VT = BOTTOM_NODE;
        } else {
            VT = MIDDLE_NODE;
        }

        return VT;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        ImageView image;
        View from, to;

        ViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            image = itemView.findViewById(R.id.image);
            from = itemView.findViewById(R.id.fromView);
            to = itemView.findViewById(R.id.toView);

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