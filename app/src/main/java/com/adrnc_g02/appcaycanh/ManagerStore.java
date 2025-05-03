 package com.adrnc_g02.appcaycanh;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import Model.Product;

 public class ManagerStore extends AppCompatActivity {
     private Button btnAddProduct, btnAddLine;
     private ArrayList<Product> dataProduct;
     private DatabaseReference tblProduct;
     private FirebaseDatabase database;
     private FirebaseAuth auth;
     private GenericFunction<Customer> customerGenericFunction;
     private SessionControl session;

     private DatabaseReference customerRef, addressRef;
     private FirebaseUser currentUser;

     private GridLayoutManager gridLayoutManager;
     private GenericFunction genericFunction = new GenericFunction();
     private RecyclerView pGrid;
     private TextView txtHome, txtCart, txtManager, username, logout;
     private ImageView icHome, icCart, icManager;
     private LinearLayout btnCart, btnHome, btnManager;
     private TextView txtHello;
     private String currentUserID = "";

     private DatabaseReference myRef;

     private ProductApdater productAdater;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manager_store);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        initializeFirebase();
        loadUserData();
        btnonClick();
        logout.setOnClickListener(view -> {
            session.signOutCompletely();
            Log.d(TAG, "Logout button clicked");
        });
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
     private void initializeFirebase() {
         auth = FirebaseAuth.getInstance();
         currentUser = auth.getCurrentUser();
         database = FirebaseDatabase.getInstance();
         customerRef = database.getReference("Customer");
         session = new SessionControl(this);
         customerGenericFunction = new GenericFunction<Customer>();
         Log.d(TAG, "GenericFunction initialized: " + (customerGenericFunction != null));
     }
    private void  init(){
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserID = cUser.getUid();
        username = findViewById(R.id.userEmail);
        txtHello = findViewById(R.id.txtGreeting);
        btnHome = findViewById(R.id.home);
        btnCart = findViewById(R.id.cart);
        logout = findViewById(R.id.Logout);
        btnManager = findViewById(R.id.manager);
        txtHome = findViewById(R.id.textHome);
        txtCart = findViewById(R.id.txtcart);
        txtManager = findViewById(R.id.txtmanager);
        icCart = findViewById(R.id.iccart);
        icHome = findViewById(R.id.ichome);
        icManager = findViewById(R.id.icmanager);
    }
    private void btnonClick(){
        int selectedColor = getResources().getColor(R.color.green);
        int unselectedColor = getResources().getColor(R.color.gray);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtHome.setTextColor(selectedColor);
                icHome.setColorFilter(selectedColor);
                txtCart.setTextColor(unselectedColor);
                icCart.setColorFilter(unselectedColor);
                txtManager.setTextColor(unselectedColor);
                icManager.setColorFilter(unselectedColor);
                Intent cartIntent = new Intent(getApplicationContext(), Admin.class);
                startActivity(cartIntent);
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
     private void loadUserData() {
         Log.d(TAG, "loadUserData called");
         if (currentUser == null) {
             username.setText("Chua dang nhap");
             Log.d(TAG, "No current user, set text to 'Chua dang nhap'");
             return;
         }

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
                             Toast.makeText(ManagerStore.this, "Loi tai thong tin", Toast.LENGTH_SHORT).show();
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
}