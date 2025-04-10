package com.adrnc_g02.appcaycanh;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;


import Model.Product;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProductApdater extends RecyclerView.Adapter<ProductApdater.MyViewHolder> {
    Context context;
    List<Product> products;
    public ProductApdater(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(products.get(position).getPhoto()).into(holder.imageView);
        holder.productName.setText(products.get(position).getNameProc());
        holder.productPrice.setText(products.get(position).getPrice());
        holder.productItem.setOnClickListener(new View.OnClickListener() {
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
        return products == null ? 0 : products.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        CardView productItem;
        TextView productName, productPrice,productDetail,product;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            productItem = itemView.findViewById(R.id.productCard);
            imageView = itemView.findViewById(R.id.imgPlant);
            productName = itemView.findViewById(R.id.txtPlantName);
            productPrice =itemView.findViewById(R.id.txtPlantPrice);
        }
    }
    public void searchData(ArrayList<Product> productList){
        products = productList;
        notifyDataSetChanged();
    }
}
