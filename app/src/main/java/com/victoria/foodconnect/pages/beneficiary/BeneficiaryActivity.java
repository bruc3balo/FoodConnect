package com.victoria.foodconnect.pages.beneficiary;

import static com.victoria.foodconnect.SplashScreen.logout;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.login.LoginActivity.setWindowColors;
import static com.victoria.foodconnect.pages.ProgressActivity.inSpinnerProgress;
import static com.victoria.foodconnect.pages.ProgressActivity.outSpinnerProgress;
import static com.victoria.foodconnect.pages.seller.SellerActivity.closeDrawer;
import static com.victoria.foodconnect.pages.seller.SellerActivity.setNavDetails;
import static com.victoria.foodconnect.utils.SimilarityClass.alike;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.adapter.BuyProductRvAdapter;
import com.victoria.foodconnect.databinding.ActivityBeneficiaryBinding;
import com.victoria.foodconnect.globals.cartDb.CartViewMode;
import com.victoria.foodconnect.globals.productDb.ProductViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.pages.CartActivity;
import com.victoria.foodconnect.pages.beneficiary.fragment.BuyerDonationsStats;
import com.victoria.foodconnect.pages.beneficiary.fragment.BuyerPurchasesStats;
import com.victoria.foodconnect.pages.beneficiary.fragment.OrdersFragment;
import com.victoria.foodconnect.pages.beneficiary.fragment.ReceiveDonationsFragment;
import com.victoria.foodconnect.pages.transporter.TransporterActivity;
import com.victoria.foodconnect.pages.transporter.fragments.TransporterDonationStats;
import com.victoria.foodconnect.pages.transporter.fragments.TransporterPurchaseStats;
import com.victoria.foodconnect.utils.MyLinkedMap;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import me.relex.circleindicator.CircleIndicator3;

public class BeneficiaryActivity extends AppCompatActivity {

