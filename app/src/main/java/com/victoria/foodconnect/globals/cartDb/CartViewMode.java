package com.victoria.foodconnect.globals.cartDb;

import static com.victoria.foodconnect.globals.GlobalVariables.CART;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.victoria.foodconnect.models.Models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CartViewMode extends AndroidViewModel {

    private FirebaseDatabase database;
    private DatabaseReference myRef;


    public CartViewMode(@NonNull Application application) {
        super(application);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(CART);
        myRef.keepSynced(true);
    }

    private MutableLiveData<List<Models.Cart>> getMyCart() {
        MutableLiveData<List<Models.Cart>> mutableLiveData = new MutableLiveData<>();

        List<Models.Cart> cartList = new ArrayList<>();



        myRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {

                //for children
                myRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        System.out.println("ITEM ADDED");

                        Models.Cart cart = snapshot.getValue(Models.Cart.class);
                        cartList.add(cart);


                        System.out.println("cart in size " + cartList.size());
                        System.out.println("snap in size " + userSnapshot.getChildrenCount());

                        if (cartList.size() == userSnapshot.getChildrenCount()) {
                            mutableLiveData.setValue(cartList);
                            System.out.println("cart list is set ");
                        }

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        System.out.println("ITEM CHANGED");

                        Models.Cart newCart = snapshot.getValue(Models.Cart.class);
                        Optional<Models.Cart> oldCart = cartList.stream().filter(i -> {
                            assert newCart != null;
                            return i.getProductId().equals(newCart.getProductId());
                        }).findFirst();

                        if (oldCart.isPresent()) {
                            cartList.set(cartList.indexOf(oldCart.get()), newCart);
                        } else {
                            cartList.add(newCart);

                        }

                        mutableLiveData.setValue(cartList);
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        System.out.println("ITEM REMOVED");
                        System.out.println(cartList.size() + " old cart size");
                        Models.Cart removedCart = snapshot.getValue(Models.Cart.class);
                        assert removedCart != null;
                        System.out.println("ITEM REMOVED "+removedCart.getProductId());
                        cartList.removeIf(i -> i.getProductId().equals(Objects.requireNonNull(removedCart).getProductId()));
                        mutableLiveData.setValue(cartList);
                        System.out.println(cartList.size() + " new cart size");
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        try {
                            System.out.println(" my cart  moved :: " + getObjectMapper().writeValueAsString(snapshot.getValue()));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        System.out.println(" my cart cancelled  :: " + error.getMessage());
                    }
                });



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return mutableLiveData;
    }

    private MutableLiveData<Optional<Models.Cart>> saveNewCart(Models.Cart cart) {
        MutableLiveData<Optional<Models.Cart>> mutableLiveData = new MutableLiveData<>();

        myRef.child(cart.getUserId()).child(cart.getProductId()).setValue(cart).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mutableLiveData.setValue(Optional.of(cart));
            } else {
                mutableLiveData.setValue(Optional.empty());
            }
        });

        return mutableLiveData;
    }

    private MutableLiveData<Boolean> deleteACart(String productId) {

        MutableLiveData<Boolean> mutableLiveData = new MutableLiveData<>();

        myRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(productId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println("DELETED " + productId);
                mutableLiveData.setValue(true);
            } else {
                System.out.println("FAILED TO DELETE " + productId);
                mutableLiveData.setValue(false);
            }
        });

        return mutableLiveData;
    }

    //expose

    public LiveData<List<Models.Cart>> getCartList() {
        return getMyCart();
    }

    public LiveData<Optional<Models.Cart>> saveCartItem(Models.Cart cart) {
        return saveNewCart(cart);
    }

    public LiveData<Boolean> deleteCart(String productId) {
        return deleteACart(productId);
    }
}
