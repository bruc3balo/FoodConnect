package com.victoria.foodconnect.adapter;


import static android.graphics.Color.DKGRAY;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;
import static com.victoria.foodconnect.globals.GlobalVariables.GAS;
import static com.victoria.foodconnect.globals.GlobalVariables.LIQUID;
import static com.victoria.foodconnect.globals.GlobalVariables.SOLID;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.makeramen.roundedimageview.RoundedImageView;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.globals.cartDb.CartViewMode;
import com.victoria.foodconnect.globals.productDb.ProductViewModel;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.CartActivity;
import com.victoria.foodconnect.utils.JsonResponse;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class CartRvAdapter extends RecyclerView.Adapter<CartRvAdapter.ViewHolder> {

    private ItemClickListener mClickListener;

    private final CartActivity mContext;
    private final LinkedList<Models.Cart> cartList;
    private final LinkedList<Models.Product> allProducts;


    public CartRvAdapter(CartActivity context, LinkedList<Models.Cart> cartList, LinkedList<Models.Product> allProducts) {
        this.mContext = context;
        this.cartList = cartList;
        this.allProducts = allProducts;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Models.Cart cart = cartList.get(position);

        Optional<Models.Product> optionalProduct = allProducts.stream().filter(i -> i.getId().equals(cart.getProductId())).findFirst();

        if (!optionalProduct.isPresent()) {
            holder.deleteItem.setOnClickListener(v -> deleteItem("Delete " + cart.getProductId() + " from cart ?", cart.getId(), s -> {
                String id = cart.getProductId();
                removeCartItem(id);
                s.dismiss();
                return id;
            }));
        }

        if (optionalProduct.isPresent()) {

            Models.Product product = optionalProduct.get();

            //Image
            Glide.with(mContext).load(product.getImage()).into(holder.productImage);
            holder.productTitle.setText(product.getName());

            //description
            holder.productDescription.setText(product.getProduct_description());

            //seller
            holder.seller.setText(product.getSellersId() + " ( " + product.getUnitsLeft() + " )");

            holder.removeItem.setOnClickListener(v -> {

                if (cart.getNumberOfItems() - 1 <= 0) {
                    holder.removeItem.setImageTintList(ColorStateList.valueOf(RED));
                } else {
                    holder.removeItem.setImageTintList(ColorStateList.valueOf(DKGRAY));
                    cart.setNumberOfItems(cart.getNumberOfItems() - 1);
                    updateMYCartItem(mContext, mContext, cart);
                }

                new Handler().postDelayed(() -> holder.removeItem.setImageTintList(ColorStateList.valueOf(WHITE)), 400);
            });

            holder.addItem.setOnClickListener(v -> {

                if (cart.getNumberOfItems() + 1 <= product.getUnitsLeft()) {

                    holder.addItem.setImageTintList(ColorStateList.valueOf(DKGRAY));
                    cart.setNumberOfItems(cart.getNumberOfItems() + 1);
                    updateMYCartItem(mContext, mContext, cart);

                    System.out.println("Running ... " + cart.getNumberOfItems());

                } else {
                    holder.addItem.setImageTintList(ColorStateList.valueOf(RED));
                }

                new Handler().postDelayed(() -> holder.addItem.setImageTintList(ColorStateList.valueOf(WHITE)), 400);
            });

            holder.total.setText(String.valueOf(getTotal(cart.getNumberOfItems(), product.getPrice())));

            holder.items.setText(String.valueOf(cart.getNumberOfItems()));

//            holder.items.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    if (s == null || s.toString().isEmpty()) {
//                        holder.total.setText(String.valueOf(getTotal(0, product.getPrice())));
//                    } else {
//                        int wishItems = Integer.parseInt(s.toString());
//                        if (wishItems <= product.getUnitsLeft()) {
//                            cart.setNumberOfItems(wishItems);
//                            updateMYCartItem(mContext, mContext, cart);
//                        } else {
//                            holder.items.setError("Max stock is ( " + product.getUnitsLeft() + " ) ");
//                            holder.items.setText(String.valueOf(product.getUnitsLeft()));
//                            holder.items.requestFocus();
//                        }
//                    }
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
//
//                }
//            });

            holder.total.setText(String.valueOf(getTotal(cart.getNumberOfItems(), product.getPrice())));

            holder.deleteItem.setOnClickListener(v -> deleteItem("Delete " + product.getName() + " from cart ?", cart.getId(), s -> {
                String id = cart.getProductId();
                removeCartItem(id);
                s.dismiss();
                return id;
            }));

            switch (product.getUnit()) {
                default:
                case SOLID:
                    holder.productPrice.setText(product.getPrice().toString().concat(" KSH").concat(" per item"));
                    break;

                case GAS:
                    holder.productPrice.setText(product.getPrice().toString().concat(" KSH").concat(" per item"));
                    break;

                case LIQUID:
                    holder.productPrice.setText(product.getPrice().toString().concat(" KSH").concat(" per item"));
                    break;
            }
        }


    }

    private void updateMYCartItem(ViewModelStoreOwner owner, Activity activity, Models.Cart cart) {
        System.out.println("updating cart ...");
        new ViewModelProvider(owner).get(CartViewMode.class).saveCartItem(cart).observe((LifecycleOwner) activity, newCartItem -> {
            if (!newCartItem.isPresent()) {
                Toast.makeText(activity, "Failed to get updated cart", Toast.LENGTH_SHORT).show();
                return;
            }

        });

    }


    private void removeCartItem(String productId) {
        new ViewModelProvider(mContext).get(CartViewMode.class).deleteCart(productId).observe(mContext, deleted -> {
            if (!deleted) {
                Toast.makeText(mContext, "Failed to delete cart", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(mContext, "Deleted item", Toast.LENGTH_SHORT).show();
        });
    }

    public static BigDecimal getTotal(int items, BigDecimal price) {
        return new BigDecimal(items).multiply(price);
    }

    private void deleteItem(String s, String cartId, Function<Dialog, String> function) {
        Dialog d = new Dialog(mContext);
        d.setContentView(R.layout.yes_no_layout);
        d.show();
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView info = d.findViewById(R.id.infoTv);
        info.setText(s);

        Button no = d.findViewById(R.id.noButton);
        no.setOnClickListener(v -> d.dismiss());


        Button yes = d.findViewById(R.id.yesButton);
        yes.setOnClickListener(v -> function.apply(d));
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RoundedImageView productImage;
        TextView productTitle, productDescription, productPrice, total, seller;
        TextView items;
        ImageButton removeItem, addItem, deleteItem;

        ViewHolder(View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.productImage);
            productTitle = itemView.findViewById(R.id.productTitle);
            productDescription = itemView.findViewById(R.id.productDescription);
            productPrice = itemView.findViewById(R.id.productPrice);
            total = itemView.findViewById(R.id.total);
            seller = itemView.findViewById(R.id.seller);
            removeItem = itemView.findViewById(R.id.removeItem);
            addItem = itemView.findViewById(R.id.addItem);
            items = itemView.findViewById(R.id.items);

            deleteItem = itemView.findViewById(R.id.deleteItem);

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