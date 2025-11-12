package backend;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller for admin endpoints.
 * Handles movie management, showtime scheduling, promotions, and user management.
 * All endpoints require admin authentication.
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AdminAPIController {

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
     *         "movieDescription": "...", "duration": "120 min", 
     *         "posterUrl": "...", "trailerUrl": "...", "isCurrentlyShowing": true }
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
            String description = (String) request.get("movieDescription");
            String duration = (String) request.get("duration");
            String posterUrl = (String) request.get("posterUrl");
            String trailerUrl = (String) request.get("trailerUrl");
            Boolean isCurrentlyShowing = (Boolean) request.get("isCurrentlyShowing");
            
            // Validate required fields
            if (title == null || title.trim().isEmpty() ||
                genre == null || genre.trim().isEmpty() ||
                rating == null || rating.trim().isEmpty() ||
                description == null || description.trim().isEmpty() ||
                duration == null || duration.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Missing required fields");
                return ResponseEntity.badRequest().body(response);
            }
            
            // TODO: Call database function to insert movie
            // int movieId = MovieDBFunctions.addMovie(title, genre, rating, description, 
            //                                          duration, posterUrl, trailerUrl, isCurrentlyShowing);
            
            response.put("success", true);
            response.put("message", "Movie added successfully");
            // response.put("movieId", movieId);
            
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
            // TODO: Call database function to get showrooms
            // List<Showroom> showrooms = ShowtimeDBFunctions.getAllShowrooms();
            
            response.put("success", true);
            // response.put("showrooms", showrooms);
            
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
     * Body: { "movieId": 1, "showroomId": 1, "showDate": "2025-11-15", "showTime": "19:30" }
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
            Integer showroomId = (Integer) request.get("showroomId");
            String showDate = (String) request.get("showDate");
            String showTime = (String) request.get("showTime");
            
            // Validate required fields
            if (movieId == null || showroomId == null || 
                showDate == null || showDate.trim().isEmpty() ||
                showTime == null || showTime.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Missing required fields");
                return ResponseEntity.badRequest().body(response);
            }
            
            // TODO: Check for showtime conflicts (same showroom, date, time)
            // boolean hasConflict = ShowtimeDBFunctions.checkConflict(showroomId, showDate, showTime);
            // if (hasConflict) {
            //     response.put("success", false);
            //     response.put("message", "Showtime conflict: This showroom is already booked at this date/time");
            //     return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            // }
            
            // TODO: Call database function to add showtime
            // int showtimeId = ShowtimeDBFunctions.addShowtime(movieId, showroomId, showDate, showTime);
            
            response.put("success", true);
            response.put("message", "Showtime added successfully");
            // response.put("showtimeId", showtimeId);
            
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
            // TODO: Call database function to get showtimes
            // List<Showtime> showtimes = ShowtimeDBFunctions.getShowtimesByMovie(movieId);
            
            response.put("success", true);
            // response.put("showtimes", showtimes);
            
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
     * Body: { "promoCode": "SAVE20", "discountPercent": 20, "startDate": "2025-11-01", 
     *         "endDate": "2025-12-31" }
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
            String promoCode = (String) request.get("promoCode");
            Integer discountPercent = (Integer) request.get("discountPercent");
            String startDate = (String) request.get("startDate");
            String endDate = (String) request.get("endDate");
            
            // Validate required fields
            if (promoCode == null || promoCode.trim().isEmpty() ||
                discountPercent == null || discountPercent < 1 || discountPercent > 100 ||
                startDate == null || startDate.trim().isEmpty() ||
                endDate == null || endDate.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Invalid or missing required fields (promo code, discount %, start/end date)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // TODO: Call database function to create promotion
            // int promoId = PromotionDBFunctions.createPromotion(promoCode, discountPercent, startDate, endDate);
            
            response.put("success", true);
            response.put("message", "Promotion created successfully");
            // response.put("promoId", promoId);
            
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
     * Body: { "promoId": 1, "subject": "Special Offer!", "message": "Save 20%..." }
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
            Integer promoId = (Integer) request.get("promoId");
            String subject = (String) request.get("subject");
            String message = (String) request.get("message");
            
            // Validate required fields
            if (promoId == null || 
                subject == null || subject.trim().isEmpty() ||
                message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Missing required fields");
                return ResponseEntity.badRequest().body(response);
            }
            
            // TODO: Get all users who opted in for marketing
            // List<User> subscribedUsers = UserDBFunctions.getSubscribedUsers();
            
            // TODO: Send email to each subscribed user
            // for (User user : subscribedUsers) {
            //     EmailService.sendPromotionEmail(user.getEmail(), user.getFirstName(), subject, message);
            // }
            
            response.put("success", true);
            response.put("message", "Promotion emails sent successfully");
            // response.put("recipientCount", subscribedUsers.size());
            
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
            // TODO: Call database function to get promotions
            // List<Promotion> promotions = PromotionDBFunctions.getAllPromotions();
            
            response.put("success", true);
            // response.put("promotions", promotions);
            
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
