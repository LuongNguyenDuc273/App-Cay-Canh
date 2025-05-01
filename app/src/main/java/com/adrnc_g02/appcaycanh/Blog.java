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
import androidx.recyclerview.widget.RecyclerView;

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

        // Set up back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Set up view all button
        btnViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to product list with "Cây phong thủy" filter
                // Thay ProductList bằng Activity danh sách sản phẩm thực tế của bạn
                Intent intent = new Intent(Blog.this, ProductAdmin.class);
                intent.putExtra("CATEGORY", "Cây phong thủy");
                startActivity(intent);
            }
        });

        // Set up related product recycler view with horizontal layout
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewRelatedProducts.setLayoutManager(layoutManager);

        // Initialize product list
        productList = new ArrayList<>();
        relatedProductAdapter = new ProductApdater(Blog.this, productList);
        recyclerViewRelatedProducts.setAdapter(relatedProductAdapter);

        // Load product data
        loadProductData();
    }

    private void loadProductData() {
        DatabaseReference productRef = genericFunction.getTableReference("Product");

        // Sử dụng ValueEventListener thay vì get() để đảm bảo callback hoạt động
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
                            Log.d(TAG, "Loaded product: " + product.getNameProc() + ", Quantity: " + product.getReQuantity());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing product data: " + e.getMessage());
                    }
                }

                if (productList.size() > 0) {
                    // Sort by quantity in descending order
                    try {
                        Collections.sort(productList, new Comparator<Product>() {
                            @Override
                            public int compare(Product p1, Product p2) {
                                int qty1 = Integer.parseInt(p1.getReQuantity());
                                int qty2 = Integer.parseInt(p2.getReQuantity());
                                return Integer.compare(qty2, qty1); // Descending order
                            }
                        });

                        // Lấy tối đa 5 sản phẩm hoặc ít hơn nếu không đủ
                        int maxProducts = Math.min(productList.size(), 5);
                        List<Product> topProducts = new ArrayList<>(productList.subList(0, maxProducts));

                        // Update adapter with new data
                        productList.clear();
                        productList.addAll(topProducts);
                        relatedProductAdapter.notifyDataSetChanged();

                        Log.d(TAG, "Displaying " + productList.size() + " products");
                    } catch (Exception e) {
                        Log.e(TAG, "Error sorting products: " + e.getMessage());
                        Toast.makeText(Blog.this, "Lỗi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "No products found");
                    Toast.makeText(Blog.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(Blog.this, "Lỗi truy vấn cơ sở dữ liệu: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}