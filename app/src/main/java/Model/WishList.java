package Model;

public class WishList {
    private String IDCus;
    private String IDProc;

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

    public WishList(String IDCus) {
        this.IDCus = IDCus;
    }

    public WishList(){}
}
