package com.adrnc_g02.appcaycanh.Order;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adrnc_g02.appcaycanh.Productdetail.ProductDetail;
import com.adrnc_g02.appcaycanh.R;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {
    private Context context;
    private List<OrderAdapter.OrderProductItem> productItems;

    public OrderDetailAdapter(Context context, List<OrderAdapter.OrderProductItem> productItems) {
        this.context = context;
        this.productItems = productItems;
    }

    public void updateData(List<OrderAdapter.OrderProductItem> newData) {
        this.productItems = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_orderdetail, parent, false);
        return new OrderDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        OrderAdapter.OrderProductItem item = productItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return productItems != null ? productItems.size() : 0;
    }

    public class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvVariant;
        private TextView tvDiscountPrice;
        private TextView tvQuantity;
        private LinearLayout layoutitem;

        public OrderDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvVariant = itemView.findViewById(R.id.tvProductDescription);
            tvDiscountPrice = itemView.findViewById(R.id.tvDiscountPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            layoutitem = itemView.findViewById(R.id.layoutItemOrder);
        }

        public void bind(OrderAdapter.OrderProductItem item) {
            tvProductName.setText(item.getProduct().getNameProc());
            tvVariant.setText(item.getProduct().getDescribe());
            // Format and set price
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedPrice = formatter.format(item.getPrice()).replace("₫", "đ");
            tvDiscountPrice.setText(formattedPrice);

            // Set quantity
            tvQuantity.setText("x" + item.getQuantity());

            // Load image using Glide
            if (item.getProduct().getPhoto() != null && !item.getProduct().getPhoto().isEmpty()) {
                Glide.with(context)
                        .load(item.getProduct().getPhoto())
                        .placeholder(R.drawable.plant_placeholder)
                        .error(R.drawable.plant_placeholder)
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.plant_placeholder);
            }
            layoutitem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ProductDetail.class);
                    intent.putExtra("Image", item.getProduct().getPhoto());
                    intent.putExtra("Name", item.getProduct().getNameProc());
                    intent.putExtra("Line", item.getProduct().getIDLine());
                    intent.putExtra("Price", item.getProduct().getPrice());
                    intent.putExtra("Description", item.getProduct().getDescribe());
                    intent.putExtra("Quantity", item.getProduct().getReQuantity());
                    intent.putExtra("Key", item.getProduct().getIDProc());
                    context.startActivity(intent);
                }
            });
        }
    }
}
