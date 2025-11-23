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
@RequestMapping("/api/booking")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class BookingAPIController {
    @Autowired
    private EmailService emailService;
    @Autowired
    private BookingFunctions bookingFunctions;




    // ==================== MOVIE MANAGEMENT ====================

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