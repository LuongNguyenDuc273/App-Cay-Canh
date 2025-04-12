package com.adrnc_g02.appcaycanh;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private EditText searchEditText;
    private RecyclerView suggestionsRecyclerView;
    private SearchAdapter searchAdapter;
    private View contentContainer;
    private ImageButton backButton;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Anh xa
        searchEditText = findViewById(R.id.search_edit_text);
        suggestionsRecyclerView = findViewById(R.id.suggestions_recycler_view);
        contentContainer = findViewById(R.id.content_container);
        backButton = findViewById(R.id.back_button);

        // Khoi tao recycler view
        suggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new SearchAdapter(this, filteredProducts);
        suggestionsRecyclerView.setAdapter(searchAdapter);

        // An nut back
        backButton.setOnClickListener(v -> {
            finish();
        });

        //Su kien tim kiem
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
                // su kien tim kiem
                searchEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                        // Filter products based on search text
                        String searchText = charSequence.toString().toLowerCase().trim();
                        filterProducts(searchText);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                // Su kien khi an vao san pham
                searchAdapter.setOnProductClickListener(product -> {
                    // Handle product click here
                    // For example, navigate to product details
                    searchEditText.setText(product.getNameProc());
                    suggestionsRecyclerView.setVisibility(View.GONE);
                    contentContainer.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
                Toast.makeText(Search.this, "Firebase error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterProducts(String searchText) {
        filteredProducts.clear();

        if (searchText.isEmpty()) {
            // Hide suggestions if search text is empty
            suggestionsRecyclerView.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
            return;
        }

        // Show products that match the search text
        for (Product product : allProducts) {
            // Search by name, ID, or description
            if (product.getNameProc().toLowerCase().contains(searchText) ||
                    product.getIDProc().toLowerCase().contains(searchText) ||
                    product.getDescribe().toLowerCase().contains(searchText)) {
                filteredProducts.add(product);
            }
        }

        // Update UI - ensure RecyclerView is visible
        searchAdapter.notifyDataSetChanged();

        // Always make the RecyclerView visible if we have results
        if (filteredProducts.isEmpty()) {
            suggestionsRecyclerView.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
        } else {
            suggestionsRecyclerView.setVisibility(View.VISIBLE);
            contentContainer.setVisibility(View.GONE);
        }
    }
}