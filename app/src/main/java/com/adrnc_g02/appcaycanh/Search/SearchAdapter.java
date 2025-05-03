package com.adrnc_g02.appcaycanh.Search;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.adrnc_g02.appcaycanh.Productdetail.ProductDetail;
import com.adrnc_g02.appcaycanh.R;

import java.util.List;
import Model.Product;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private Context context;
    private List<Product> products;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public SearchAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.suggestionText.setText(product.getNameProc());

        holder.searchItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProductDetail.class);
                intent.putExtra("Image", products.get(holder.getAdapterPosition()).getPhoto());
                intent.putExtra("Name", products.get(holder.getAdapterPosition()).getNameProc());
                intent.putExtra("Line",products.get(holder.getAdapterPosition()).getIDLine());
                intent.putExtra("Price", products.get(holder.getAdapterPosition()).getPrice());
                intent.putExtra("Description",products.get(holder.getAdapterPosition()).getDescribe());
                intent.putExtra("Quantity",products.get(holder.getAdapterPosition()).getReQuantity());
                intent.putExtra("Key", products.get(holder.getAdapterPosition()).getIDProc());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView suggestionText;
        ConstraintLayout searchItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            suggestionText = itemView.findViewById(R.id.suggestion_text);
            searchItem = itemView.findViewById(R.id.search_item);
        }
    }
}