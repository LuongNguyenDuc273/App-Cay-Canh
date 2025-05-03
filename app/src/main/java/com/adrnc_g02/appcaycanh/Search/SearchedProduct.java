package com.adrnc_g02.appcaycanh.Search;

import android.os.Bundle;
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

import com.adrnc_g02.appcaycanh.Productdetail.ProductApdater;
import com.adrnc_g02.appcaycanh.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Model.Product;

public class SearchedProduct extends AppCompatActivity {
    // UI elements
    private TextView txtSearch;
    private ImageButton backButton;
    private RecyclerView listSearchedProduct;

    // Adapter
    private ProductApdater productApdater;

    // Data
    private List<Product> AllProducts = new ArrayList<>();
    private List<Product> SearchedProducts = new ArrayList<>();

    // Firebase
    DatabaseReference databaseReference;

    // Layout Manager
    private GridLayoutManager gridLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Thiet lap giao dien co ban
        setupUI();

        // Khoi tao view
        initializeViews();

        // Thiet lap Recycler View
        setupRecyclerView();

        // Load san pham tu Firebase
        loadProductsFromFirebase();

        // Thiet lap listeners
        setupListeners();
    }

    /**
     * Thiet lap giao dien co ban va xu ly insets cho he thong.
     */
    private void setupUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_searched_product);
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
        txtSearch = findViewById(R.id.searchQueryText);
        backButton = findViewById(R.id.backButton);
        listSearchedProduct = findViewById(R.id.recyclerViewPlants);

        // Set text cho txtSearch
        String searchQuery = getIntent().getStringExtra("searchQuery");
        txtSearch.setText("San pham Lien quan den: " + searchQuery);
    }

    /**
     * Thiet lap Recycler View.
     */
    private void setupRecyclerView() {
        gridLayoutManager = new GridLayoutManager(this, 2);
        listSearchedProduct.setLayoutManager(gridLayoutManager);
        productApdater = new ProductApdater(SearchedProduct.this, SearchedProducts);
        listSearchedProduct.setAdapter(productApdater);
    }

    /**
     * Load san pham tu Firebase.
     */
    private void loadProductsFromFirebase() {
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

                // Sau khi load du lieu, loc san pham
                String searchQuery = getIntent().getStringExtra("searchQuery");
                if (searchQuery != null && !searchQuery.isEmpty()) {
                    filterProducts(searchQuery.toLowerCase());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xu ly loi
            }
        });
    }

    /**
     * Thiet lap cac listeners.
     */
    private void setupListeners() {
        // An nut back
        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    /**
     * Loc san pham.
     * @param searchText Text tim kiem.
     */
    private void filterProducts(String searchText) {
        SearchedProducts.clear();
        // Show san pham match voi search text
        for (Product product : AllProducts) {
            // Search by name, ID, or description
            if (product.getNameProc().toLowerCase().contains(searchText) ||
                    product.getIDProc().toLowerCase().contains(searchText) ||
                    product.getDescribe().toLowerCase().contains(searchText)) {
                SearchedProducts.add(product);
            }
        }

        // Cap nhat UI
        productApdater.notifyDataSetChanged();
    }
}
