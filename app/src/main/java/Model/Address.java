package Model;

public class Address {
    private String AddressLoc;
    public String getAddressLoc() {
        return AddressLoc;
    }

    public void setAddressLoc(String addressLoc) {
        AddressLoc = addressLoc;
    }

    public Address(String addressLoc) {
        AddressLoc = addressLoc;
    }

    public Address(){}
}
