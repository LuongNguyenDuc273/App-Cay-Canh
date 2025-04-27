package com.adrnc_g02.appcaycanh;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Model.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Profile extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    // Views
    private TextView nameUser, logout, orderHistory;
    private EditText phoneEdit, gmailEdit, fullnameEdit, birthdayEdit, addressEdit;
    private ImageView personalInfoArrow, addressArrow, datePicker;
    private LinearLayout
            personalInfoHeader, personalInfoContent, addressHeader, addressContent,
            add_Address, waitingConfirmationLayout, waitingPickupLayout;
    private BottomNavigationView bottomNavigationView;
    private RecyclerView addressRecycleView;

    // Adapters
    private AddressAdapter addressAdapter;

    // Firebase
    private String currentUserID = "";
    private FirebaseDatabase database;
    private DatabaseReference customerRef, addressRef;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Helpers
    private SessionControl session;
    private GenericFunction<Customer> customerGenericFunction;
    private GenericFunction addressGenericFunction = new GenericFunction();
    private List<Address> addressList = new ArrayList<>();

    // State
    private boolean isPersonalInfoExpanded = false;
    private boolean isAddressExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Thiết lập giao diện cơ bản
        setupUI();

        // Khởi tạo và ánh xạ view
        initializeViews();

        // Thiết lập bottom navigation
        setupBottomNavigation();

        // Thiết lập phần thông tin cá nhân
        setupPersonalInfoSection();

        // Thiết lập phần địa chỉ
        setupAddressSection();

        // Thiết lập phần đơn hàng
        setupOrderSections();

        // Khởi tạo Firebase
        initializeFirebase();

        // Tải dữ liệu người dùng
        loadUserData();

        // Tải địa chỉ
        loadAddress();
    }

    /**
     * Thiết lập giao diện cơ bản và edge-to-edge
     */
    private void setupUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Xử lý insets cho hệ thống (thanh trạng thái, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Khởi tạo và ánh xạ tất cả các view từ layout
     */
    private void initializeViews() {
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserID = cUser.getUid();
        // TextViews
        nameUser = findViewById(R.id.username);
        logout = findViewById(R.id.Logout);
        orderHistory = findViewById(R.id.tvOrderHistory);

        // EditTexts
        phoneEdit = findViewById(R.id.phone_number);
        gmailEdit = findViewById(R.id.gmail_address);
        fullnameEdit = findViewById(R.id.fullname);
        birthdayEdit = findViewById(R.id.birthday);
        setupBirthdayInput(); // Thiết lập xử lý nhập ngày sinh

        // ImageViews
        personalInfoArrow = findViewById(R.id.personal_info_arrow);
        addressArrow = findViewById(R.id.address_arrow);
        datePicker = findViewById(R.id.btn_date_picker);
        datePicker.setOnClickListener(v -> showDatePickerDialog()); // Xử lý click date picker

        // LinearLayouts
        personalInfoHeader = findViewById(R.id.personal_info_header);
        personalInfoContent = findViewById(R.id.personal_info_content);
        addressHeader = findViewById(R.id.address_header);
        addressContent = findViewById(R.id.address_content);
        add_Address = findViewById(R.id.add_address);
        waitingConfirmationLayout = findViewById(R.id.waiting_confirmation_layout);
        waitingPickupLayout = findViewById(R.id.waiting_delivery_layout);

        // RecyclerView cho danh sách địa chỉ
        addressRecycleView = findViewById(R.id.address_recycler_view);
        addressRecycleView.setLayoutManager(new LinearLayoutManager(this));
        addressAdapter = new AddressAdapter(addressList, this, currentUserID);
        addressRecycleView.setAdapter(addressAdapter);

        // Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.navProfile); // Mặc định chọn tab Profile
    }

    /**
     * Thiết lập bottom navigation và xử lý sự kiện chuyển trang
     */
    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navHome) {
                startActivity(new Intent(Profile.this, MainActivity.class));
            } else if (itemId == R.id.navCart) {
                // Xử lý khi click vào giỏ hàng (chưa implement)
            } else if (itemId == R.id.navExplore) {
                startActivity(new Intent(Profile.this, ShoppingCart.class));
            } else if (itemId == R.id.navProfile) {
                return true; // Đã ở trang Profile, không làm gì
            }
            return true;
        });
    }

    /**
     * Thiết lập phần thông tin cá nhân gồm:
     * - Nút logout
     * - Xử lý expand/collapse
     * - Lịch sử đơn hàng
     */
    private void setupPersonalInfoSection() {
        // Xử lý logout
        logout.setOnClickListener(view -> {
            session.signOutCompletely();
            Log.d(TAG, "Logout button clicked");
        });

        // Xử lý expand/collapse thông tin cá nhân
        personalInfoHeader.setOnClickListener(v -> {
            isPersonalInfoExpanded = !isPersonalInfoExpanded;
            personalInfoContent.setVisibility(isPersonalInfoExpanded ? View.VISIBLE : View.GONE);
            rotateArrow(personalInfoArrow, isPersonalInfoExpanded);
            Log.d(TAG, "Personal Info Header clicked, isExpanded: " + isPersonalInfoExpanded);
        });

        // Xử lý chuyển sang trang lịch sử đơn hàng
        orderHistory.setOnClickListener(view -> {
            startActivity(new Intent(Profile.this, OrderHistory.class));
        });
    }

    /**
     * Thiết lập phần địa chỉ gồm:
     * - Xử lý expand/collapse
     * - Nút thêm địa chỉ mới
     */
    private void setupAddressSection() {
        // Xử lý expand/collapse danh sách địa chỉ
        addressHeader.setOnClickListener(v -> {
            isAddressExpanded = !isAddressExpanded;
            addressContent.setVisibility(isAddressExpanded ? View.VISIBLE : View.GONE);
            rotateArrow(addressArrow, isAddressExpanded);
            Log.d(TAG, "Address Header clicked, isExpanded: " + isAddressExpanded);
        });

        // Xử lý thêm địa chỉ mới
        add_Address.setOnClickListener(v -> showAddAddressDialog());
    }

    /**
     * Thiết lập các section đơn hàng gồm:
     * - Đơn hàng chờ xác nhận
     * - Đơn hàng chờ lấy
     */
    private void setupOrderSections() {
        // Xử lý chuyển sang trang đơn hàng chờ xác nhận
        waitingConfirmationLayout.setOnClickListener(view -> {
            startActivity(new Intent(Profile.this, WaitingConfirmation.class));
        });

        // Xử lý chuyển sang trang đơn hàng chờ lấy
        waitingPickupLayout.setOnClickListener(view -> {
            startActivity(new Intent(Profile.this, WaitingPickup.class));
        });
    }

    /**
     * Khởi tạo các service Firebase gồm:
     * - Authentication
     * - Database
     * - Session control
     */
    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        customerRef = database.getReference("Customer");
        session = new SessionControl(this);
        customerGenericFunction = new GenericFunction<Customer>();
        Log.d(TAG, "GenericFunction initialized: " + (customerGenericFunction != null));
    }

    /**
     * Thiết lập xử lý nhập ngày sinh:
     * - Tự động thêm dấu "/" khi nhập
     * - Validate ngày hợp lệ
     */
    private void setupBirthdayInput() {
        birthdayEdit.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private String ddmmyyyy = "DDMMYYYY";
            private Calendar cal = Calendar.getInstance();

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d]", ""); // Chỉ giữ số
                    String cleanC = current.replaceAll("[^\\d]", "");

                    int cl = clean.length();
                    int sel = cl;

                    // Tự động thêm dấu "/"
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }

                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8) {
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        // Validate ngày hợp lệ
                        int day = Integer.parseInt(clean.substring(0, 2));
                        int mon = Integer.parseInt(clean.substring(2, 4));
                        int year = Integer.parseInt(clean.substring(4, 8));

                        mon = mon < 1 ? 1 : Math.min(mon, 12);
                        cal.set(Calendar.MONTH, mon - 1);
                        year = Math.max(year, 1900);
                        cal.set(Calendar.YEAR, year);
                        day = Math.min(day, cal.getActualMaximum(Calendar.DATE));
                        clean = String.format("%02d%02d%04d", day, mon, year);
                    }

                    // Định dạng lại chuỗi ngày tháng
                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4), clean.substring(4, 8));

                    sel = Math.max(sel, 0);
                    current = clean;
                    birthdayEdit.setText(current);
                    birthdayEdit.setSelection(Math.min(sel, current.length()));
                }
            }
        });

        // Xử lý click vào EditText ngày sinh
        birthdayEdit.setOnClickListener(v -> {
            birthdayEdit.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(birthdayEdit, InputMethodManager.SHOW_IMPLICIT);
        });
    }

    /**
     * Tải dữ liệu người dùng từ Firebase
     * - Lấy thông tin từ Intent (nếu có)
     * - Lấy thông tin từ Firebase Authentication
     * - Lấy thông tin chi tiết từ Firebase Database
     */
    private void loadUserData() {
        Log.d(TAG, "loadUserData called");
        if (currentUser == null) {
            nameUser.setText("Chua dang nhap");
            Log.d(TAG, "No current user, set text to 'Chua dang nhap'");
            return;
        }

        // Lấy thông tin từ Intent (nếu có)
        String userName = getIntent().getStringExtra("a");
        String userName2 = getIntent().getStringExtra("b");
        String userGmail = currentUser.getEmail();

        Log.d(TAG, "userName from intent: " + userName);
        Log.d(TAG, "userName2 from intent: " + userName2);
        Log.d(TAG, "userGmail from currentUser: " + userGmail);

        // Ưu tiên hiển thị tên từ Intent trước
        if (userName != null) {
            nameUser.setText(userName);
            Log.d(TAG, "Set nameUser to userName");
        } else if (userName2 != null) {
            nameUser.setText(userName2);
            Log.d(TAG, "Set nameUser to userName2");
        } else {
            nameUser.setText(userGmail);
            Log.d(TAG, "Set nameUser to userGmail");
        }

        // Nếu có email, query thông tin từ Database
        if (userGmail != null) {
            Log.d(TAG, "Querying Customer node with Gmail: " + userGmail);
            customerRef.orderByChild("gmail")
                    .equalTo(userGmail)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d(TAG, "onDataChange called, dataSnapshot exists: " + dataSnapshot.exists());
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    Customer customer = snapshot.getValue(Customer.class);
                                    Log.d(TAG, "Customer object from snapshot: " + customer);
                                    if (customer != null) {
                                        displayInfo(customer); // Hiển thị thông tin
                                        currentUserID = customer.getIDCus(); // Lưu ID người dùng
                                        Log.d(TAG, "currentUserID set to: " + currentUserID);
                                        setupEditText(); // Thiết lập EditText sau khi có dữ liệu
                                    }
                                }
                            } else {
                                // Nếu không có trong Database, dùng thông tin từ Authentication
                                phoneEdit.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "");
                                gmailEdit.setText(currentUser.getEmail());
                                fullnameEdit.setText("");
                                birthdayEdit.setText("");
                                Log.d(TAG, "No data found in database, using currentUser info");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Database error: " + error.getMessage());
                            Toast.makeText(Profile.this, "Loi tai thong tin", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    //Tải danh sách địa chỉ từ Firebase Database
    private void loadAddress() {
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserID = cUser.getUid();
        if (currentUserID.isEmpty()) {
            return;
        }

        // Lấy reference đến node Address của user hiện tại
        addressRef = database.getReference("Customer").child(currentUserID).child("Address");
        addressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshott) {
                addressList.clear();
                for (DataSnapshot snapshott : dataSnapshott.getChildren()) {
                    Address address = snapshott.getValue(Address.class);
                    if (address != null) {
                        addressList.add(address); // Thêm địa chỉ vào danh sách
                    }
                }
                addressAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Lỗi tải địa chỉ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Hiển thị thông tin người dùng lên giao diện
    private void displayInfo(Customer customer) {
        Log.d(TAG, "displayInfo called with customer: " + customer);
        // Hiển thị tên, ưu tiên tên từ Database
        if (customer.getNameCus() != null && !customer.getNameCus().isEmpty()) {
            nameUser.setText(customer.getNameCus());
        } else {
            nameUser.setText(currentUser.getEmail());
        }

        // Hiển thị các thông tin khác
        phoneEdit.setText(customer.getPhone() != null ? customer.getPhone() : "");
        gmailEdit.setText(customer.getGmail() != null ? customer.getGmail() : currentUser.getEmail());
        fullnameEdit.setText(customer.getNameCus() != null ? customer.getNameCus() : "");
        birthdayEdit.setText(customer.getBirthday() != null ? customer.getBirthday() : "");
        Log.d(TAG, "Phone number set to: " + phoneEdit.getText().toString());
        Log.d(TAG, "Gmail set to: " + gmailEdit.getText().toString());
    }

    //Xoay mũi tên khi expand/collapse
    private void rotateArrow(ImageView arrow, boolean isExpanded) {
        float fromDegree = isExpanded ? 0f : 90f;
        float toDegree = isExpanded ? 90f : 0f;

        // Tạo animation xoay
        RotateAnimation rotateAnimation = new RotateAnimation(
                fromDegree,
                toDegree,
                Animation.RELATIVE_TO_SELF, 0.5f, // Xoay quanh tâm
                Animation.RELATIVE_TO_SELF, 0.5f
        );

        rotateAnimation.setDuration(250);
        rotateAnimation.setFillAfter(true); // Giữ trạng thái sau khi xoay
        arrow.startAnimation(rotateAnimation);
    }

    //Cập nhật field lên Firebase Database
    public void updateField(String tableName, String itemId, String fieldName, Object newValue, View v) {
        Log.d(TAG, "updateField called with: tableName=" + tableName +
                ", itemId=" + itemId + ", fieldName=" + fieldName + ", newValue=" + newValue);

        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(this, "Không thể cập nhật! ID không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ẩn bàn phím và clear focus
        if (v != null) {
            v.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }

        // Gọi hàm update từ GenericFunction
        customerGenericFunction.updateData(tableName, itemId, fieldName, newValue)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Profile.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "updateData failed: " + (task.getException() != null ? task.getException().getMessage() : ""));
                        Toast.makeText(Profile.this, "Cập nhật thất bại: " +
                                        (task.getException() != null ? task.getException().getMessage() : ""),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Hiển thị dialog thêm địa chỉ mới
     */
    private void showAddAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm địa chỉ mới");

        // Sử dụng layout item_address cho dialog
        View dialogView = getLayoutInflater().inflate(R.layout.item_address, null);
        builder.setView(dialogView);

        EditText addressText = dialogView.findViewById(R.id.address_text);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String addressStr = addressText.getText().toString();
            Log.d("Dia Chi", "Dia chi da luu la: " + addressText.getText().toString());
            if (!addressStr.isEmpty()) {
                addNewAddress(addressStr); // Gọi hàm thêm địa chỉ
            } else {
                Toast.makeText(this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Thêm địa chỉ mới vào Firebase Database
     * @param addressStr Địa chỉ mới
     */
    private void addNewAddress(String addressStr) {
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserID = cUser.getUid();
        Log.d("THEM DIA CHI", "Khoi tao function them DC");
        Log.d("CURRENTUSERID", "ID hien tai la: " + currentUserID);

        if (currentUserID.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng Address mới
        String idAddress = addressGenericFunction.getTableReference("Customer")
                .child(currentUserID).child("Address").push().getKey();
        Address newAddress = new Address(addressStr, idAddress);

        if (addressStr != null) {
            // Thêm vào Database
            addressGenericFunction.getTableReference("Customer")
                    .child(currentUserID).child("Address").child(idAddress).setValue(newAddress);
        }
    }

    /**
     * Hiển thị DatePickerDialog để chọn ngày sinh
     */
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();

        // Nếu đã có ngày sinh, parse thành ngày/tháng/năm
        if (!birthdayEdit.getText().toString().isEmpty()) {
            try {
                String[] dateParts = birthdayEdit.getText().toString().split("/");
                int day = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]) - 1; // Month is 0-based
                int year = Integer.parseInt(dateParts[2]);
                calendar.set(year, month, day);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Tạo DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    // Định dạng ngày và hiển thị lên EditText
                    String formattedDate = String.format(Locale.getDefault(),
                            "%02d/%02d/%d", day, month + 1, year);
                    birthdayEdit.setText(formattedDate);
                    // Tự động lưu khi chọn ngày
                    updateField("Customer", currentUserID, "birthday", formattedDate, birthdayEdit);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Thiết lập ngày tối đa là hôm nay
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    // Hàm xử lý xóa (có thể giữ nguyên như cũ)
    public void deleteAddress(String idAddress,int position) {
        // Kiểm tra position hợp lệ trước khi xử lý
        if (position < 0 || position >= addressList.size()) {
            Toast.makeText(this, "Vị trí không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa địa chỉ")
                .setMessage("Bạn có chắc chắn muốn xóa địa chỉ này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Thực hiện xóa từ Firebase
                    DatabaseReference ref = FirebaseDatabase.getInstance()
                            .getReference("Customer")
                            .child(currentUserID)
                            .child("Address")
                            .child(idAddress);

                    ref.removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateAddressIndexes() {
        DatabaseReference ref = database.getReference("Customer")
                .child(currentUserID)
                .child("address");

        ref.setValue(addressList); // Ghi đè toàn bộ danh sách với index mới
    }

    /**
     * Thiết lập sự kiện cho các EditText:
     * - Tự động lưu khi nhấn Enter hoặc Done
     */
    private void setupEditText() {
        Log.d(TAG, "setupEditText called");

        // Xử lý số điện thoại
        phoneEdit.setOnEditorActionListener((v, actionId, event) -> {
            Log.d(TAG, "setOnEditorActionListener called, actionId: " + actionId + ", event: " + event);
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String newPhoneNumber = phoneEdit.getText().toString();
                Log.d(TAG, "Enter key pressed, newPhoneNumber: " + newPhoneNumber);
                updateField("Customer", currentUserID, "phone", newPhoneNumber, phoneEdit);
                Log.d("PhoneText", phoneEdit.getText().toString());
                return true;
            }
            return false;
        });

        // Xử lý tên đầy đủ
        fullnameEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String newFullname = fullnameEdit.getText().toString();
                updateField("Customer", currentUserID, "nameCus", newFullname, fullnameEdit);
                return true;
            }
            return false;
        });

        // Xử lý ngày sinh
        birthdayEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String newBirthday = birthdayEdit.getText().toString();
                updateField("Customer", currentUserID, "birthday", newBirthday, birthdayEdit);
                return true;
            }
            return false;
        });
    }
}