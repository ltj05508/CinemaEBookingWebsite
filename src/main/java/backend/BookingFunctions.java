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

    private BookingDBFunctions bookingDBFunctions;


    public int[][] getSeatsForShow(String movieId, String showroom_id) {
            if (movieId != null && showroom_id != null) {
                return bookingDBFunctions.getSeatsForShow(movieId, showroom_id);
            }
            else {
                return null;
            }
    }


    }