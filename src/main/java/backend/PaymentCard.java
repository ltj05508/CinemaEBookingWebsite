package backend;

import com.google.gson.Gson;
import org.apache.catalina.security.DeployXmlPermission;

import java.util.Date;

//Potential problem converting from Java Date to SQL Date?
public class PaymentCard {
    private String cardId;
    private String cardNumber;
    private String billingAddressId;
    private Date expirationDate;
    private String customerId;

    public PaymentCard() {}

    public PaymentCard(String cardId, String cardNumber, String billingAddressId, Date expirationDate, String customerId) {
        this.cardId = cardId;
        this.cardNumber = cardNumber;
        this.billingAddressId = billingAddressId;
        this.expirationDate = expirationDate;
        this.customerId = customerId;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String getCardId() {return cardId;}
    public String getCardNumber() {return cardNumber;}
    public String getBillingAddressId() {return billingAddressId;}
    public Date getExpirationDate() {return expirationDate;}
    public String getCustomerId() {return customerId;}

    public void setCardId(String cardId) {this.cardId = cardId;}
    public void setCardNumber(String cardNumber) {this.cardNumber = cardNumber;}
    public void setBillingAddress(String billingAddressId) {this.billingAddressId = billingAddressId;}
    public void setExpirationDate(Date expirationDate) {this.expirationDate = expirationDate;}
    public void setCustomerId(String customerId) {this.customerId = customerId;}

    @Override
    public String toString() {
        return "PaymentCard{" +
                "cardId=" + cardId +
                ", cardNumber='" + cardNumber + '\'' +
                ", billingAddressId='" + billingAddressId + '\'' +
                ", expirationDate='" + expirationDate + '\'' +
                ", customerId='" + customerId + '\'' +
                '}';
    }
}
