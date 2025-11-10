package backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.servlet.http.HttpSession;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller for authentication endpoints.
 * Handles registration, login, logout, email verification, and password reset.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthAPIController {
    
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Autowired
    private UserFunctions userFunctions;

    @GetMapping("/getUserInfo/{email}")
    public ResponseEntity<String> getUserInfo(@PathVariable String email) {
        try {
            User myUser = UserDBFunctions.findUserByEmail(email);
            if (myUser != null) {
                return ResponseEntity.ok(myUser.toJson());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\":\"Failed to retrieve movie: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/updateUser")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestBody Map<String, String> request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Check if logged in
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                response.put("success", false);
                response.put("message", "Not logged in");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Get email from session (don't trust client-provided email)
            String email = (String) session.getAttribute("email");
            String firstName = request.get("firstName");
            String lastName = request.get("lastName");
            String marketingOptInStr = request.get("marketingOptIn");

            // Validate inputs
            if (firstName == null || lastName == null) {
                response.put("success", false);
                response.put("message", "Missing required fields");
                return ResponseEntity.badRequest().body(response);
            }

            // Parse marketingOptIn (default to false if not provided)
            boolean marketingOptIn = "true".equalsIgnoreCase(marketingOptInStr);

            // Update profile in database using UserDBFunctions
            //boolean updated = UserDBFunctions.updateProfile(email, firstName, lastName, marketingOptIn);

            boolean updated = userFunctions.updateProfile(email, firstName, lastName,
                    (String) session.getAttribute("password"), (String) session.getAttribute("password"), marketingOptIn);
            
            if (!updated) {
                response.put("success", false);
                response.put("message", "Failed to update profile");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
            // Update session attributes
            session.setAttribute("firstName", firstName);
            session.setAttribute("lastName", lastName);

            response.put("success", true);
            response.put("message", "Update successful! Please check your email for verification code.");
            response.put("email", email);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Change user password.
     * POST /api/auth/changePassword
     * Body: { "currentPassword": "oldpass123", "newPassword": "newpass456" }
     */
    @PostMapping("/changePassword")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Check if logged in
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                response.put("success", false);
                response.put("message", "Not logged in");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Get email from session
            String email = (String) session.getAttribute("email");
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            // Validate inputs
            if (currentPassword == null || newPassword == null || newPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Missing required fields");
                return ResponseEntity.badRequest().body(response);
            }

            if (newPassword.length() < 8) {
                response.put("success", false);
                response.put("message", "Password must be at least 8 characters");
                return ResponseEntity.badRequest().body(response);
            }

            // Verify current password
            User user = UserDBFunctions.findUserByEmail(email);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                response.put("success", false);
                response.put("message", "Current password is incorrect");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Hash new password and update
            String hashedPassword = passwordEncoder.encode(newPassword);
            //boolean updated = UserFunctions.updateProfile(email, (String) session.getAttribute("firstName"), (String) session.getAttribute("lastName"), currentPassword, newPassword, false);
            boolean updated = userFunctions.changePassword(email, hashedPassword);
            
            if (!updated) {
                response.put("success", false);
                response.put("message", "Failed to update password");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            response.put("success", true);
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Register a new user.
     * POST /api/auth/register
     * Body: { "firstName": "John", "lastName": "Doe", "email": "john@example.com", "password": "password123" }
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String firstName = request.get("firstName");
            String lastName = request.get("lastName");
            String email = request.get("email");
            String password = request.get("password");
            String marketingOptIn = request.get("marketingOptIn");
            
            // Validate inputs
            if (firstName == null || lastName == null || email == null || password == null) {
                response.put("success", false);
                response.put("message", "Missing required fields");
                return ResponseEntity.badRequest().body(response);
            }

            // Parse marketingOptIn - handle both string ("0"/"1") and boolean (true/false)
            boolean marketingOptInValue = false;
            if (marketingOptIn != null) {
                if (marketingOptIn.equals("1") || marketingOptIn.equalsIgnoreCase("true")) {
                    marketingOptInValue = true;
                }
            }

            //userFunctions = new UserFunctions(new EmailService());
            // Register user
            String verificationCode = userFunctions.registerUser(firstName, lastName, email, password, marketingOptInValue);
            
            if (verificationCode == null) {
                response.put("success", false);
                response.put("message", "Registration failed. Email may already be registered.");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("message", "Registration successful! Please check your email for verification code.");
            response.put("email", email);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Verify email with verification code.
     * POST /api/auth/verify
     * Body: { "code": "123456" }
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String code = request.get("code");
            
            if (code == null || code.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Verification code is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verify email
            boolean verified = userFunctions.verifyEmail(code);
            
            if (!verified) {
                response.put("success", false);
                response.put("message", "Invalid or expired verification code");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("message", "Email verified successfully! You can now login.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Login a user.
     * POST /api/auth/login
     * Body: { "email": "john@example.com", "password": "password123" }
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request, 
                                                     HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            if (email == null || password == null) {
                response.put("success", false);
                response.put("message", "Email and password are required");
                return ResponseEntity.badRequest().body(response);
            }

            System.out.println("10");
            
            // Attempt login
            UserFunctions.LoginResult loginResult = userFunctions.login(email, password);
            
            if (loginResult == null || !loginResult.isSuccess()) {
                response.put("success", false);
                response.put("message", "Invalid credentials or account not activated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Store user info in session
            session.setAttribute("userId", loginResult.getUserId());
            session.setAttribute("email", loginResult.getEmail());
            session.setAttribute("firstName", loginResult.getFirstName());
            session.setAttribute("lastName", loginResult.getLastName());
            session.setAttribute("role", loginResult.getRole());
            session.setAttribute("loggedIn", true);
            
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("user", Map.of(
                "userId", loginResult.getUserId(),
                "firstName", loginResult.getFirstName(),
                "lastName", loginResult.getLastName(),
                "email", loginResult.getEmail(),
                "role", loginResult.getRole()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Logout a user.
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.err.println("In logout");
            // Invalidate session
            session.invalidate();
            
            response.put("success", true);
            response.put("message", "Logout successful");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Request password reset - sends reset email.
     * POST /api/auth/forgot-password
     * Body: { "email": "john@example.com" }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Request password reset
            boolean sent = userFunctions.requestPasswordReset(email);
            
            if (!sent) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("message", "Password reset email sent. Please check your email.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Reset password with token.
     * POST /api/auth/reset-password
     * Body: { "token": "abc-123-def-456", "newPassword": "newpassword123" }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            
            if (token == null || newPassword == null) {
                response.put("success", false);
                response.put("message", "Token and new password are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Reset password
            boolean reset = userFunctions.resetPassword(token, newPassword);
            
            if (!reset) {
                response.put("success", false);
                response.put("message", "Invalid or expired reset token, or password too short (min 8 characters)");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("message", "Password reset successful! You can now login with your new password.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Check if user is logged in (for frontend).
     * GET /api/auth/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        
        if (loggedIn != null && loggedIn) {
            String email = (String) session.getAttribute("email");
            
            // Fetch the full user from the database to get marketingOptIn
            User user = UserDBFunctions.findUserByEmail(email);
            
            response.put("loggedIn", true);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", session.getAttribute("userId"));
            userInfo.put("firstName", session.getAttribute("firstName"));
            userInfo.put("lastName", session.getAttribute("lastName"));
            userInfo.put("email", session.getAttribute("email"));
            userInfo.put("role", session.getAttribute("role"));
            
            // Add marketingOptIn from database
            if (user != null) {
                userInfo.put("marketingOptIn", user.getMarketingOptIn());
            } else {
                userInfo.put("marketingOptIn", false);
            }
            
            response.put("user", userInfo);
        } else {
            response.put("loggedIn", false);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get the current user's addresses.
     * GET /api/auth/addresses
     */
    @GetMapping("/addresses")
    public ResponseEntity<Map<String, Object>> getAddresses(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        if (loggedIn == null || !loggedIn) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String userId = (String) session.getAttribute("userId");
        try {
            Address addr = UserDBFunctions.getCustomerAddress(userId);
            if (addr == null) {
                response.put("success", true);
                response.put("addresses", new java.util.ArrayList<>());
            } else {
                response.put("success", true);
                response.put("addresses", java.util.List.of(addr));
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get the current user's payment cards (masked).
     * GET /api/auth/payment-cards
     */
    @GetMapping("/payment-cards")
    public ResponseEntity<Map<String, Object>> getPaymentCards(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        if (loggedIn == null || !loggedIn) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String userId = (String) session.getAttribute("userId");
        try {
            java.util.List<PaymentCard> cards = UserDBFunctions.getCustomerPaymentCards(userId);
            java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
            for (PaymentCard c : cards) {
                String num = c.getCardNumber() == null ? "" : c.getCardNumber();
                String last4 = num.length() >= 4 ? num.substring(num.length() - 4) : num;
                String masked = "**** **** **** " + last4;
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                //m.put("cardId", c.getCardId());
                //m.put("cardNumber", num);
                m.put("last4", last4);
                //m.put("masked", masked);
                m.put("expirationMonth", c.getExpirationDate() != null ? c.getExpirationDate().toString() : null);
                //m.put("billingAddressId", c.getBillingAddressId());
                out.add(m);
            }

            response.put("success", true);
            response.put("cards", out);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
