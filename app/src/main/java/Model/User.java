package Model;

public class User {
    private String Gmail;
    private String Password;
    private String Role;
    private String Status;

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

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public User(String gmail, String password, String role, String status) {
        Gmail = gmail;
        Password = password;
        Role = role;
        Status = status;
    }

    public User(){}
}
