package com.adrnc_g02.appcaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Model.Product;

public class Search extends AppCompatActivity {

    // UI elements
    private EditText searchEditText;
    private RecyclerView suggestionsRecyclerView;
    private View contentContainer;
    private ImageButton backButton;
    private ImageView searchIcon;

    // Adapter
    private SearchAdapter searchAdapter;

    // Data
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();

    // Firebase
    DatabaseReference databaseReference;

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

        // Thiet lap cac listeners
        setupListeners();
    }

    /**
     * Thiet lap giao dien co ban va xu ly insets cho he thong.
     */
    private void setupUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
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
        searchEditText = findViewById(R.id.search_edit_text);
        suggestionsRecyclerView = findViewById(R.id.suggestions_recycler_view);
        contentContainer = findViewById(R.id.content_container);
        backButton = findViewById(R.id.back_button);
        searchIcon = findViewById(R.id.search_icon);
    }

    /**
     * Thiet lap Recycler View.
     */
    private void setupRecyclerView() {
        suggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new SearchAdapter(this, filteredProducts);
        suggestionsRecyclerView.setAdapter(searchAdapter);
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
                        allProducts.add(product);
                    }
                }
                Log.d("SearchDebug", "Loaded " + allProducts.size() + " products from Firebase");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xu ly loi neu can
                Toast.makeText(Search.this, "Firebase error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Thiet lap cac listeners.
     */
    private void setupListeners() {
        // An nut back
        backButton.setOnClickListener(v -> {
            onBackPressed();
        });

        // Su kien tim kiem
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Loc san pham dua tren search text
                String searchText = charSequence.toString().toLowerCase().trim();
                filterProducts(searchText);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Su kien tim kiem
        searchIcon.setOnClickListener(view -> {
            Intent intent = new Intent(Search.this, SearchedProduct.class);
            intent.putExtra("searchQuery", searchEditText.getText().toString());
            startActivity(intent);
        });
    }

    /**
     * Loc san pham.
     * @param searchText Text tim kiem.
     */
    private void filterProducts(String searchText) {
        filteredProducts.clear();

        if (searchText.isEmpty()) {
            // An suggestions neu search text rong
            suggestionsRecyclerView.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
            return;
        }

        // Show san pham match voi search text
        for (Product product : allProducts) {
            // Search by name, ID, or description
            if (product.getNameProc().toLowerCase().contains(searchText) ||
                    product.getIDProc().toLowerCase().contains(searchText) ||
                    product.getDescribe().toLowerCase().contains(searchText)) {
                filteredProducts.add(product);
            }
        }

        // Cap nhat UI - dam bao RecyclerView hien thi
        searchAdapter.notifyDataSetChanged();

        // Luon hien thi RecyclerView neu co ket qua
        if (filteredProducts.isEmpty()) {
            suggestionsRecyclerView.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
        } else {
            suggestionsRecyclerView.setVisibility(View.VISIBLE);
            contentContainer.setVisibility(View.GONE);
        }
    }
}
