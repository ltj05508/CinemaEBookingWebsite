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
 * REST API Controller for booking endpoints.
 *
 *
 */
@RestController
//@RequestMapping("/api/booking")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class BookingAPIController {
    //@Autowired
    private BookingFunctions bookingFunctions;

    @GetMapping("/api/booking/test")
    public void testingMethod() {
        System.out.println("\nTest called!\n");
    }


    /**
     * Returns the seats (available/unavailable) for a selected showtime.
     * GET /api/booking/seats/{id}/{showtime}
     * Path variables: id (movie ID), showtime (formatted time like "2:00 PM")
     */
    @GetMapping("/seats/{id}/{showtime}")
    public ResponseEntity<Map<String, Object>> getSeats(
            @PathVariable String id,
            @PathVariable String showtime) {
        Map<String, Object> response = new HashMap<>();
        bookingFunctions = new BookingFunctions();

        try {
            System.out.println("\nIn BookingAPIController\n");
            Showroom showroom = bookingFunctions.getSeatsForShow(id, showtime);
            response.put("success", true);
            response.put("showroom", showroom);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get seat availability for a specific showtime.
     * Returns list of already-booked seat IDs.
     * GET /api/booking/availability/{showtimeId}
     */
    @GetMapping("/availability/{id}/{showtime}")
    public ResponseEntity<Map<String, Object>> getSeatAvailability(
            @PathVariable String id,
            @PathVariable String showtime) {
        Map<String, Object> response = new HashMap<>();
        bookingFunctions = new BookingFunctions();
        
        try {
            List<String> bookedSeats = bookingFunctions.getBookedSeats(id, showtime);
            
            response.put("success", true);
            response.put("bookedSeats", bookedSeats);
            response.put("movieId", id);
            response.put("showtime", showtime);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get ticket pricing information.
     * GET /api/booking/prices
     */
    @GetMapping("/prices")
    public ResponseEntity<Map<String, Object>> getTicketPrices() {
        Map<String, Object> response = new HashMap<>();
        bookingFunctions = new BookingFunctions();
        
        try {
            Map<String, Double> prices = new HashMap<>();
            prices.put("adult", 12.00);
            prices.put("senior", 10.00);
            prices.put("child", 8.00);
            
            response.put("success", true);
            response.put("prices", prices);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== BOOKING MANAGEMENT ====================

    /**
     * Create a new booking with tickets.
     * POST /api/booking/create
     * Body: {
     *   "movieId": 1,
     *   "showtimeId": 1,
     *   "seats": ["A1", "A2"],
     *   "tickets": [
     *     {"seatId": "A1", "type": "adult"},
     *     {"seatId": "A2", "type": "child"}
     *   ],
     *   "promoCode": "PROMO123" (optional)
     * }
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody Map<String, Object> request,
                                                             HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        bookingFunctions = new BookingFunctions();
        
        try {
            // Check if user is logged in
            /*
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                response.put("success", false);
                response.put("message", "You must be logged in to create a booking");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
             */
            
            String userId = (String) session.getAttribute("userId");
            
            // Extract booking data
            Integer movieId = (Integer) request.get("movieId");
            Integer showtimeId = (Integer) request.get("showtimeId");
            List<Map<String, String>> tickets = (List<Map<String, String>>) request.get("tickets");
            String promoCode = (String) request.get("promoCode");
            
            // Validate required fields
            if (movieId == null || showtimeId == null || tickets == null || tickets.isEmpty()) {
                response.put("success", false);
                response.put("message", "Missing required fields (movieId, showtimeId, tickets)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create booking through service layer
            Integer bookingId = bookingFunctions.createBooking(userId, movieId, showtimeId, tickets, promoCode);
            if (bookingId == null) {
                response.put("success", false);
                response.put("message", "Failed to create booking. Seats may already be taken.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Send confirmation email
            /*
            try {
                String email = (String) session.getAttribute("email");
                String firstName = (String) session.getAttribute("firstName");
                
                if (email != null && firstName != null) {
                    bookingFunctions.sendBookingConfirmation(bookingId, email, firstName, movieId, showtimeId, tickets);
                }
            } catch (Exception e) {
                // Log error but don't fail the booking
                System.err.println("Failed to send confirmation email: " + e.getMessage());
            }
             */
            
            response.put("success", true);
            response.put("message", "Booking created successfully");
            response.put("bookingId", bookingId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get booking details by ID.
     * GET /api/booking/{bookingId}
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<Map<String, Object>> getBooking(@PathVariable Integer bookingId,
                                                          HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        bookingFunctions = new BookingFunctions();

        try {
            // Check if user is logged in
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                response.put("success", false);
                response.put("message", "You must be logged in to view bookings");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String userId = (String) session.getAttribute("userId");
            
            // Get booking details
            Map<String, Object> booking = bookingFunctions.getBookingById(bookingId, userId);
            
            if (booking == null) {
                response.put("success", false);
                response.put("message", "Booking not found or access denied");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            response.put("success", true);
            response.put("booking", booking);
            
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
        //ResponseEntity<Map<String, Object>> authCheck = null;
        //if (authCheck != null) return authCheck;

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
     * Get all showtimes for a movie.
     * GET /api/admin/showtimes/{movieId}
     */
    @GetMapping("/showtimes/{movieId}")
    public ResponseEntity<Map<String, Object>> getShowtimes(@PathVariable int movieId,
                                                            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        // Check admin auth
        //ResponseEntity<Map<String, Object>> authCheck = checkAdminAuth(session);
        //if (authCheck != null) return authCheck;

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
    }