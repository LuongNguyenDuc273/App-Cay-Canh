package Model;

public class User {
    private String Gmail;
    private String Password;

    public String getGmail() {
        return Gmail;
    }

    public void setGmail(String gmail) {
        Gmail = gmail;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public User(String gmail, String password) {
        Gmail = gmail;
        Password = password;
    }

    public User(){}
}
