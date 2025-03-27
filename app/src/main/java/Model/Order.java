package Model;

public class Order {
    private String IDOrder;
    private String IDCus;
    private double TotalPayment;
    private int TotalQuantity;
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

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public Order(String IDOrder, String IDCus, double totalPayment, int totalQuantity, String status) {
        this.IDOrder = IDOrder;
        this.IDCus = IDCus;
        TotalPayment = totalPayment;
        TotalQuantity = totalQuantity;
        Status = status;
    }

    public Order(){}
}
