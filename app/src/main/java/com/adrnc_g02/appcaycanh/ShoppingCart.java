package com.adrnc_g02.appcaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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
    private BottomNavigationView bottomNavigationView;
    private MenuNavigation menuNavigation = new MenuNavigation(this);
    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private CheckBox cbSelectAll;
    private TextView tvTotalAmount;
    private Button btnCheckout;
    private ImageButton btnBack;
    private List<Cart> cartItems = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
    private OrderManagment orderManagment = new OrderManagment();
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        rvCartItems = findViewById(R.id.rv_cart_items);
        cbSelectAll = findViewById(R.id.cb_select_all);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        btnCheckout = findViewById(R.id.btn_checkout);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        btnBack = findViewById(R.id.btn_back);

        // Setup back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Setup bottom navigation
        bottomNavigationView.setSelectedItemId(R.id.navExplore);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            menuNavigation.navigateTo(itemId);
            return true;
        });

        // Load data
        fetchProductAndCart();

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
                Toast.makeText(this, "Vui lòng chọn sản phẩm trước khi thanh toán", Toast.LENGTH_SHORT).show();
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

    private void loadSampleData() {
        // Add sample products
        products.add(new Product("P001", "L001", "Chậu Tự Tưới Bằng Sứ", "20", "245000", "Chậu tự tưới cao cấp", ""));
        products.add(new Product("P002", "L002", "Cây Phát Tài", "5", "180000", "Cây phong thủy", ""));
        products.add(new Product("P003", "L001", "Xẻng Làm Vườn Mini", "0", "50000", "Dụng cụ làm vườn", ""));

        // Add sample cart items
        cartItems.add(new Cart("C001", "P001", 1));
        cartItems.add(new Cart("C001", "P002", 2));
        cartItems.add(new Cart("C001", "P003", 1));
    }

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
            });
        }
    }

    private void updateTotal() {
        double total = cartAdapter.getTotal();
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        tvTotalAmount.setText(formatter.format(total) + "₫");
    }

    private void updateSelectAllCheckbox() {
        List<Cart> selectedItems = cartAdapter.getSelectedCarts();
        cbSelectAll.setChecked(selectedItems.size() == cartItems.size() && !cartItems.isEmpty());
    }

    private void fetchProductAndCart(){
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
                // Xử lý lỗi nếu cần
            }
        });
    }

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
