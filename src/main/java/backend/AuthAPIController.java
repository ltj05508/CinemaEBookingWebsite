package backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller for authentication endpoints.
 * Handles registration, login, logout, email verification, and password reset.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthAPIController {
    
    @Autowired
    private UserFunctions userFunctions;
    
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
            if (firstName == null || lastName == null || email == null || password == null || marketingOptIn == null) {
                response.put("success", false);
                response.put("message", "Missing required fields");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Register user
            String verificationCode = userFunctions.registerUser(firstName, lastName, email, password, Boolean.parseBoolean(marketingOptIn));
            
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
            response.put("loggedIn", true);
            response.put("user", Map.of(
                "userId", session.getAttribute("userId"),
                "firstName", session.getAttribute("firstName"),
                "lastName", session.getAttribute("lastName"),
                "email", session.getAttribute("email"),
                "role", session.getAttribute("role")
            ));
        } else {
            response.put("loggedIn", false);
        }
        
        return ResponseEntity.ok(response);
    }
}
