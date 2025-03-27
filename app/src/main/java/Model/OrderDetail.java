package Model;

public class OrderDetail {
    private String IDProc;
    private double TotalAmount;
    private int TotalQuantity;
    private double price;

    public String getIDProc() {
        return IDProc;
    }

    public void setIDProc(String IDProc) {
        this.IDProc = IDProc;
    }

    public double getTotalAmount() {
        return TotalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        TotalAmount = totalAmount;
    }

    public int getTotalQuantity() {
        return TotalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        TotalQuantity = totalQuantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public OrderDetail(String IDProc, double totalAmount, int totalQuantity, double price) {
        this.IDProc = IDProc;
        TotalAmount = totalAmount;
        TotalQuantity = totalQuantity;
        this.price = price;
    }

    public OrderDetail(){}
}
