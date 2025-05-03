package com.adrnc_g02.appcaycanh.Session;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adrnc_g02.appcaycanh.Home.MainActivity;
import com.adrnc_g02.appcaycanh.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 1234; // Request code cho Google Sign-In
    private Button loginggbtn; // Button dang nhap bang Google
    private TextView register; // TextView chuyen sang trang dang ky
    private EditText edtemail, edtpassword; // EditText nhap email va password
    private GoogleSignInClient client; // Google Sign-In client
    private ImageButton loginbtn; // Button dang nhap bang email
    private FirebaseAuth mAuth; // Firebase Authentication
    private SessionControl session; // Session control
    private DatabaseReference userRef;
    private AccessControl accessControl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khoi tao cac view
        initializeViews();
        session = new SessionControl(this);
        accessControl = new AccessControl(this);

        // Khoi tao Google Sign-In
        initializeGoogleSignIn();
        userRef = FirebaseDatabase.getInstance().getReference("Customer");


        // Thiet lap listeners
        setupListeners();
    }

    /**
     * Khoi tao cac view
     */
    private void initializeViews() {
        loginggbtn = findViewById(R.id.logingg_btn);
        register = findViewById(R.id.register_tv);
        edtemail = findViewById(R.id.login_edt);
        edtpassword = findViewById(R.id.password_edt);
        loginbtn = findViewById(R.id.login_btn);
    }

    /**
     * Khoi tao Google Sign-In
     */
    private void initializeGoogleSignIn() {
        session = new SessionControl(this);
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(this, options);
    }

    /**
     * Thiet lap listeners cho cac button va text view
     */
    private void setupListeners() {
        loginggbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIngg();
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Dang nhap bang email va password
     */
    private void signIn() {
        mAuth = FirebaseAuth.getInstance();
        String email = edtemail.getText().toString().trim();
        String password = edtpassword.getText().toString().trim();

        // Kiem tra email va password
        if (!validateEmailPassword(email, password)) {
            return;
        }

        // Dang nhap voi Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signIn:success");
                            Toast.makeText(Login.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "signIn: User email: " + user.getEmail());
                                checkUserRole(user.getEmail());
                            } else {
                                accessControl.redirectToHomePage(Login.this);
                            }
                        } else {
                            Log.w(TAG, "signIn:failure", task.getException());
                            Toast.makeText(Login.this, "Lỗi đăng nhập: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkUserRole(String email) {
        userRef.orderByChild("gmail").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String role = userSnapshot.child("role").getValue(String.class);
                        Log.d(TAG, "onDataChange: User role: " + role);
                        if (role == null) {
                            role = AccessControl.ROLE_USER;
                        }
                        accessControl.saveUserSession(email, role);
                        accessControl.redirectToHomePage(Login.this);
                        return;
                    }
                }
                accessControl.saveUserSession(email, AccessControl.ROLE_USER);
                accessControl.redirectToHomePage(Login.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Login.this, "Lỗi kết nối database: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Kiem tra email va password
     * @param email Email
     * @param password Password
     * @return True neu hop le, false neu khong
     */
    private boolean validateEmailPassword(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(Login.this, "Hay dien email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(Login.this, "Email khong hop le", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(Login.this, "Hay dien mat khau", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(Login.this, "Mat khau phai co it nhat 6 ky tu", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Dang nhap bang Google
     */
    private void signIngg() {
        Intent signInIntent = client.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Ket qua tra ve tu viec mo Intent tu Google Sign-In
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Luu thong tin nguoi dung tu Google Sign-In
                                    session.saveUserToDatabase("FIRST_LOGIN_GOOGLE");
                                    Intent intent = new Intent(Login.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
        // Kiem tra xem nguoi dung da dang nhap hay chua
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}
