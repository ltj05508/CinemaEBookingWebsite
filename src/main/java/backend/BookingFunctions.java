package backend;

import jakarta.validation.constraints.Email;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * Service layer for booking-related business logic.
 */
@Service
public class BookingFunctions {
    public BookingFunctions() { }

    private BookingDBFunctions bookingDBFunctions = new BookingDBFunctions();

    /**
     * Get showroom with seat availability for a specific movie and showtime.
     * Converts 12-hour format (e.g., "2:00 PM") to 24-hour format (e.g., "14:00:00").
     * 
     * @param movieId The movie ID
     * @param showtime The showtime in 12-hour format (e.g., "2:00 PM", "12:30 AM")
     * @return Showroom object with seat information, or null if not found
     */
    public Showroom getSeatsForShow(String movieId, String showtime) {
        if (movieId == null || showtime == null) {
            System.err.println("MovieId or showtime is null");
            return null;
        }

        try {
            // Convert 12-hour format to 24-hour format using Java Time API
            DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("h:mm a");
            DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
            
            LocalTime time = LocalTime.parse(showtime.trim(), inputFormat);
            String militaryTime = time.format(outputFormat);
            
            System.out.println("Converted showtime: " + showtime + " -> " + militaryTime);
            
            Showroom activeShowroom = bookingDBFunctions.getSeatsForShow(movieId, militaryTime);
            
            if (activeShowroom != null) {
                return activeShowroom;
            }
            
            System.err.println("No showroom found for movieId: " + movieId + ", showtime: " + militaryTime);
            return null;
            
        } catch (DateTimeParseException e) {
            System.err.println("Invalid time format: " + showtime + ". Expected format: 'h:mm AM/PM'");
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Error in getSeatsForShow: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a new booking with tickets for selected seats.
     * 
     * @param userId User creating the booking
     * @param movieId Movie being booked
     * @param showtimeId Showtime for the movie
     * @param tickets List of tickets with seat and type information
     * @param promoCode Optional promotion code
     * @return Booking ID if successful, null otherwise
     */
    public String createBooking(String userId, Integer movieId, Integer showtimeId, 
                                List<Map<String, String>> tickets, String promoCode) {
        try {
            // Validate inputs
            if (userId == null || movieId == null || showtimeId == null || 
                tickets == null || tickets.isEmpty()) {
                System.err.println("Invalid booking parameters");
                return null;
            }

            // Calculate total price based on ticket types
            double totalPrice = 0.0;
            Map<String, Double> prices = Map.of(
                "adult", 12.00,
                "senior", 10.00,
                "child", 8.00
            );

            for (Map<String, String> ticket : tickets) {
                String type = ticket.get("type");
                if (type != null && prices.containsKey(type)) {
                    totalPrice += prices.get(type);
                } else {
                    totalPrice += 12.00; // Default to adult price
                }
            }

            // Apply promotion discount if provided
            String promoId = null;
            if (promoCode != null && !promoCode.trim().isEmpty()) {
                Map<String, Object> promo = PromotionDBFunctions.getPromotionByCode(promoCode);
                if (promo != null && PromotionDBFunctions.isPromotionActive(promoCode)) {
                    promoId = (String) promo.get("promoId");
                    double discountPercent = ((Number) promo.get("discountPercent")).doubleValue();
                    totalPrice = totalPrice * (1 - discountPercent / 100.0);
                }
            }

            // Create booking in database
            String bookingId = bookingDBFunctions.createBooking(
                userId, showtimeId, totalPrice, promoId, tickets
            );

            if (bookingId != null) {
                System.out.println("Booking created successfully: " + bookingId);
            }

            return bookingId;

        } catch (Exception e) {
            System.err.println("Error creating booking: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get booking details by booking ID.
     * Verifies the booking belongs to the requesting user.
     * 
     * @param bookingId The booking ID to retrieve
     * @param userId The user ID making the request
     * @return Booking details or null if not found/unauthorized
     */
    public Map<String, Object> getBookingById(String bookingId, String userId) {
        try {
            if (bookingId == null || userId == null) {
                return null;
            }

            return bookingDBFunctions.getBookingById(bookingId, userId);

        } catch (Exception e) {
            System.err.println("Error retrieving booking: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}