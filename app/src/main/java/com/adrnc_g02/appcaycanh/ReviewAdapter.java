package com.adrnc_g02.appcaycanh;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Model.Order;
import Model.OrderDetail;
import Model.Product;
import Model.Review;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Product> allProducts;
    private List<Order> orderList;
    private GenericFunction genericFunction = new GenericFunction();
    private OrderDetailAdapter productAdapter;

    public ReviewAdapter(Context context, List<Order> orderList, List<Product> allProducts) {
        this.context = context;
        this.orderList = orderList != null ? orderList : new ArrayList<>();
        this.allProducts = allProducts;
    }

    public void updateData(List<Order> newData) {
        this.orderList = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Order order = orderList.get(position);;
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivUserAvatar;
        private TextView tvUsername;
        private TextView tvReviewDate;
        private RatingBar ratingBar;
        private TextView tvReviewContent;
        private RecyclerView orderItemsRecyclerView;
        private OrderDetailAdapter orderDetailAdapter;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvReviewContent = itemView.findViewById(R.id.tvReviewContent);
            orderItemsRecyclerView = itemView.findViewById(R.id.orderItemsRecyclerView);

            // Setup nested RecyclerView for order items
            orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            orderItemsRecyclerView.setNestedScrollingEnabled(false);
            productAdapter = new OrderDetailAdapter(context, new ArrayList<>());
            orderItemsRecyclerView.setAdapter(productAdapter);
        }

        public void bind(Order order) {
            setUser(order);
            setReview(order);
            tvReviewDate.setText(order.getOrderTime());

            // Setup order items adapter
            setOrderDetails(order, productAdapter, allProducts);
        }

        public void setUser(Order order) {
            DatabaseReference nameRef = genericFunction.getTableReference("Customer")
                    .child(order.getIDCus()).child("nameCus");

            nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    String username = dataSnapshot.getValue(String.class);
                    tvUsername.setText(username);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle possible errors!
                    tvUsername.setText("Error fetching username"); // Or handle the error more gracefully
                    Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
                }
            });

            ivUserAvatar.setImageResource(R.drawable.profile);
        }

        public void setReview(Order order) {
            genericFunction.getTableReference("Order")
                    .child(order.getIDOrder()).child("Review").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Review review = snapshot.getValue(Review.class);

                            // Check if review is null, and set default values if it is
                            if (review == null) {
                                ratingBar.setRating(0);
                                tvReviewContent.setText("null");
                            } else {
                                ratingBar.setRating(review.getRating());
                                tvReviewContent.setText(review.getComment());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

        private void setOrderDetails(Order order, OrderDetailAdapter productAdapter, List<Product> allProducts) {
            genericFunction.getTableReference("Order").child(order.getIDOrder())
                    .child("OrderDetail").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            //Create list to store each product
                            List<OrderAdapter.OrderProductItem> productItems = new ArrayList<>();
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
                                    OrderAdapter.OrderProductItem item = new OrderAdapter.OrderProductItem(
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
                            //Update the information to the new data
                            productAdapter.updateData(productItems);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }
    }
}