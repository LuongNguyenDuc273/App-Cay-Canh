package com.adrnc_g02.appcaycanh;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Model.Order;
import Model.OrderDetail;
import Model.Product;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orderList;
    private List<Product> allProducts;
    private GenericFunction genericFunction = new GenericFunction();

    public OrderAdapter(Context context, List<Order> orderList, List<Product> allProducts) {
        this.context = context;
        this.orderList = orderList;
        this.allProducts = allProducts;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order, allProducts, context); // Pass allProducts to bind
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderName;
        private TextView tvStatus;
        private TextView tvTotalPrice;
        private TextView tvTotalText;
        private RecyclerView rvProducts;
        private OrderDetailAdapter productAdapter;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderName = itemView.findViewById(R.id.tvOrderName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvTotalText = itemView.findViewById(R.id.tvTotalText);
            rvProducts = itemView.findViewById(R.id.rvProducts);
            rvProducts.setLayoutManager(new LinearLayoutManager(context));
            productAdapter = new OrderDetailAdapter(context, new ArrayList<>());
            rvProducts.setAdapter(productAdapter);
        }

        //Public void bind, modified to load products and use data from that product
        public void bind(Order order, List<Product> allProducts, Context context) {
            //Set the information for the current view holder.
            setOrderDetails(order, productAdapter, tvOrderName, allProducts);
            // Set order status
            tvStatus.setText(order.getStatus());
            // Format and set total price
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedPrice = formatter.format(order.getTotalPayment()).replace("₫", "đ");
            tvTotalPrice.setText(formattedPrice);
            // Set total quantity text
            String totalText = "Tổng số tiền (" + order.getTotalQuantity() + " sản phẩm):";
            tvTotalText.setText(totalText);
        }

        //Function used to load order details
        public void setOrderDetails(Order order, OrderDetailAdapter productAdapter, TextView tvOrderName, List<Product> allProducts) {
            genericFunction.getTableReference("Order").child(order.getIDOrder())
                    .child("OrderDetail").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            //Create list to store each product
                            List<OrderProductItem> productItems = new ArrayList<>();
                            StringBuilder procNameBuilder = new StringBuilder();
                            //Loop through all of the itemsnapshot
                            for (com.google.firebase.database.DataSnapshot itemsnapshot : snapshot.getChildren()) {
                                //Get each OrderDetail and then add them to the product details.
                                OrderDetail orderDetail = itemsnapshot.getValue(OrderDetail.class);
                                Product product = null;
                                for (Product innerProduct : allProducts) {
                                    if (innerProduct.getIDProc().equals(orderDetail.getIDProc())) {
                                        product = innerProduct;
                                    }
                                }
                                //If the product is not null
                                if (product != null) {
                                    OrderProductItem item = new OrderProductItem(
                                            product,
                                            orderDetail.getTotalQuantity(),
                                            orderDetail.getPrice()
                                    );
                                    //Then add the item to the product
                                    productItems.add(item);
                                    if (procNameBuilder.length() > 0) {
                                        procNameBuilder.append(", ");
                                    }
                                    procNameBuilder.append(product.getNameProc());
                                }
                            }
                            //Set order name, using a few product names.
                            String orderName = procNameBuilder.toString();
                            if (orderName.length() > 50) {
                                orderName = orderName.substring(0, 47) + "...";
                            }
                            //Set name to order list
                            tvOrderName.setText(orderName);
                            //Update the information to the new data
                            productAdapter.updateData(productItems);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }
    }

    // Static class to hold product data for display
    public static class OrderProductItem {
        private Product product;
        private int quantity;
        private double price;

        public OrderProductItem(Product product, int quantity, double price) {
            this.product = product;
            this.quantity = quantity;
            this.price = price;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }
    }
}
