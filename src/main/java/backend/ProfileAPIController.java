package backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for user profile management.
 * Handles profile updates, address management, and payment card management.
 */
@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ProfileAPIController {
    
    @Autowired
    private UserFunctions userFunctions;
    
    /**
     * Get user profile information.
     * GET /api/profile
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfile(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if logged in
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                response.put("success", false);
                response.put("message", "Not logged in");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String email = (String) session.getAttribute("email");
            String userId = (String) session.getAttribute("userId");
            
            // Get user from database
            User user = UserDBFunctions.findUserByEmail(email);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Get address
            Address address = userFunctions.getAddress(userId);
            
            // Get payment cards
            List<PaymentCard> cards = userFunctions.getPaymentCards(userId);
            
            response.put("success", true);
            response.put("user", Map.of(
                "userId", user.getUserId(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "role", session.getAttribute("role")
            ));
            response.put("address", address);
            response.put("paymentCards", cards);
            response.put("cardCount", cards.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Update user profile (name and/or password).
     * PUT /api/profile
     * Body: { "firstName": "John", "lastName": "Doe", "currentPassword": "old", "newPassword": "new" }
     * Note: email cannot be changed
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, String> request,
                                                             HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if logged in
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                response.put("success", false);
                response.put("message", "Not logged in");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String email = (String) session.getAttribute("email");
            String firstName = request.get("firstName");
            String lastName = request.get("lastName");
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String marketingOptInStr = request.get("marketingOptIn");

            boolean marketingOptIn = "true".equalsIgnoreCase(marketingOptInStr);
            
            // Update profile
            boolean updated = userFunctions.updateProfile(email, firstName, lastName, 
                                                         currentPassword, newPassword, marketingOptIn);
            
            if (!updated) {
                response.put("success", false);
                response.put("message", "Profile update failed. Check current password or field values.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Update session if name changed
            if (firstName != null) session.setAttribute("firstName", firstName);
            if (lastName != null) session.setAttribute("lastName", lastName);
            
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get customer's address.
     * GET /api/profile/address
     */
    @GetMapping("/address")
    public ResponseEntity<Map<String, Object>> getAddress(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if logged in
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                response.put("success", false);
                response.put("message", "Not logged in");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String userId = (String) session.getAttribute("userId");
            Address address = userFunctions.getAddress(userId);
            
            response.put("success", true);
            response.put("address", address);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Update customer's address (only ONE address allowed).
     * PUT /api/profile/address
     * Body: { "street": "123 Main St", "city": "Atlanta", "state": "GA", "postalCode": "30301", "country": "USA" }
     */
    @PutMapping("/address")
    public ResponseEntity<Map<String, Object>> updateAddress(@RequestBody Map<String, String> request,
                                                             HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if logged in
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                response.put("success", false);
                response.put("message", "Not logged in");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String userId = (String) session.getAttribute("userId");
            String street = request.get("street");
            String city = request.get("city");
            String state = request.get("state");
            String postalCode = request.get("postalCode");
            String country = request.get("country");
            
            // Validate inputs
            if (street == null || city == null || state == null || postalCode == null || country == null) {
                response.put("success", false);
                response.put("message", "All address fields are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Save address
            boolean saved = userFunctions.saveAddress(userId, street, city, state, postalCode, country);
            
            if (!saved) {
                response.put("success", false);
                response.put("message", "Failed to save address");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("message", "Address saved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get customer's billing address.
     * GET /api/profile/billing-address
     */
    @GetMapping("/billing-address")
    public ResponseEntity<Map<String, Object>> getBillingAddress(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if logged in
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                response.put("success", false);
                response.put("message", "Not logged in");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String userId = (String) session.getAttribute("userId");
            Address billingAddress = userFunctions.getBillingAddress(userId);
            
            response.put("success", true);
            response.put("address", billingAddress);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Update customer's billing address.
     * PUT /api/profile/billing-address
     * Body: { "street": "123 Main St", "city": "Atlanta", "state": "GA", "postalCode": "30301", "country": "USA" }
     */
    @PutMapping("/billing-address")
    public ResponseEntity<Map<String, Object>> updateBillingAddress(@RequestBody Map<String, String> request,
                                                                     HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if logged in
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                response.put("success", false);
                response.put("message", "Not logged in");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String userId = (String) session.getAttribute("userId");
            String street = request.get("street");
            String city = request.get("city");
            String state = request.get("state");
            String postalCode = request.get("postalCode");
            String country = request.get("country");
            
            // Validate inputs
            if (street == null || city == null || state == null || postalCode == null || country == null) {
                response.put("success", false);
                response.put("message", "All address fields are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Save billing address
            boolean saved = userFunctions.saveBillingAddress(userId, street, city, state, postalCode, country);
            
            if (!saved) {
                response.put("success", false);
                response.put("message", "Failed to save billing address");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("message", "Billing address saved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get all payment cards for customer.
     * GET /api/profile/cards
     */
    @GetMapping("/cards")
    public ResponseEntity<Map<String, Object>> getCards(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if logged in
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                response.put("success", false);
                response.put("message", "Not logged in");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String userId = (String) session.getAttribute("userId");
            List<PaymentCard> cards = userFunctions.getPaymentCards(userId);
            
            response.put("success", true);
            response.put("cards", cards);
            response.put("count", cards.size());
            response.put("canAddMore", cards.size() < 4);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Add a payment card (max 4 cards).
     * POST /api/profile/cards
     * Body: { "cardNumber": "1234567890123456", "expirationDate": "2025-12-31", "billingAddressId": "addr-id" }
     */
    @PostMapping("/cards")
    public ResponseEntity<Map<String, Object>> addCard(@RequestBody Map<String, String> request,
                                                       HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if logged in
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                response.put("success", false);
                response.put("message", "Not logged in");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String userId = (String) session.getAttribute("userId");
            String cardNumber = request.get("cardNumber");
            String expirationDateStr = request.get("expirationDate");
            String billingAddressId = request.get("billingAddressId");
            
            // Validate inputs
            if (cardNumber == null || expirationDateStr == null) {
                response.put("success", false);
                response.put("message", "Card number and expiration date are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Parse date
            Date expirationDate = Date.valueOf(expirationDateStr);
            
            // Add card (encrypted)
            String cardId = userFunctions.addPaymentCard(userId, cardNumber, expirationDate, billingAddressId);
            
            if (cardId == null) {
                response.put("success", false);
                response.put("message", "Failed to add card. You may have reached the 4-card limit.");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("message", "Payment card added successfully");
            response.put("cardId", cardId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Delete a payment card.
     * DELETE /api/profile/cards/{cardId}
     */
    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Map<String, Object>> deleteCard(@PathVariable String cardId,
                                                          HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if logged in
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                response.put("success", false);
                response.put("message", "Not logged in");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String userId = (String) session.getAttribute("userId");
            
            // Delete card
            boolean deleted = userFunctions.deletePaymentCard(cardId, userId);
            
            if (!deleted) {
                response.put("success", false);
                response.put("message", "Failed to delete card");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("message", "Payment card deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
