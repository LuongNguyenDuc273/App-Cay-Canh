package com.adrnc_g02.appcaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    TextView user;
    Button btn;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    FirebaseUser cUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            mAuth = FirebaseAuth.getInstance();
//            user = findViewById(R.id.user);
//            btn = findViewById(R.id.logout);
            cUser = mAuth.getCurrentUser();
            recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ProductAdapter();
            recyclerView.setAdapter(adapter);
            if(user == null)
            {
                Intent intn = new Intent(getApplicationContext(), Login.class);
                startActivity(intn);
                finish();
            }
            else {
                user.setText(cUser.getEmail());
            }
//            btn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    FirebaseAuth.getInstance().signOut();
//                    Intent intn = new Intent(getApplicationContext(), Login.class);
//                    startActivity(intn);
//                    finish();
//                }
//            });
            return insets;

        });
    }
}