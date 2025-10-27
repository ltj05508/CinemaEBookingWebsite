package backend;

public class Admin extends User{
    private String adminId;

    public Admin() {
        super();
    }

    public Admin(String userId, String firstName, String lastName, String email, String password,
                 boolean loginStatus, boolean marketingOptIn, String adminId) {
        super(userId, firstName, lastName, email, password, loginStatus, marketingOptIn);
        this.adminId = adminId;
    }

    public String getAdminId() {return adminId;}
    public void setAdminId(String adminId) {this.adminId = adminId;}

    @Override
    public String toString() {
        return "Admin{" +
                "userId=" + getUserId() +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", password='" + getPassword() + '\'' +
                ", loginStatus=" + getLoginStatus() + '\'' +
                ", adminId=" + adminId + '\'' +
                '}';
    }

}
