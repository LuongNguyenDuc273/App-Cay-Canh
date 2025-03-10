package com.adrnc_g02.appcaycanh;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

public class Login extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    TextView changeToRegister;
    EditText dangnhap_edt,password_edt;
    ImageButton btnLogin;

    @Override
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
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            changeToRegister = findViewById(R.id.register);
            dangnhap_edt = findViewById(R.id.dangnhap_edt);
            password_edt = findViewById(R.id.password_edt);
            btnLogin = findViewById(R.id.dangnhap_btn);
            mAuth = FirebaseAuth.getInstance();
            changeToRegister.setPaintFlags(changeToRegister.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            changeToRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent rintent = new Intent(Login.this, Register.class);
                    startActivity(rintent);
                }
            });
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email,pass,cf_pass;
                    email = String.valueOf(dangnhap_edt.getText());
                    pass = String.valueOf(password_edt.getText());

                    if(TextUtils.isEmpty(email)){
                        Toast.makeText(Login.this,"Enter Your Email",Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(TextUtils.isEmpty(pass)){
                        Toast.makeText(Login.this,"Enter Your Password",Toast.LENGTH_LONG).show();
                        return;
                    }
                    mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(Login.this,"Log In Successful",Toast.LENGTH_LONG).show();
                                Intent mainIntent = new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(mainIntent);
                                finish();
                            }else{
                                Toast.makeText(Login.this,"Email or Password is incorrect. Please try again!",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            });
            return insets;

        });
    }

}