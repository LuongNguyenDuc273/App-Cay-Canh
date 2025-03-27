package Model;

public class Address {
    private String IDCus;
    private String AddressLoc;

    public String getIDCus() {
        return IDCus;
    }

    public void setIDCus(String IDCus) {
        this.IDCus = IDCus;
    }

    public String getAddressLoc() {
        return AddressLoc;
    }

    public void setAddressLoc(String addressLoc) {
        AddressLoc = addressLoc;
    }

    public Address(String IDCus, String addressLoc) {
        this.IDCus = IDCus;
        AddressLoc = addressLoc;
    }

    public Address(){}
}
