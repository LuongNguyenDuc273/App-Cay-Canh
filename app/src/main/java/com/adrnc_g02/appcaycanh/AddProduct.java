package com.adrnc_g02.appcaycanh;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import Model.Line;
import Model.Product;

public class AddProduct extends AppCompatActivity {
    private EditText edtProductName, edtQuantity, edtPrice, edtDescribe;
    private Button btnCancel, btnSave;
    private ImageView imageViewProduct;
    private Spinner spinnerProductType;
    private String selectedLineID, imgURL;
    private Uri uri;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private StorageReference storageReference;

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
        edtProductName = findViewById(R.id.editTextProductName);
        edtQuantity = findViewById(R.id.editTextQuantity);
        edtPrice = findViewById(R.id.editTextPrice);
        edtDescribe=findViewById(R.id.editTextDescribe);
        btnSave = findViewById(R.id.buttonSave);
        imageViewProduct = findViewById(R.id.imageViewProduct);
        spinnerProductType = findViewById(R.id.spinnerProductType);

        //Lay du lieu Line tu database cho spinner
        UpdateSpinner();

        //Mo cua so chon anh
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK){
                            Intent data = result.getData();
                            uri = data.getData();
                            imageViewProduct.setImageURI(uri);
                        } else {
                            Toast.makeText(AddProduct.this, "Chưa chọn ảnh", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        //Su kien chon anh
        imageViewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });

        //Su kien them san pham
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveProduct();
            }
        });

    }

    private void SaveProduct(){
        storageReference = FirebaseStorage.getInstance().getReference().child("Product").child(uri.getLastPathSegment());
        if (uri == null) {
            // No image selected!
            Toast.makeText(AddProduct.this, "Xin bạn hãy chọn ảnh trước khi lưu sản phẩm.", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Android Images")
                .child(uri.getLastPathSegment());

        storageReference.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        imgURL = uri.toString();
                        UploadProduct();
                    });
                })
                .addOnFailureListener(e -> {
                    // Display an error message to the user
                    Toast.makeText(AddProduct.this, "Chọn ảnh thất bại.", Toast.LENGTH_SHORT).show();
                });
    }

    private void UploadProduct(){
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Product");
        String productName = edtProductName.getText().toString();
        String quantity = edtQuantity.getText().toString().trim();
        String price = edtPrice.getText().toString().trim();
        String Describe = edtDescribe.getText().toString();
        String key = myRef.push().getKey();

        Product product = new Product(key, selectedLineID, productName, quantity, price, Describe, imgURL);

        myRef.child(key).setValue(product).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(AddProduct.this, "Saved", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddProduct.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void UpdateSpinner(){
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
