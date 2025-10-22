package backend;

public class Customer extends User{
    private String customerId;
    private State state;

    public Customer() {
        super();
        state = State.Active;
    }

    public Customer(String userId, String firstName, String lastName, String email, String password,
                    boolean loginStatus, String customerId, State state){
        super(userId, firstName, lastName, email, password, loginStatus);
        customerId = this.customerId;
        state = this.state;
    }

    public String getCustomerId() {return customerId;}
    public State getState() {return state;}

    public void setCustomerId(String customerId) {customerId = this.customerId;}
    public void setState(State state) {state = this.state;}

    @Override
    public String toString() {
        return "Customer{" +
                "userId=" + getUserId() +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", password='" + getPassword() + '\'' +
                ", loginStatus=" + getLoginStatus() + '\'' +
                ", customerId=" + customerId + '\'' +
                ", state=" + state + '\'' +
                '}';
    }
}
