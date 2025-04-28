package com.adrnc_g02.appcaycanh;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;


import Model.Product;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProductApdater extends RecyclerView.Adapter<ProductApdater.MyViewHolder> {
    Context context;
    List<Product> products;
    private DatabaseReference tblProduct;
    private GenericFunction genericFunction = new GenericFunction();
    public ProductApdater(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
        this.tblProduct = genericFunction.getTableReference("Product");

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
        if(context instanceof ProductAdmin){
            holder.productItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showOptionsDialog(holder.getAdapterPosition());
                    return true;
                }
            });
        }
        holder.productItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Line ID", products.get(holder.getAdapterPosition()).getIDLine());
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
    private void showOptionsDialog(int position){
        final CharSequence[] options = {"Xem chi tiết", "Sửa sản phẩm", "Xóa sản phẩm", "Hủy"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Tùy chọn sản phẩm");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which)
                {
                    case 0:
                        navigateToProductDetail(position);
                        break;
                    case 1:
                        navigateToEditProduct(position);
                        break;
                    case 2:
                        confirmDelete(position);
                        break;
                    case 3:
                        dialog.dismiss();
                        break;
                }
            }
        });
        builder.show();
    }
    private void navigateToProductDetail(int position) {
        Intent intent = new Intent(context, ProductDetail.class);
        intent.putExtra("Image", products.get(position).getPhoto());
        intent.putExtra("Name", products.get(position).getNameProc());
        intent.putExtra("Line", products.get(position).getIDLine());
        intent.putExtra("Price", products.get(position).getPrice());
        intent.putExtra("Description", products.get(position).getDescribe());
        intent.putExtra("Quantity", products.get(position).getReQuantity());
        intent.putExtra("Key", products.get(position).getIDProc());
        context.startActivity(intent);
    }
    private void navigateToEditProduct(int position) {
        Intent intent = new Intent(context, EditProduct.class);
        intent.putExtra("Image", products.get(position).getPhoto());
        intent.putExtra("Name", products.get(position).getNameProc());
        intent.putExtra("Line", products.get(position).getIDLine());
        intent.putExtra("Price", products.get(position).getPrice());
        intent.putExtra("Description", products.get(position).getDescribe());
        intent.putExtra("Quantity", products.get(position).getReQuantity());
        intent.putExtra("Key", products.get(position).getIDProc());
        context.startActivity(intent);
    }
    private void confirmDelete(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc chắn muốn xóa sản phẩm "+products.get(position).getNameProc()+ " ?");
        builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProduct(position);

            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
    private void deleteProduct(int postion){
        String productID = products.get(postion).getIDProc();
        tblProduct.child(productID).removeValue()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Đã xóa sản phẩm thành công", Toast.LENGTH_SHORT).show();
                        products.remove(postion);
                        notifyItemRemoved(postion);
                        notifyItemRangeChanged(postion,products.size());
                    }
                });
    }

}
