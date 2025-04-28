package com.adrnc_g02.appcaycanh;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        private Button btnStatus; // Add this button

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderName = itemView.findViewById(R.id.tvOrderName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvTotalText = itemView.findViewById(R.id.tvTotalText);
            btnStatus = itemView.findViewById(R.id.btnStatus); // Initialize the button
            rvProducts = itemView.findViewById(R.id.rvProducts);
            rvProducts.setLayoutManager(new LinearLayoutManager(context));
            productAdapter = new OrderDetailAdapter(context, new ArrayList<>());
            rvProducts.setAdapter(productAdapter);
        }

        private GradientDrawable createRoundedDrawable(int color) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(40); // Corner radius in pixels
            drawable.setColor(color);
            return drawable;
        }

        public void bind(Order order, List<Product> allProducts, Context context) {
            //Set the information for the current view holder.
            setOrderDetails(order, productAdapter, tvOrderName, allProducts);
            // Set order status and button properties based on order status
            if (!(context instanceof OrderHistory) && !(context instanceof WaitingConfirmation)) {
                btnStatus.setVisibility(View.GONE);
            }
            switch(order.getStatus()) {
                case "PENDING_CONFIRMATION":
                    tvStatus.setText("Chờ xác nhận");
                    tvStatus.setTextColor(context.getResources().getColor(R.color.orange));
                    if(context instanceof OrderHistory || context instanceof WaitingConfirmation)
                    {
                        // Set button to "Hủy đơn" with red color
                        btnStatus.setText("Hủy đơn");
                        btnStatus.setBackground(createRoundedDrawable(Color.parseColor("#FF5252"))); // Red color
                        btnStatus.setVisibility(View.VISIBLE);
                    }
                    break;

                case "CANCELLED":
                    tvStatus.setText("Đã hủy");
                    tvStatus.setTextColor(context.getResources().getColor(R.color.red));
                    if(context instanceof OrderHistory || context instanceof WaitingConfirmation){
                        // Set button to "Mua lại" with green color
                        btnStatus.setText("Mua lại");
                        btnStatus.setBackground(createRoundedDrawable(Color.parseColor("#13C123"))); // Green color
                        btnStatus.setVisibility(View.VISIBLE);
                    }
                    break;

                case "PENDING_PICKUP":
                    tvStatus.setTextColor(context.getResources().getColor(R.color.orange));
                    tvStatus.setText("Chờ lấy hàng");
                    if(context instanceof OrderHistory || context instanceof WaitingConfirmation){

                        // Set button to "Chờ giao hàng" but hide it
                        btnStatus.setText("Chờ giao hàng");
                        btnStatus.setVisibility(View.GONE);
                    }
                    break;

                case "PENDING_COMPLETED":
                    tvStatus.setText("Giao hàng thành công");
                    tvStatus.setTextColor(context.getResources().getColor(R.color.green));

                    // Set button to "Đã nhận được hàng" with green color
                    btnStatus.setText("Đã nhận được");
                    btnStatus.setBackground(createRoundedDrawable(Color.parseColor("#13C123"))); // Green color
                    btnStatus.setVisibility(View.VISIBLE);
                    break;

                case "COMPLETED_WAIT_REVIEW":
                    tvStatus.setText("Giao hàng thành công");
                    tvStatus.setTextColor(context.getResources().getColor(R.color.green));

                    // Set button to "Đánh giá" with green color
                    btnStatus.setText("Đánh giá");
                    btnStatus.setBackground(createRoundedDrawable(Color.parseColor("#13C123"))); // Green color
                    btnStatus.setVisibility(View.VISIBLE);

                    // Thêm sự kiện click cho nút Đánh giá
                    btnStatus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Tạo Intent để chuyển sang trang Review
                            Intent intent = new Intent(context, AddReview.class);
                            // Truyền orderID sang trang Review
                            intent.putExtra("orderID", order.getIDOrder());
                            // Bắt đầu Activity mới
                            context.startActivity(intent);
                        }
                    });
                    if(context instanceof OrderHistory || context instanceof WaitingConfirmation){

                        btnStatus.setText("Đánh giá");
                        btnStatus.setBackground(createRoundedDrawable(Color.parseColor("#13C123"))); // Green color
                        btnStatus.setVisibility(View.VISIBLE);
                    }
                    break;

                case "COMPLETED":
                    tvStatus.setText("Giao hàng thành công");
                    tvStatus.setTextColor(context.getResources().getColor(R.color.green));

                    // Set button to "Mua lại" with green color
                    btnStatus.setText("Mua lại");
                    btnStatus.setBackground(createRoundedDrawable(Color.parseColor("#13C123"))); // Green color
                    btnStatus.setVisibility(View.VISIBLE);
                    break;

                default:
                    // Handle any other status if needed
                    if(context instanceof OrderHistory || context instanceof WaitingConfirmation)
                    {
                        btnStatus.setVisibility(View.GONE);
                    }
                    break;
            }

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
