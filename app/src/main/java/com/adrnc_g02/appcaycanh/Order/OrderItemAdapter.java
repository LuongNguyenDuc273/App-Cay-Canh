package com.adrnc_g02.appcaycanh.Order;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adrnc_g02.appcaycanh.R;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private Context context;
    private List<OrderConfirmation.OrderItem> orderItems;

    public OrderItemAdapter(Context context, List<OrderConfirmation.OrderItem> orderItems) {
        this.context = context;
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_confirmation, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderConfirmation.OrderItem item = orderItems.get(position);

        // Set product name
        holder.productName.setText(item.product.getNameProc());

        // Set quantity
        holder.quantity.setText("x" + item.cart.getQuantity());

        // Format and set price
        try {
            double price = Double.parseDouble(item.product.getPrice());
            double totalPrice = price * item.cart.getQuantity();
            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            holder.productPrice.setText(formatter.format(totalPrice) + "₫");
        } catch (NumberFormatException e) {
            holder.productPrice.setText(item.product.getPrice() + "₫");
        }

        // Load image
        if (item.product.getPhoto() != null && !item.product.getPhoto().isEmpty()) {
            Glide.with(context)
                    .load(item.product.getPhoto())
                    .placeholder(R.drawable.plant_placeholder)
                    .into(holder.productImage);
        } else {
            holder.productImage.setImageResource(R.drawable.plant_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, quantity;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.iv_product);
            productName = itemView.findViewById(R.id.tv_product_name);
            productPrice = itemView.findViewById(R.id.tv_product_price);
            quantity = itemView.findViewById(R.id.tv_quantity);
        }
    }
}