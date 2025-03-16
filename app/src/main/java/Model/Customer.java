package Model;

public class Customer {
    private String IDCus;
    private String NameCus;
    private String Gmail;
    private String Birthday;
    private String Phone;
    private String Address;
    private String Password;

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public void setIDCus(String IDCus) {
        this.IDCus = IDCus;
    }

    public void setNameCus(String nameCus) {
        NameCus = nameCus;
    }

    public void setGmail(String gmail) {
        Gmail = gmail;
    }

    public void setBirthday(String birthday) {
        Birthday = birthday;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getIDCus() {
        return IDCus;
    }

    public String getNameCus() {
        return NameCus;
    }

    public String getGmail() {
        return Gmail;
    }

    public String getBirthday() {
        return Birthday;
    }

    public String getPhone() {
        return Phone;
    }

    public String getAddress() {
        return Address;
    }

    public Customer(String IDCus, String nameCus, String gmail, String birthday, String phone, String address, String password) {
        this.IDCus = IDCus;
        NameCus = nameCus;
        Gmail = gmail;
        Birthday = birthday;
        Phone = phone;
        Address = address;
        Password = password;
    }

    public Customer(){};
}
