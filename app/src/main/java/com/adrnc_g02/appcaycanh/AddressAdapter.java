package com.adrnc_g02.appcaycanh;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Model.Address;

// Khai bao lop AddressAdapter ke thua tu RecycleView.Adapter su dung 1 ViewHolder
public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    // Danh sach address can hien thi
    static List<Address> addressList;
    static Context context;
    static String currentUserID;

    // Khoi tao danh sach va gan vao bien addressList
    public AddressAdapter(List<Address> addressList, Context context, String currentUserID) {
        this.addressList = addressList;
        this.context = context;
        this.currentUserID = currentUserID;
    }

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
        String address = addressList.get(position).getAddressLoc();
        holder.bind(address, position);
    }

    // Lay so luong item can hien thi
    @Override
    public int getItemCount() {
        return addressList.size();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        private final EditText addressText;
        private final ImageView btnDelete;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            addressText = itemView.findViewById(R.id.address_text);
            btnDelete = itemView.findViewById(R.id.btn_delete_address);
        }

        private void bind(String address, int position){
            addressText.setText(address);
            addressText.setOnEditorActionListener((v, actionId, event) -> {
                if(actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                    updateAddress(v, position);
                    return true;
                }
                return false;
            });

//            btnDelete.setOnClickListener(v -> {
//                if(context instanceof Profile){
//                    ((Profile) context).deleteAddress(addressList.get(position).getIDAdress(), position);}
//            });

            btnDelete.setOnClickListener(v -> {
                if (context instanceof Profile && position >= 0 && position < addressList.size()) {
                    Address addresss = addressList.get(position);
                    ((Profile) context).deleteAddress(
                            addresss.getIDAdress(),
                            position
                    );
                }
            });

        }

        private void updateAddress(View v, int position){
            String newAddress = addressText.getText().toString();
            String idAddress = addressList.get(position).getIDAdress();
            Address address = new Address(newAddress, idAddress);
            if (context instanceof Profile) {
                ((Profile) context).updateField(
                        "Customer",
                        currentUserID,
                        "Address/" + idAddress, // Đường dẫn field dạng address/0, address/1...
                        address,
                        v
                );
            }
        }
    }
}