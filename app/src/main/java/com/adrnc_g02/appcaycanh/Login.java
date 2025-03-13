package com.adrnc_g02.appcaycanh;

import android.content.Intent;
import android.credentials.GetCredentialRequest;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    private Button loginggbtn;
    private ImageButton btnLogin;
    private TextView toRegister;
    private EditText email_tv,pass_tv;
    private GoogleSignInClient client;
    private static final int RC_SIGN_IN = 1234;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://appcaycanh-default-rtdb.firebaseio.com/");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Anh xa
        btnLogin = findViewById(R.id.login_btn);
        email_tv = findViewById(R.id.login_edt);
        pass_tv = findViewById(R.id.password_edt);
        loginggbtn = findViewById(R.id.logingg_btn);
        toRegister = findViewById(R.id.signin_tv);
        toRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });
        //Khoi tao gg sign in
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(this, options); // Initialize client HERE

        //su kien dang nhap gg
        loginggbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        //su kien dang nhap bang email va pass
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, pass;
                email = email_tv.getText().toString();
                pass = pass_tv.getText().toString();
                if(email.isEmpty() || pass.isEmpty())
                {
                    Toast.makeText(Login.this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_LONG).show();
                } else {
                    databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean userFound = false;
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                // Lấy email từ mỗi tài khoản người dùng
                                String userEmail = userSnapshot.child("Email").getValue(String.class);

                                // Kiểm tra xem email nhập vào có khớp không
                                if (userEmail != null && userEmail.equals(email)) {
                                    userFound = true;
                                    String userId = userSnapshot.getKey();

                                    // Kiểm tra mật khẩu
                                    String password = userSnapshot.child("password").getValue(String.class);
                                    if (password != null && password.equals(pass)) {
                                        Toast.makeText(Login.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                                        // Lưu userId vào SharedPreferences hoặc nơi khác nếu cần
                                        // SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                        // preferences.edit().putString("userId", userId).apply();

                                        startActivity(new Intent(Login.this, MainActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(Login.this, "Mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                }
                            }

                            if (!userFound) {
                                Toast.makeText(Login.this, "Email không tồn tại. Vui lòng nhập lại!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Login.this, "Đã xảy ra lỗi khi đăng nhập", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void signIn() {
        Intent signInIntent = client.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_SHORT);
                                }
                            }
                        });
            } catch (ApiException e) {
                Toast.makeText(Login.this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
    // Hàm sanitize email để loại bỏ các ký tự không hợp lệ
    private String sanitizeEmail(String email) {
        return email.replace(".", "_").replace("@", "_");
    }
}