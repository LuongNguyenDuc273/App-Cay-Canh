package com.adrnc_g02.appcaycanh;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
            return;
        }
        String userID = cUser.getUid();
        String orderID = genericFunction.getTableReference("Order").push().getKey();

        // Use the price directly instead of querying products again
        double productPrice = Double.parseDouble(price);
        totalPayment = productPrice * productQuantity;

        OrderDetail orderDetail = new OrderDetail(idProc, totalPayment, productQuantity, productPrice);

        Order order = new Order(orderID, userID, totalPayment, totalQuantity, "PENDING_CONFIRMATION");
        genericFunction.getTableReference("Order").child(orderID).setValue(order);
        genericFunction.getTableReference("Order")
                .child(orderID)
                .child("OrderDetail")
                .child(orderDetail.getIDProc())
                .setValue(orderDetail);
    }

    public void addOrder(List<Cart> cartOrder) {
        totalPayment = 0;
        totalQuantity = 0;
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        if (cUser == null) {
            return;
        }

        String userID = cUser.getUid();
        String orderID = genericFunction.getTableReference("Order").push().getKey();

        fetchCartAndProductsForOrder(orderID, userID, cartOrder);
    }

    private void fetchCartAndProductsForOrder(String orderID, String userID, List<Cart> cartOrder) {
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
                createOrderDetails(orderID, userID, cartOrder, productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void createOrderDetails(String orderID, String userID, List<Cart> cartList, List<Product> productList) {
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

        Order order = new Order(orderID, userID, totalPayment, totalQuantity, "PENDING_CONFIRMATION");
        genericFunction.getTableReference("Order").child(orderID).setValue(order);

        for(OrderDetail orderDetail : orderDetails){
            genericFunction.getTableReference("Order")
                    .child(orderID)
                    .child("OrderDetail")
                    .child(orderDetail.getIDProc())
                    .setValue(orderDetail);
        }
    }

}
