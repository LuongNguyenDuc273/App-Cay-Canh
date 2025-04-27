package com.adrnc_g02.appcaycanh;

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
    private RecyclerView rvOrder;
    private ImageButton backButton;
    private OrderAdapter orderAdapter;
    private GenericFunction genericFunction = new GenericFunction();
    private List<Product> allProducts = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_waiting_pickup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khoi tao view
        backButton = findViewById(R.id.btnBack);
        rvOrder = findViewById(R.id.rvOrders);
        rvOrder.setLayoutManager(new LinearLayoutManager(this));

        // Su kien quay lai
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(WaitingPickup.this, Profile.class));
        });

        //Load the products and then load the orders.
        loadProducts(() -> {
            loadOrders();
        });
    }

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

                // Initialize RecyclerView and Adapter *after* data is loaded
                runOnUiThread(() -> {
                    orderAdapter = new OrderAdapter(WaitingPickup.this, filterOrderList, allProducts);
                    rvOrder.setAdapter(orderAdapter);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors appropriately.  Log the error!
                Log.e("FirebaseError", "Error fetching orders: " + error.getMessage());
            }
        });
    }
}