    private ActivityBeneficiaryBinding binding;
    private BuyProductRvAdapter buyProductRvAdapter;
    private final MyLinkedMap<String, LinkedList<Models.Product>> productLinkedList = new MyLinkedMap<>();
    private final LinkedList<Models.Cart> cartLinkedList = new LinkedList<>();
    private final MyLinkedMap<String, LinkedList<Models.Product>> allProductLinkedList = new MyLinkedMap<>();
    private ProductViewModel productViewModel;
    private boolean backPressed = false;
    private TextView badge;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBeneficiaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_hamburger);
        toolbar.setNavigationOnClickListener(view -> binding.getRoot().openDrawer(GravityCompat.START));

        CircleIndicator3 indicator = binding.productCategoryIndicator;

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        if (userRepository != null) {
            userRepository.getUserLive().observe(this, appUser -> {
                if (appUser.isPresent()) {
                    binding.text.setText("Welcome "+appUser.get().getUsername());
                    setNavDetails(appUser.get(), binding.beneNavigation.getHeaderView(0), BeneficiaryActivity.this);
                }
            });
        }

        ViewPager2 productsRv = binding.productsRv;
        buyProductRvAdapter = new BuyProductRvAdapter(BeneficiaryActivity.this, productLinkedList, cartLinkedList);
        productsRv.setAdapter(buyProductRvAdapter);
        buyProductRvAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());
        productsRv.setUserInputEnabled(true);
        //productsRv.setPageTransformer(new DepthPageTransformer());
        productsRv.setOffscreenPageLimit(3);
        productsRv.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        productsRv.requestTransform();
        productsRv.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        productsRv.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        productsRv.setClipToPadding(false);
        productsRv.setClipChildren(false);
        productsRv.setPadding(0,20,0,20);

        indicator.setViewPager(productsRv);

        badge = findViewById(R.id.cartNo);
        binding.layoutforprofileimage.setOnClickListener(v -> goToCart());

        DrawerLayout drawerLayout = binding.getRoot();
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        NavigationView donorNavigation = binding.beneNavigation;
        View header = donorNavigation.getHeaderView(0);
        donorNavigation.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                default:
                case R.id.orders:
                    goToOrders();
                    closeDrawer(drawerLayout);
                    break;

                case R.id.donations:
                    goToDonations();
                    closeDrawer(drawerLayout);
                    break;

                case R.id.purchaseStats:
                    closeDrawer(drawerLayout);
                    goToBuyerPurchases();
                    break;

                case R.id.donationsStats:
                    closeDrawer(drawerLayout);
                    goToBuyerDonations();

                    break;
            }
            return false;
        });

        getCarts();

        // optional
        setWindowColors(this);
    }

    private void goToBuyerDonations() {
        startActivity(new Intent(BeneficiaryActivity.this, BuyerDonationsStats.class));
    }

    private void goToBuyerPurchases() {
        startActivity(new Intent(BeneficiaryActivity.this, BuyerPurchasesStats.class));
    }

    private void goToOrders() {
        startActivity(new Intent(this, OrdersFragment.class));
    }

    private void goToDonations() {
        startActivity(new Intent(this, ReceiveDonationsFragment.class));
    }

    private void goToCart() {
        System.out.println("GOING TO CART");
        startActivity(new Intent(BeneficiaryActivity.this, CartActivity.class));
    }

    public void getCarts() {
        new ViewModelProvider(this).get(CartViewMode.class).getCartList().observe(this, carts -> {

            System.out.println("size of cart is " + carts.size());



            if (carts.isEmpty()) {
                Toast.makeText(BeneficiaryActivity.this, "No items in cart", Toast.LENGTH_SHORT).show();
                cartLinkedList.clear();
                buyProductRvAdapter.notifyDataSetChanged();
                visibleBadge();
                return;
            }




            cartLinkedList.clear();
            cartLinkedList.addAll(carts);
            buyProductRvAdapter.notifyDataSetChanged();
            visibleBadge();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.buyer_menu, menu);
        MenuItem searchViewItem = menu.findItem(R.id.app_bar_search);
        final SearchView searchView = (SearchView) searchViewItem.getActionView();
        searchView.setQueryHint("Search by product name");

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> Toast.makeText(BeneficiaryActivity.this, "" + hasFocus, Toast.LENGTH_SHORT).show());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                new Handler().post(() -> {
                    try {
                        searchProducts(query);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                return false;
            }
        });

        searchView.setOnSearchClickListener(v -> searchProducts(""));

        searchView.setOnCloseListener(() -> {
            searchProducts("");
            return false;
        });

        MenuItem logout = menu.findItem(R.id.logout);
        logout.setOnMenuItemClickListener(item -> {
            logout(BeneficiaryActivity.this);
            return false;
        });


        return super.onCreateOptionsMenu(menu);
    }

    private void searchProducts(String query) {
        productLinkedList.clear();
        buyProductRvAdapter.notifyDataSetChanged();

        if (query.isEmpty()) {
            productLinkedList.putAll(allProductLinkedList);
            buyProductRvAdapter.notifyDataSetChanged();
        } else {

            List<String> list = new ArrayList<>();

            buyProductRvAdapter.notifyDataSetChanged();
            allProductLinkedList.forEach((category, product) -> {
                final boolean[] added = {false};
                product.forEach(p -> {
                    if (alike(query, p.getName())) {
                        System.out.println(category + " : " + added[0]);
                        if (!added[0]) {
                            added[0] = true;
                            productLinkedList.put(category, new LinkedList<>());
                            System.out.println(p.getName() + " : " + category + " " + added[0]);
                        }

                        if (productLinkedList.get(category) != null) {
                            list.add(p.getName());
                            System.out.println("search result " + list);
                            Objects.requireNonNull(productLinkedList.get(category)).add(p);
                            buyProductRvAdapter.notifyDataSetChanged();

                        }
                        //Objects.requireNonNull(
                        //
                    }
                });
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void populateProducts() {
        System.out.println("products populated "+allProductLinkedList.size());
        inSpinnerProgress(binding.pb,null);
        productViewModel.getAllBuyerProducts().observe(this, products -> {
            outSpinnerProgress(binding.pb,null);
            if (!products.isEmpty()) {
                productLinkedList.clear();
                allProductLinkedList.putAll(products);
                productLinkedList.putAll(products);
                buyProductRvAdapter.notifyDataSetChanged();
                System.out.println("products populated "+allProductLinkedList.size());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        backPressed = false;
        visibleBadge();
        populateProducts();
        //getCarts();
    }

    private void visibleBadge() {
        if (cartLinkedList.isEmpty()) {
            binding.layoutforprofileimage.setVisibility(View.GONE);
        } else {
            binding.layoutforprofileimage.setVisibility(View.VISIBLE);
        }

        badge.setText(String.valueOf(cartLinkedList.size()));
    }

    @Override
    public void onBackPressed() {
        if (!backPressed) {
            backPressed = true;
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
            if (!isFinishing() && !isDestroyed()) {
                new Handler().postDelayed((Runnable) () -> backPressed = false, 1000);
            }
        } else {
            finishAffinity();
        }
    }
}