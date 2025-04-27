package Model;

public class Address {
    private String AddressLoc;
    private String IDAdress;
    public String getAddressLoc() {
        return AddressLoc;
    }

    public String getIDAdress() {
        return IDAdress;
    }

    public void setIDAdress(String IDAdress) {
        this.IDAdress = IDAdress;
    }

    public void setAddressLoc(String addressLoc) {
        AddressLoc = addressLoc;
    }

    public Address(String addressLoc, String idAddress) {
        AddressLoc = addressLoc;
        IDAdress = idAddress;
    }

    public Address(){}
}
