package com.adrnc_g02.appcaycanh;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import Model.MonthlyRevenue;

public class Admin extends AppCompatActivity {
    private GenericFunction genericFunction = new GenericFunction();
    private BarChart barChart;
    private ArrayList<BarEntry> revenueList;
    private LinearLayout confirmProducts, cancelProducts, successProducts, holdProducts;
    private final String[] monthNames = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
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

        initializeViews();
        setUsername();
        updateQuantityStatus();
        setupBarchar();
        loadRevenue();

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
    private void initializeViews() {
        barChart = findViewById(R.id.bar_chart);
        txtHello = findViewById(R.id.txtGreeting);
        username = findViewById(R.id.userEmail);
        quantityCancel = findViewById(R.id.cancel);
        quantityConfirm = findViewById(R.id.confirm);
        quantityHolding = findViewById(R.id.hold);
        quantityReturn = findViewById(R.id.returnn);
        holdProducts = findViewById(R.id.btnHolding);
        successProducts = findViewById(R.id.btnSuccess);
        confirmProducts = findViewById(R.id.btnConfirm);
        cancelProducts = findViewById(R.id.btnCancel);
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
    private void loadRevenue(){
        revenueList = new ArrayList<>();
        ArrayList<MonthlyRevenue> monthlyRevenueList = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        DatabaseReference orderRef = genericFunction.getTableReference("Order");
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float[] monthlyRevenue = new float[12];
                for(DataSnapshot order: snapshot.getChildren()){
                   String status = order.child("status").getValue(String.class);
                   if(status!=null && status.equals("COMPLETED")){
                       Object totalPaymentObj  = order.child("totalPayment").getValue();
                       if(totalPaymentObj !=null){
                           float totalPayment = Float.parseFloat(totalPaymentObj.toString());
                           Object timeObj = order.child("time").getValue();
                           if(timeObj!=null){
                               try {
                                   long timestamp;
                                   if (timeObj instanceof Long) {
                                       timestamp = (Long) timeObj;
                                   } else {
                                       String timeStr = timeObj.toString();
                                       SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                       Date date = format.parse(timeStr);
                                       timestamp = date != null ? date.getTime() : 0;
                                   }
                                   Calendar calendar = Calendar.getInstance();
                                   calendar.setTimeInMillis(timestamp);
                                   int orderYear = calendar.get(Calendar.YEAR);
                                   int orderMonth = calendar.get(Calendar.MONTH);
                                   if (orderYear == currentYear) {
                                       monthlyRevenue[orderMonth] += totalPayment;
                                   }
                               } catch (ParseException e) {
                                   Log.e("DateParseError", "Không thể chuyển đổi ngày: " + timeObj.toString(), e);
                               }
                           }
                       }
                   }
                }
                monthlyRevenueList.clear();
                revenueList.clear();
                for (int i=0; i<12;i++){
                    monthlyRevenueList.add(new MonthlyRevenue(monthNames[i], monthlyRevenue[i]));
                    revenueList.add(new BarEntry(i, monthlyRevenue[i]));
                }
                updateBarchart();
                Log.d("Revenue", "Doanh thu đã được cập nhật");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RevenueError", "Không thể đọc dữ liệu doanh thu", error.toException());

            }
        });

    }
    private void setupBarchar(){
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getDescription().setEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);
        barChart.setScaleEnabled(false);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setTextSize(10f);
        xAxis.setDrawLabels(true);
        xAxis.setLabelCount(12, false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int monthIndex = (int) value;
                if (monthIndex >= 0 && monthIndex < 12) {
                    return monthNames[monthIndex];
                }
                return "";
            }
        });

        // Setup Y axis
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);

        // Other chart settings
        barChart.getLegend().setEnabled(true);
        barChart.animateY(1000);
    }

    private void updateBarchart() {
        if(revenueList == null ||revenueList.isEmpty()){
            Log.e("BarChart", "Không có dữ liệu doanh thu để hiển thị");
            return;
        }
        BarDataSet barDataSet = new BarDataSet(revenueList, "Doanh Thu (VND)");
        barDataSet.setColor(getResources().getColor(R.color.green));
        barDataSet.setValueTextColor(getResources().getColor(R.color.black));
        barDataSet.setValueTextSize(10f);
        barDataSet.setHighlightEnabled(true);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.7f);

        barData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if(value == 0) return  "";
                if(value > 1000000){
                    return String.format("%.1fM", value / 1000000);
                } else if (value >= 1000) {
                    return String.format("%.0fK", value / 1000);
                }
                return String.format("%.0f", value);
            }
        });
        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.highlightValues(null);
        barChart.notifyDataSetChanged();
        barChart.invalidate();
    }

}