package com.adrnc_g02.appcaycanh;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.net.ParseException;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Register extends AppCompatActivity {

    //tao mot doi tuong database de truy cap vao firebase database real time
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://appcaycanh-default-rtdb.firebaseio.com/");
    EditText Email, password, cfpassword, userName, Phone, address, dateOfBirth,userID;
    ImageButton btnRegister;
    TextView changeToLogIn;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            Intent mainIntent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        userID = findViewById(R.id.userId);
        Email = findViewById(R.id.register_edt);
        mAuth = FirebaseAuth.getInstance();
        password = findViewById(R.id.password_edt);
        cfpassword = findViewById(R.id.password_cf);
        btnRegister = findViewById(R.id.register_btn);
        changeToLogIn = findViewById(R.id.login);
        userName = findViewById(R.id.fullname_edt);
        Phone = findViewById(R.id.phone_edt);
        address = findViewById(R.id.address_edt);
        dateOfBirth = findViewById(R.id.date_edt);
        changeToLogIn.setPaintFlags(changeToLogIn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        changeToLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(Register.this, Login.class);
                startActivity(newIntent);
            }
        });
        dateOfBirth.setOnClickListener(v -> {
            // Lấy ngày hiện tại
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Tạo DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this, (view, year1, monthOfYear, dayOfMonth) -> {
                // Định dạng ngày theo kiểu dd/MM/yyyy
                String formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                dateOfBirth.setText(formattedDate);  // Gán ngày cho EditText
            }, year, month, day);

            // Hiển thị DatePickerDialog
            datePickerDialog.show();
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, pass, cf_pass, name, phone, add, dateStr;
                final Date[] date = new Date[1];
                email = String.valueOf(Email.getText());
                pass = String.valueOf(password.getText());
                cf_pass = String.valueOf(cfpassword.getText());
                name = String.valueOf(userName.getText());
                phone = String.valueOf(Phone.getText());
                add = String.valueOf(address.getText());
                dateStr = String.valueOf(dateOfBirth.getText());

                if (email.isEmpty() || name.isEmpty() || phone.isEmpty() || add.isEmpty() || dateStr.isEmpty() || pass.isEmpty() || cf_pass.isEmpty()) {
                    Toast.makeText(Register.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(cf_pass) || !pass.equals(cf_pass)) {
                    Toast.makeText(Register.this, "Mật khẩu bạn điền không giống nhau, vui lòng điền lại", Toast.LENGTH_LONG).show();
                    return;
                } else {

                    // Kiểm tra và chuyển đổi định dạng ngày sinh từ chuỗi sang đối tượng Date
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    try {
                        date[0] = dateFormat.parse(dateStr); // Chuyển đổi chuỗi ngày sinh thành đối tượng Date
                    } catch (ParseException | java.text.ParseException e) {
                        // Nếu không thể chuyển đổi ngày sinh, thông báo lỗi cho người dùng
                        Toast.makeText(Register.this, "Định dạng ngày sinh không hợp lệ. Vui lòng nhập lại theo định dạng dd/MM/yyyy.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Tìm ID lớn nhất hiện tại và tạo ID mới
                    databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // Tìm ID lớn nhất hiện tại
                            int maxNumber = 0;
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String userId = userSnapshot.getKey();
                                if (userId != null && userId.startsWith("C")) {
                                    try {
                                        int number = Integer.parseInt(userId.substring(1));
                                        if (number > maxNumber) {
                                            maxNumber = number;
                                        }
                                    } catch (NumberFormatException e) {
                                        // Bỏ qua các ID không phải dạng số
                                    }
                                }
                            }

                            // Tạo ID mới dạng Cxxx
                            int newNumber = maxNumber + 1;
                            String newUserId = String.format("C%03d", newNumber); // Định dạng với 3 chữ số, thêm 0 ở đầu nếu cần

                            // Lưu người dùng mới với ID vừa tạo
                            databaseReference.child("users").child(newUserId).child("fullName").setValue(name);
                            databaseReference.child("users").child(newUserId).child("Email").setValue(email);
                            databaseReference.child("users").child(newUserId).child("phoneNumber").setValue(phone);
                            databaseReference.child("users").child(newUserId).child("dateOfBirth").setValue(date[0].getTime());
                            databaseReference.child("users").child(newUserId).child("address").setValue(add);
                            databaseReference.child("users").child(newUserId).child("password").setValue(pass);

                            Toast.makeText(Register.this, "Đăng ký thành công! ID của bạn là: " + newUserId, Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Register.this, "Đã xảy ra lỗi khi đăng ký", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }
    private String sanitizeEmail(String email) {
        return email.replace(".", "_").replace("@", "_");
    }
}