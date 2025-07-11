package com.adrnc_g02.appcaycanh.Home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adrnc_g02.appcaycanh.Adminn.AddLine;
import com.adrnc_g02.appcaycanh.Adminn.AddProduct;
import com.adrnc_g02.appcaycanh.Adminn.Admin;
import com.adrnc_g02.appcaycanh.Generic.GenericFunction;
import com.adrnc_g02.appcaycanh.Menu.MenuNavigation;
import com.adrnc_g02.appcaycanh.MyAdapter;
import com.adrnc_g02.appcaycanh.Order.OrderItemAdapter;
import com.adrnc_g02.appcaycanh.Productdetail.ProductApdater;
import com.adrnc_g02.appcaycanh.Profile.Profile;
import com.adrnc_g02.appcaycanh.R;
import com.adrnc_g02.appcaycanh.Search.Search;
import com.adrnc_g02.appcaycanh.Session.AccessControl;
import com.adrnc_g02.appcaycanh.Session.SessionControl;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Model.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // UI elements
    private static final String TAG = "MainActivity";
    private AccessControl accessControl;
    private RecyclerView listbnt, listProduct;
    private Button btnlogout, btnAll;
    private BottomNavigationView bottomNavigationView;
    private ImageView admin;
    private CardView searchbar;
    private SearchView search;
    private String currentUserID = "";
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private ArrayList<BarEntry> revenueList;
    private OrderItemAdapter orderItemAdapter;
    private TextView txtHome, txtCart, txtManager;
    private DatabaseReference customerRef, addressRef;
    private GenericFunction<Customer> customerGenericFunction;
    private SessionControl session;
    private FirebaseDatabase database;

    private TextView nd, Username;
    private ImageButton btnFavorite, btnNotification;

    // Firebase
    private DatabaseReference tbline, tblProduct;
    private DatabaseReference orderDetailQuanTity;
    private GoogleSignInClient mGoogleSignInClient; // GoogleSignInClient

    // Adapters and Layout Managers
    private LinearLayoutManager linearLayoutManager;
    private MyAdapter myAdapter;
    private ProductApdater productApdater;
    private GridLayoutManager gridLayoutManager;

    // Data
    private ArrayList<Line> dataLine;
    private ArrayList<Product> dataProduct;
    private HashMap<String, Integer> productSalesMap = new HashMap<>();

    // Helper classes
    private MenuNavigation menuNavigation = new MenuNavigation(this);
    private GenericFunction genericFunction = new GenericFunction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        accessControl = new AccessControl(this);
        if (!accessControl.checkAccessForActivity(MainActivity.class)) {
            accessControl.redirectUnauthorizedAccess(this);
            return;
        }
        // Thiet lap giao dien co ban
        setupUI();

        // Anh xa cac view
        initializeViews();

        // Khoi tao Firebase
        initializeFirebase();

        // Thiet lap Adapter cho RecyclerView
        setupRecyclerViews();

        // Load du lieu
        loadData();

        // Thiet lap listeners
        setupListeners();
    }

    /**
     * Thiet lap giao dien co ban va xu ly insets cho he thong.
     */
    private void setupUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Anh xa cac view tu layout.
     */
    private void initializeViews() {
        session = new SessionControl(this);
        btnFavorite = findViewById(R.id.btnFavorite); //Test
        btnNotification = findViewById(R.id.btnNotification); //Test
        auth = FirebaseAuth.getInstance(); // Khoi tao Firebase Auth
        listbnt = findViewById(R.id.listbutton);
        listProduct = findViewById(R.id.recyclerViewPlants);
        Username = findViewById(R.id.txtUsername);
        btnAll = findViewById(R.id.all);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        search = findViewById(R.id.searchView);
        searchbar = findViewById(R.id.searchContainer);
        admin = findViewById(R.id.imgProfile);
    }

    /**
     * Khoi tao Firebase va lay du lieu tham chieu.
     */
    private void initializeFirebase() {
        database = FirebaseDatabase.getInstance();
        tbline = database.getReference("Line");
        tblProduct = database.getReference("Product");
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        customerRef = database.getReference("Customer");
        session = new SessionControl(this);
        customerGenericFunction = new GenericFunction<Customer>();
    }

    /**
     * Thiet lap RecyclerViews va Adapters.
     */
    private void setupRecyclerViews() {
        // Danh muc san pham
        dataLine = new ArrayList<Line>();
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        listbnt.setLayoutManager(linearLayoutManager);
        myAdapter = new MyAdapter(MainActivity.this, dataLine, new MyAdapter.OnLineClickListener() {
            @Override
            public void onLineClick(int position, Line line) {
                btnAll.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.bg_button_outline));
                btnAll.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.black));
                Log.d("LineClick", "Line clicked: position=" + position + ", lineId=" + line.getIDLine() + ", lineName=" + line.getNameLine());
                filterProductsByLine(line.getIDLine());
            }
        });
        myAdapter.setAllButton(btnAll);
        listbnt.setAdapter(myAdapter);

        // San pham
        dataProduct = new ArrayList<Product>();
        gridLayoutManager = new GridLayoutManager(this, 2);
        listProduct.setLayoutManager(gridLayoutManager);
        productApdater = new ProductApdater(MainActivity.this, dataProduct);
        listProduct.setAdapter(productApdater);
    }

    /**
     * Load du lieu ban dau.
     */
    private void loadData() {
        getAllLine();
        loadBestSellingProducts();
        loadUserData();
        setupGreeting();
        initGoogleSignIn();
    }

    /**
     * Thiet lap cac listeners cho cac button va cac thanh phan khac.
     */
    private void setupListeners() {
        // Chuyen trang
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            menuNavigation.navigateTo(itemId);
            return true;
        });

        // Chuyen sang trang admin
        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = getIntent().getStringExtra("userName");
                String userName2 = getIntent().getStringExtra("userName2");
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                        .child("Customer").child(currentUser.getUid());
                userRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String userRole = snapshot.getValue(String.class);
                        Intent intent;
                        if (userRole != null && userRole.equals("Admin")) {
                            intent = new Intent(getApplicationContext(), Admin.class);
                        } else {
                            intent = new Intent(getApplicationContext(), Profile.class);
                        }
                        intent.putExtra("userEmail", userName);
                        intent.putExtra("userEmail2", userName2);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

        //Test chuyen sang them san pham
        btnFavorite.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddProduct.class);
            startActivity(intent);
        });

        //Test chuyen sang them danh muc san pham
        btnNotification.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddLine.class);
            startActivity(intent);
        });

        btnAll.setOnClickListener(v -> {
            btnAll.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.bg_button_green));
            btnAll.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.white));
            myAdapter.selectAllButton();
            productApdater.searchData(dataProduct);
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Search.class);
                startActivity(intent);
            }
        });
    }

    private void loadUserData() {
        Log.d(TAG, "loadUserData called");
        if (currentUser == null) {
            Username.setText("Chua dang nhap");
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
            Username.setText(userName);
            Log.d(TAG, "Set nameUser to userName");
        } else if (userName2 != null) {
            Username.setText(userName2);
            Log.d(TAG, "Set nameUser to userName2");
        } else {
            Username.setText(userGmail);
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
                            Toast.makeText(MainActivity.this, "Loi tai thong tin", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void displayInfo(Customer customer) {
        Log.d(TAG, "displayInfo called with customer: " + customer);
        // Hiển thị tên, ưu tiên tên từ Database
        if (customer.getNameCus() != null && !customer.getNameCus().isEmpty()) {
            Username.setText(customer.getNameCus());
        } else {
            Username.setText(currentUser.getEmail());
        }
    }


    /**
     *  Thiet lap loi chao theo thoi gian
     */
    private void setupGreeting() {
        TextView txtGreeting = findViewById(R.id.txtGreeting);
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour <= 10) {
            greeting = "Chao buoi sang ☀️";
        } else if (hour >= 11 && hour <= 12) {
            greeting = "Chao buoi trua 🍱";
        } else if (hour >= 13 && hour <= 17) {
            greeting = "Chao buoi chieu 🌤️";
        } else if (hour >= 18 && hour <= 21) {
            greeting = "Chao buoi toi 🌆";
        } else {
            greeting = "Khuya roi 😴";
        }
        txtGreeting.setText(greeting);
    }
    /**
     * Khoi tao Google Sign-In client
     */
    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Replace with your web client ID
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    /**
     * Loc san pham theo danh muc.
     * @param idLine ID cua danh muc.
     */
    private void filterProductsByLine(String idLine) {
        if (idLine == null || idLine.isEmpty()) {
            productApdater.searchData(dataProduct);
            return;
        }
        ArrayList<Product> filterProduct = new ArrayList<>();
        for (Product product : dataProduct) {
            if (product.getIDLine() != null && product.getIDLine().equals(idLine)) {
                filterProduct.add(product);
            }
        }
        productApdater.searchData(filterProduct);
    }

    /**
     * Thiet lap ten nguoi dung.
     */
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

    /**
     * Load san pham ban chay nhat.
     */
    public void loadBestSellingProducts() {
        DatabaseReference orderRef = database.getReference("Order");
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productSalesMap.clear();
                Log.d("BestSeller", "So ban ghi OrderDetail: " + snapshot.getChildrenCount());

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    DataSnapshot orderDetailSnapshot = orderSnapshot.child("OrderDetail");
                    Log.d("BestSeller", "OrderID: " + orderSnapshot.getKey() + ", Co OrderDetail: " + orderDetailSnapshot.exists());
                    for (DataSnapshot detaiOder : orderDetailSnapshot.getChildren()) {
                        String productID = detaiOder.child("idproc").getValue(String.class);
                        Integer quantity = detaiOder.child("totalQuantity").getValue(Integer.class);
                        Log.d("BestSeller", "Chi tiet don hang - ProductID: " + productID + ", So luong: " + quantity);
                        if (productID != null && quantity != null) {
                            if (productSalesMap.containsKey(productID)) {
                                int currentQuantity = productSalesMap.get(productID);
                                productSalesMap.put(productID, currentQuantity + quantity);
                            } else {
                                productSalesMap.put(productID, quantity);
                            }
                        }
                    }
                }
                for (Map.Entry<String, Integer> entry : productSalesMap.entrySet()) {
                    Log.d("BestSeller", "Tong so luong ban - San pham ID: " + entry.getKey() + ", Tong ban: " + entry.getValue());
                }
                if (productSalesMap.isEmpty()) {
                    Log.d("BestSeller", "Khong co du lieu ban hang, hien thi tat ca san pham");
                    getAllProduct();
                    return;
                }
                List<Map.Entry<String, Integer>> sortedProduct = new ArrayList<>(productSalesMap.entrySet());
                Collections.sort(sortedProduct, new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });

                List<String> bestSellingProductIds = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : sortedProduct) {
                    bestSellingProductIds.add(entry.getKey());
                    Log.d("BestSeller", "Sap xep theo ban chay - ID: " + entry.getKey() + ", So luong: " + entry.getValue());
                }
                dataProduct.clear();
                for (String productId : bestSellingProductIds) {
                    tblProduct.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Product product = snapshot.getValue(Product.class);
                            if (product != null) {
                                dataProduct.add(product);
                                Log.d("BestSeller", "Da them san pham: " + product.getNameProc() +
                                        ", So luong ban: " + productSalesMap.get(product.getIDProc()));
                                productApdater.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("BestSeller", "Loi khi tai san pham: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("BestSeller", "Loi khi tai du lieu don hang: " + error.getMessage());
                getAllProduct();
            }
        });
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
                productApdater.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Loi kho tai cac products: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Error fetching lines", error.toException());
            }
        });
    }

    /**
     * Lay tat ca cac danh muc.
     */
    private void getAllLine() {
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
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Loi kho tai cac lines: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Error fetching lines", error.toException());
            }
        });
    }
        @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("onStart", "User: " + cUser.getUid());
        genericFunction.getTableReference("User").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot usersnapshot: snapshot.getChildren()){
                    User user = usersnapshot.getValue(User.class);
                    if(user!=null && user.getGmail().equals(cUser.getEmail()) && user.getStatus().equals("FIRST_LOGIN_GOOGLE")){
                        String key = cUser.getUid();
                        Customer cModule = new Customer(key,"", cUser.getEmail(), "", "");
                        genericFunction.addData("Customer", key, cModule);
                        User nUser = new User(cUser.getEmail(), "Customer", "NOT_FIRST_LOGIN_GOOGLE");
                        genericFunction.getTableReference("User").child(key).setValue(nUser);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
