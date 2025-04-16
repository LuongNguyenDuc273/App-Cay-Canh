package Model;

public class User {
    private String Gmail;
    private String Role;
    private String Status;

    public String getGmail() {
        return Gmail;
    }

    public void setGmail(String gmail) {
        Gmail = gmail;
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

    public User(String gmail, String role, String status) {
        Gmail = gmail;
        Role = role;
        Status = status;
    }

    public User(){}
}
