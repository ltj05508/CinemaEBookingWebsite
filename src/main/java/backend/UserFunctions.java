package backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles registration, login, password reset, edit profile, etc.
 */
@Service
public class UserFunctions {
    
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final EmailService emailService;
    
    public UserFunctions(EmailService emailService) {
        this.emailService = emailService;
    }
    
    /**
     * Register a new user with email verification.
     * @param firstName User's first name
     * @param lastName User's last name
     * @param email User's email address
     * @param password Plain text password (will be hashed)
     * @return Verification code if successful, null if error
     */
    public String registerUser(String firstName, String lastName, String email, String password, boolean marketingOptIn) {
        System.out.println("=== Starting user registration ===");
        System.out.println("Email: " + email);
        
        // Validate inputs
        if (firstName == null || firstName.trim().isEmpty() ||
            lastName == null || lastName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.length() < 8) {
            System.err.println("❌ Invalid registration data");
            return null;
        }
        System.out.println("✅ Input validation passed");
        
        // Check if email already exists
        User existingUser = UserDBFunctions.findUserByEmail(email);
        if (existingUser != null) {
            System.err.println("❌ Email already registered");
            return null;
        }
        System.out.println("✅ Email is unique");
        
        // Hash password
        String hashedPassword = passwordEncoder.encode(password);
        System.out.println("✅ Password hashed");
        
        // Create user in database (state = Inactive)
        String userId = UserDBFunctions.createUser(firstName, lastName, email, hashedPassword, marketingOptIn);
        if (userId == null) {
            System.err.println("❌ Failed to create user in database");
            return null;
        }
        System.out.println("✅ User created in database with ID: " + userId);
        
        // Generate verification code
        String verificationCode = CodeManager.generateVerificationCode(email);
        System.out.println("✅ Verification code generated: " + verificationCode);
        
        // Send verification email
        try {
            emailService.sendVerificationCodeEmail(email, firstName, verificationCode);
            System.out.println("✅ Verification email sent successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to send verification email: " + e.getMessage());
            e.printStackTrace();
            // Still return the code so user can verify manually if needed
        }
        
        return verificationCode;
    }
    
    /**
     * Verify user's email with verification code.
     * @param code 6-digit verification code
     * @return true if successful, false otherwise
     */
    public boolean verifyEmail(String code) {
        // Validate code and get email
        String email = CodeManager.validateVerificationCode(code);
        if (email == null) {
            System.err.println("Invalid or expired verification code");
            return false;
        }
        
        // Activate customer account
        boolean activated = UserDBFunctions.activateCustomer(email);
        if (!activated) {
            System.err.println("Failed to activate customer account");
            return false;
        }
        
        return true;
    }
    
    /**
     * Login a user with email and password.
     * @param email User's email
     * @param password Plain text password
     * @return LoginResult with user info and role, null if login failed
     */
    public LoginResult login(String email, String password) {
        // Find user
        User user = UserDBFunctions.findUserByEmail(email);
        if (user == null) {
            System.err.println("User not found");
            return null;
        }
        
        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            System.err.println("Invalid password");
            return null;
        }
        
        // Check if customer is active
        if (!UserDBFunctions.isCustomerActive(user.getUserId())) {
            System.err.println("Account not activated. Please verify your email.");
            return null;
        }
        
        // Check if admin
        boolean isAdmin = UserDBFunctions.isAdmin(user.getUserId());
        String role = isAdmin ? "ADMIN" : "CUSTOMER";
        
