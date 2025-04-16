package com.adrnc_g02.appcaycanh;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Model.Cart;
import Model.Order;
import Model.OrderDetail;
import Model.Product;

public class OrderManagment {
    private GenericFunction genericFunction = new GenericFunction();
    private double totalPayment = 0;
    private int totalQuantity = 0;

    public void addOrder(String idProc, int productQuantity, String price) {
        totalPayment = 0;
        totalQuantity = 1;
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        if (cUser == null) {
            // Handle the case where user is not logged in
            return;
        }
        String userID = cUser.getUid();
        String orderID = genericFunction.getTableReference("Order").push().getKey();
        List<Product> products = fetchProducts();
        for(Product product : products){
            if(product.getIDProc().equals(idProc)){
                totalPayment = Double.parseDouble(product.getPrice()) * productQuantity;
                OrderDetail orderDetail = new OrderDetail(idProc, totalPayment, productQuantity, Double.parseDouble(price));
                genericFunction.getTableReference("Order")
                        .child(orderID)
                        .child("OrderDetail")
                        .child(idProc)
                        .setValue(orderDetail);
            }
        }
        Order order = new Order(orderID, userID, totalPayment, totalQuantity, "PENDING_CONFIRMATION");
        genericFunction.getTableReference("Order").child(orderID).setValue(order);

    }
    public void addOrder() {
        totalPayment = 0;
        totalQuantity = 0;
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        if (cUser == null) {
            // Handle the case where user is not logged in
            return;
        }

        String userID = cUser.getUid();
        String orderID = genericFunction.getTableReference("Order").push().getKey();

        // Fetch cart data and create order
        fetchCartAndProductsForOrder(orderID, userID);
    }

    private void fetchCartAndProductsForOrder(String orderID, String userID) {
        // First, fetch all products
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

                // After products are loaded, fetch cart items
                fetchCartItems(orderID, userID, productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });
    }

    private void fetchCartItems(String orderID, String userID, List<Product> productList) {
        genericFunction.getTableReference("Customer").child(userID).child("Cart").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot cartSnapshot) {
                List<Cart> cartList = new ArrayList<>();

                for (DataSnapshot itemSnapshot : cartSnapshot.getChildren()) {
                    Cart cart = itemSnapshot.getValue(Cart.class);
                    if (cart != null) {
                        cartList.add(cart);
                    }
                }

                // Now process cart items and create order details
                createOrderDetails(orderID, userID, cartList, productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });
    }

    private void createOrderDetails(String orderID, String userID, List<Cart> cartList, List<Product> productList) {
        Log.d(TAG, "createOrderDetails - Starting for orderID: " + orderID);
        Log.d(TAG, "createOrderDetails - Cart items: " + cartList.size() + ", Products: " + productList.size());

        List<OrderDetail> orderDetails = new ArrayList();
        Log.d(TAG, "createOrderDetails - Created orderDetails list");

        for (Cart cart : cartList) {
            String productID = cart.getIDProc();
            int quantity = cart.getQuantity();
            double price = 0;
            Log.d(TAG, "createOrderDetails - Processing cart item: ID=" + productID + ", quantity=" + quantity);

            for (Product product : productList) {
                if (product.getIDProc().equals(productID)) {
                    price = Double.parseDouble(product.getPrice());
                    Log.d(TAG, "createOrderDetails - Found matching product with price: " + price);
                    break;
                }
            }

            if (price == 0) {
                Log.w(TAG, "createOrderDetails - No matching product found for ID: " + productID);
            }

            double totalAmount = price * quantity;
            totalQuantity += quantity; // Add quantity, not amount
            totalPayment += totalAmount; // Add to total payment

            Log.d(TAG, "createOrderDetails - Item total: " + totalAmount + ", Running totals: payment=" + totalPayment + ", quantity=" + totalQuantity);

            OrderDetail orderDetail = new OrderDetail(productID, totalAmount, quantity, price);
            orderDetails.add(orderDetail);
            Log.d(TAG, "createOrderDetails - Added OrderDetail to list: ID=" + productID);
        }

        // Create the main order with the calculated summary
        Order order = new Order(orderID, userID, totalPayment, totalQuantity, "PENDING_CONFIRMATION");
        genericFunction.getTableReference("Order").child(orderID).setValue(order)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "createOrderDetails - Main order added successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "createOrderDetails - Failed to add main order: " + e.getMessage()));

        Log.d(TAG, "createOrderDetails - Adding " + orderDetails.size() + " order details to Firebase");
        for(OrderDetail orderDetail : orderDetails){
            Log.d(TAG, "createOrderDetails - Adding detail for product: " + orderDetail.getIDProc());
            genericFunction.getTableReference("Order")
                    .child(orderID)
                    .child("OrderDetail")
                    .child(orderDetail.getIDProc())
                    .setValue(orderDetail)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "createOrderDetails - OrderDetail added successfully for product: " + orderDetail.getIDProc()))
                    .addOnFailureListener(e -> Log.e(TAG, "createOrderDetails - Failed to add OrderDetail for product: " + orderDetail.getIDProc() + ", error: " + e.getMessage()));
        }

        // Clear the cart after successful order creation
        clearUserCart(userID);
    }

    private void clearUserCart(String userID) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            genericFunction.getTableReference("Customer").child(userID).child("Cart").removeValue();
        }
    }

    private List<Product> fetchProducts(){
        List<Product> productList = new ArrayList<>();
        genericFunction.getTableReference("Product").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot productSnapshot) {

                for (DataSnapshot itemSnapshot : productSnapshot.getChildren()) {
                    Product product = itemSnapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }

                // After products are loaded, fetch cart items
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });
        return productList;
    }
}