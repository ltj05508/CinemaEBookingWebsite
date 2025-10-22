package backend;

public class Customer extends User{
    private String customerId;
    private State state;
    private PaymentCard[] paymentCards = new PaymentCard[3];

    public Customer() {
        super();
        state = State.Active;
    }

    public Customer(String userId, String firstName, String lastName, String email, String password,
                    boolean loginStatus, String customerId, State state){
        super(userId, firstName, lastName, email, password, loginStatus);
        this.customerId = customerId;
        this.state = state;
    }

    public String getCustomerId() {return customerId;}
    public State getState() {return state;}
    public PaymentCard[] getPaymentCards() {return paymentCards;}

    public void setCustomerId(String customerId) {this.customerId = customerId;}
    public void setState(State state) {this.state = state;}
    public void setPaymentCard(PaymentCard card) {
        for(int i = 0; i < 3; i++) {
            if (paymentCards[i] == null) {
                paymentCards[i] = card;
                break;
            }
        }
    }

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
                "}";
    }
}
