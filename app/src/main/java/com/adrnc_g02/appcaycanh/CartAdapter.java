package com.adrnc_g02.appcaycanh;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adrnc_g02.appcaycanh.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import Model.Cart;
import Model.Product;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private CartListener listener;

    // Combined model for simplicity
    public static class CartItem {
        public Cart cart;
        public Product product;
        public boolean isSelected;

        public CartItem(Cart cart, Product product) {
            this.cart = cart;
            this.product = product;
            this.isSelected = false;
        }
    }

    // Simple listener interface
    public interface CartListener {
        void onQuantityChanged();
        void onSelectionChanged();
    }

    public CartAdapter(Context context, List<Cart> carts, List<Product> products) {
        this.context = context;
        this.cartItems = new ArrayList<>();

        // Match carts with products
        for (Cart cart : carts) {
            for (Product product : products) {
                if (cart.getIDProc().equals(product.getIDProc())) {
                    cartItems.add(new CartItem(cart, product));
                    break;
                }
            }
        }
    }

    public void setCartListener(CartListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        // Set product info
        holder.productName.setText(item.product.getNameProc());
        holder.price.setText(item.product.getPrice() + " VND");

        // Set placeholder image
        Glide.with(context).load(item.product.getPhoto()).into(holder.image);

        // Check availability
        try {
            int stock = Integer.parseInt(item.product.getReQuantity());
            if (stock <= 0) {
                holder.status.setVisibility(View.VISIBLE);
            } else {
                holder.status.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            holder.status.setVisibility(View.GONE);
        }

        // Set quantity
        holder.quantity.setText(String.valueOf(item.cart.getQuantity()));

        // Set selection state
        holder.checkBox.setChecked(item.isSelected);

        // Set click listeners
        holder.decreaseBtn.setOnClickListener(v -> {
            int qty = item.cart.getQuantity();
            if (qty > 1) {
                item.cart.setQuantity(qty - 1);
                holder.quantity.setText(String.valueOf(qty - 1));
                if (listener != null) listener.onQuantityChanged();
            }
        });

        holder.increaseBtn.setOnClickListener(v -> {
            int qty = item.cart.getQuantity();
            item.cart.setQuantity(qty + 1);
            holder.quantity.setText(String.valueOf(qty + 1));
            if (listener != null) listener.onQuantityChanged();
        });

        holder.checkBox.setOnClickListener(v -> {
            item.isSelected = holder.checkBox.isChecked();
            if (listener != null) listener.onSelectionChanged();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    // Select or deselect all items
    public void selectAll(boolean select) {
        for (CartItem item : cartItems) {
            item.isSelected = select;
        }
        notifyDataSetChanged();
    }

    // Calculate total price of selected items
    public double getTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected) {
                try {
                    double price = Double.parseDouble(item.product.getPrice());
                    total += price * item.cart.getQuantity();
                } catch (Exception e) {
                    // Skip if price format is invalid
                }
            }
        }
        return total;
    }

    // Get selected cart items
    public List<Cart> getSelectedCarts() {
        List<Cart> selected = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.isSelected) {
                selected.add(item.cart);
            }
        }
        return selected;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView productName, price, status, quantity;
        Button decreaseBtn, increaseBtn;
        CheckBox checkBox;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.iv_product);
            productName = itemView.findViewById(R.id.tv_product_name);
            price = itemView.findViewById(R.id.tv_price);
            status = itemView.findViewById(R.id.tv_status);
            quantity = itemView.findViewById(R.id.tv_quantity);
            decreaseBtn = itemView.findViewById(R.id.btn_decrease);
            increaseBtn = itemView.findViewById(R.id.btn_increase);
            checkBox = itemView.findViewById(R.id.cb_select_item);
        }
    }
}
