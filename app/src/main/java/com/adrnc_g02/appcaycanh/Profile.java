package com.adrnc_g02.appcaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import Model.Customer;
import Model.Address;
import Model.User;
import Model.Employee;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;
import java.util.ArrayList;

public class Profile extends AppCompatActivity {
    TextView Name, logout, phoneNumber, emailAddress;
    SessionControl session;
    private LinearLayout personalInfoHeader, waitingConfirmationLayout;;
    private BottomNavigationView bottomNavigationView;

    private LinearLayout personalInfoContent;
    private ImageView personalInfoArrow;

    private LinearLayout addressHeader;
    private LinearLayout addressContent;
    private ImageView addressArrow;

    private boolean isPersonalInfoExpanded = false;
    private boolean isAddressExpanded = false;

    private FirebaseDatabase database;
    private DatabaseReference customerRef, addressRef;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        customerRef = database.getReference("Customer");
        addressRef = database.getReference("Address");

        // khởi tạo views
        Name = findViewById(R.id.username);
        logout = findViewById(R.id.Logout);
        phoneNumber = findViewById(R.id.phone_number);
        emailAddress = findViewById(R.id.email_address);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.navProfile);
        waitingConfirmationLayout = findViewById(R.id.waiting_confirmation_layout);

        // Su kien chuyen sang cho xac nhan
        waitingConfirmationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profile.this, WaitingConfirmation.class));
            }
        });

        //Chuyen trang
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navHome){
                startActivity(new Intent(Profile.this, MainActivity.class));
            }
            else if (itemId == R.id.navCart) {
            }
            else if (itemId == R.id.navExplore) {
                startActivity(new Intent(Profile.this, ShoppingCart.class));
            }
            else if (itemId == R.id.navProfile) {
                return true;
            }
            return true;

        });

        //setUsername();
        session = new SessionControl(this);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                session.signOutCompletely();
            }
        });

        // Khởi tạo các view
        personalInfoHeader = findViewById(R.id.personal_info_header);
        personalInfoContent = findViewById(R.id.personal_info_content);
        personalInfoArrow = findViewById(R.id.personal_info_arrow);

        addressHeader = findViewById(R.id.address_header);
        addressContent = findViewById(R.id.address_content);
        addressArrow = findViewById(R.id.address_arrow);

        // Thiết lập sự kiện click cho Thông tin cá nhân
        personalInfoHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPersonalInfoExpanded = !isPersonalInfoExpanded;
                personalInfoContent.setVisibility(isPersonalInfoExpanded ? View.VISIBLE : View.GONE);
                rotateArrow(personalInfoArrow, isPersonalInfoExpanded);
            }
        });

        // Thiết lập sự kiện click cho Địa chỉ
        addressHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAddressExpanded = !isAddressExpanded;
                addressContent.setVisibility(isAddressExpanded ? View.VISIBLE : View.GONE);
                rotateArrow(addressArrow, isAddressExpanded);
            }
        });

        // Load user data
        loadUserData();
    }

    private void loadUserData() {
        if (currentUser == null) {
            Name.setText("Chưa đăng nhập");
            return;
        }

        // Try to get username from intent first
        String userName = getIntent().getStringExtra("a");
        String userName2 = getIntent().getStringExtra("b");

        if (userName != null) {
            Name.setText(userName);
        } else if (userName2 != null) {
            Name.setText(userName2);
        } else {
            // Fallback to email if no name provided
            Name.setText(currentUser.getEmail());
        }

        // Load customer data from Firebase
        String userEmail = currentUser.getEmail();
        if (userEmail != null) {
            // Query customer data by email
            customerRef.orderByChild("Gmail").equalTo(userEmail)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    Customer customer = snapshot.getValue(Customer.class);
                                    if (customer != null) {
                                        updateCustomerUI(customer);
                                        loadAddressData(customer.getIDCus());
                                    }
                                }
                            } else {
                                // If no customer data found, use Firebase user info
                                phoneNumber.setText(currentUser.getPhoneNumber() != null ?
                                        currentUser.getPhoneNumber() : "Chưa cập nhật");
                                emailAddress.setText(currentUser.getEmail());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("Profile", "Error loading customer data", databaseError.toException());
                            Toast.makeText(Profile.this, "Lỗi tải thông tin khách hàng", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private void loadAddressData(String customerId) {
        addressRef.orderByChild("IDCus").equalTo(customerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Address address = snapshot.getValue(Address.class);
                                if (address != null) {
                                    updateAddressUI(address);
                                }
                            }
                        } else {
                            // Handle case when no address exists
                            TextView addressView = findViewById(R.id.address_1); // You need to add this TextView in your XML
                            if (addressView != null) {
                                addressView.setText("Chưa có địa chỉ");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Profile", "Error loading address data", databaseError.toException());
                    }
                });
    }

    private void updateCustomerUI(Customer customer) {
        // Update personal info
        if (customer.getNameCus() != null && !customer.getNameCus().isEmpty()) {
            Name.setText(customer.getNameCus());
        }

        phoneNumber.setText(customer.getPhone() != null ? customer.getPhone() : "Chưa cập nhật");
        emailAddress.setText(customer.getGmail() != null ? customer.getGmail() : "Chưa cập nhật");

        // You can add more fields here as needed
    }

    private void updateAddressUI(Address address) {
        // Find or create a TextView for address in your address_content layout
        TextView addressView = findViewById(R.id.address_1);
        if (addressView != null) {
            addressView.setText(address.getAddressLoc() != null ?
                    address.getAddressLoc() : "Chưa cập nhật địa chỉ");
        }
    }

    // Hàm xoay mũi tên
    private void rotateArrow(ImageView arrow, boolean isExpanded) {
        float fromDegrees = isExpanded ? 0f : 90f;
        float toDegrees = isExpanded ? 90f : 0f;

        RotateAnimation rotateAnimation = new RotateAnimation(
                fromDegrees,
                toDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );

        rotateAnimation.setDuration(300);
        rotateAnimation.setFillAfter(true);
        arrow.startAnimation(rotateAnimation);
    }

//    private void setUsername() {
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser != null) {
//            String userName = getIntent().getStringExtra("a");
//            String userName2 = getIntent().getStringExtra("b");
//            if (userName != null) {
//                Name.setText(userName);
//            } else if (userName2 != null) {
//                Name.setText(userName2);
//            }
//            else {
//                Name.setText(currentUser.getEmail());
//            }
//        }
//        else {
//            Name.setText("Chua Dang Nhap");
//        }
//    }
}