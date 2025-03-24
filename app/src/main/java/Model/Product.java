package Model;

public class Product {
    private String IDProc;
    private String IDLine;
    private String NameProc;
    private String ReQuantity;
    private String Price;
    private String Describe;
    private String Photo;

    public String getIDProc() {
        return IDProc;
    }

    public void setIDProc(String IDProc) {
        this.IDProc = IDProc;
    }

    public String getIDLine() {
        return IDLine;
    }

    public void setIDLine(String IDLine) {
        this.IDLine = IDLine;
    }

    public String getNameProc() {
        return NameProc;
    }

    public void setNameProc(String nameProc) {
        NameProc = nameProc;
    }

    public String getReQuantity() {
        return ReQuantity;
    }

    public void setReQuantity(String reQuantity) {
        ReQuantity = reQuantity;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getDescribe() {
        return Describe;
    }

    public void setDescribe(String describe) {
        Describe = describe;
    }

    public String getPhoto() {
        return Photo;
    }

    public void setPhoto(String photo) {
        Photo = photo;
    }

    public Product(String IDProc, String IDLine, String nameProc, String reQuantity, String price, String describe, String photo) {
        this.IDProc = IDProc;
        this.IDLine = IDLine;
        NameProc = nameProc;
        ReQuantity = reQuantity;
        Price = price;
        Describe = describe;
        Photo = photo;
    }

    public Product(){}
}
