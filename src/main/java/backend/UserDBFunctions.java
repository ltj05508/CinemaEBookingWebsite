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
    public static boolean updateProfile(String email, String firstName, String lastName, boolean marketingOptIn) {
        Connection conn = ConnectToDatabase.conn;
        
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE Users SET first_name = ?, last_name = ?, marketing_opt_in = ? WHERE email = ?"
            );
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setBoolean(3, marketingOptIn);
            stmt.setString(4, email);
            int rows = stmt.executeUpdate();
            
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get shipping address for a customer (the main address NOT linked to a payment card).
     * @param customerId Customer's ID
     * @return Address object if exists, null otherwise
     */
    public static Address getCustomerAddress(String customerId) {
        Connection conn = ConnectToDatabase.conn;
        
        try {
            // Get the address NOT being used as a billing address
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT address_id, street, city, state, postal_code, country FROM Addresses " +
                "WHERE customer_id = ? AND address_id NOT IN " +
                "(SELECT billing_address_id FROM PaymentCards WHERE customer_id = ? AND billing_address_id IS NOT NULL) " +
                "LIMIT 1"
            );
            stmt.setString(1, customerId);
            stmt.setString(2, customerId);
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
     * Get billing address for a customer (from PaymentCard's billing_address_id or standalone).
     * @param customerId Customer's ID
     * @return Address object if exists, null otherwise
     */
    public static Address getCustomerAddressByType(String customerId, String addressType) {
        if ("billing".equals(addressType)) {
            Connection conn = ConnectToDatabase.conn;
            
            try {
                // First try to get billing address linked to payment card
                PreparedStatement stmt = conn.prepareStatement(
                    "SELECT a.address_id, a.street, a.city, a.state, a.postal_code, a.country " +
                    "FROM Addresses a " +
                    "JOIN PaymentCards pc ON a.address_id = pc.billing_address_id " +
                    "WHERE pc.customer_id = ? LIMIT 1"
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
                
                // If no card-linked billing address, look for standalone billing address
                // (an address that's not the shipping address and not linked to any card)
                PreparedStatement standaloneStmt = conn.prepareStatement(
                    "SELECT a.address_id, a.street, a.city, a.state, a.postal_code, a.country " +
                    "FROM Addresses a " +
                    "WHERE a.customer_id = ? " +
                    "AND a.address_id NOT IN (" +
                    "  SELECT billing_address_id FROM PaymentCards WHERE customer_id = ? AND billing_address_id IS NOT NULL" +
                    ") " +
                    "AND a.address_id != (SELECT address_id FROM Addresses WHERE customer_id = ? LIMIT 1) " +
                    "LIMIT 1"
                );
                standaloneStmt.setString(1, customerId);
                standaloneStmt.setString(2, customerId);
                standaloneStmt.setString(3, customerId);
                ResultSet rsStandalone = standaloneStmt.executeQuery();
                
                if (rsStandalone.next()) {
                    Address address = new Address();
                    address.setAddressId(rsStandalone.getString("address_id"));
                    address.setStreet(rsStandalone.getString("street"));
                    address.setCity(rsStandalone.getString("city"));
                    address.setState(rsStandalone.getString("state"));
                    address.setPostalCode(rsStandalone.getString("postal_code"));
                    address.setCountry(rsStandalone.getString("country"));
                    address.setCustomerId(customerId);
                    return address;
                }
                
                return null;
                
            } catch (SQLException e) {
                System.err.println("Error getting billing address: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } else {
            // For shipping or any other type, get the main address
            return getCustomerAddress(customerId);
        }
    }
    
    /**
     * Update or create customer's shipping address.
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
            // Check if they have a shipping address (not used as billing)
            Address existing = getCustomerAddress(customerId);
            
            if (existing != null) {
                // Update existing shipping address
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE Addresses SET street = ?, city = ?, state = ?, postal_code = ?, country = ? " +
                    "WHERE address_id = ?"
                );
                stmt.setString(1, street);
                stmt.setString(2, city);
                stmt.setString(3, state);
                stmt.setString(4, postalCode);
                stmt.setString(5, country);
                stmt.setString(6, existing.getAddressId());
                stmt.executeUpdate();
            } else {
                // Create new shipping address
                String addressId = UUID.randomUUID().toString();
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO Addresses (address_id, street, city, state, postal_code, country, customer_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)"
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
     * Save billing address - creates NEW address row and links to payment card (or creates standalone if no card yet).
     * @param customerId Customer's ID
     * @param street Street address
     * @param city City
     * @param state State
     * @param postalCode Postal code
     * @param country Country
     * @param addressType "billing" (kept for compatibility but ignored)
     * @return true if successful, false otherwise
     */
    public static boolean saveCustomerAddressByType(String customerId, String street, String city, 
                                                     String state, String postalCode, String country, String addressType) {
        if ("billing".equals(addressType)) {
            Connection conn = ConnectToDatabase.conn;
            
            try {
                // First, check if there's an existing billing address
                Address existingBillingAddr = getCustomerAddressByType(customerId, "billing");
                
                if (existingBillingAddr != null) {
                    // Update existing billing address
                    PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE Addresses SET street = ?, city = ?, state = ?, postal_code = ?, country = ? " +
                        "WHERE address_id = ?"
                    );
                    updateStmt.setString(1, street);
                    updateStmt.setString(2, city);
                    updateStmt.setString(3, state);
                    updateStmt.setString(4, postalCode);
                    updateStmt.setString(5, country);
                    updateStmt.setString(6, existingBillingAddr.getAddressId());
                    updateStmt.executeUpdate();
                    return true;
                }
                
                // No existing billing address, check if they have a payment card
                PreparedStatement cardStmt = conn.prepareStatement(
                    "SELECT card_id, billing_address_id FROM PaymentCards WHERE customer_id = ? LIMIT 1"
                );
                cardStmt.setString(1, customerId);
                ResultSet cardRs = cardStmt.executeQuery();
                
                if (cardRs.next()) {
                    // They have a card, create address and link it
                    String cardId = cardRs.getString("card_id");
                    String existingBillingAddressId = cardRs.getString("billing_address_id");
                    
                    if (existingBillingAddressId != null) {
                        // Update existing billing address linked to card
                        PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE Addresses SET street = ?, city = ?, state = ?, postal_code = ?, country = ? " +
                            "WHERE address_id = ?"
                        );
                        updateStmt.setString(1, street);
                        updateStmt.setString(2, city);
                        updateStmt.setString(3, state);
                        updateStmt.setString(4, postalCode);
                        updateStmt.setString(5, country);
                        updateStmt.setString(6, existingBillingAddressId);
                        updateStmt.executeUpdate();
                    } else {
                        // Create NEW address row for billing and link to card
                        String newAddressId = UUID.randomUUID().toString();
                        PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO Addresses (address_id, street, city, state, postal_code, country, customer_id) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)"
                        );
                        insertStmt.setString(1, newAddressId);
                        insertStmt.setString(2, street);
                        insertStmt.setString(3, city);
                        insertStmt.setString(4, state);
                        insertStmt.setString(5, postalCode);
                        insertStmt.setString(6, country);
                        insertStmt.setString(7, customerId);
                        insertStmt.executeUpdate();
                        
                        // Link new address to payment card
                        PreparedStatement linkStmt = conn.prepareStatement(
                            "UPDATE PaymentCards SET billing_address_id = ? WHERE card_id = ?"
                        );
                        linkStmt.setString(1, newAddressId);
                        linkStmt.setString(2, cardId);
                        linkStmt.executeUpdate();
                    }
                } else {
                    // No card yet - just create a standalone billing address
                    // When they add a card later, we'll link it automatically
                    String newAddressId = UUID.randomUUID().toString();
                    PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO Addresses (address_id, street, city, state, postal_code, country, customer_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)"
                    );
                    insertStmt.setString(1, newAddressId);
                    insertStmt.setString(2, street);
                    insertStmt.setString(3, city);
                    insertStmt.setString(4, state);
                    insertStmt.setString(5, postalCode);
                    insertStmt.setString(6, country);
                    insertStmt.setString(7, customerId);
                    insertStmt.executeUpdate();
                    
                    System.out.println("Created standalone billing address (no card to link yet)");
                }
                
                return true;
                
            } catch (SQLException e) {
                System.err.println("Error saving billing address: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            // For shipping, use regular saveCustomerAddress
            return saveCustomerAddress(customerId, street, city, state, postalCode, country);
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
            System.out.println("DEBUG: getCustomerPaymentCards called with customerId = " + customerId);
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
            
            System.out.println("DEBUG: Found " + cards.size() + " cards for customer " + customerId);
            
        } catch (SQLException e) {
            System.err.println("Error getting payment cards: " + e.getMessage());
            e.printStackTrace();
        }
        
        return cards;
    }
    
    /**
     * Add a payment card for a customer. Maximum 4 cards allowed.
     * Automatically links any standalone billing address to the new card.
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
            System.out.println("DEBUG: customerId = " + customerId);
            System.out.println("DEBUG: existing cards count = " + existingCards.size());
            if (existingCards.size() >= 4) {
                System.err.println("Customer already has 4 payment cards (maximum allowed)");
                return null;
            }
            
            // If no billing address provided, try to find standalone billing address
            if (billingAddressId == null) {
                Address standaloneBilling = getCustomerAddressByType(customerId, "billing");
                if (standaloneBilling != null) {
                    billingAddressId = standaloneBilling.getAddressId();
                    System.out.println("DEBUG: Auto-linking standalone billing address: " + billingAddressId);
                }
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
            
            System.out.println("DEBUG: Payment card added successfully with ID: " + cardId);
            
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

    public static void setLoginStatus(boolean loginStatus, String email) {
        Connection conn = ConnectToDatabase.conn;

        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE Users SET login_status = ? WHERE email = ?");
            stmt.setBoolean(1, loginStatus);
            stmt.setString(2, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error setting login status: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
