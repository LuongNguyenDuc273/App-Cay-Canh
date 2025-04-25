package com.adrnc_g02.appcaycanh;

import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class Admin extends AppCompatActivity {
    private GenericFunction genericFunction = new GenericFunction();

    private TextView txtHello, username, quantityConfirm, quantityCancel, quantityReturn, quantityHolding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtHello = findViewById(R.id.txtGreeting);
        username = findViewById(R.id.userEmail);
        quantityCancel = findViewById(R.id.cancel);
        quantityConfirm = findViewById(R.id.confirm);
        quantityHolding = findViewById(R.id.hold);
        quantityReturn = findViewById(R.id.returnn);
        setUsername();
        updateQuantityStatus();

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour <= 10) {greeting = "Chào buổi sáng ☀️";}
        else if (hour >= 11 && hour <= 12) {greeting = "Chào buổi trưa 🍱";}
        else if (hour >= 13 && hour <= 17) {greeting = "Chào buổi chiều 🌤️";}
        else if (hour >= 18 && hour <= 21) {greeting = "Chào buổi tối 🌆";}
        else {greeting = "Khuya rồi 😴";}
        txtHello.setText(greeting);

    }

    private void setUsername() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userName = getIntent().getStringExtra("userEmail");
            String userName2 = getIntent().getStringExtra("userEmail2");
            if (userName != null) {
                username.setText(userName);
            } else if (userName2 != null) {
                username.setText(userName2);
            } else {
                username.setText(currentUser.getEmail());
            }
        } else {
            username.setText("Chua Dang Nhap");
        }
    }

    private void updateQuantityStatus() {
        final int[] confirm = {0};
        final int[] cancel = {0};
        final int[] hold = {0};
        final int[] back = {0};
        DatabaseReference order = genericFunction.getTableReference("Order");
        order.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                confirm[0] = 0;
                cancel[0] = 0;
                hold[0] = 0;
                back[0] = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String Status = dataSnapshot.child("status").getValue(String.class);
                    if (Status.equals("PENDING_PICKUP")) {
                        hold[0] += 1;
                    } else if (Status.equals("PENDING_CONFIRMATION")) {
                        confirm[0] += 1;
                    } else if (Status.equals("COMPLETED")) {
                        back[0] += 1;
                    } else if (Status.equals("CANCELLED")) {
                        cancel[0] += 1;
                    }
                }
                updateStatusUI(confirm[0], cancel[0], hold[0], back[0]);
                Log.e("Quantity", "confirm: " + confirm[0] + ", cancel: " + cancel[0] + ", hold: " + hold[0] + ", back: " + back[0]);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DatabaseError", "Không thể đọc đơn hàng", error.toException());
            }

        });
    }

    private void updateStatusUI(int confirmCount, int cancelCount, int holdCount, int backCount) {
        quantityCancel.setText(String.valueOf(cancelCount));
        quantityReturn.setText(String.valueOf(backCount));
        quantityHolding.setText(String.valueOf(holdCount));
        quantityConfirm.setText(String.valueOf(confirmCount));
    }

}