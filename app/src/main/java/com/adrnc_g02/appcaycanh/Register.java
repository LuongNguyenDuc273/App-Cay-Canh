package com.adrnc_g02.appcaycanh;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Register extends AppCompatActivity {
    EditText email, password, cfpassword;
    ImageButton btnRegister;
    TextView changeToLogIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            email = findViewById(R.id.register_edt);
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

            return insets;
        });
    }
}