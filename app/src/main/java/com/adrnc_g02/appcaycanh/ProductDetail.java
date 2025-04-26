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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import Model.Address;
import Model.Cart;

public class ProductDetail extends AppCompatActivity {
    private ImageView imageView;
    private ImageButton back, btnAddToCart;
    private TextView productName, productPrice, productCategory, productDes, productStatus;
    private Button btnBuy, btnIncreaseQuantity, btnDecreaseQuantity;
    private EditText tvQuantity;
    private  GenericFunction genericFunction = new GenericFunction();
    private OrderManagment orderManagment = new OrderManagment();

    private int currentQuantity = 1; // Gia tri mac dinh

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khoi tao view
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

        // Dat gia tri ban dau cho tvQuantity
        tvQuantity.setText(String.valueOf(currentQuantity));

        // Su kien nut back
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });

        // Gan su kien cho nut tang so luong
        btnIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentQuantity++;
                tvQuantity.setText(String.valueOf(currentQuantity));
            }
        });

        // Gan su kien cho nut giam so luong
        btnDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentQuantity > 1) {
                    currentQuantity--;
                    tvQuantity.setText(String.valueOf(currentQuantity));
                }
            }
        });

        // Hien thi thong tin san pham
        LoadProduct();

        // Su kien them vao gio hang
        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    addToCart();
            }
        });

        // SU kien mua hang
        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newOrder();
            }
        });

    }

    public void LoadProduct() {
        String imageUrl = getIntent().getStringExtra("Image");
        String name = getIntent().getStringExtra("Name");
        String price = getIntent().getStringExtra("Price");
        String line = getIntent().getStringExtra("Line");
        int quantity = getIntent().getStringExtra("Quantity") != null ?
                Integer.parseInt(getIntent().getStringExtra("Quantity")) : 0;
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
            productStatus.setText("Còn hàng");
        } else {
            btnBuy.setEnabled(false);
            btnAddToCart.setEnabled(false);
            btnAddToCart.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray)));
            btnAddToCart.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            btnIncreaseQuantity.setEnabled(false);
            btnDecreaseQuantity.setEnabled(false);
            productStatus.setText("Tạm hết hàng");
            productStatus.setTextColor(getResources().getColor(R.color.red));
            productStatus.setBackground(getResources().getDrawable(R.drawable.bg_unavailable_status));
            btnBuy.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray)));
        }
        Glide.with(this).load(imageUrl).into(imageView);
        productName.setText(name);
        productPrice.setText(price);
        productDes.setText(des);
    }

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CartError", "Failed to check cart: " + error.getMessage());
            }
        });
    }

    private void newOrder(){
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
}
