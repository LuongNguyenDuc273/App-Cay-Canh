package com.adrnc_g02.appcaycanh;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Model.Line;

public class AddProduct extends AppCompatActivity {
    private EditText edtProductName, edtQuantity, edtPrice;
    private Spinner spinnerProductType;
    private String selectedLineID;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Anh xa
        spinnerProductType = findViewById(R.id.spinnerProductType);

        //Lay du lieu Line tu database cho spinner
        updateSpinner();

    }

    private void updateSpinner(){
        database= FirebaseDatabase.getInstance();
        myRef = database.getReference("Line");
        List<Line> lineList = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lineList.clear(); // Clear danh sách trước khi thêm dữ liệu mới

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Line line = childSnapshot.getValue(Line.class);
                    if (line != null) {
                        lineList.add(line);
                    }
                }

                // Tạo một danh sách chỉ chứa NameLine để hiển thị trong Spinner
                List<String> nameList = new ArrayList<>();
                for (Line line : lineList) {
                    nameList.add(line.getNameLine());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddProduct.this, android.R.layout.simple_spinner_dropdown_item, nameList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerProductType.setAdapter(adapter);

                // Lắng nghe sự kiện khi một item được chọn trong Spinner
                spinnerProductType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // Lấy IDLine tương ứng với item được chọn
                        selectedLineID = lineList.get(position).getIDLine();
                        Toast.makeText(AddProduct.this, "Selected Line ID: " + selectedLineID, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Xử lý khi không có item nào được chọn (tùy chọn)
                        if (!lineList.isEmpty()) {
                            selectedLineID = lineList.get(0).getIDLine(); // Lấy ID của phần tử đầu tiên
                        } else {
                            selectedLineID = null; // Hoặc một giá trị mặc định khác
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu có
                Toast.makeText(AddProduct.this, "Lỗi khi lấy dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
