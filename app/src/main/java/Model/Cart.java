package Model;

public class Cart {
    private String IDCus;
    private String IDProc;
    private int Quantity;

    public String getIDCus() {
        return IDCus;
    }

    public void setIDCus(String IDCus) {
        this.IDCus = IDCus;
    }

    public String getIDProc() {
        return IDProc;
    }

    public void setIDProc(String IDProc) {
        this.IDProc = IDProc;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        Quantity = quantity;
    }

    public Cart(String IDCus, String IDProc, int quantity) {
        this.IDCus = IDCus;
        this.IDProc = IDProc;
        Quantity = quantity;
    }

    public Cart(){};
}
