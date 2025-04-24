package com.adrnc_g02.appcaycanh;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import Model.Customer;

public class Register extends AppCompatActivity {
    private EditText edtemail, edtpassword, edtcfpassword;
    private ImageButton btnRegister;
    private TextView edtFullName, edtPhone, edtAddress, changeToLogIn, dateSet;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    SessionControl session;
    private final GenericFunction genericFunction = new GenericFunction();
    private FirebaseUser cUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Anh xa
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

        //Su kien chon ngay
        dateSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initDatePicker();
            }
        });

        //su kien an dang ky
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intiRegister();
            }
        });

        //su kien chuyen sang dang nhap
        changeToLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });
    }

    //Khoi tao ngay thang nam hien tai de hien thi
    private void initDatePicker(){
        // Lấy ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Tạo DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                Register.this, (view1, year1, monthOfYear, dayOfMonth) -> {
            // Định dạng ngày theo kiểu dd/MM/yyyy
            String formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
            dateSet.setText(formattedDate);  // Gán ngày cho EditText
        }, year, month, day);

        // Hiển thị DatePickerDialog
        datePickerDialog.show();
    }

    //function dang ky
    private void intiRegister(){
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
            Toast.makeText(Register.this, "Hãy điền email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(Register.this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(Register.this, "Hãy điền mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(Register.this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        // Confirm Password validation
        if (TextUtils.isEmpty(cfPassword)) {
            Toast.makeText(Register.this, "Hãy xác nhận mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(cfPassword)) {
            Toast.makeText(Register.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Full Name validation
        if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(Register.this, "Hãy điền họ tên", Toast.LENGTH_SHORT).show();
            return;
        }

        // Phone validation (add more robust checks as needed)
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(Register.this, "Hãy điền số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }
        // Consider adding a check for valid phone number format using a regular expression.

        // Address validation (optional)
        // if (TextUtils.isEmpty(address)) { ... }

        // Birthday validation
        if (TextUtils.isEmpty(birthday)|| birthday.equals("Chọn ngày")) {
            Toast.makeText(Register.this, "Hãy chọn ngày sinh", Toast.LENGTH_SHORT).show();
            return;
        }

        // Address validation
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(Register.this, "Hãy điền địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }
        // Luu vao authentication fire base
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cUser = mAuth.getCurrentUser();
                        //Luu vao realtime database
                        if(cUser!=null)
                        {
                            String key = cUser.getUid();
                            Customer cModule = new Customer(key, fullName, email, birthday, phone);
                            session.saveUserToDatabase("LOGIN_EMAIL");
                            genericFunction.addData("Customer", key, cModule);
                            startActivity(new Intent(Register.this, MainActivity.class));
                            Toast.makeText(Register.this, "Đã tạo tài khoản", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthException e) {
                            // Handle specific Firebase Authentication exceptions
                            if (e.getErrorCode().equals("ERROR_EMAIL_ALREADY_IN_USE")) {
                                Toast.makeText(Register.this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show();
                            } else if (e.getErrorCode().equals("ERROR_WEAK_PASSWORD")) {
                                Toast.makeText(Register.this, "Mật khẩu quá yếu", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Register.this, "Lỗi đăng ký: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(Register.this, "Lỗi đăng ký: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
