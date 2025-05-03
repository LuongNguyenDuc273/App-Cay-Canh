package com.adrnc_g02.appcaycanh;

import android.util.Log;

import androidx.annotation.NonNull;

import com.adrnc_g02.appcaycanh.Generic.GenericFunction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Model.Cart;
import Model.Order;
import Model.OrderDetail;
import Model.Product;

public class OrderManagment {
    private GenericFunction genericFunction = new GenericFunction();
    private double totalPayment = 0;
    private int totalQuantity = 0;

    public void addOrder(List<Cart> cartOrder, String address) {
        totalPayment = 0;
        totalQuantity = 0;
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        if (cUser == null) {
            return;
        }

        String userID = cUser.getUid();
        String orderID = genericFunction.getTableReference("Order").push().getKey();
        Log.d("OrderManagmentaddOrder", "Selected Address: " + address); // Log địa chỉ đã chọn

        fetchCartAndProductsForOrder(orderID, userID, cartOrder, address);
    }

    private void fetchCartAndProductsForOrder(String orderID, String userID, List<Cart> cartOrder, String address) {
        genericFunction.getTableReference("Product").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot productSnapshot) {
                List<Product> productList = new ArrayList<>();

                for (DataSnapshot itemSnapshot : productSnapshot.getChildren()) {
                    Product product = itemSnapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
                createOrderDetails(orderID, userID, cartOrder, productList, address);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void createOrderDetails(String orderID, String userID, List<Cart> cartList, List<Product> productList, String address) {
        List<OrderDetail> orderDetails = new ArrayList();

        for (Cart cart : cartList) {
            String productID = cart.getIDProc();
            int quantity = cart.getQuantity();
            double price = 0;

            for (Product product : productList) {
                if (product.getIDProc().equals(productID)) {
                    price = Double.parseDouble(product.getPrice());
                    break;
                }
            }

            double totalAmount = price * quantity;
            totalQuantity = cartList.size();
            totalPayment += totalAmount;

            OrderDetail orderDetail = new OrderDetail(productID, totalAmount, quantity, price);
            orderDetails.add(orderDetail);
        }

        //Lay ngay gio hien tai
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy ss:mm:HH", Locale.getDefault()); // Quan trọng: sử dụng Locale
        String currentDateTime = formatter.format(now);

        Order order = new Order(orderID, userID, totalPayment, totalQuantity, address, currentDateTime, "PENDING_CONFIRMATION");
        genericFunction.getTableReference("Order").child(orderID).setValue(order);

        for(OrderDetail orderDetail : orderDetails){
            genericFunction.getTableReference("Order")
                    .child(orderID)
                    .child("OrderDetail")
                    .child(orderDetail.getIDProc())
                    .setValue(orderDetail);
            updateProductQuantity(orderDetail.getIDProc(), orderDetail.getTotalQuantity());
        }

        removeCart(userID, cartList);
    }

    public void removeCart(String userID ,List<Cart> cartList){
        for (Cart cart : cartList) {
            genericFunction.getTableReference("Customer")
                    .child(userID)
                    .child("Cart")
                    .child(cart.getIDProc())
                    .removeValue();

            // Log for debugging
            Log.d("CartAdapter", "Removing item: " + cart.getIDProc());
        }
    }

    public void updateProductQuantity(String productID, int buyQuantity){
        // Get reference to the product in Firebase
        DatabaseReference productRef = genericFunction.getTableReference("Product").child(productID);

        // Use a transaction to safely update the quantity
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);

                if (product != null) {
                    try {
                        // Get current quantity
                        int currentQuantity = Integer.parseInt(product.getReQuantity());

                        // Calculate new quantity (making sure it doesn't go below 0)
                        int newQuantity = Math.max(0, currentQuantity - buyQuantity);

                        // Update the quantity in Firebase
                        productRef.child("reQuantity").setValue(String.valueOf(newQuantity));

                        Log.d("ProductUpdate", "Updated product: " + productID +
                                " from quantity: " + currentQuantity +
                                " to: " + newQuantity);
                    } catch (NumberFormatException e) {
                        Log.e("ProductUpdate", "Failed to parse quantity: " + e.getMessage());
                    }
                } else {
                    Log.e("ProductUpdate", "Product not found: " + productID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProductUpdate", "Failed to update product quantity: " + error.getMessage());
            }
        });
    }

}
