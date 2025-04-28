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
    private String userId;

    // Adapter
    private OrderItemAdapter orderItemAdapter;

    // Helper Class
    private OrderManagment orderManagment = new OrderManagment();

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

        // Lay ID nguoi dung
        getUserId();

        // Khoi tao view
        initViews();

        // Lay cac san pham duoc chon tu intent
        getIntentData();

        // Tim nap du lieu
        fetchAddresses();
        fetchOrderItems();

        // Thiet lap listeners
        setupListeners();
    }

    /**
     * Lay ID nguoi dung dang dang nhap
     */
    private void getUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Vui long dang nhap de tiep tuc", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Khoi tao cac view
     */
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        rvOrderItems = findViewById(R.id.rv_order_items);
        spinnerAddress = findViewById(R.id.spinner_address);
        btnAddAddress = findViewById(R.id.btn_add_address);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvFinalAmount = findViewById(R.id.tv_final_amount);

        // Thiet lap RecyclerView
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        orderItemAdapter = new OrderItemAdapter(this, orderItems);
        rvOrderItems.setAdapter(orderItemAdapter);
    }

    /**
     *  Lay du lieu tu intent
     */
    private void getIntentData() {
        // Lay cac ID gio hang duoc chon tu intent
        ArrayList<String> selectedCartIds = getIntent().getStringArrayListExtra("selected_cart_items");

        // Neu khong co san pham duoc chon, ket thuc activity
        if (selectedCartIds == null || selectedCartIds.isEmpty()) {
            Toast.makeText(this, "Khong co san pham nao duoc chon", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Thiet lap cac listeners
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            isFromProductDetail = getIntent().getBooleanExtra("is_from_product_detail", false);
            if (isFromProductDetail) {
                orderManagment.removeCart(userId, cartList);
            }
            onBackPressed();
        });

        btnAddAddress.setOnClickListener(v -> {
            // Mo activity hoac dialog them dia chi
            Toast.makeText(this, "Chuc nang them dia chi se duoc phat trien sau", Toast.LENGTH_SHORT).show();
        });

        btnPlaceOrder.setOnClickListener(v -> {
            String selectedAddress = spinnerAddress.getSelectedItem().toString(); // Lay dia chi tu Spinner
            Log.d("OrderConfirmation", "Dia chi da chon: " + selectedAddress); // Log dia chi da chon
            if (selectedAddress.isEmpty()) {
                Toast.makeText(this, "Vui long chon dia chi giao hang", Toast.LENGTH_SHORT).show();
                return;
            }
            orderManagment.addOrder(cartList, selectedAddress); // Truyen dia chi vao addOrder
            startActivity(new Intent(OrderConfirmation.this, WaitingConfirmation.class));
        });
    }

    /**
     * Tim nap dia chi tu Firebase
     */
    private void fetchAddresses() {
        // Xoa cac dia chi hien co
        addressList.clear();

        // Lay dia chi tu Firebase
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

                // Cap nhat spinner sau khi tim nap
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
                        "Loi khi tai dia chi: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Tim nap cac san pham tu gio hang
     */
    private void fetchOrderItems() {
        ArrayList<String> selectedCartIds = getIntent().getStringArrayListExtra("selected_cart_items");
        // Neu khong co san pham duoc chon, ket thuc activity
        if (selectedCartIds == null || selectedCartIds.isEmpty()) {
            Toast.makeText(this, "Khong co san pham nao duoc chon", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tao danh sach de luu cac san pham trong gio hang
        // Tim nap cac san pham tu gio hang
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

                // Sau khi tim nap gio hang, tim nap san pham
                fetchProducts(cartList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderConfirmation.this,
                        "Loi khi tai gio hang: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Tim nap thong tin chi tiet san pham tu Firebase
     * @param cartList Danh sach cac san pham trong gio hang
     */
    private void fetchProducts(List<Cart> cartList) {
        if (cartList.isEmpty()) {
            Toast.makeText(this, "Khong tim thay san pham nao", Toast.LENGTH_SHORT).show();
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
                        // Tim san pham tuong ung trong gio hang
                        for (Cart cart : cartList) {
                            if (cart.getIDProc().equals(product.getIDProc())) {
                                orderItems.add(new OrderItem(cart, product));

                                // Tinh tong tien
                                try {
                                    double price = Double.parseDouble(product.getPrice());
                                    subtotal += price * cart.getQuantity();
                                } catch (NumberFormatException e) {
                                    // Bo qua neu gia khong hop le
                                }
                                break; // Thoat vong lap sau khi tim thay
                            }
                        }
                    }
                }

                // Cap nhat UI
                updateOrderSummary();
                orderItemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderConfirmation.this,
                        "Loi khi tai san pham: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cap nhat thong tin tom tat don hang
     */
    private void updateOrderSummary() {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        tvSubtotal.setText(formatter.format(subtotal) + "₫");

        //double total = subtotal + shippingFee; // Comment dong nay de loai bo phi ship
        double total = subtotal; // Thay bang tong tien khong co phi ship
        tvTotalAmount.setText(formatter.format(total) + "₫");
        tvFinalAmount.setText(formatter.format(total) + "₫");
    }

    /**
     * Dat hang
     */
    private void placeOrder() {
        if (orderItems.isEmpty()) {
            Toast.makeText(this, "Khong co san pham nao de dat hang", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedAddress = spinnerAddress.getSelectedItem().toString();
        if (selectedAddress.isEmpty()) {
            Toast.makeText(this, "Vui long chon dia chi giao hang", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hien thi loading hoac vo hieu hoa button
        btnPlaceOrder.setEnabled(false);

        // Tao ID don hang
        String orderId = UUID.randomUUID().toString();

        // Tao doi tuong don hang
        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("userId", userId);
        order.put("address", selectedAddress);
        //order.put("total", subtotal + shippingFee); // Comment dong nay de loai bo phi ship
        order.put("total", subtotal); // Thay bang tong tien khong co phi ship
        order.put("status", "Pending");
        order.put("createdAt", System.currentTimeMillis());

        // Them san pham vao don hang
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

        // Luu vao Firebase
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Orders").child(orderId);
        orderRef.setValue(order).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Xoa san pham khoi gio hang
                removeItemsFromCart();

                // Hien thi thong bao thanh cong
                Toast.makeText(OrderConfirmation.this,
                        "Dat hang thanh cong!", Toast.LENGTH_SHORT).show();

                // Quay lai trang chu
                finish();
            } else {
                // Hien thi loi
                Toast.makeText(OrderConfirmation.this,
                        "Dat hang that bai: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
                btnPlaceOrder.setEnabled(true);
            }
        });
    }

    /**
     * Xoa cac san pham da dat hang khoi gio hang
     */
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
