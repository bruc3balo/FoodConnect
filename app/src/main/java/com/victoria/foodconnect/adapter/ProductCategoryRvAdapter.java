package com.victoria.foodconnect.adapter;


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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.globals.productDb.ProductViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.admin.ProductCategoryManagement;
import com.victoria.foodconnect.utils.JsonResponse;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class ProductCategoryRvAdapter extends RecyclerView.Adapter<ProductCategoryRvAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final ProductCategoryManagement activity;
    private final LinkedList<Models.ProductCategory> productCategoryArrayList;
    private final ProductViewModel productViewModel;


    public ProductCategoryRvAdapter(ProductCategoryManagement context, LinkedList<Models.ProductCategory> productCategoryArrayList) {
        this.activity = context;
        this.productCategoryArrayList = productCategoryArrayList;
        productViewModel = new ViewModelProvider(activity).get(ProductViewModel.class);
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_category_item, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Models.ProductCategory productCategory = productCategoryArrayList.get(holder.getAdapterPosition());
        holder.categoryName.setText(productCategory.getName());

        setStatus(productCategory.getDeleted(), holder.deleted);
        setStatus(productCategory.getDisabled(), holder.disabled);

        holder.disabled.setOnClickListener(v -> showConfirmationDialog(productCategory.getDisabled() ? "Enable " + productCategory.getName().concat("?") : "Disable " + productCategory.getName().concat(" ?"), currentStateDeleted -> {
            toggleDisabled(productCategory.getName(), currentStateDeleted, holder.disabled);
            return null;
        }, productCategory.getDisabled()));

        holder.deleted.setOnClickListener(v -> showConfirmationDialog(productCategory.getDeleted() ? "Restore " + productCategory.getName().concat("?") : "Delete " + productCategory.getName().concat(" ?"), currentStateDeleted -> {
            toggleDeleted(productCategory.getName(), currentStateDeleted, holder.deleted);
            return null;
        }, productCategory.getDeleted()));

    }

    private void toggleDeleted(String categoryName, Boolean currentState, ImageButton button)   {
        productViewModel.updateProductCategoryLive(categoryName, new Models.ProductCategoryUpdateForm(!currentState, null)).observe(activity, jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                Toast.makeText(activity, "Failed update " + categoryName, Toast.LENGTH_SHORT).show();
                return;
            }

            JsonResponse response = jsonResponse.get();

            if (!response.isHas_error() && response.isSuccess() && response.getData() != null) {

                try {
                    JsonObject categoryObject = new JsonObject(getObjectMapper().writeValueAsString(jsonResponse.get().getData()));
                    Models.ProductCategory category = getObjectMapper().readValue(new JsonObject(categoryObject.getMap()).toString(), Models.ProductCategory.class);

                    setStatus(category.getDeleted(), button);
                    Toast.makeText(activity, category.getDeleted() ? categoryName + "Deleted" : categoryName + " Restored", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                    refreshList();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void refreshList() {
        activity.getProductCategories();
    }

    private void toggleDisabled(String categoryName, Boolean currentState, ImageButton button) {
        productViewModel.updateProductCategoryLive(categoryName, new Models.ProductCategoryUpdateForm(null, !currentState)).observe(activity, jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                Toast.makeText(activity, "Failed update " + categoryName, Toast.LENGTH_SHORT).show();
                return;
            }

            JsonResponse response = jsonResponse.get();

            if (!response.isHas_error() && response.isSuccess() && response.getData() != null) {

                try {
                    JsonObject categoryObject = new JsonObject(getObjectMapper().writeValueAsString(jsonResponse.get().getData()));
                    Models.ProductCategory category = getObjectMapper().readValue(new JsonObject(categoryObject.getMap()).toString(), Models.ProductCategory.class);

                    setStatus(category.getDeleted(), button);
                    notifyDataSetChanged();
                    Toast.makeText(activity, category.getDisabled() ? categoryName + " Disabled" : categoryName + " Enabled", Toast.LENGTH_SHORT).show();
                    refreshList();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    private void showConfirmationDialog(String infoS, Function<Boolean, Void> yesFunction, Boolean currentState) {
        Dialog d = new Dialog(activity);
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

    private void setStatus(boolean deleted, ImageButton button) {
        if (deleted) {
            button.setImageTintList(ColorStateList.valueOf(Color.RED));
        } else {
            button.setImageTintList(ColorStateList.valueOf(Color.GREEN));
        }
    }

    @Override
    public int getItemCount() {
        return productCategoryArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageButton disabled, deleted;
        TextView categoryName;

        ViewHolder(View itemView) {
            super(itemView);

            disabled = itemView.findViewById(R.id.disabled);
            deleted = itemView.findViewById(R.id.deleted);
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