        // Return login result
        return new LoginResult(user.getUserId(), user.getFirstName(), user.getLastName(), 
                              user.getEmail(), role, true);
    }
    
    /**
     * Request password reset - generates token and sends email.
     * @param email User's email
     * @return true if email sent, false otherwise
     */
    public boolean requestPasswordReset(String email) {
        // Check if user exists
        User user = UserDBFunctions.findUserByEmail(email);
        if (user == null) {
            System.err.println("User not found");
            return false;
        }
        
        // Generate reset token
        String resetToken = CodeManager.generateResetToken(email);
        
        // Send reset email
        emailService.sendPasswordResetEmail(email, user.getFirstName(), resetToken);
        
        return true;
    }
    
    /**
     * Reset password using reset token.
     * @param token Reset token
     * @param newPassword New plain text password
     * @return true if successful, false otherwise
     */
    public boolean resetPassword(String token, String newPassword) {
        // Validate password
        if (newPassword == null || newPassword.length() < 8) {
            System.err.println("Password must be at least 8 characters");
            return false;
        }
        
        // Validate token and get email
        String email = CodeManager.validateResetToken(token);
        if (email == null) {
            System.err.println("Invalid or expired reset token");
            return false;
        }
        
        // Hash new password
        String hashedPassword = passwordEncoder.encode(newPassword);
        
        // Update password in database
        boolean updated = UserDBFunctions.updatePassword(email, hashedPassword);
        if (!updated) {
            System.err.println("Failed to update password");
            return false;
        }
        
        return true;
    }
    
    /**
     * Update user profile (name and/or password).
     * @param email User's email
     * @param firstName New first name (can be null to keep unchanged)
     * @param lastName New last name (can be null to keep unchanged)
     * @param currentPassword Current password (required if changing password)
     * @param newPassword New password (null if not changing)
     * @return true if successful, false otherwise
     */
    public boolean updateProfile(String email, String firstName, String lastName, 
                                 String currentPassword, String newPassword) {
        // Find user
        User user = UserDBFunctions.findUserByEmail(email);
        if (user == null) {
            System.err.println("User not found");
            return false;
        }
        
        // If changing password, verify current password
        if (newPassword != null && !newPassword.isEmpty()) {
            if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
                System.err.println("Current password is incorrect");
                return false;
            }
            
            if (newPassword.length() < 8) {
                System.err.println("New password must be at least 8 characters");
                return false;
            }
            
            // Hash and update password
            String hashedPassword = passwordEncoder.encode(newPassword);
            boolean passwordUpdated = UserDBFunctions.updatePassword(email, hashedPassword);
            if (!passwordUpdated) {
                System.err.println("Failed to update password");
                return false;
            }
            
            // Send notification
            emailService.sendProfileChangeNotification(email, user.getFirstName(), 
                "Your password has been changed.");
        }
        
        // Update name if provided
        if (firstName != null && lastName != null) {
            boolean profileUpdated = UserDBFunctions.updateProfile(email, firstName, lastName);
            if (!profileUpdated) {
                System.err.println("Failed to update profile");
                return false;
            }
            
            // Send notification if name changed
            if (!firstName.equals(user.getFirstName()) || !lastName.equals(user.getLastName())) {
                emailService.sendProfileChangeNotification(email, firstName, 
                    "Your name has been updated to " + firstName + " " + lastName);
            }
        }
        
        return true;
    }
    
    /**
     * Add a payment card for a customer (encrypted).
     * @param customerId Customer's ID
     * @param cardNumber Plain card number (will be encrypted)
     * @param expirationDate Expiration date
     * @param billingAddressId Billing address ID (can be null)
     * @return Card ID if successful, null otherwise
     */
    public String addPaymentCard(String customerId, String cardNumber, 
                                 java.sql.Date expirationDate, String billingAddressId) {
        try {
            // Encrypt card number
            String encryptedCardNumber = EncryptionUtil.encrypt(cardNumber);
            
            // Add to database (max 4 cards enforced in UserDBFunctions)
            String cardId = UserDBFunctions.addPaymentCard(customerId, encryptedCardNumber, 
                                                          expirationDate, billingAddressId);
            
            if (cardId == null) {
                System.err.println("Failed to add payment card (may have reached 4-card limit)");
                return null;
            }
            
            return cardId;
            
        } catch (Exception e) {
            System.err.println("Error encrypting card number: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get decrypted payment cards for a customer.
     * @param customerId Customer's ID
     * @return List of PaymentCard objects with decrypted card numbers
     */
    public java.util.List<PaymentCard> getPaymentCards(String customerId) {
        java.util.List<PaymentCard> cards = UserDBFunctions.getCustomerPaymentCards(customerId);
        
        // Decrypt card numbers
        for (PaymentCard card : cards) {
            try {
                String decryptedNumber = EncryptionUtil.decrypt(card.getCardNumber());
                card.setCardNumber(decryptedNumber);
            } catch (Exception e) {
                System.err.println("Error decrypting card: " + e.getMessage());
                // Keep encrypted value if decryption fails
            }
        }
        
        return cards;
    }
    
    /**
     * Delete a payment card.
     * @param cardId Card ID
     * @param customerId Customer's ID (for security)
     * @return true if successful, false otherwise
     */
    public boolean deletePaymentCard(String cardId, String customerId) {
        return UserDBFunctions.deletePaymentCard(cardId, customerId);
    }
    
    /**
     * Save customer's address (only one address allowed).
     * @param customerId Customer's ID
     * @param street Street address
     * @param city City
     * @param state State
     * @param postalCode Postal code
     * @param country Country
     * @return true if successful, false otherwise
     */
    public boolean saveAddress(String customerId, String street, String city, 
                               String state, String postalCode, String country) {
        return UserDBFunctions.saveCustomerAddress(customerId, street, city, state, postalCode, country);
    }
    
    /**
     * Get customer's address.
     * @param customerId Customer's ID
     * @return Address object or null if not found
     */
    public Address getAddress(String customerId) {
        return UserDBFunctions.getCustomerAddress(customerId);
    }
    
    /**
     * Inner class to hold login result data.
     */
    public static class LoginResult {
        private String userId;
        private String firstName;
        private String lastName;
        private String email;
        private String role; // "ADMIN" or "CUSTOMER"
        private boolean success;
        
        public LoginResult(String userId, String firstName, String lastName, 
                          String email, String role, boolean success) {
            this.userId = userId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.role = role;
            this.success = success;
        }
        
        public String getUserId() { return userId; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public boolean isSuccess() { return success; }
    }
}
