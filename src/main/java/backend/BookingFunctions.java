package backend;

import jakarta.validation.constraints.Email;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 *
 *
 *
 */
@Service
public class BookingFunctions {
    public BookingFunctions() { }

    private BookingDBFunctions bookingDBFunctions = new BookingDBFunctions();


    public Showroom getSeatsForShow(String movieId, String showroom_id) {
        Showroom activeShowroom = null;
        if (movieId != null && showroom_id != null) {
            activeShowroom = bookingDBFunctions.getSeatsForShow(movieId, showroom_id);
        }

        if (activeShowroom != null) {
            return activeShowroom;
        }

        System.err.println("\nProblem in getSeatsForShow function in BookingFunctions class!\n");
        return null;

    }


    }