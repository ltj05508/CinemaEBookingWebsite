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


    public Showroom getSeatsForShow(String movieId, String showtime) {
        Showroom activeShowroom = null;
        if (movieId != null && showtime != null) {
            String[] temp = showtime.split(" ");
            System.out.println(temp[1]);
            if (temp[1].equals("PM")) {
                System.out.println("In military time converter\n");
                String[] hour = temp[0].split(":");
                int hourInt = Integer.parseInt(hour[0]);
                hourInt = hourInt + 12;
                showtime = hourInt + ":" + hour[1];
                //showtime = temp[0] + " " + temp[1];
            } else {
                showtime = temp[0];
            }
            showtime = showtime + ":00";
            System.out.println("Final showtime: " +showtime);
            activeShowroom = bookingDBFunctions.getSeatsForShow(movieId, showtime);
        }

        if (activeShowroom != null) {
            return activeShowroom;
        }

        System.err.println("\nProblem in getSeatsForShow function in BookingFunctions class!\n");
        return null;

    }


    }