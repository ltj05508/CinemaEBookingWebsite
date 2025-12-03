package backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * REST API Controller for booking endpoints.
 *
 *
 */
@RestController
//@RequestMapping("/api/booking")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3002"}, allowCredentials = "true")
public class BookingAPIController {
    @Autowired
    private EmailService emailService;
    
    private BookingFunctions bookingFunctions;

    private MovieSearchandFilter movieSearchandFilter;

    @GetMapping("/api/booking/tes")
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

    /**
     * Calculate order quote with optional promotion code.
     * POST /api/booking/quote
     * Body: {
     *   "movieId": 1,
     *   "showtimeId": 1,
     *   "tickets": [{"seatId": "A1", "type": "adult"}, {"seatId": "A2", "type": "child"}],
     *   "promoCode": "SAVE20" (optional)
     * }
     */
    @PostMapping("/quote")
    public ResponseEntity<Map<String, Object>> calculateQuote(@RequestBody Map<String, Object> request,
                                                              HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract data
            Integer movieId = null;
            Object movieIdObj = request.get("movieId");
            if (movieIdObj != null) {
                movieId = Integer.valueOf(movieIdObj.toString());
            }
            
            Integer showtimeId = null;
            Object showtimeIdObj = request.get("showtimeId");
            if (showtimeIdObj != null) {
                showtimeId = Integer.valueOf(showtimeIdObj.toString());
            }
            
            List<Map<String, String>> tickets = (List<Map<String, String>>) request.get("tickets");
            String promoCode = (String) request.get("promoCode");
            
            // Validate required fields
            if (movieId == null || showtimeId == null || tickets == null || tickets.isEmpty()) {
                response.put("success", false);
                response.put("message", "Missing required fields (movieId, showtimeId, tickets)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Calculate subtotal based on ticket types
            double subtotal = 0.0;
            for (Map<String, String> ticket : tickets) {
                String type = ticket.get("type");
                if ("senior".equals(type)) {
                    subtotal += 10.00;
                } else if ("child".equals(type)) {
                    subtotal += 8.00;
                } else {
                    subtotal += 12.00; // adult or default
                }
            }
            
            // Apply promotion if provided
            double discount = 0.0;
            String appliedPromoId = null;
            if (promoCode != null && !promoCode.trim().isEmpty()) {
                Map<String, Object> promo = PromotionDBFunctions.getPromotionByCode(promoCode);
                if (promo != null && PromotionDBFunctions.isPromotionActive(promoCode)) {
                    double discountPercent = (Double) promo.get("discountPercent");
                    discount = subtotal * (discountPercent / 100.0);
                    appliedPromoId = (String) promo.get("promoId");
                } else {
                    response.put("success", false);
                    response.put("message", "Invalid or expired promotion code");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            double total = subtotal - discount;
            
            Map<String, Object> quote = new HashMap<>();
            quote.put("subtotal", subtotal);
            quote.put("discount", discount);
            quote.put("total", total);
            if (appliedPromoId != null) {
                quote.put("promoId", appliedPromoId);
            }
            
            response.put("success", true);
            response.put("quote", quote);
            
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
     *   "tickets": [
     *     {"seatId": "A1", "type": "adult"},
     *     {"seatId": "A2", "type": "child"}
     *   ],
     *   "promoCode": "PROMO123" (optional),
     *   "cardId": "card-uuid" (required)
     * }
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody Map<String, Object> request,
                                                             HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        BookingDBFunctions bookingDB = new BookingDBFunctions();
        movieSearchandFilter = new MovieSearchandFilter();
        
        try {
            // Check if user is logged in
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                response.put("success", false);
                response.put("message", "You must be logged in to create a booking");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String userId = (String) session.getAttribute("userId");
            String email = (String) session.getAttribute("email");
            String firstName = (String) session.getAttribute("firstName");
            
            // Extract booking data
            Integer movieId = null;
            Object movieIdObj = request.get("movieId");
            if (movieIdObj != null) {
                movieId = Integer.valueOf(movieIdObj.toString());
            }
            
            Integer showtimeId = null;
            Object showtimeIdObj = request.get("showtimeId");
            if (showtimeIdObj != null) {
                showtimeId = Integer.valueOf(showtimeIdObj.toString());
            }
            
            List<Map<String, String>> tickets = (List<Map<String, String>>) request.get("tickets");
            String promoCode = (String) request.get("promoCode");
            String cardId = (String) request.get("cardId");
            
            // Validate required fields
            if (movieId == null || showtimeId == null || tickets == null || tickets.isEmpty()) {
                response.put("success", false);
                response.put("message", "Missing required fields (movieId, showtimeId, tickets)");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (cardId == null || cardId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Payment card is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verify card belongs to user
            List<PaymentCard> userCards = UserDBFunctions.getCustomerPaymentCards(userId);
            boolean cardValid = userCards.stream().anyMatch(c -> cardId.equals(c.getCardId()));
            if (!cardValid) {
                response.put("success", false);
                response.put("message", "Invalid payment card");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Calculate total price
            double totalPrice = 0.0;
            for (Map<String, String> ticket : tickets) {
                String type = ticket.get("type");
                if ("senior".equals(type)) {
                    totalPrice += 10.00;
                } else if ("child".equals(type)) {
                    totalPrice += 8.00;
                } else {
                    totalPrice += 12.00;
                }
            }
            
            // Apply promotion if provided
            String promoId = null;
            if (promoCode != null && !promoCode.trim().isEmpty()) {
                Map<String, Object> promo = PromotionDBFunctions.getPromotionByCode(promoCode);
                if (promo != null && PromotionDBFunctions.isPromotionActive(promoCode)) {
                    double discountPercent = (Double) promo.get("discountPercent");
                    double discount = totalPrice * (discountPercent / 100.0);
                    totalPrice -= discount;
                    promoId = (String) promo.get("promoId");
                }
            }
            
            // Create booking
            Integer bookingId = bookingDB.createBooking(userId, showtimeId, totalPrice, promoId, tickets);
            if (bookingId == null) {
                response.put("success", false);
                response.put("message", "Failed to create booking. Seats may already be taken.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Send confirmation email
            try {
                if (email != null && firstName != null && emailService != null) {
                    // Get movie and showtime details for email
                    Movie movie = movieSearchandFilter.getMovieById(movieId);
                    String movieTitle = movie != null ? movie.getTitle() : "Movie #" + movieId;
                    
                    // Get showtime
                    String showtime = "N/A";
                    try {
                        List<Map<String, Object>> showtimes = ShowtimeDBFunctions.getShowtimesByMovie(movieId);
                        for (Map<String, Object> st : showtimes) {
                            if (showtimeId.equals(st.get("showtimeId"))) {
                                showtime = String.valueOf(st.get("showtime"));
                                break;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Could not fetch showtime: " + e.getMessage());
                    }
                    
                    List<String> seatIds = new ArrayList<>();
                    for (Map<String, String> ticket : tickets) {
                        seatIds.add(ticket.get("seatId"));
                    }
                    
                    emailService.sendBookingConfirmationEmail(
                        email, firstName, String.valueOf(bookingId),
                        movieTitle, showtime, seatIds, totalPrice
                    );
                }
            } catch (Exception e) {
                // Log error but don't fail the booking
                System.err.println("Failed to send confirmation email: " + e.getMessage());
                e.printStackTrace();
            }
            
            response.put("success", true);
            response.put("message", "Booking created successfully");
            response.put("bookingId", bookingId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            e.printStackTrace();
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
        BookingDBFunctions bookingDB = new BookingDBFunctions();

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
            Map<String, Object> booking = bookingDB.getBookingById(bookingId, userId);
            
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

    /**
     * Get order history for the logged-in user.
     * GET /api/profile/bookings
     */
    @GetMapping("/api/profile/bookings")
    public ResponseEntity<Map<String, Object>> getOrderHistory(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if user is logged in
            Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
            if (loggedIn == null || !loggedIn) {
                response.put("success", false);
                response.put("message", "You must be logged in to view order history");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String userId = (String) session.getAttribute("userId");
            
            // Get all bookings for user
            List<Map<String, Object>> bookings = UserDBFunctions.getCustomerBookings(userId);
            
            response.put("success", true);
            response.put("bookings", bookings);
            
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