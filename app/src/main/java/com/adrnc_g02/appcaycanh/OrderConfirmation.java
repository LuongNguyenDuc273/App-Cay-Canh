package com.adrnc_g02.appcaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import Model.Address;
import Model.Cart;
import Model.Product;

public class OrderConfirmation extends AppCompatActivity {

    // UI Components
    private ImageButton btnBack;
    private RecyclerView rvOrderItems;
    private Spinner spinnerAddress;
    private Button btnAddAddress;
    private Button btnPlaceOrder;
    private TextView tvSubtotal, tvShippingFee, tvTotalAmount, tvFinalAmount;

    // Data
    private boolean isFromProductDetail;
    private List<Cart> cartList = new ArrayList<>();
    private List<OrderItem> orderItems = new ArrayList<>();
    private List<String> addressList = new ArrayList<>();
    private double subtotal = 0;
    //private double shippingFee = 30000; // Default shipping fee - Comment dòng này để loại bỏ phí ship
    private OrderItemAdapter orderItemAdapter;
    private OrderManagment orderManagment = new OrderManagment();
    private String userId;

    // Combine Cart and Product for easier handling
    public static class OrderItem {
        public Cart cart;
        public Product product;

        public OrderItem(Cart cart, Product product) {
            this.cart = cart;
            this.product = product;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        // Get user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initViews();

        // Get selected cart items from intent
        ArrayList<String> selectedCartIds = getIntent().getStringArrayListExtra("selected_cart_items");

        // Fetch data
        fetchAddresses();
        fetchOrderItems(selectedCartIds);

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        rvOrderItems = findViewById(R.id.rv_order_items);
        spinnerAddress = findViewById(R.id.spinner_address);
        btnAddAddress = findViewById(R.id.btn_add_address);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        //  tvShippingFee = findViewById(R.id.tv_shipping_fee);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvFinalAmount = findViewById(R.id.tv_final_amount);

        // Setup RecyclerView
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        orderItemAdapter = new OrderItemAdapter(this, orderItems);
        rvOrderItems.setAdapter(orderItemAdapter);

        // Format shipping fee
        //NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        //tvShippingFee.setText(formatter.format(shippingFee) + "₫"); // Comment dòng này để ẩn phí ship
        //tvShippingFee.setText("0₫"); // Thay bằng 0đ
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v ->{
            isFromProductDetail = getIntent().getBooleanExtra("is_from_product_detail", false);
            if(isFromProductDetail){
                orderManagment.removeCart(userId, cartList);
            }
            onBackPressed();
        });

        btnAddAddress.setOnClickListener(v -> {
            // Open address adding activity or dialog
            Toast.makeText(this, "Chức năng thêm địa chỉ sẽ được phát triển sau", Toast.LENGTH_SHORT).show();
        });

        btnPlaceOrder.setOnClickListener(v -> {
            String selectedAddress = spinnerAddress.getSelectedItem().toString(); // Lấy địa chỉ từ Spinner
            Log.d("OrderConfirmation", "Selected Address: " + selectedAddress); // Log địa chỉ đã chọn
            if (selectedAddress.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            orderManagment.addOrder(cartList, selectedAddress); // Truyền địa chỉ vào addOrder
            startActivity(new Intent(OrderConfirmation.this, WaitingConfirmation.class));
        });

    }

    private void fetchAddresses() {
        // Clear existing addresses
        addressList.clear();

        // Add a default address for demonstration
        //addressList.add("Địa chỉ nhà riêng: 123 Nguyễn Văn Linh, Q.7, TP.HCM");

        // Fetch addresses from Firebase
        DatabaseReference addressRef = FirebaseDatabase.getInstance()
                .getReference("Customer")
                .child(userId)
                .child("Address");

        addressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                    Address address = addressSnapshot.getValue(Address.class);
                    if (address != null) {
                        addressList.add(address.getAddressLoc().toString());
                    }
                }

