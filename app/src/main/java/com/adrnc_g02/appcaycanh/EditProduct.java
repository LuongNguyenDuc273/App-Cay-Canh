package com.adrnc_g02.appcaycanh;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import Model.Line;

public class EditProduct extends AppCompatActivity {
    private EditText edtName, edtPrice, edtQuantity, edtDes;
    private Button btnImg, btnCancel, btnUpdate;
    private ImageView imageView;
    private Spinner spinner;
    private Uri imageUri;
    private String currentImageUrl = "";
    private String productId = "";
    private boolean isImageChanged = false;
    private List<Line> lineList = new ArrayList<>();
    private String selectedLineId = "";
    private GenericFunction genericFunction = new GenericFunction();
    private DatabaseReference tblProduct, tblLine;
    private StorageReference storageReference;

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
        result ->{
                if (result.getResultCode() == RESULT_OK && result.getData() != null){
                    imageUri = result.getData().getData();
                    imageView.setImageURI(imageUri);
                    isImageChanged = true;
                }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        getData();
        setUpSpinner();
        setUpClickButton();
    }
    private void init(){
        edtName = findViewById(R.id.edtNamePlant);
        edtDes = findViewById(R.id.edtDescribe);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtPrice = findViewById(R.id.edtPrice);
        btnImg = findViewById(R.id.btnSelectImage);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);
        spinner = findViewById(R.id.spinnerLine);
        imageView = findViewById(R.id.imgPreview);
    }
    private void getData(){
        tblProduct = genericFunction.getTableReference("Product");
        tblLine = genericFunction.getTableReference("Line");
        Intent intent = getIntent();
        if(intent!=null){
            productId = intent.getStringExtra("Key");
            currentImageUrl = intent.getStringExtra("Image");
            selectedLineId = intent.getStringExtra("Line");
            edtName.setText(intent.getStringExtra("Name"));
            edtPrice.setText(intent.getStringExtra("Price"));
            edtDes.setText(intent.getStringExtra("Description"));
            edtQuantity.setText(intent.getStringExtra("Quantity"));
            if(currentImageUrl != null && !currentImageUrl.isEmpty()){
                Glide.with(this).load(currentImageUrl).into(imageView);
            }
        }
    }
    private void setUpSpinner(){
        tblLine.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lineList.clear();
                for(DataSnapshot lines :snapshot.getChildren()){
                    Line line = lines.getValue(Line.class);
                    if (line!=null){
                        lineList.add(line);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProduct.this,
                        android.R.layout.simple_spinner_item,
                        getLineNames());
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                for (int i=0; i<lineList.size();i++){
                    if(lineList.get(i).getIDLine().equals(selectedLineId))
                    {
                        spinner.setSelection(i);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProduct.this, "Lỗi khi tải danh sách loại cây: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLineId = lineList.get(position).getIDLine();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private List<String> getLineNames() {
        List<String> names = new ArrayList<>();
        for (Line line : lineList) {
            names.add(line.getNameLine());
        }
        return names;
    }
    private void setUpClickButton(){
        btnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProduct();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void openImagePicker(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncher.launch(intent);
    }
    private void updateProduct(){
        String name = edtName.getText().toString().trim();
        String price = edtPrice.getText().toString().trim();
        String quantity = edtQuantity.getText().toString().trim();
        String description = edtDes.getText().toString().trim();
        if (name.isEmpty() || price.isEmpty() || quantity.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        if(isImageChanged && imageUri != null){
            final StorageReference fileRef = storageReference.child("product_image/" + UUID.randomUUID().toString());
            fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();
                            saveProduct(name,price,quantity,description,imageUrl);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProduct.this, "Lỗi khi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }else {
            saveProduct(name, price, quantity, description, currentImageUrl);
        }
    }
    private void saveProduct(String name, String price, String quantity, String des, String imageUrl){
        Map<String, Object> updates = new HashMap<>();
        updates.put("nameProc",name);
        updates.put("price", price);
        updates.put("reQuantity", quantity);
        updates.put("describe", des);
        updates.put("photo", imageUrl);
        updates.put("IDLine", selectedLineId);
        tblProduct.child(productId).updateChildren(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused)
            {
                Toast.makeText(EditProduct.this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                finish();
            }
        })
            .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProduct.this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}