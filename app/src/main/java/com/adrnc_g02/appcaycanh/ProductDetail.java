package com.adrnc_g02.appcaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProductDetail extends AppCompatActivity {
    private ImageView imageView;
    private ImageButton back;
    private TextView productName, productPrice, productCategory, productDes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imageView = findViewById(R.id.ivProductImage);
        productName = findViewById(R.id.tvProductName);
        productPrice = findViewById(R.id.tvProductPrice);
        productDes= findViewById(R.id.tvProductDescription);
        productCategory = findViewById(R.id.tvProductCategory);
        back= findViewById(R.id.btnBack);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });

        String imageUrl = getIntent().getStringExtra("Image");
        String name = getIntent().getStringExtra("Name");
        String price = getIntent().getStringExtra("Price");
        String line = getIntent().getStringExtra("Line");
        String des = getIntent().getStringExtra("Description");
        String key = getIntent().getStringExtra("Key");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Line").child(line);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    String lineName = snapshot.child("nameLine").getValue(String.class);
                    if (lineName != null) {
                        Log.d("FirebaseData", "Tên Line: " + lineName);
                        productCategory.setText(lineName);
                    }
                }
                else {
                    Log.d("FirebaseData", "LineID không tồn tại!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Lỗi: " + error.getMessage());
            }
        });
        Glide.with(this).load(imageUrl).into(imageView);
        productName.setText(name);
        productPrice.setText(price);
        productDes.setText(des);
    }
}