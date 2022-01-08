package com.victoria.foodconnect.adapter;


import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.GAS;
import static com.victoria.foodconnect.globals.GlobalVariables.LIQUID;
import static com.victoria.foodconnect.globals.GlobalVariables.SOLID;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.globals.cartDb.CartViewMode;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.beneficiary.BeneficiaryActivity;
import com.victoria.foodconnect.utils.JsonResponse;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


public class BuyerRvAdapter extends RecyclerView.Adapter<BuyerRvAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final BeneficiaryActivity mContext;
    private final LinkedList<Models.Product> productLinkedList;
    private final List<Models.Cart> cartLinkedList;



    public BuyerRvAdapter(BeneficiaryActivity context, LinkedList<Models.Product> productLinkedList, List<Models.Cart> cartLinkedList) {
        this.mContext = context;
        this.productLinkedList = productLinkedList;
        this.cartLinkedList = cartLinkedList;

    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item_buy, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Models.Product product = productLinkedList.get(position);

        try {
            System.out.println("product is "+getObjectMapper().writeValueAsString(product));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        String orange = "#ffffbb33";

        Glide.with(mContext).load(product.getImage()).into(holder.productImage);
        holder.productTitle.setText(product.getName());
        holder.productDescription.setText(product.getProduct_description());

        if (cartLinkedList.stream().map(Models.Cart::getProductId).collect(Collectors.toList()).contains(product.getId())) {
            holder.addToCart.setBackgroundTintList(ColorStateList.valueOf(GREEN));
        } else {
            holder.addToCart.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(orange)));
        }

        holder.productPrice.setText(product.getPrice().toString().concat(" KSH").concat(" per "+product.getUnit()));



        holder.addToCart.setOnClickListener(v -> {


            if (Objects.requireNonNull(cartLinkedList).stream().map(Models.Cart::getProductId).collect(Collectors.toList()).contains(product.getId())) {
                holder.addToCart.setImageTintList(ColorStateList.valueOf(RED));
                Toast.makeText(mContext, "Item already added", Toast.LENGTH_SHORT).show();
            } else {
                holder.addToCart.setImageTintList(ColorStateList.valueOf(GREEN));

                if (userRepository.getUser().isPresent()) {
                    new ViewModelProvider(mContext).get(CartViewMode.class).saveCartItem(new Models.Cart(Objects.requireNonNull(userRepository.getUser().get().getUid()), product.getId(), 0)).observe(mContext, new Observer<Optional<Models.Cart>>() {
                        @Override
                        public void onChanged(Optional<Models.Cart> cart) {
                            if (!cart.isPresent()) {
                                holder.addToCart.setImageTintList(ColorStateList.valueOf(RED));
                                Toast.makeText(mContext, "Failed to add cart item", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            holder.addToCart.setBackgroundTintList(ColorStateList.valueOf(GREEN));
                            mContext.getCarts();

                        }
                    });


                } else {
                    holder.addToCart.setImageTintList(ColorStateList.valueOf(RED));
                }

            }

            //notifyDataSetChanged();
            new Handler().postDelayed(() -> holder.addToCart.setImageTintList(ColorStateList.valueOf(WHITE)), 400);
        });

    }

    @Override
    public int getItemCount() {
        return productLinkedList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView productImage;
        TextView productTitle, productDescription,productPrice;
        ImageButton addToCart;

        ViewHolder(View itemView) {
            super(itemView);
            addToCart = itemView.findViewById(R.id.addToCart);
            productImage = itemView.findViewById(R.id.productImage);
            productTitle = itemView.findViewById(R.id.productTitle);
            productDescription = itemView.findViewById(R.id.productDescription);
            productPrice = itemView.findViewById(R.id.productPrice);

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