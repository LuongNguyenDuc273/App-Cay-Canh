package com.adrnc_g02.appcaycanh.Cart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adrnc_g02.appcaycanh.Menu.MenuNavigation;
import com.adrnc_g02.appcaycanh.Order.OrderConfirmation;
import com.adrnc_g02.appcaycanh.OrderManagment;
import com.adrnc_g02.appcaycanh.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Model.Cart;
import Model.Product;

public class ShoppingCart extends AppCompatActivity {

    // UI elements
    private RecyclerView rvCartItems;
    private CheckBox cbSelectAll;
    private TextView tvTotalAmount;
    private Button btnCheckout;
    private ImageButton btnBack;
    private BottomNavigationView bottomNavigationView;

    // Adapter
    private CartAdapter cartAdapter;

    // Data
    private List<Cart> cartItems = new ArrayList<>();
    private List<Product> products = new ArrayList<>();

    // Firebase
    DatabaseReference databaseReference;

    // Helper classes
    private MenuNavigation menuNavigation = new MenuNavigation(this);
    private OrderManagment orderManagment = new OrderManagment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Thiet lap giao dien co ban
        setupUI();

        // Khoi tao view
        initializeViews();

        // Thiet lap bottom navigation
        setupBottomNavigation();

        // Thiet lap listeners
        setupListeners();

        // Load du lieu
        fetchProductAndCart();
    }

    /**
     * Thiet lap giao dien co ban va xu ly insets cho he thong.
     */
    private void setupUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping_cart);
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
        rvCartItems = findViewById(R.id.rv_cart_items);
        cbSelectAll = findViewById(R.id.cb_select_all);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        btnCheckout = findViewById(R.id.btn_checkout);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        btnBack = findViewById(R.id.btn_back);
    }

    /**
     * Thiet lap bottom navigation.
     */
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navExplore);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            menuNavigation.navigateTo(itemId);
            return true;
        });
    }

    /**
     * Thiet lap cac listeners.
     */
    private void setupListeners() {
        // Setup back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Setup "Select All" checkbox
        cbSelectAll.setOnClickListener(v -> {
            boolean isChecked = cbSelectAll.isChecked();
            cartAdapter.selectAll(isChecked);
            updateTotal();
        });

        // Setup checkout button
        btnCheckout.setOnClickListener(v -> {
            List<Cart> selectedItems = cartAdapter.getSelectedCarts();
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "Vui long chon san pham truoc khi thanh toan", Toast.LENGTH_SHORT).show();
            } else {
                // Create a list of product IDs to pass to OrderConfirmation
                ArrayList<String> selectedProductIds = new ArrayList<>();
                for (Cart cart : selectedItems) {
                    selectedProductIds.add(cart.getIDProc());
                }

                // Start OrderConfirmation activity
                Intent intent = new Intent(ShoppingCart.this, OrderConfirmation.class);
                intent.putStringArrayListExtra("selected_cart_items", selectedProductIds);
                startActivity(intent);
            }
        });
    }

    /**
     * Thiet lap Recycler View.
     */
    private void setupRecyclerView() {
        // Check if both products and cartItems are loaded
        if (!products.isEmpty() && !cartItems.isEmpty()) {
            // Setup RecyclerView
            rvCartItems.setLayoutManager(new LinearLayoutManager(this));
            cartAdapter = new CartAdapter(this, cartItems, products);
            rvCartItems.setAdapter(cartAdapter);

            // Setup listeners
            cartAdapter.setCartListener(new CartAdapter.CartListener() {
                @Override
                public void onQuantityChanged() {
                    updateTotal();
                }

                @Override
                public void onSelectionChanged() {
                    updateTotal();
                    updateSelectAllCheckbox();
                }

                @Override
                public void onItemDeleted(String productId) {
                    // You can show a confirmation toast or handle it specially
                    Toast.makeText(ShoppingCart.this, "Da xoa san pham khoi gio hang", Toast.LENGTH_SHORT).show();
                    updateTotal();
                    updateSelectAllCheckbox();
                }
            });
        }
    }

    /**
     * Cap nhat tong tien.
     */
    private void updateTotal() {
        double total = cartAdapter.getTotal();
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        tvTotalAmount.setText(formatter.format(total) + "₫");
    }

    /**
     * Cap nhat trang thai checkbox select all.
     */
    private void updateSelectAllCheckbox() {
        List<Cart> selectedItems = cartAdapter.getSelectedCarts();
        cbSelectAll.setChecked(selectedItems.size() == cartItems.size() && !cartItems.isEmpty());
    }

    /**
     * Load san pham va gio hang.
     */
    private void fetchProductAndCart() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Product");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear(); // Clear the list before adding new data
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Product product = itemSnapshot.getValue(Product.class);
                    if (product != null) {
                        products.add(product);
                    }
                }
                Log.d("CartDebug", "Loaded " + products.size() + " products from Firebase");
                fetchCart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xu ly loi neu can
            }
        });
    }

    /**
     * Load gio hang tu firebase.
     */
    private void fetchCart() {
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Customer").child(cUser.getUid()).child("Cart");

        // Use addListenerForSingleValueEvent instead of addValueEventListener
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItems.clear(); // Clear the list before adding new data
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Cart cart = itemSnapshot.getValue(Cart.class);
                    if (cart != null) {
                        cartItems.add(cart);
                    }
                }
                Log.d("CartDebug", "Loaded " + cartItems.size() + " Cart item from Firebase");
                setupRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors if needed
            }
        });
    }
}
