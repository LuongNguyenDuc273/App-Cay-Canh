package com.adrnc_g02.appcaycanh.Generic;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.Task;

public class GenericFunction<T> {
    private final DatabaseReference databaseReference;

    public GenericFunction() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    // Thêm dữ liệu (Firebase Realtime sẽ tự động thêm nếu chưa có)
    public Task<Void> addData(String tableName, String itemId, T data) {
        return databaseReference.child(tableName).child(itemId).setValue(data);
    }

    // Xóa dữ liệu theo ID
    public Task<Void> deleteData(String tableName, String itemId) {
        return databaseReference.child(tableName).child(itemId).removeValue();
    }

    // Lấy tham chiếu đến một bảng trong Firebase
    public DatabaseReference getTableReference(String tableName) {
        return databaseReference.child(tableName);
    }

    // Lấy tham chiếu đến một phần tử cụ thể theo ID
    public DatabaseReference getItemReference(String tableName, String itemId) {
        return databaseReference.child(tableName).child(itemId);
    }
    // Cập nhật dữ liệu theo field name và giá trị mới
    public Task<Void> updateData(String tableName, String itemId, String fieldName, Object newValue) {
        return databaseReference.child(tableName).child(itemId).child(fieldName).setValue(newValue);
    }
}