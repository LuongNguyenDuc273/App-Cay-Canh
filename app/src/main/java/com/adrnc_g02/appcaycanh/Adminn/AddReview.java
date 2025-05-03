package com.adrnc_g02.appcaycanh.Adminn;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adrnc_g02.appcaycanh.Generic.GenericFunction;
import com.adrnc_g02.appcaycanh.Order.OrderAdapter;
import com.adrnc_g02.appcaycanh.Order.OrderDetailAdapter;
import com.adrnc_g02.appcaycanh.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Model.Order;
import Model.OrderDetail;
import Model.Product;
import Model.Review;

public class AddReview extends AppCompatActivity {
    private static final String TAG = "ReviewActivity";

    // Views
    private RatingBar ratingBar;
    private EditText comment;
    private Button btnSubmit;
    private RecyclerView orderItemsRecyclerView;
    private Toolbar toolbar;

    // Adapters
    private OrderDetailAdapter orderDetailAdapter;

    // Firebase
    private DatabaseReference reviewsRef;
    private DatabaseReference ordersRef;
    private DatabaseReference orderDetailsRef;
    private String currentUserID;
    private String orderID;

    // Data
    private Order currentOrder;
    private List<Product> productList = new ArrayList<>();
    private List<OrderDetail> orderDetails = new ArrayList<>();
    private GenericFunction genericFunction = new GenericFunction<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addreview);

        // Khởi tạo và ánh xạ view
        initializeViews();

        // Nhận dữ liệu từ Intent
        getIntentData();

        // Thiết lập toolbar
        setupToolbar();

        // Khởi tạo Firebase
        initializeFirebase();

        // Tải dữ liệu sản phẩm
        loadProducts();

        // Thiết lập sự kiện nút Submit
        setupSubmitButton();
    }

    private void initializeViews() {
        // Views
        ratingBar = findViewById(R.id.ratingBar);
        comment = findViewById(R.id.comment);
        btnSubmit = findViewById(R.id.btn_submit_review);
        toolbar = findViewById(R.id.toolbar);

        // RecyclerView
        orderItemsRecyclerView = findViewById(R.id.orderDetailItems);
        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderDetailAdapter = new OrderDetailAdapter(AddReview.this, new ArrayList<>());
        orderItemsRecyclerView.setAdapter(orderDetailAdapter);
    }

    private void getIntentData(){
        orderID = getIntent().getStringExtra("orderID");
        Log.d("getIntentData", "orderID: "+ orderID);

        // Gọi hàm để lấy currentOrder từ Firebase bằng orderID
        getCurrentOrderFromFirebase(orderID);
    }

    private void getCurrentOrderFromFirebase(String orderID) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Order");

        ordersRef.child(orderID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Chuyển đổi dữ liệu thành đối tượng Order
                    currentOrder = dataSnapshot.getValue(Order.class);

                    // Nếu lấy được đơn hàng, in ra log để kiểm tra
                    Log.d("getCurrentOrder", "currentOrder: " + currentOrder);

                    // Lấy ID người dùng từ đơn hàng, nếu cần
                    if (currentOrder != null) {
                        currentUserID = currentOrder.getIDCus();
                        Log.d("getCurrentOrder", "currentUserID: " + currentUserID);
                    }
                } else {
                    Log.e("getCurrentOrder", "Order not found for orderID: " + orderID);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("getCurrentOrder", "Error retrieving data: " + databaseError.getMessage());
            }
        });
    }


    private void setupToolbar(){
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initializeFirebase(){
        reviewsRef = genericFunction.getTableReference("Review");
        ordersRef = genericFunction.getTableReference("Order");

        if(orderID != null && !orderID.isEmpty()){
            orderDetailsRef = ordersRef.child(orderID).child("OrderDetail");
        }
    }
    private void loadProducts() {
        genericFunction.getTableReference("Product").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemsnapshot : snapshot.getChildren()) {
                    Product product = itemsnapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
//                setOrderDetails(orderDetailAdapter, productList);
                loadOrderDetails();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching products: " + error.getMessage());
            }
        });
    }

    private  void loadOrderDetails(){
        if(orderID == null || orderID.isEmpty()){
            return;
        }
        genericFunction.getTableReference("Order").child(orderID)
                .child("OrderDetail").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<OrderAdapter.OrderProductItem> productItems = new ArrayList<>();
                        StringBuilder procNameBuilder = new StringBuilder();

                        for (DataSnapshot itemsnapshot : snapshot.getChildren()){
                            OrderDetail orderDetail = itemsnapshot.getValue(OrderDetail.class);
                            if (orderDetail != null){
                                orderDetails.add(orderDetail);

                                // Tìm sản phẩm tương ứng trong danh sách sản phẩm
                                Product matchedProduct = null;

                                for (Product product : productList) {
                                    if (product.getIDProc().equals(orderDetail.getIDProc())) {
                                        matchedProduct = product;
                                        break;
                                    }
                                }

                                // Nếu tìm thấy sản phẩm, thêm vào danh sách hiển thị
                                if (matchedProduct != null) {
                                    OrderAdapter.OrderProductItem item = new OrderAdapter.OrderProductItem(
                                            matchedProduct,
                                            orderDetail.getTotalQuantity(),
                                            orderDetail.getPrice()
                                    );
                                    productItems.add(item);

                                    // Tạo chuỗi tên sản phẩm
                                    if (procNameBuilder.length() > 0) {
                                        procNameBuilder.append(", ");
                                    }
                                    procNameBuilder.append(matchedProduct.getNameProc());
                                }

                            }
                        }
                        // Cập nhật dữ liệu cho adapter
                        orderDetailAdapter.updateData(productItems);
                        Log.d(TAG, "Loaded order details: " + productItems.size() + " items");
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading order details: " + error.getMessage());
                        Toast.makeText(AddReview.this, "Lỗi khi tải chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Thiết lập sự kiện cho nút Submit đánh giá
     */
    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> {
            int rating = (int) ratingBar.getRating();
            String Comment = comment.getText().toString().trim();

            if (!validateReview(rating, Comment)) {
                return;
            }

            // Tạo và gửi đánh giá
            submitReview(createReview(rating, Comment));
        });
    }

    /**
     * Xác thực dữ liệu đánh giá
     */
    private boolean validateReview(int rating, String comment) {
        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    /**
     * Tạo đối tượng Review từ dữ liệu nhập
     */
    private Review createReview(int rating, String comment) {
        Review review = new Review();
        // Kiểm tra currentUserID trước khi gán
        if (currentUserID != null && !currentUserID.isEmpty()) {
            review.setIDCus(currentUserID);
        } else {
            Log.e(TAG, "currentUserID is null or empty");
            // Có thể cần xử lý thêm ở đây
        }
        review.setRating(rating);
        review.setComment(comment);
        return review;
    }

    /**
     * Gửi đánh giá lên Firebase
     */
    private void submitReview(Review review) {
        if (orderID == null || orderID.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra và log thông tin review
        Log.d(TAG, "Review data - IDCus: " + review.getIDCus() +
                ", Rating: " + review.getRating() +
                ", Comment: " + review.getComment());

        // Tạo key cho đánh giá trong nhánh Review của Order
//        String reviewId = ordersRef.child(orderID).child("Review").push().getKey();
//        if (reviewId == null) {
//            Toast.makeText(this, "Lỗi khi tạo đánh giá", Toast.LENGTH_SHORT).show();
//            return;
//        }

        // Lưu đánh giá vào nhánh Reviews của Order
        ordersRef.child(orderID).child("Review").setValue(review)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateOrderStatus();
                        Toast.makeText(this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Lỗi khi gửi đánh giá", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to submit review", task.getException());
                    }
                });
    }

    /**
     * Cập nhật trạng thái đơn hàng thành COMPLETED
     */
    private void updateOrderStatus() {
        if (orderID != null && !orderID.isEmpty()) {
            ordersRef.child(orderID)
                    .child("status")
                    .setValue("COMPLETED")
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update order status", e);
                    });
        }
    }

    //Function used to load order details
    public void setOrderDetails(OrderDetailAdapter productAdapter, List<Product> allProducts) {
        genericFunction.getTableReference("Order").child(orderID)
                .child("OrderDetail").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        //Create list to store each product
                        List<OrderAdapter.OrderProductItem> productItems = new ArrayList<>();
                        StringBuilder procNameBuilder = new StringBuilder();
                        //Loop through all of the itemsnapshot
                        for (com.google.firebase.database.DataSnapshot itemsnapshot : snapshot.getChildren()) {
                            //Get each OrderDetail and then add them to the product details.
                            OrderDetail orderDetail = itemsnapshot.getValue(OrderDetail.class);
                            Product product = null;
                            for (Product innerProduct : allProducts) {
                                if (innerProduct.getIDProc().equals(orderDetail.getIDProc())) {
                                    product = innerProduct;
                                }
                            }
                            //If the product is not null
                            if (product != null) {
                                OrderAdapter.OrderProductItem item = new OrderAdapter.OrderProductItem(
                                        product,
                                        orderDetail.getTotalQuantity(),
                                        orderDetail.getPrice()
                                );
                                //Then add the item to the product
                                productItems.add(item);
                                if (procNameBuilder.length() > 0) {
                                    procNameBuilder.append(", ");
                                }
                                procNameBuilder.append(product.getNameProc());
                            }
                        }
                        //Update the information to the new data
                        productAdapter.updateData(productItems);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
    private void receiveIntentData() {
        currentOrder = (Order) getIntent().getSerializableExtra("ORDER");
        productList = (List<Product>) getIntent().getSerializableExtra("PRODUCTS");

        if (currentOrder == null || productList == null) {
            Toast.makeText(this, "Lỗi khi nhận dữ liệu đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserID = currentOrder.getIDCus();
    }
}