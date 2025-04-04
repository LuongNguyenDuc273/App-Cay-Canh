package com.adrnc_g02.appcaycanh;

import static android.provider.Settings.System.getString;
import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Model.User;

public class SessionControl {
    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;
    private Context context;
    public SessionControl(Context context) {
        this.context = context; // Initialize the context
        // Configure Google Sign In
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, options); // Initialize client HERE
        auth = FirebaseAuth.getInstance(); // Initialize Firebase Auth
    }
    public static void saveUserToDatabase(FirebaseUser user) {
        if (user != null) {
            String email = user.getEmail();

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = database.getReference("User");

            usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        User newUser = new User(email, "", "", "");

                        usersRef.child(user.getUid()).setValue(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    System.out.println("Thông tin người dùng đã được lưu vào database.");
                                })
                                .addOnFailureListener(e -> {
                                    System.out.println("Lỗi khi lưu thông tin người dùng vào database: " + e.getMessage());
                                });
                    } else {
                        System.out.println("Thông tin người dùng đã tồn tại trong database.");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("Lỗi khi đọc dữ liệu từ database: " + databaseError.getMessage());
                }
            });
        }
    }

    public void signOutCompletely() { // Get context
        auth.signOut();
        mGoogleSignInClient.signOut();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(context, Login.class); // Replace LoginActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
