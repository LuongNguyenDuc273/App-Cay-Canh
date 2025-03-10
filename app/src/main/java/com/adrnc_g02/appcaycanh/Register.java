package com.adrnc_g02.appcaycanh;

import android.content.Intent;
import android.graphics.Paint;
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

public class Register extends AppCompatActivity {
    EditText Email, password, cfpassword,userName,Phone;
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
            Email = findViewById(R.id.register_edt);
            mAuth = FirebaseAuth.getInstance();
            password = findViewById(R.id.password_edt);
            cfpassword = findViewById(R.id.password_cf);
            btnRegister = findViewById(R.id.register_btn);
            changeToLogIn = findViewById(R.id.login);
            changeToLogIn.setPaintFlags(changeToLogIn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            changeToLogIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newIntent = new Intent(Register.this, Login.class);
                    startActivity(newIntent);
                }
            });
            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email,pass,cf_pass;
                    email = String.valueOf(Email.getText());
                    pass = String.valueOf(password.getText());
                    cf_pass = String.valueOf(cfpassword.getText());

                    if(TextUtils.isEmpty(email)){
                        Toast.makeText(Register.this,"Enter Your Email",Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(TextUtils.isEmpty(pass)){
                        Toast.makeText(Register.this,"Enter Your Password",Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(TextUtils.isEmpty(cf_pass)|| !pass.equals(cf_pass)){
                        Toast.makeText(Register.this,"Your Confirm Password is not correct, Please try again! ",Toast.LENGTH_LONG).show();
                        return;
                    }
                    mAuth.createUserWithEmailAndPassword(email,pass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(Register.this,"Account Created",Toast.LENGTH_LONG).show();
                                        Intent logIntent = new Intent(getApplicationContext(), Login.class);
                                        startActivity(logIntent);
                                        finish();
                                    }
                                    else
                                    {
                                        Toast.makeText(Register.this,"Authentication Failure",Toast.LENGTH_LONG).show();
                                    }

                                }
                            });
                }
            });
            return insets;
        });
    }
}