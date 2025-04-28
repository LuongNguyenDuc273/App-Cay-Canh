package com.adrnc_g02.appcaycanh;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import Model.Cart;
import Model.Order;
import Model.OrderDetail;
import Model.Product;

public class ProductDetail extends AppCompatActivity {

    // UI elements
    private ImageView imageView;
    private ImageButton back, btnAddToCart;
    private RecyclerView rvReview;
    private TextView productName, productPrice, productCategory, productDes, productStatus;
    private Button btnBuy, btnIncreaseQuantity, btnDecreaseQuantity;
    private EditText tvQuantity;

    // Adapter
    private ReviewAdapter reviewAdapter;

    // Data
    private List<Product> allProducts = new ArrayList<>();
    private int productQuantity = 0;
    private int currentQuantity = 1; // Gia tri mac dinh

    // Helper class
    private GenericFunction genericFunction = new GenericFunction();
    private OrderManagment orderManagment = new OrderManagment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Thiet lap giao dien co ban
        setupUI();

        // Khoi tao view
        initializeViews();

        // Load thong tin san pham
        loadProductInfo();

        // Thiet lap listeners
        setupListeners();

        // Load danh gia san pham
        loadProductReviews();
    }

    /**
     * Thiet lap giao dien co ban va xu ly insets cho he thong.
     */
    private void setupUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Khoi tao cac view.
     */
    private void initializeViews() {
        imageView = findViewById(R.id.ivProductImage);
        productName = findViewById(R.id.tvProductName);
        productPrice = findViewById(R.id.tvProductPrice);
        productDes = findViewById(R.id.tvProductDescription);
        productCategory = findViewById(R.id.tvProductCategory);
        productStatus = findViewById(R.id.tvProductStatus);
        back = findViewById(R.id.btnBack);
        btnBuy = findViewById(R.id.btnBuyNow);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnIncreaseQuantity = findViewById(R.id.btnIncreaseQuantity);
        btnDecreaseQuantity = findViewById(R.id.btnDecreaseQuantity);
        tvQuantity = findViewById(R.id.tvQuantity);
        rvReview = findViewById(R.id.rvReview);
        rvReview.setLayoutManager(new LinearLayoutManager(this));

        // Dat gia tri ban dau cho tvQuantity
        tvQuantity.setText(String.valueOf(currentQuantity));
    }

    /**
     * Thiet lap cac listeners cho cac button.
     */
    private void setupListeners() {
        // Su kien nut back
        back.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        });

        // Gan su kien cho nut tang so luong
        btnIncreaseQuantity.setOnClickListener(v -> {
            int checkquantity = currentQuantity + 1;
            if (checkquantity > productQuantity) {
                Toast.makeText(ProductDetail.this, "So luong da dat gioi han", Toast.LENGTH_SHORT).show();
            } else {
                currentQuantity++;
                tvQuantity.setText(String.valueOf(currentQuantity));
            }
        });

        // Gan su kien cho nut giam so luong
        btnDecreaseQuantity.setOnClickListener(v -> {
            if (currentQuantity > 1) {
                currentQuantity--;
                tvQuantity.setText(String.valueOf(currentQuantity));
            }
        });

        // Su kien them vao gio hang
        btnAddToCart.setOnClickListener(view -> {
            addToCart();
        });

        // SU kien mua hang
        btnBuy.setOnClickListener(view -> {
            newOrder();
        });
    }

    /**
     * Hien thi thong tin san pham
     */
    public void loadProductInfo() {
        String imageUrl = getIntent().getStringExtra("Image");
        String name = getIntent().getStringExtra("Name");
        String price = getIntent().getStringExtra("Price");
        String line = getIntent().getStringExtra("Line");
        int quantity = getIntent().getStringExtra("Quantity") != null ?
                Integer.parseInt(getIntent().getStringExtra("Quantity")) : 0;
        productQuantity = quantity;
        Log.d("Kiem Tra", "So luong" + quantity);
        String des = getIntent().getStringExtra("Description");
        String key = getIntent().getStringExtra("Key");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Line").child(line);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String lineName = snapshot.child("nameLine").getValue(String.class);
                    if (lineName != null) {
                        Log.d("FirebaseData", "Ten Line: " + lineName);
                        productCategory.setText(lineName);
                    }
                } else {
                    Log.d("FirebaseData", "LineID khong ton tai!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Loi: " + error.getMessage());
            }
        });
        if (quantity > 0) {
            productStatus.setText("Con hang: " + quantity + " san pham");
        } else {
            btnBuy.setEnabled(false);
            btnAddToCart.setEnabled(false);
            btnAddToCart.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray)));
            btnAddToCart.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            btnIncreaseQuantity.setEnabled(false);
            btnDecreaseQuantity.setEnabled(false);
            productStatus.setText("Tam het hang");
            productStatus.setTextColor(getResources().getColor(R.color.red));
            productStatus.setBackground(getResources().getDrawable(R.drawable.bg_unavailable_status));
            btnBuy.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray)));
        }
        Glide.with(this).load(imageUrl).into(imageView);
        productName.setText(name);
        productPrice.setText(price);
        productDes.setText(des);
    }

    /**
     * Them san pham vao gio hang
     */
    public void addToCart() {
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        String IDProc = getIntent().getStringExtra("Key");
        String IDCus = cUser.getUid();
        int Quantity = Integer.parseInt(tvQuantity.getText().toString());

        // Reference to the specific cart item
        DatabaseReference cartItemRef = genericFunction.getTableReference("Customer")
                .child(IDCus).child("Cart").child(IDProc);

        // Check if the product already exists in the cart
        cartItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Product already exists in cart, get current quantity and add new quantity
                    Cart existingCart = snapshot.getValue(Cart.class);
                    if (existingCart != null) {
                        int newQuantity = existingCart.getQuantity() + Quantity;
                        existingCart.setQuantity(newQuantity);
                        cartItemRef.setValue(existingCart);
                    }
                } else {
                    // Product doesn't exist in cart, add as new
                    Cart cart = new Cart(IDCus, IDProc, Quantity);
                    cartItemRef.setValue(cart);
                }
                Toast.makeText(ProductDetail.this, "Thếm sản phẩm vào giỏ hàng thành công", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CartError", "Failed to check cart: " + error.getMessage());
            }
        });
    }

    /**
     * Chuyen sang trang dat hang
     */
    private void newOrder() {
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        String IDProc = getIntent().getStringExtra("Key");
        String IDCus = cUser.getUid();
        int Quantity = Integer.parseInt(tvQuantity.getText().toString());
        //orderManagment.addOrder(IDProc, Quantity, getIntent().getStringExtra("Price"));
        Cart cart = new Cart(IDCus, IDProc, Quantity);
        genericFunction.getTableReference("Customer")
                .child(IDCus).child("Cart").child(IDProc).setValue(cart);
        ArrayList<String> selectedProductIds = new ArrayList<>();
        selectedProductIds.add(IDProc);
        // Start OrderConfirmation activity
        Intent intent = new Intent(ProductDetail.this, OrderConfirmation.class);
        intent.putStringArrayListExtra("selected_cart_items", selectedProductIds);
        intent.putExtra("is_from_product_detail", true);
        startActivity(intent);
    }

    /**
     * Load danh sach san pham tu database
     * @param runnable Callback sau khi load xong san pham.
     */
    private void loadProductsFromDB(Runnable runnable) {
        genericFunction.getTableReference("Product").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemsnapshot : snapshot.getChildren()) {
                    Product product = itemsnapshot.getValue(Product.class);
                    if (product != null) {
                        allProducts.add(product);
                    }
                }
                runnable.run();//call back after products are set.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching products: " + error.getMessage());
            }
        });
    }

    /**
     * Load danh sach don hang
     */
    protected void loadOrders() {
        genericFunction.getTableReference("Order").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Order> OrderCompletedList = new ArrayList<>();

                for (DataSnapshot itemsnapshot : snapshot.getChildren()) {
                    Order order = itemsnapshot.getValue(Order.class);
                    if (order != null && order.getStatus().equals("COMPLETED")) {
                        OrderCompletedList.add(order);
                    }
                }
                checkOrderComment(OrderCompletedList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors appropriately.  Log the error!
                Log.e("FirebaseError", "Error fetching orders: " + error.getMessage());
            }
        });
    }

    /**
     * Kiem tra danh gia san pham
     * @param OrderCompletedList Danh sach don hang da hoan thanh
     */
    public void checkOrderComment(List<Order> OrderCompletedList) {
        List<Order> filterOrderList = new ArrayList<>();
        final int totalOrders = OrderCompletedList.size();
        final AtomicInteger completedCallbacks = new AtomicInteger(0);

        for (Order order : OrderCompletedList) {
            genericFunction.getTableReference("Order")
                    .child(order.getIDOrder()).child("OrderDetail")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot itemsnapshot : snapshot.getChildren()) {
                                OrderDetail orderDetail = itemsnapshot.getValue(OrderDetail.class);
                                if (orderDetail != null && orderDetail.getIDProc().equals(getIntent().getStringExtra("Key"))) {
                                    synchronized (filterOrderList) { // Đảm bảo an toàn khi nhiều thread
                                        filterOrderList.add(order);
                                    }
                                    break;
                                }
                            }

                            if (completedCallbacks.incrementAndGet() == totalOrders) {
                                // Tat ca orders da xu ly xong
                                runOnUiThread(() -> {
                                    if (filterOrderList.isEmpty()) {
                                        // If there are no reviews, hide RecyclerView and show TextView
                                        rvReview.setVisibility(View.GONE);

                                        // Check if TextView already exists to avoid creating duplicates
                                        View parent = (View) rvReview.getParent();
                                        if (parent instanceof android.view.ViewGroup) {
                                            android.view.ViewGroup parentGroup = (android.view.ViewGroup) parent;

                                            // Check if there's already a TextView with ID tvNoReviews
                                            TextView existingNoReviewsView = parentGroup.findViewById(R.id.tvNoReviews);

                                            if (existingNoReviewsView == null) {
                                                // Create a new TextView
                                                TextView tvNoReviews = new TextView(ProductDetail.this);
                                                tvNoReviews.setId(R.id.tvNoReviews);
                                                tvNoReviews.setText("San pham chua co danh gia");
                                                tvNoReviews.setTextSize(15);
                                                tvNoReviews.setTextColor(getResources().getColor(R.color.gray));
                                                tvNoReviews.setGravity(android.view.Gravity.CENTER);
                                                tvNoReviews.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

                                                parentGroup.addView(tvNoReviews);
                                            }
                                        }
                                    } else {
                                        // If there are reviews, show RecyclerView and set the adapter
                                        rvReview.setVisibility(View.VISIBLE);

                                        // Remove the "no reviews" TextView if it exists
                                        View parent = (View) rvReview.getParent();
                                        if (parent instanceof android.view.ViewGroup) {
                                            android.view.ViewGroup parentGroup = (android.view.ViewGroup) parent;
                                            View noReviewsView = parentGroup.findViewById(R.id.tvNoReviews);
                                            if (noReviewsView != null) {
                                                parentGroup.removeView(noReviewsView);
                                            }
                                        }

                                        // Set the adapter
                                        reviewAdapter = new ReviewAdapter(ProductDetail.this, filterOrderList, allProducts);
                                        rvReview.setAdapter(reviewAdapter);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            if (completedCallbacks.incrementAndGet() == totalOrders) {
                                runOnUiThread(() -> {
                                    reviewAdapter = new ReviewAdapter(ProductDetail.this, filterOrderList, allProducts);
                                    rvReview.setAdapter(reviewAdapter);
                                });
                            }
                        }
                    });
        }
    }

    /**
     * Load danh gia san pham
     */
    private void loadProductReviews() {
        loadProductsFromDB(() -> {
            loadOrders();
        });
    }
}
