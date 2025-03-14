package com.adrnc_g02.appcaycanh;

import android.content.Intent;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {
    EditText edtemail, edtpassword, edtcfpassword;
    ImageButton btnRegister;
    TextView changeToLogIn;
    FirebaseAuth mAuth;

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
        edtemail = findViewById(R.id.register_edt);
        edtpassword = findViewById(R.id.password_edt);
        edtcfpassword = findViewById(R.id.password_cf);
        btnRegister = findViewById(R.id.register_btn);
        mAuth = FirebaseAuth.getInstance();

        //su kien an dang ky
        btnRegister.setOnClickListener(view -> {
            String email = edtemail.getText().toString().trim();
            String password = edtpassword.getText().toString().trim();
            String cfPassword = edtcfpassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Register.this, "Hãy điền email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Register.this, "Hãy điền mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(cfPassword)) {
                Toast.makeText(Register.this, "Hãy xác nhận mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(cfPassword)) {
                Toast.makeText(Register.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(Register.this, "Đã tạo tài khoản", Toast.LENGTH_SHORT).show();
                            // Optionally navigate to another activity after successful registration.
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
        });
    }
}
