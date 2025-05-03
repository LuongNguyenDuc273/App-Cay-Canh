package com.adrnc_g02.appcaycanh.Session;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.adrnc_g02.appcaycanh.Generic.GenericFunction;
import com.adrnc_g02.appcaycanh.Home.MainActivity;
import com.adrnc_g02.appcaycanh.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import Model.Address;
import Model.Customer;

public class Register extends AppCompatActivity {
    // UI elements
    private EditText edtemail, edtpassword, edtcfpassword;
    private ImageButton btnRegister;
    private TextView edtFullName, edtPhone, edtAddress, changeToLogIn, dateSet;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseUser cUser;

    // Helper class
    SessionControl session;
    private final GenericFunction genericFunction = new GenericFunction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Thiet lap giao dien co ban
        setupUI();

        // Anh xa
        initializeViews();

        // Thiet lap listeners
        setupListeners();
    }

    /**
     * Thiet lap giao dien co ban va xu ly insets cho he thong.
     */
    private void setupUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
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
        session = new SessionControl(this);
        edtFullName = findViewById(R.id.fullname_edt);
        edtemail = findViewById(R.id.register_edt);
        edtPhone = findViewById(R.id.phone_edt);
        edtAddress = findViewById(R.id.address_edt);
        edtpassword = findViewById(R.id.password_edt);
        edtcfpassword = findViewById(R.id.password_cf);
        btnRegister = findViewById(R.id.register_btn);
        changeToLogIn = findViewById(R.id.login_tv);
        dateSet = findViewById(R.id.birthday_tv);
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Thiet lap cac listeners.
     */
    private void setupListeners() {
        // Su kien chon ngay
        dateSet.setOnClickListener(view -> {
            initDatePicker();
        });

        // Su kien an dang ky
        btnRegister.setOnClickListener(view -> {
            intiRegister();
        });

        // Su kien chuyen sang dang nhap
        changeToLogIn.setOnClickListener(view -> {
            startActivity(new Intent(Register.this, Login.class));
        });
    }

    /**
     * Khoi tao ngay thang nam hien tai de hien thi.
     */
    private void initDatePicker() {
        // Lay ngay hien tai
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Tao DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                Register.this, (view1, year1, monthOfYear, dayOfMonth) -> {
            // Dinh dang ngay theo kieu dd/MM/yyyy
            String formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
            dateSet.setText(formattedDate);  // Gan ngay cho EditText
        }, year, month, day);

        // Hien thi DatePickerDialog
        datePickerDialog.show();
    }

    /**
     * Function dang ky.
     */
    private void intiRegister() {
        database = FirebaseDatabase.getInstance();
        DatabaseReference cusRef = database.getReference("Customer");
        DatabaseReference userRef = database.getReference("User");
        String email = edtemail.getText().toString().trim();
        String password = edtpassword.getText().toString().trim();
        String cfPassword = edtcfpassword.getText().toString().trim();
        String fullName = edtFullName.getText().toString();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString();
        String birthday = dateSet.getText().toString().trim();

        // Email validation
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(Register.this, "Hay dien email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(Register.this, "Email khong hop le", Toast.LENGTH_SHORT).show();
            return;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(Register.this, "Hay dien mat khau", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(Register.this, "Mat khau phai co it nhat 6 ky tu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Confirm Password validation
        if (TextUtils.isEmpty(cfPassword)) {
            Toast.makeText(Register.this, "Hay xac nhan mat khau", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(cfPassword)) {
            Toast.makeText(Register.this, "Mat khau khong khop", Toast.LENGTH_SHORT).show();
            return;
        }

        // Full Name validation
        if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(Register.this, "Hay dien ho ten", Toast.LENGTH_SHORT).show();
            return;
        }

        // Phone validation (add more robust checks as needed)
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(Register.this, "Hay dien so dien thoai", Toast.LENGTH_SHORT).show();
            return;
        }
        // Consider adding a check for valid phone number format using a regular expression.

        // Address validation (optional)
        // if (TextUtils.isEmpty(address)) { ... }

        // Birthday validation
        if (TextUtils.isEmpty(birthday) || birthday.equals("Chon ngay")) {
            Toast.makeText(Register.this, "Hay chon ngay sinh", Toast.LENGTH_SHORT).show();
            return;
        }

        // Address validation
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(Register.this, "Hay dien dia chi", Toast.LENGTH_SHORT).show();
            return;
        }
        // Luu vao authentication fire base
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cUser = mAuth.getCurrentUser();
                        //Luu vao realtime database
                        if (cUser != null) {
                            String key = cUser.getUid();
                            String idAddress = genericFunction.getTableReference("Customer").child(key).child("Address").push().getKey();
                            Customer cModule = new Customer(key, fullName, email, birthday, phone);

                            Address address1 = new Address(address, key);
                            session.saveUserToDatabase("LOGIN_EMAIL");
                            genericFunction.addData("Customer", key, cModule);
                            genericFunction.getTableReference("Customer").child(key).child("Address").child(key).setValue(address1);
                            startActivity(new Intent(Register.this, MainActivity.class));
                            Toast.makeText(Register.this, "Da tao tai khoan", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthException e) {
                            // Handle specific Firebase Authentication exceptions
                            if (e.getErrorCode().equals("ERROR_EMAIL_ALREADY_IN_USE")) {
                                Toast.makeText(Register.this, "Email da duoc su dung", Toast.LENGTH_SHORT).show();
                            } else if (e.getErrorCode().equals("ERROR_WEAK_PASSWORD")) {
                                Toast.makeText(Register.this, "Mat khau qua yeu", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Register.this, "Loi dang ky: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(Register.this, "Loi dang ky: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
