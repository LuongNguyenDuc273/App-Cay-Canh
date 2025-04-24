package com.adrnc_g02.appcaycanh;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    TextView nameUser,
            logout;
    EditText phone,
            gmail;

    private ImageView personalInfoArrow,
            addressArrow;

    private LinearLayout personalInfoHeader,
            personalInfoContent,
            addressHeader,
            addressContent;
    private String currentUserID = "";

    private boolean isPersonalInfoExpanded = false;
    private boolean isAddressExpanded = false;

    SessionControl session;

    private FirebaseDatabase database;
    private DatabaseReference customerRef,
            addressRef;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private GenericFunction<Customer> customerGenericFunction;
    private GenericFunction<Address> addressGenericFunction;

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

        // Anh xa TextView
        nameUser = findViewById(R.id.username);
        logout = findViewById(R.id.Logout);
        // Xu ly su kien cua TextView
        // DANG XUAT
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                session.signOutCompletely();
                Log.d(TAG, "Logout button clicked");
            }
        });

        // Anh xa EditText
        phone = findViewById(R.id.phone_number);
        gmail = findViewById(R.id.gmail_address);

        // Anh xa ImageView
        personalInfoArrow = findViewById(R.id.personal_info_arrow);
        addressArrow = findViewById(R.id.address_arrow);

        // Anh xa LinearLayout
        personalInfoHeader = findViewById(R.id.personal_info_header);
        personalInfoContent = findViewById(R.id.personal_info_content);
        addressHeader = findViewById(R.id.address_header);
        addressContent = findViewById(R.id.address_content);
        // Xu ly su kien cua LinearLayout
        personalInfoHeader.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isPersonalInfoExpanded = !isPersonalInfoExpanded;
                personalInfoContent.setVisibility(isPersonalInfoExpanded ? View.VISIBLE : View.GONE);
                rotateArrow(personalInfoArrow, isPersonalInfoExpanded);
                Log.d(TAG, "Personal Info Header clicked, isExpanded: " + isPersonalInfoExpanded);
            }
        });

        addressHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAddressExpanded = !isAddressExpanded;
                addressContent.setVisibility(isAddressExpanded ? View.VISIBLE : View.GONE);
                rotateArrow(addressArrow, isAddressExpanded);
                Log.d(TAG, "Address Header clicked, isExpanded: " + isAddressExpanded);
            }
        });

        // Khoi tao Firebase
        auth = FirebaseAuth.getInstance(); // xac thuc nguoi dung
        currentUser = auth.getCurrentUser(); // lay nguoi dung hien tai
        database = FirebaseDatabase.getInstance(); // truy cap csdl
        customerRef = database.getReference("Customer"); // tham chieu den node "Customer"
        session = new SessionControl(this);

        // Khoi tao GenericFunction (replace with your actual implementation)
        customerGenericFunction = new GenericFunction<Customer>(); // Sửa ở đây
        Log.d(TAG, "GenericFunction initialized: " + (customerGenericFunction != null));

        loadUserData();
    }

    private void loadUserData() {
        Log.d(TAG, "loadUserData called");
        if (currentUser == null) {
            nameUser.setText("Chua dang nhap");
            Log.d(TAG, "No current user, set text to 'Chua dang nhap'");
            return;
        }

        // Lay ten tam thoi
        String userName = getIntent().getStringExtra("a");
        String userName2 = getIntent().getStringExtra("b");
        String userGmail = currentUser.getEmail();

        Log.d(TAG, "userName from intent: " + userName);
        Log.d(TAG, "userName2 from intent: " + userName2);
        Log.d(TAG, "userGmail from currentUser: " + userGmail);

        if (userName != null) {
            nameUser.setText(userName);
            Log.d(TAG, "Set nameUser to userName");
        } else if (userName2 != null) {
            nameUser.setText(userName2);
            Log.d(TAG, "Set nameUser to userName2");
        } else {
            nameUser.setText(userGmail);
            Log.d(TAG, "Set nameUser to userGmail");
        }

        // Lay ten chinh thuc tu database tranh tinh huong ten tam thoi da la ten cu
        // Load du lieu lay tu firebase
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
                                        displayInfo(customer);
                                        currentUserID = customer.getIDCus();
                                        Log.d(TAG, "currentUserID set to: " + currentUserID);
                                        setupEditText();
                                    }
                                }
                            } else {
                                // neu trong database trong thi se lay du lieu tam
                                phone.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "");
                                gmail.setText(currentUser.getEmail());
                                Log.d(TAG, "No data found in database, using currentUser info");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Database error: " + error.getMessage());
                            Toast.makeText(Profile.this,
                                    "Loi tai thong tin",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Lay thong tin tu database va hien thi
    private void displayInfo(Customer customer) {
        Log.d(TAG, "displayInfo called with customer: " + customer);
        // Cap nhat ten
        if (customer.getNameCus() != null && !customer.getNameCus().isEmpty()) {
            nameUser.setText(customer.getNameCus());
            Log.d(TAG, "Set nameUser to customer.getNameCus()");
        } else {
            nameUser.setText(currentUser.getEmail());
            Log.d(TAG, "Set nameUser to currentUser.getEmail()");
        }

        // Cap nhat so dien thoai va gmail
        phone.setText(customer.getPhone() != null ? customer.getPhone() : "");
        gmail.setText(customer.getGmail() != null ? customer.getGmail() : currentUser.getEmail());

        Log.d(TAG, "Phone number set to: " + phone.getText().toString());
        Log.d(TAG, "Gmail set to: " + gmail.getText().toString());
    }

    // Xoay icon_arrow
    private void rotateArrow(ImageView arrow, boolean isExpanded) {
        float fromDegree = isExpanded ? 0f : 90f;
        // Vi tri muon xoay toi
        float toDegree = isExpanded ? 90f : 0f;

        RotateAnimation rotateAnimation = new RotateAnimation(
                fromDegree,
                toDegree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );

        rotateAnimation.setDuration(250);
        rotateAnimation.setFillAfter(true);
        arrow.startAnimation(rotateAnimation);
    }

    // Chinh sua du lieu cho EditText
    private void updateField(String tableName, String itemId, String fieldName, Object newValue) {
        Log.d(TAG, "updateField called with: tableName=" + tableName +
                ", itemId=" + itemId + ", fieldName=" + fieldName + ", newValue=" + newValue);

        if (itemId == null || itemId.isEmpty()) {
            Log.w(TAG, "Invalid itemId, update aborted"); // Log cảnh báo
            Toast.makeText(this, "Không thể cập nhật! ID không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Calling customerGenericFunction.updateData");
        customerGenericFunction.updateData(tableName, itemId, fieldName, newValue)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "updateData completed successfully");
                        Toast.makeText(Profile.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "updateData failed: " + (task.getException() != null ? task.getException().getMessage() : ""));
                        Toast.makeText(Profile.this, "Cập nhật thất bại: " +
                                        (task.getException() != null ? task.getException().getMessage() : ""),
                                Toast.LENGTH_SHORT).show();
                    }
                });
        // Hide keyboard and clear focus
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(phone.getWindowToken(), 0);
        phone.clearFocus();
        Log.d(TAG, "Keyboard hidden and focus cleared");

    }

    // Xu ly su kien cho EditText
    private void setupEditText() {
        Log.d(TAG, "setupEditText called");
        // Xu ly cho so dien thoai
        phone.setOnEditorActionListener((v, actionId, event) -> {
            Log.d(TAG, "setOnEditorActionListener called, actionId: " + actionId + ", event: " + event);
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String newPhoneNumber = phone.getText().toString();
                Log.d(TAG, "Enter key pressed, newPhoneNumber: " + newPhoneNumber);
                updateField("Customer", currentUserID, "phone", newPhoneNumber);
                Log.d("PhoneText", phone.getText().toString());
                return true;
            }
            return false;
        });
    }
}
