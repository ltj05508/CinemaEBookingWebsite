package backend;

import jakarta.validation.constraints.Email;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
}