                // Update spinner after fetching
                ArrayAdapter<String> addressAdapter = new ArrayAdapter<>(
                        OrderConfirmation.this,
                        android.R.layout.simple_spinner_item,
                        addressList);
                addressAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAddress.setAdapter(addressAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderConfirmation.this,
                        "Lỗi khi tải địa chỉ: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchOrderItems(ArrayList<String> selectedCartIds) {
        // If we don't have selected items, finish activity
        if (selectedCartIds == null || selectedCartIds.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào được chọn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Create list for storing Cart items


        // Fetch Cart items
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("Customer")
                .child(userId)
                .child("Cart");

        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot cartSnapshot : snapshot.getChildren()) {
                    Cart cart = cartSnapshot.getValue(Cart.class);
                    String productId = cartSnapshot.getKey();

                    if (cart != null && selectedCartIds.contains(productId)) {
                        // Set the product ID if not already set
                        if (cart.getIDProc() == null || cart.getIDProc().isEmpty()) {
                            cart.setIDProc(productId);
                        }
                        cartList.add(cart);
                    }
                }

                // After fetching carts, fetch products
                fetchProducts(cartList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderConfirmation.this,
                        "Lỗi khi tải giỏ hàng: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchProducts(List<Cart> cartList) {
        if (cartList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm nào", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Product");

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderItems.clear();
                subtotal = 0;

                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);

                    if (product != null) {
                        // Find corresponding cart item in cartList
                        for (Cart cart : cartList) {
                            if (cart.getIDProc().equals(product.getIDProc())) {
                                orderItems.add(new OrderItem(cart, product));

                                // Calculate subtotal
                                try {
                                    double price = Double.parseDouble(product.getPrice());
                                    subtotal += price * cart.getQuantity();
                                } catch (NumberFormatException e) {
                                    // Skip if price is not valid
                                }
                                break; // Break inner loop after finding match
                            }
                        }
                    }
                }

                // Update UI
                updateOrderSummary();
                orderItemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderConfirmation.this,
                        "Lỗi khi tải sản phẩm: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderSummary() {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        tvSubtotal.setText(formatter.format(subtotal) + "₫");

        //double total = subtotal + shippingFee; // Comment dòng này để loại bỏ phí ship
        double total = subtotal; // Thay bằng tổng tiền không có phí ship
        tvTotalAmount.setText(formatter.format(total) + "₫");
        tvFinalAmount.setText(formatter.format(total) + "₫");
    }


    private void placeOrder() {
        if (orderItems.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào để đặt hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedAddress = spinnerAddress.getSelectedItem().toString();
        if (selectedAddress.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading or disable button
        btnPlaceOrder.setEnabled(false);

        // Generate unique order ID
        String orderId = UUID.randomUUID().toString();

        // Create order object
        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("userId", userId);
        order.put("address", selectedAddress);
        //order.put("total", subtotal + shippingFee); // Comment dòng này để loại bỏ phí ship
        order.put("total", subtotal); // Thay bằng tổng tiền không có phí ship
        order.put("status", "Pending");
        order.put("createdAt", System.currentTimeMillis());

        // Add items to order
        Map<String, Object> items = new HashMap<>();
        for (OrderItem item : orderItems) {
            Map<String, Object> itemDetails = new HashMap<>();
            itemDetails.put("productId", item.product.getIDProc());
            itemDetails.put("name", item.product.getNameProc());
            itemDetails.put("price", item.product.getPrice());
            itemDetails.put("quantity", item.cart.getQuantity());

            items.put(item.product.getIDProc(), itemDetails);
        }
        order.put("items", items);

        // Save to Firebase
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Orders").child(orderId);
        orderRef.setValue(order).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Remove items from cart
                removeItemsFromCart();

                // Show success message
                Toast.makeText(OrderConfirmation.this,
                        "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();

                // Navigate back to home
                finish();
            } else {
                // Show error
                Toast.makeText(OrderConfirmation.this,
                        "Đặt hàng thất bại: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
                btnPlaceOrder.setEnabled(true);
            }
        });
    }

    private void removeItemsFromCart() {
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("Customer")
                .child(userId)
                .child("Cart");

        for (OrderItem item : orderItems) {
            cartRef.child(item.product.getIDProc()).removeValue();
        }
    }
}
