package backend;

import com.google.gson.Gson;

public class Address {
    private String addressId;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String customerId;

    public Address() {
    }

    public Address(String addressId, String street, String city, String state, 
                   String postalCode, String country, String customerId) {
        this.addressId = addressId;
        this.street = street;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.customerId = customerId;
    }

    public String getAddressId() { return addressId; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }
    public String getCustomerId() { return customerId; }

    public void setAddressId(String addressId) { this.addressId = addressId; }
    public void setStreet(String street) { this.street = street; }
    public void setCity(String city) { this.city = city; }
    public void setState(String state) { this.state = state; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public void setCountry(String country) { this.country = country; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public String toString() {
        return "Address{" +
                "addressId='" + addressId + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                ", customerId='" + customerId + '\'' +
                '}';
    }
}
