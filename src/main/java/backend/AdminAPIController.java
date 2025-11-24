package backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for admin endpoints.
 * Handles movie management, showtime scheduling, promotions, and user management.
 * All endpoints require admin authentication.
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3002"}, allowCredentials = "true")
public class AdminAPIController {

    @Autowired
    private EmailService emailService;

    /**
     * Helper method to check if user is logged in as admin.
     */
    private ResponseEntity<Map<String, Object>> checkAdminAuth(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        if (loggedIn == null || !loggedIn) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equalsIgnoreCase(role)) {
            response.put("success", false);
            response.put("message", "Unauthorized - Admin access required");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        return null; // Auth successful
    }

    // ==================== MOVIE MANAGEMENT ====================

    /**
     * Add a new movie.
     * POST /api/admin/movies
     * Body: { "title": "Movie Title", "genre": "Action", "rating": "PG-13", 
     *         "description": "...", "durationMinutes": 120, 
     *         "posterUrl": "...", "trailerUrl": "...", "currentlyShowing": true }
     */
    @PostMapping("/movies")
    public ResponseEntity<Map<String, Object>> addMovie(@RequestBody Map<String, Object> request, 
                                                         HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Check admin auth
        ResponseEntity<Map<String, Object>> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        try {
            // Extract and validate movie data
            String title = (String) request.get("title");
            String genre = (String) request.get("genre");
            String rating = (String) request.get("rating");
            String description = (String) request.get("description");
            Integer durationMinutes = (Integer) request.get("durationMinutes");
            String posterUrl = (String) request.get("posterUrl");
            String trailerUrl = (String) request.get("trailerUrl");
            Boolean currentlyShowing = (Boolean) request.get("currentlyShowing");
            
            // Validate required fields
            if (title == null || title.trim().isEmpty() ||
                genre == null || genre.trim().isEmpty() ||
                rating == null || rating.trim().isEmpty() ||
                description == null || description.trim().isEmpty() ||
                durationMinutes == null || durationMinutes <= 0) {
                response.put("success", false);
                response.put("message", "Missing required fields");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Set defaults
            if (currentlyShowing == null) currentlyShowing = false;
            
            // Call database function to insert movie
            int movieId = MovieDBFunctions.addMovie(title, genre, rating, description, 
                                                    durationMinutes, posterUrl, trailerUrl, currentlyShowing);
            
            if (movieId > 0) {
                response.put("success", true);
                response.put("message", "Movie added successfully");
                response.put("movieId", movieId);
            } else {
                response.put("success", false);
                response.put("message", "Failed to add movie");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== SHOWTIME MANAGEMENT ====================

    /**
     * Get all showrooms.
     * GET /api/admin/showrooms
     */
    @GetMapping("/showrooms")
    public ResponseEntity<Map<String, Object>> getShowrooms(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Check admin auth

        ResponseEntity<Map<String, Object>> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;


        
        try {
            // Call database function to get showrooms
            List<Map<String, Object>> showrooms = ShowtimeDBFunctions.getAllShowrooms();
            
            response.put("success", true);
            response.put("showrooms", showrooms);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Add a showtime for a movie.
     * POST /api/admin/showtimes
     * Body: { "movieId": 1, "showroomId": "1", "showtime": "19:30:00" }
     */
    @PostMapping("/showtimes")
    public ResponseEntity<Map<String, Object>> addShowtime(@RequestBody Map<String, Object> request,
                                                           HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Check admin auth
        ResponseEntity<Map<String, Object>> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        try {
            // Extract showtime data
            Integer movieId = (Integer) request.get("movieId");
            String showroomId = (String) request.get("showroomId");
            String showtime = (String) request.get("showtime");
            
            // Validate required fields
            if (movieId == null || 
                showroomId == null || showroomId.trim().isEmpty() ||
                showtime == null || showtime.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Missing required fields (movieId, showroomId, showtime)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check for showtime conflicts (same showroom, time)
            boolean hasConflict = ShowtimeDBFunctions.checkConflict(showroomId, showtime);
            if (hasConflict) {
                response.put("success", false);
                response.put("message", "Showtime conflict: This showroom is already booked at this time");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            // Call database function to add showtime
            int showtimeId = ShowtimeDBFunctions.addShowtime(movieId, showroomId, showtime);
            
            if (showtimeId > 0) {
                response.put("success", true);
                response.put("message", "Showtime added successfully");
                response.put("showtimeId", showtimeId);
            } else {
                response.put("success", false);
                response.put("message", "Failed to add showtime");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all showtimes for a movie.
     * GET /api/admin/showtimes/{movieId}
     */
    @GetMapping("/showtimes/{movieId}")
    public ResponseEntity<Map<String, Object>> getShowtimes(@PathVariable int movieId,
                                                            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Check admin auth
        ResponseEntity<Map<String, Object>> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        try {
            // Call database function to get showtimes
            List<Map<String, Object>> showtimes = ShowtimeDBFunctions.getShowtimesByMovie(movieId);
            
            response.put("success", true);
            response.put("showtimes", showtimes);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== PROMOTION MANAGEMENT ====================

    /**
     * Create a promotion.
     * POST /api/admin/promotions
     * Body: { "code": "SAVE20", "description": "Save 20%!", "discountPercent": 20.0, 
     *         "validFrom": "2025-11-01", "validTo": "2025-12-31" }
     */
    @PostMapping("/promotions")
    public ResponseEntity<Map<String, Object>> createPromotion(@RequestBody Map<String, Object> request,
                                                               HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Check admin auth
        ResponseEntity<Map<String, Object>> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        try {
            // Extract promotion data
            String code = (String) request.get("code");
            String description = (String) request.get("description");
            Object discountObj = request.get("discountPercent");
            String validFrom = (String) request.get("validFrom");
            String validTo = (String) request.get("validTo");
            
            // Convert discount to double
            double discountPercent = 0.0;
            if (discountObj instanceof Integer) {
                discountPercent = ((Integer) discountObj).doubleValue();
            } else if (discountObj instanceof Double) {
                discountPercent = (Double) discountObj;
            }
            
            // Validate required fields
            if (code == null || code.trim().isEmpty() ||
                description == null || description.trim().isEmpty() ||
                discountPercent < 1 || discountPercent > 100 ||
                validFrom == null || validFrom.trim().isEmpty() ||
                validTo == null || validTo.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Invalid or missing required fields (code, description, discount %, validFrom, validTo)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Call database function to create promotion
            String promoId = PromotionDBFunctions.createPromotion(code, description, discountPercent, validFrom, validTo);
            
            if (promoId != null) {
                response.put("success", true);
                response.put("message", "Promotion created successfully");
                response.put("promoId", promoId);
            } else {
                response.put("success", false);
                response.put("message", "Failed to create promotion");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Send promotion email to subscribed users.
     * POST /api/admin/promotions/email
     * Body: { "subject": "Special Offer!", "message": "Save 20%..." }
     */
    @PostMapping("/promotions/email")
    public ResponseEntity<Map<String, Object>> sendPromotionEmail(@RequestBody Map<String, Object> request,
                                                                  HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Check admin auth
        ResponseEntity<Map<String, Object>> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        try {
            // Extract email data
            String subject = (String) request.get("subject");
            String message = (String) request.get("message");
            
            // Validate required fields
            if (subject == null || subject.trim().isEmpty() ||
                message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Missing required fields (subject, message)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Get all users who opted in for marketing
            List<Map<String, String>> subscribedUsers = UserDBFunctions.getSubscribedUsers();
            
            // Send email to each subscribed user
            int sentCount = 0;
            for (Map<String, String> user : subscribedUsers) {
                try {
                    emailService.sendPromotionEmail(
                        user.get("email"), 
                        user.get("firstName"), 
                        subject, 
                        message
                    );
                    sentCount++;
                } catch (Exception e) {
                    System.err.println("Failed to send email to " + user.get("email") + ": " + e.getMessage());
                }
            }
            
            response.put("success", true);
            response.put("message", "Promotion emails sent successfully");
            response.put("recipientCount", sentCount);
            response.put("totalSubscribers", subscribedUsers.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all promotions.
     * GET /api/admin/promotions
     */
    @GetMapping("/promotions")
    public ResponseEntity<Map<String, Object>> getPromotions(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Check admin auth
        ResponseEntity<Map<String, Object>> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        try {
            // Call database function to get promotions
            List<Map<String, Object>> promotions = PromotionDBFunctions.getAllPromotions();
            
            response.put("success", true);
            response.put("promotions", promotions);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== USER MANAGEMENT ====================

    /**
     * Get all users (with pagination support).
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers(@RequestParam(required = false, defaultValue = "0") int page,
                                                        @RequestParam(required = false, defaultValue = "50") int size,
                                                        HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Check admin auth
        ResponseEntity<Map<String, Object>> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        try {
            // TODO: Call database function to get users
            // List<User> users = UserDBFunctions.getAllUsers(page, size);
            
            response.put("success", true);
            // response.put("users", users);
            // response.put("page", page);
            // response.put("size", size);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Suspend or activate a user account.
     * PUT /api/admin/users/{userId}/status
     * Body: { "suspended": true/false }
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(@PathVariable String userId,
                                                                @RequestBody Map<String, Object> request,
                                                                HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Check admin auth
        ResponseEntity<Map<String, Object>> authCheck = checkAdminAuth(session);
        if (authCheck != null) return authCheck;
        
        try {
            Boolean suspended = (Boolean) request.get("suspended");
            
            if (suspended == null) {
                response.put("success", false);
                response.put("message", "Missing 'suspended' field");
                return ResponseEntity.badRequest().body(response);
            }
            
            // TODO: Call database function to update user status
            // boolean updated = UserDBFunctions.updateUserStatus(userId, suspended);
            
            response.put("success", true);
            response.put("message", suspended ? "User suspended" : "User activated");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
