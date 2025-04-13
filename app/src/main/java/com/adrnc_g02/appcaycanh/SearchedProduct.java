package com.adrnc_g02.appcaycanh;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Model.Product;

public class SearchedProduct extends AppCompatActivity {
    private TextView txtSearch;
    private ImageButton backButton;
    private List<Product> AllProducts = new ArrayList<>();
    private List<Product> SearchedProducts = new ArrayList<>();
    private ProductApdater productApdater;
    private GridLayoutManager gridLayoutManager;
    private RecyclerView listSearchedProduct;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_searched_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khoi tao view
        txtSearch = findViewById(R.id.searchQueryText);
        backButton = findViewById(R.id.backButton);
        listSearchedProduct = findViewById(R.id.recyclerViewPlants);

        // Set recycler view
        gridLayoutManager = new GridLayoutManager(this, 2);
        listSearchedProduct.setLayoutManager(gridLayoutManager);
        productApdater = new ProductApdater(SearchedProduct.this,SearchedProducts);
        listSearchedProduct.setAdapter(productApdater);


        // Set text cho txtSearch
        String searchQuery =  getIntent().getStringExtra("searchQuery");
        txtSearch.setText("Sản phẩm Liên quan đến: " + searchQuery);


        // Nap du lieu san pham
        databaseReference = FirebaseDatabase.getInstance().getReference("Product");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Product product = itemSnapshot.getValue(Product.class);
                    if (product != null) {
                        AllProducts.add(product);
                    }
                }

                // Move this inside onDataChange - after data is loaded
                if (searchQuery != null && !searchQuery.isEmpty()) {
                    filterProducts(searchQuery.toLowerCase());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // An nut back
        backButton.setOnClickListener(v -> {
            finish();
        });

        //

    }

    private void filterProducts(String searchText) {
        SearchedProducts.clear();
        // Show products that match the search text
        for (Product product : AllProducts) {
            // Search by name, ID, or description
            if (product.getNameProc().toLowerCase().contains(searchText) ||
                    product.getIDProc().toLowerCase().contains(searchText) ||
                    product.getDescribe().toLowerCase().contains(searchText)) {
                SearchedProducts.add(product);
            }
        }

        // Update UI
        productApdater.notifyDataSetChanged();
    }


}