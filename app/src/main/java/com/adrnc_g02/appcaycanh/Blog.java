package com.adrnc_g02.appcaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import Model.Product;

public class Blog extends AppCompatActivity {

    private static final String TAG = "BlogActivity";
    private RecyclerView recyclerViewRelatedProducts;
    private ProductApdater relatedProductAdapter;
    private List<Product> productList;
    private TextView btnViewAll;
    private ImageButton btnBack;
    private GenericFunction genericFunction = new GenericFunction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);

        // Initialize views
        recyclerViewRelatedProducts = findViewById(R.id.recyclerViewRelatedProducts);
        btnViewAll = findViewById(R.id.btnViewAll);
        btnBack = findViewById(R.id.btnBack);

        // 1. Setup LayoutManager first
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerViewRelatedProducts.setLayoutManager(layoutManager);

        // 2. Initialize adapter
        productList = new ArrayList<>();
        relatedProductAdapter = new ProductApdater(Blog.this, productList);
        recyclerViewRelatedProducts.setAdapter(relatedProductAdapter);

        // 3. Configure RecyclerView properties
        recyclerViewRelatedProducts.setHasFixedSize(true);

        // 4. Attach SnapHelper with error handling
        try {
            SnapHelper snapHelper = new LinearSnapHelper();
            snapHelper.attachToRecyclerView(recyclerViewRelatedProducts);
        } catch (IllegalStateException e) {
            Log.e(TAG, "SnapHelper already attached: " + e.getMessage());
        }

        // Set up click listeners
        btnBack.setOnClickListener(v -> onBackPressed());

        btnViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(Blog.this, ProductAdmin.class);
            intent.putExtra("CATEGORY", "Cây phong thủy");
            startActivity(intent);
        });

        // Load data
        loadProductData();
    }

    private void loadProductData() {
        DatabaseReference productRef = genericFunction.getTableReference("Product");

        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Product product = snapshot.getValue(Product.class);
                        if (product != null) {
                            product.setIDProc(snapshot.getKey());
                            productList.add(product);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing product: " + e.getMessage());
                    }
                }

                if (!productList.isEmpty()) {
                    sortAndDisplayProducts();
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError(databaseError);
            }
        });
    }

    private void sortAndDisplayProducts() {
        try {
            // Sort by quantity (descending)
            Collections.sort(productList, (p1, p2) ->
                    Integer.compare(
                            Integer.parseInt(p2.getReQuantity()),
                            Integer.parseInt(p1.getReQuantity())
                    )
            );

            // Get top 5 products
            int maxProducts = Math.min(productList.size(), 5);
            List<Product> topProducts = new ArrayList<>(productList.subList(0, maxProducts));

            // Update UI
            productList.clear();
            productList.addAll(topProducts);
            relatedProductAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.e(TAG, "Sorting error: " + e.getMessage());
            Toast.makeText(this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmptyState() {
        Log.d(TAG, "No products found");
        Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
    }

    private void handleDatabaseError(DatabaseError error) {
        Log.e(TAG, "Database error: " + error.getMessage());
        Toast.makeText(this, "Lỗi kết nối cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
    }
}