package com.adrnc_g02.appcaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Model.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView listbnt,listProduct;
    private Button btnlogout;
    private BottomNavigationView bottomNavigationView;
    private ArrayList<Line> dataLine;
    private ArrayList<Product> dataProduct;
    private FirebaseDatabase database;

    private LinearLayoutManager linearLayoutManager;
    MyAdapter myAdapter;
    private ProductApdater productApdater;
    private TextView nd,Username;
    private GridLayoutManager gridLayoutManager;
    private DatabaseReference tbline, tblProduct;
    private ImageButton btnFavorite, btnNotification;

    SessionControl session;
    private FirebaseAuth auth; //Added FirebaseAuth instance variable
    private GoogleSignInClient mGoogleSignInClient; // Add GoogleSignInClient

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Anh xa
        session = new SessionControl(this);
        btnFavorite = findViewById(R.id.btnFavorite); //Test
        btnNotification = findViewById(R.id.btnNotification); //Test
        auth = FirebaseAuth.getInstance(); // Initialize Firebase Auth here
        listbnt = findViewById(R.id.listbutton);
        listProduct = findViewById(R.id.recyclerViewPlants);
        Username = findViewById(R.id.txtUsername);
        bottomNavigationView = findViewById(R.id.bottomNavigation);


        //Chuyen trang
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navHome){
                return true;
            }
            else if (itemId == R.id.navCart) {
            }
            else if (itemId == R.id.navExplore) {
                startActivity(new Intent(MainActivity.this, ShoppingCart.class));
            }
            else if (itemId == R.id.navProfile) {
                session.signOutCompletely();
            }
            return true;
        });

        //hien thi danh muc san pham
        database = FirebaseDatabase.getInstance();
        tbline = database.getReference("Line");
        dataLine = new ArrayList<Line>();
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        listbnt.setLayoutManager(linearLayoutManager);
        myAdapter = new MyAdapter(MainActivity.this, dataLine);
        listbnt.setAdapter(myAdapter);
        getAllLine();

        // hien thi cac san pham
        tblProduct = database.getReference("Product");
        dataProduct = new ArrayList<Product>();
        gridLayoutManager = new GridLayoutManager(this, 2);
        listProduct.setLayoutManager(gridLayoutManager);
        productApdater = new ProductApdater(MainActivity.this,dataProduct);
        listProduct.setAdapter(productApdater);
        getAllProduct();
        // hien thi user
        setUsername();

        //Test chuyen sang them sang pham
        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddProduct.class);
                startActivity(intent);
            }
        });

        //Test chuyen sang them danh muc san pham
        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddLine.class);
                startActivity(intent);
            }
        });


        // Initialize Google Sign-In client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Replace with your web client ID
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


//        btnlogout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                signOutCompletely();
//            }
//        });
    }

    private void setUsername() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userName = getIntent().getStringExtra("userName");
            String userName2 = getIntent().getStringExtra("userName2");
            if (userName != null) {
                Username.setText(userName);
            } else if (userName2 != null) {
                Username.setText(userName2);
            } else {
                Username.setText(currentUser.getEmail());
            }
        } else {
            Username.setText("Chua Dang Nhap");
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
                productApdater.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Loi kho tai cac products: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Error fetching lines", error.toException());
            }
        });
    }

    private void getAllLine() {
        tbline.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataLine.clear();
                for(DataSnapshot lineSnapshot:snapshot.getChildren())
                {
                    Line line = lineSnapshot.getValue(Line.class);
                    if (line!=null)
                    {
                        dataLine.add(line);
                    }
                }
                Log.d("FirebaseData", "Final dataLine size: " + dataLine.size());
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Loi kho tai cac lines: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Error fetching lines", error.toException());
            }
        });
    }


}
