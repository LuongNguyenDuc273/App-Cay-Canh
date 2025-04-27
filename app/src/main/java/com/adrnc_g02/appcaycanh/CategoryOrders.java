package com.adrnc_g02.appcaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Model.Order;
import Model.Product;

public class CategoryOrders extends AppCompatActivity {
    private ImageView back;
    private TextView cate;
    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private List<Product> allProducts = new ArrayList<>();

    GenericFunction genericFunction = new GenericFunction();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category_orders);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setInit();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(getApplicationContext(), Admin.class);
                startActivity(backIntent);
                finish();

            }
        });
        loadProducts(()->{
            getData();
        });
    }
    private void setInit(){
        back = findViewById(R.id.btnBack);
        cate = findViewById(R.id.txtCate);
        rvOrders = findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
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

    private void getData(){
        String type = getIntent().getStringExtra("cate");
        switch (type) {
            case "COMPLETED":
                cate.setText("Hoan Thanh");
                break;
            case "CANCELLED":
                cate.setText("Don Huy");
                break;
            case "PENDING_PICKUP":
                cate.setText("Cho Lay Hang");
                break;
        }
        Log.d("CATEGORY",type);
        DatabaseReference orders = genericFunction.getTableReference("Order");
        orders.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Order> filterOrderList = new ArrayList<>();
                for (DataSnapshot itemsnapshot : snapshot.getChildren()) {
                    Order order = itemsnapshot.getValue(Order.class);
                    if (order != null && order.getStatus().equals(type)) {
                        filterOrderList.add(order);
                    }
                }
                runOnUiThread(() -> {
                    orderAdapter = new OrderAdapter(CategoryOrders.this, filterOrderList, allProducts);
                    rvOrders.setAdapter(orderAdapter);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching orders: " + error.getMessage());
            }
        });
    }
}