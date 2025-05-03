package com.adrnc_g02.appcaycanh.Session;

import android.content.Context;
import android.content.Intent;

import com.adrnc_g02.appcaycanh.Generic.GenericFunction;
import com.adrnc_g02.appcaycanh.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Model.User;

public class SessionControl {
    private final FirebaseAuth auth;
    private final GoogleSignInClient mGoogleSignInClient;
    private final Context context;
    private GenericFunction genericFunction;
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
    public static void saveUserToDatabase(String status) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = database.getReference("User");

            usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        User newUser = new User(email,"CUSTOMER", status);

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
