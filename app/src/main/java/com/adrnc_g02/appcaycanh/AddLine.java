package com.adrnc_g02.appcaycanh;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import Model.Line;

public class AddLine extends AppCompatActivity {
    Button button, button2;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private final List<Line> lineList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_line);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Anh xa
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);

        //test
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddLine.this, MainActivity.class);
                startActivity(intent);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addLine();
                Toast.makeText(AddLine.this, "Them thanh cong", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void addLine(){
        genLineList();
        database= FirebaseDatabase.getInstance();
        myRef = database.getReference("Line");
        for (Line line : lineList) {
            myRef.child(line.getIDLine()).setValue(line).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(AddLine.this, "Them thanh cong", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //Test thoi nhe
    private void genLineList(){
        Line line1 = new Line("1", "Line 1");
        Line line2 = new Line("2", "Line 2");
        Line line3 = new Line("3", "Line 3");
        Line line4 = new Line("4", "Line 4");
        lineList.add(line1);
        lineList.add(line2);
        lineList.add(line3);
        lineList.add(line4);
    }
}