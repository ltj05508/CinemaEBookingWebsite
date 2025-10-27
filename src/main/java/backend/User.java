package backend;

import com.google.gson.Gson;

public class User {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private boolean loginStatus;
    private boolean marketingOptIn;


    public User() {
    }

    public User(String userId, String firstName, String lastName, String email, String password,
            boolean loginStatus, boolean marketingOptIn) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.loginStatus = loginStatus;
        this.marketingOptIn = marketingOptIn;
    }

    public String getUserId() {return userId;}
    public String getFirstName() {return firstName;}
    public String getLastName() {return lastName;}
    public String getEmail() {return email;}
    public String getPassword() {return password;}
    public boolean getLoginStatus() {return loginStatus;}
    public boolean getMarketingOptIn() {return marketingOptIn;}

    public void setUserId(String userId) {this.userId = userId;}
    public void setFirstName(String firstName) {this.firstName = firstName;}
    public void setLastName(String lastName) {this.lastName = lastName;}
    public void setEmail(String email) {this.email = email;}
    public void setPassword(String password) {this.password = password;}
    public void setLoginStatus(boolean loginStatus) {this.loginStatus = loginStatus;}
    public void setMarketingOptIn(boolean marketingOptIn) {this.marketingOptIn = marketingOptIn;}

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", loginStatus=" + loginStatus + '\'' +
                ", marketingOptIn=" + marketingOptIn +
                '}';
    }
}
