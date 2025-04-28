package com.adrnc_g02.appcaycanh;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import Model.Line;
import Model.Product;

public class Shopping extends AppCompatActivity {

    // UI elements
    private RecyclerView listProduct, listLine;
    private Button btnall;
    private TextView title;
    private BottomNavigationView bottomNavigationView;

    // Adapter
    private ProductApdater productAdater;
    private MyAdapter lineAdapter;

    // Data
    private ArrayList<Line> dataLine;
    private ArrayList<Product> dataProduct;

    // Firebase
    private FirebaseDatabase database;
    private DatabaseReference tbline, tblProduct;

    // Layout Managers
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;

    // Helper Classes
    private MenuNavigation menuNavigation = new MenuNavigation(this);
    private GenericFunction genericFunction = new GenericFunction();
    SessionControl session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Thiet lap giao dien co ban
        setupUI();

        // Anh xa
        initializeViews();

        // Thiet lap bottom navigation
        setupBottomNavigation();

        // Thiet lap Recycler View
        setupRecyclerViews();

        // Load du lieu
        loadData();
    }

    /**
     * Thiet lap giao dien co ban va xu ly insets cho he thong.
     */
    private void setupUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Anh xa cac view.
     */
    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        session = new SessionControl(this);
        listLine = findViewById(R.id.listLine);
        listProduct = findViewById(R.id.recyclerViewPlant);
        btnall = findViewById(R.id.all);
        title = findViewById(R.id.txtTitle);
    }

    /**
     * Thiet lap bottom navigation.
     */
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navCart);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            menuNavigation.navigateTo(itemId);
            return true;
        });
    }

    /**
     * Thiet lap Recycler Views.
     */
    private void setupRecyclerViews() {
        // Hien thi danh muc san pham
        database = FirebaseDatabase.getInstance();
        tbline = database.getReference("Line");
        dataLine = new ArrayList<Line>();
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        listLine.setLayoutManager(linearLayoutManager);
        lineAdapter = new MyAdapter(Shopping.this, dataLine, new MyAdapter.OnLineClickListener() {
            @Override
            public void onLineClick(int position, Line line) {
                btnall.setBackground(ContextCompat.getDrawable(Shopping.this, R.drawable.bg_button_outline));
                btnall.setTextColor(ContextCompat.getColor(Shopping.this, android.R.color.black));
                filterProductsByLine(line.getIDLine());
            }
        });
        lineAdapter.setAllButton(btnall);
        listLine.setAdapter(lineAdapter);

        // Hien thi cac san pham
        tblProduct = database.getReference("Product");
        dataProduct = new ArrayList<Product>();
        gridLayoutManager = new GridLayoutManager(this, 2);
        listProduct.setLayoutManager(gridLayoutManager);
        productAdater = new ProductApdater(Shopping.this, dataProduct);
        listProduct.setAdapter(productAdater);
    }

    /**
     * Load du lieu.
     */
    private void loadData() {
        getAllLine();
        getAllProduct();
    }

    /**
     * Lay tat ca san pham.
     */
    private void getAllProduct() {
        tblProduct.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataProduct.clear();
                for (DataSnapshot productsnapshot : snapshot.getChildren()) {
                    Product product = productsnapshot.getValue(Product.class);
                    if (product != null) {
                        dataProduct.add(product);
                    }
                }
                productAdater.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Shopping.this, "Loi kho tai cac products: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Error fetching lines", error.toException());
            }
        });
    }

    /**
     * Lay tat ca cac danh muc.
     */
    private void getAllLine() {
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText("Tat Ca");
        tbline.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataLine.clear();
                for (DataSnapshot lineSnapshot : snapshot.getChildren()) {
                    Line line = lineSnapshot.getValue(Line.class);
                    if (line != null) {
                        dataLine.add(line);
                    }
                }
                Log.d("FirebaseData", "Final dataLine size: " + dataLine.size());
                lineAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Shopping.this, "Loi kho tai cac lines: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Error fetching lines", error.toException());
            }
        });

        // Su kien khi nhan vao All
        btnall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnall.setBackground(ContextCompat.getDrawable(Shopping.this, R.drawable.bg_button_green));
                btnall.setTextColor(ContextCompat.getColor(Shopping.this, android.R.color.white));
                lineAdapter.selectAllButton();
                productAdater.searchData(dataProduct);
                filterProductsByLine(null);
            }
        });
    }

    /**
     * Loc san pham theo danh muc.
     * @param idLine ID cua danh muc.
     */
    private void filterProductsByLine(String idLine) {
        if (idLine == null || idLine.isEmpty()) {
            TextView txtTitle = findViewById(R.id.txtTitle);
            txtTitle.setText("Tat Ca");
            productAdater.searchData(dataProduct);
            return;
        }
        String lineName = "Tat Ca";
        for (Line line : dataLine) {
            if (line.getIDLine() != null && line.getIDLine().equals(idLine)) {
                lineName = line.getNameLine();
                break;
            }
        }
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(lineName);

        ArrayList<Product> filterProduct = new ArrayList<>();
        for (Product product : dataProduct) {
            if (product.getIDLine() != null && product.getIDLine().equals(idLine)) {
                filterProduct.add(product);
            }
        }
        productAdater.searchData(filterProduct);
    }
}
