package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Database functions for User, Customer, Admin, Address, and PaymentCard operations.
 * Uses ConnectToDatabase.conn for database connection.
 */
public class UserDBFunctions {
    
    /**
     * Create a new user in the Users table and Customers table (with Inactive state).
     * @param firstName User's first name
     * @param lastName User's last name
     * @param email User's email address
     * @param hashedPassword BCrypt hashed password
     * @return The generated user_id, or null if error
     */
    public static String createUser(String firstName, String lastName, String email, String hashedPassword, boolean marketingOptIn) {
        Connection conn = ConnectToDatabase.conn;
        String userId = UUID.randomUUID().toString();
        
        try {
            // Check if email already exists
            if (findUserByEmail(email) != null) {
                System.err.println("User with email " + email + " already exists");
                return null;
            }
            
            // Insert into Users table
            PreparedStatement userStmt = conn.prepareStatement(
                "INSERT INTO Users (user_id, first_name, last_name, email, password, login_status, marketing_opt_in) VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            userStmt.setString(1, userId);
            userStmt.setString(2, firstName);
            userStmt.setString(3, lastName);
            userStmt.setString(4, email);
            userStmt.setString(5, hashedPassword);
            userStmt.setBoolean(6, false); // not logged in yet
            userStmt.setBoolean(7, marketingOptIn);
            userStmt.executeUpdate();
            
            // Insert into Customers table with Inactive state
            String customerId = userId; // Same as user_id
            PreparedStatement customerStmt = conn.prepareStatement(
                "INSERT INTO Customers (customer_id, state) VALUES (?, ?)"
            );
            customerStmt.setString(1, customerId);
            customerStmt.setString(2, "Inactive"); // Inactive until email verified
            customerStmt.executeUpdate();
            
            return userId;
            
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Find a user by email address.
     * @param email User's email
     * @return User object if found, null otherwise
     */
    public static User findUserByEmail(String email) {
        Connection conn = ConnectToDatabase.conn;
        
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT user_id, first_name, last_name, email, password, login_status, marketing_opt_in FROM Users WHERE email = ?"
            );
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getString("user_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getBoolean("login_status"),
                    rs.getBoolean("marketing_opt_in")
                );
            }
            
            return null;
            
        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Activate a customer account (set state to Active).
     * @param email User's email
     * @return true if successful, false otherwise
     */
    public static boolean activateCustomer(String email) {
        Connection conn = ConnectToDatabase.conn;
        
        try {
            // Get user_id from email
            User user = findUserByEmail(email);
            if (user == null) return false;
            
            // Update Customers table
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE Customers SET state = ? WHERE customer_id = ?"
            );
            stmt.setString(1, "Active");
            stmt.setString(2, user.getUserId());
            int rows = stmt.executeUpdate();
            
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error activating customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if a user is an admin.
     * @param userId User's ID
     * @return true if user is admin, false otherwise
     */
    public static boolean isAdmin(String userId) {
        Connection conn = ConnectToDatabase.conn;
        
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT admin_id FROM Admins WHERE admin_id = ?"
            );
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            return rs.next(); // Returns true if found in Admins table
            
        } catch (SQLException e) {
            System.err.println("Error checking if user is admin: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if a customer account is active.
     * @param userId User's ID
     * @return true if active, false otherwise
     */
    public static boolean isCustomerActive(String userId) {
        Connection conn = ConnectToDatabase.conn;
        
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT state FROM Customers WHERE customer_id = ?"
            );
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String state = rs.getString("state");
                return "Active".equals(state);
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error checking customer status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update user's password.
     * @param email User's email
     * @param hashedPassword New BCrypt hashed password
     * @return true if successful, false otherwise
     */
    public static boolean updatePassword(String email, String hashedPassword) {
        Connection conn = ConnectToDatabase.conn;
        
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE Users SET password = ? WHERE email = ?"
            );
            stmt.setString(1, hashedPassword);
            stmt.setString(2, email);
            int rows = stmt.executeUpdate();
            
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update user's profile information (first name, last name).
     * @param email User's email
     * @param firstName New first name
     * @param lastName New last name
     * @return true if successful, false otherwise
     */
    public static boolean updateProfile(String email, String firstName, String lastName) {
        Connection conn = ConnectToDatabase.conn;
        
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE Users SET first_name = ?, last_name = ? WHERE email = ?"
            );
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            int rows = stmt.executeUpdate();
            
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get or create address for a customer. Users can only have ONE address.
     * @param customerId Customer's ID
     * @return Address object if exists, null otherwise
     */
    public static Address getCustomerAddress(String customerId) {
        Connection conn = ConnectToDatabase.conn;
        
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT address_id, street, city, state, postal_code, country FROM Addresses WHERE customer_id = ?"
            );
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Address address = new Address();
                address.setAddressId(rs.getString("address_id"));
                address.setStreet(rs.getString("street"));
                address.setCity(rs.getString("city"));
                address.setState(rs.getString("state"));
                address.setPostalCode(rs.getString("postal_code"));
                address.setCountry(rs.getString("country"));
                address.setCustomerId(customerId);
                return address;
            }
            
            return null;
            
        } catch (SQLException e) {
            System.err.println("Error getting customer address: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Update or create customer's address (only ONE address allowed per user).
     * @param customerId Customer's ID
     * @param street Street address
     * @param city City
     * @param state State
     * @param postalCode Postal code
     * @param country Country
     * @return true if successful, false otherwise
     */
    public static boolean saveCustomerAddress(String customerId, String street, String city, 
                                              String state, String postalCode, String country) {
        Connection conn = ConnectToDatabase.conn;
        
        try {
            // Check if address already exists
            Address existing = getCustomerAddress(customerId);
            
            if (existing != null) {
                // Update existing address
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE Addresses SET street = ?, city = ?, state = ?, postal_code = ?, country = ? WHERE customer_id = ?"
                );
                stmt.setString(1, street);
                stmt.setString(2, city);
                stmt.setString(3, state);
                stmt.setString(4, postalCode);
                stmt.setString(5, country);
                stmt.setString(6, customerId);
                stmt.executeUpdate();
            } else {
                // Insert new address
                String addressId = UUID.randomUUID().toString();
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO Addresses (address_id, street, city, state, postal_code, country, customer_id) VALUES (?, ?, ?, ?, ?, ?, ?)"
                );
                stmt.setString(1, addressId);
                stmt.setString(2, street);
                stmt.setString(3, city);
                stmt.setString(4, state);
                stmt.setString(5, postalCode);
                stmt.setString(6, country);
                stmt.setString(7, customerId);
                stmt.executeUpdate();
            }
            
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error saving customer address: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all payment cards for a customer.
     * @param customerId Customer's ID
     * @return List of PaymentCard objects
     */
    public static List<PaymentCard> getCustomerPaymentCards(String customerId) {
        Connection conn = ConnectToDatabase.conn;
        List<PaymentCard> cards = new ArrayList<>();
        
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT card_id, card_number, expiration_date, billing_address_id FROM PaymentCards WHERE customer_id = ?"
            );
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                PaymentCard card = new PaymentCard(
                    rs.getString("card_id"),
                    rs.getString("card_number"), // Will be encrypted
                    rs.getString("billing_address_id"),
                    rs.getDate("expiration_date"),
                    customerId
                );
                cards.add(card);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting payment cards: " + e.getMessage());
            e.printStackTrace();
        }
        
        return cards;
    }
    
    /**
     * Add a payment card for a customer. Maximum 4 cards allowed.
     * @param customerId Customer's ID
     * @param encryptedCardNumber Encrypted card number
     * @param expirationDate Card expiration date
     * @param billingAddressId Billing address ID (can be null)
     * @return Card ID if successful, null otherwise
     */
    public static String addPaymentCard(String customerId, String encryptedCardNumber, 
                                       Date expirationDate, String billingAddressId) {
        Connection conn = ConnectToDatabase.conn;
        
        try {
            // Check card limit (max 4 cards)
            List<PaymentCard> existingCards = getCustomerPaymentCards(customerId);
            if (existingCards.size() >= 4) {
                System.err.println("Customer already has 4 payment cards (maximum allowed)");
                return null;
            }
            
            String cardId = UUID.randomUUID().toString();
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO PaymentCards (card_id, card_number, expiration_date, customer_id, billing_address_id) VALUES (?, ?, ?, ?, ?)"
            );
            stmt.setString(1, cardId);
            stmt.setString(2, encryptedCardNumber);
            stmt.setDate(3, expirationDate);
            stmt.setString(4, customerId);
            stmt.setString(5, billingAddressId);
            stmt.executeUpdate();
            
            return cardId;
            
        } catch (SQLException e) {
            System.err.println("Error adding payment card: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Delete a payment card.
     * @param cardId Card ID to delete
     * @param customerId Customer's ID (for security check)
     * @return true if successful, false otherwise
     */
    public static boolean deletePaymentCard(String cardId, String customerId) {
        Connection conn = ConnectToDatabase.conn;
        
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM PaymentCards WHERE card_id = ? AND customer_id = ?"
            );
            stmt.setString(1, cardId);
            stmt.setString(2, customerId);
            int rows = stmt.executeUpdate();
            
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting payment card: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
