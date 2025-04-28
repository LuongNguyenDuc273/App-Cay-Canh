package com.adrnc_g02.appcaycanh;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

import Model.Customer;
import Model.Line;
import Model.Product;

public class ProductAdmin extends AppCompatActivity {
    private Button btnAddProduct, btnAddLine;
    private ArrayList<Product> dataProduct;
    private DatabaseReference tblProduct;
    private FirebaseDatabase database;

    private GridLayoutManager gridLayoutManager;
    private GenericFunction genericFunction = new GenericFunction();
    private RecyclerView pGrid;
    private TextView txtHome, txtCart, txtManager, username;
    private ImageView icHome, icCart, icManager;
    private LinearLayout btnCart, btnHome, btnManager;
    private TextView txtHello;
    private DatabaseReference myRef;
    private DatabaseReference customerRef, addressRef;
    private GenericFunction<Customer> customerGenericFunction;
    private SessionControl session;
    private String currentUserID = "";
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private ProductApdater productAdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initializeViews();
        myRef = genericFunction.getTableReference("Line");
        btnonClick();
        initializeFirebase();
        loadUserData();
        tblProduct = genericFunction.getTableReference("Product");
        dataProduct = new ArrayList<Product>();
        gridLayoutManager = new GridLayoutManager(this, 2);
        pGrid.setLayoutManager(gridLayoutManager);
        productAdater = new ProductApdater(ProductAdmin.this,dataProduct);
        pGrid.setAdapter(productAdater);
        getAllProduct();

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour <= 10) {greeting = "Chào buổi sáng ☀️";}
        else if (hour >= 11 && hour <= 12) {greeting = "Chào buổi trưa 🍱";}
        else if (hour >= 13 && hour <= 17) {greeting = "Chào buổi chiều 🌤️";}
        else if (hour >= 18 && hour <= 21) {greeting = "Chào buổi tối 🌆";}
        else {greeting = "Khuya rồi 😴";}
        txtHello.setText(greeting);
    }

    private void initializeViews() {
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserID = cUser.getUid();
        btnAddProduct = findViewById(R.id.addButton);
        btnAddLine = findViewById(R.id.addLine);
        pGrid = findViewById(R.id.productGrid);
        username = findViewById(R.id.userEmail);
        txtHello = findViewById(R.id.txtGreeting);
        btnHome = findViewById(R.id.home);
        btnCart = findViewById(R.id.cart);
        btnManager = findViewById(R.id.manager);
        txtHome = findViewById(R.id.textHome);
        txtCart = findViewById(R.id.txtcart);
        txtManager = findViewById(R.id.txtmanager);
        icHome = findViewById(R.id.ichome);
        icCart = findViewById(R.id.iccart);
        icManager = findViewById(R.id.icmanager);
    }
    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        customerRef = database.getReference("Customer");
        session = new SessionControl(this);
        customerGenericFunction = new GenericFunction<Customer>();
        Log.d(TAG, "GenericFunction initialized: " + (customerGenericFunction != null));
    }
    private void loadUserData() {
        Log.d(TAG, "loadUserData called");
        if (currentUser == null) {
            username.setText("Chua dang nhap");
            Log.d(TAG, "No current user, set text to 'Chua dang nhap'");
            return;
        }

        // Lấy thông tin từ Intent (nếu có)
        String userName = getIntent().getStringExtra("a");
        String userName2 = getIntent().getStringExtra("b");
        String userGmail = currentUser.getEmail();

        Log.d(TAG, "userName from intent: " + userName);
        Log.d(TAG, "userName2 from intent: " + userName2);
        Log.d(TAG, "userGmail from currentUser: " + userGmail);

        // Ưu tiên hiển thị tên từ Intent trước
        if (userName != null) {
            username.setText(userName);
            Log.d(TAG, "Set nameUser to userName");
        } else if (userName2 != null) {
            username.setText(userName2);
            Log.d(TAG, "Set nameUser to userName2");
        } else {
            username.setText(userGmail);
            Log.d(TAG, "Set nameUser to userGmail");
        }

        // Nếu có email, query thông tin từ Database
        if (userGmail != null) {
            Log.d(TAG, "Querying Customer node with Gmail: " + userGmail);
            customerRef.orderByChild("gmail")
                    .equalTo(userGmail)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d(TAG, "onDataChange called, dataSnapshot exists: " + dataSnapshot.exists());
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    Customer customer = snapshot.getValue(Customer.class);
                                    Log.d(TAG, "Customer object from snapshot: " + customer);
                                    if (customer != null) {
                                        displayInfo(customer); // Hiển thị thông tin
                                        currentUserID = customer.getIDCus(); // Lưu ID người dùng
                                        Log.d(TAG, "currentUserID set to: " + currentUserID);
                                    }
                                }
                            } else {
                                Log.d(TAG, "No data found in database, using currentUser info");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Database error: " + error.getMessage());
                            Toast.makeText(ProductAdmin.this, "Loi tai thong tin", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    private void displayInfo(Customer customer) {
        Log.d(TAG, "displayInfo called with customer: " + customer);
        if (customer.getNameCus() != null && !customer.getNameCus().isEmpty()) {
            username.setText(customer.getNameCus());
        } else {
            username.setText(currentUser.getEmail());
        }
    }
    private void getAllProduct() {
        tblProduct.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataProduct.clear();
                for(DataSnapshot productsnapshot:snapshot.getChildren())
                {
                    Product product = productsnapshot.getValue(Product.class);
                    if (product!=null)
                    {
                        dataProduct.add(product);
                    }
                }
                productAdater.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductAdmin.this, "Loi kho tai cac products: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Error fetching lines", error.toException());
            }
        });
    }
    private void btnonClick(){
        int selectedColor = getResources().getColor(R.color.green);
        int unselectedColor = getResources().getColor(R.color.gray);
        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddProduct.class);
                startActivity(intent);
            }
        });
        btnAddLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddLineDialog();
            }
        });
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtHome.setTextColor(selectedColor);
                icHome.setColorFilter(selectedColor);
                txtCart.setTextColor(unselectedColor);
                icCart.setColorFilter(unselectedColor);
                txtManager.setTextColor(unselectedColor);
                icManager.setColorFilter(unselectedColor);
                Intent homeIntent = new Intent(getApplicationContext(), Admin.class);
                startActivity(homeIntent);
                finish();
            }
        });
        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtCart.setTextColor(selectedColor);
                icCart.setColorFilter(selectedColor);
                txtHome.setTextColor(unselectedColor);
                icHome.setColorFilter(unselectedColor);
                txtManager.setTextColor(unselectedColor);
                icManager.setColorFilter(unselectedColor);
                Intent cartIntent = new Intent(getApplicationContext(), ProductAdmin.class);
                startActivity(cartIntent);
                finish();
            }
        });
        btnManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtManager.setTextColor(selectedColor);
                icManager.setColorFilter(selectedColor);
                txtHome.setTextColor(unselectedColor);
                icHome.setColorFilter(unselectedColor);
                txtCart.setTextColor(unselectedColor);
                icCart.setColorFilter(unselectedColor);
                Intent cartIntent = new Intent(getApplicationContext(), ManagerStore.class);
                startActivity(cartIntent);
                finish();
            }
        });
    }
    private void showAddLineDialog() {
        // Create an EditText for the dialog
        final EditText input = new EditText(this);
        input.setHint("Nhập tên loại cây");

        // Set layout parameters
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setPadding(50, 30, 50, 30);

        new AlertDialog.Builder(this)
                .setTitle("Thêm loại cây mới")
                .setView(input)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String lineName = input.getText().toString().trim();

                    if (!lineName.isEmpty()) {
                        // Make sure myRef is initialized
                        if (myRef == null) {
                            myRef = FirebaseDatabase.getInstance().getReference("Line");
                        }

                        // Generate a unique ID
                        String lineId = myRef.push().getKey();
                        Line newLine = new Line(lineId, lineName);
                        // Save to Firebase
                        myRef.child(lineId).setValue(newLine)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Thêm loại cây thành công", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Vui lòng nhập tên loại cây", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}