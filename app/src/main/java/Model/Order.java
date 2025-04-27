package Model;

public class Order {
    private String IDOrder;
    private String IDCus;
    private double TotalPayment;
    private int TotalQuantity;
    private String Address;
    private String OrderTime;
    private String Status;

    public String getIDOrder() {
        return IDOrder;
    }

    public void setIDOrder(String IDOrder) {
        this.IDOrder = IDOrder;
    }

    public String getIDCus() {
        return IDCus;
    }

    public void setIDCus(String IDCus) {
        this.IDCus = IDCus;
    }

    public double getTotalPayment() {
        return TotalPayment;
    }

    public void setTotalPayment(double totalPayment) {
        TotalPayment = totalPayment;
    }

    public int getTotalQuantity() {
        return TotalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        TotalQuantity = totalQuantity;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getOrderTime() {
        return OrderTime;
    }

    public void setOrderTime(String orderTime) {
        OrderTime = orderTime;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public Order(String IDOrder, String IDCus, double totalPayment, int totalQuantity, String address, String orderTime, String status) {
        this.IDOrder = IDOrder;
        this.IDCus = IDCus;
        this.TotalPayment = totalPayment;
        this.TotalQuantity = totalQuantity;
        this.Address = address;
        this.OrderTime = orderTime;
        this.Status = status;
    }

    public Order(){}
}
