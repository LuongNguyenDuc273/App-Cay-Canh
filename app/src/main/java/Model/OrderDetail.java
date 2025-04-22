package Model;

public class OrderDetail {
    private String idproc ;
    private double TotalAmount;
    private int totalQuantity;
    private double price;

    public String getIDProc() {
        return idproc ;
    }

    public void setIDProc(String idproc ) {
        this.idproc = idproc ;
    }

    public double getTotalAmount() {
        return TotalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        TotalAmount = totalAmount;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int TotalQuantity) {
        totalQuantity = TotalQuantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public OrderDetail(String idproc, double totalAmount, int TotalQuantity, double price) {
        this.idproc  = idproc ;
        TotalAmount = totalAmount;
        totalQuantity = TotalQuantity;
        this.price = price;
    }

    public OrderDetail(){}
}
