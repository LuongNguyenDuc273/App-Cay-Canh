package com.adrnc_g02.appcaycanh.Order;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adrnc_g02.appcaycanh.Generic.GenericFunction;
import com.adrnc_g02.appcaycanh.Profile.Profile;
import com.adrnc_g02.appcaycanh.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Model.Order;
import Model.Product;

public class WaitingPickup extends AppCompatActivity {
    // UI elements
    private RecyclerView rvOrder;
    private ImageButton backButton;

    // Adapter
    private OrderAdapter orderAdapter;

    // Data
    private List<Product> allProducts = new ArrayList<>();

    // Helper Class
    private GenericFunction genericFunction = new GenericFunction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Thiet lap giao dien co ban
        setupUI();

        // Khoi tao view
        initializeViews();

        // Thiet lap listeners
        setupListeners();

        // Load du lieu
        loadProducts(() -> {
            loadOrders();
        });
    }

    /**
     * Thiet lap giao dien co ban va xu ly insets cho he thong.
     */
    private void setupUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_waiting_pickup);
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
        rvOrder = findViewById(R.id.rvOrders);
        backButton = findViewById(R.id.btnBack);
        rvOrder.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Thiet lap cac listeners.
     */
    private void setupListeners() {
        // Su kien quay lai
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(WaitingPickup.this, Profile.class));
        });
    }

    /**
     * Load danh sach san pham.
     * @param runnable Callback sau khi load xong san pham.
     */
    private void loadProducts(Runnable runnable) {
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
     * Load danh sach don hang dang cho lay hang cua nguoi dung.
     */
    protected void loadOrders() {
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        genericFunction.getTableReference("Order").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Order> filterOrderList = new ArrayList<>();

                for (DataSnapshot itemsnapshot : snapshot.getChildren()) {
                    Order order = itemsnapshot.getValue(Order.class);
                    if (order != null && order.getStatus().equals("PENDING_PICKUP") && order.getIDCus().equals(cUser.getUid())) {
                        filterOrderList.add(order);
                    }
                }

                // Khoi tao RecyclerView va Adapter sau khi load du lieu
                runOnUiThread(() -> {
                    orderAdapter = new OrderAdapter(WaitingPickup.this, filterOrderList, allProducts);
                    rvOrder.setAdapter(orderAdapter);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xu ly loi. Log loi!
                Log.e("FirebaseError", "Error fetching orders: " + error.getMessage());
            }
        });
    }
}
