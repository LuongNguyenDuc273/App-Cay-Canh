package com.adrnc_g02.appcaycanh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Model.Address;

// Khai bao lop AddressAdapter ke thua tu RecycleView.Adapter su dung 1 ViewHolder
public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    // Danh sach address can hien thi
    private List<Address> addressList;

    // Khoi tao danh sach va gan vao bien addressList
    public AddressAdapter(List<Address> addressList) {
        this.addressList = addressList;
    }


    // Tao ViewHolder - giao dien tung item
    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        // Gan dia chi vao item de hien thi va chinh sua
        Address address = addressList.get(position);
        holder.addressText.setText(address.getAddressLoc());

        holder.btnDelete.setOnClickListener(v -> {
            // Xử lý sửa/xóa địa chỉ
            deleteAddress(address, position);
        });
    }

    // Lay so luong item can hien thi
    @Override
    public int getItemCount() {
        return addressList.size();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        EditText addressText;
        ImageView btnDelete;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            addressText = itemView.findViewById(R.id.address_text);
            btnDelete = itemView.findViewById(R.id.btn_delete_address);
        }
    }

    private void deleteAddress(Address address, int position) {
        // Triển khai xóa địa chỉ nếu cần
    }
}