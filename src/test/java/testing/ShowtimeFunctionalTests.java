package testing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Configuration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import backend.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = backend.CinemaEBookSpringBoot.class
)
public class ShowtimeFunctionalTests {
    @TestConfiguration
    static class TestConfig {
        // Empty on purpose.
        // Add @Bean definitions here only if a class needs to be Spring-managed.
    }

    @Autowired
    private TestRestTemplate restTemplate; // kept if we want to extend tests to use REST later

    // Track created entities so each test can clean up after itself
    private final java.util.List<Integer> createdMovieIds = new java.util.ArrayList<>();
    private final java.util.List<Integer> createdShowtimeIds = new java.util.ArrayList<>();
    private final java.util.List<String> createdBookingIds = new java.util.ArrayList<>();

    @Test
    public void testSchedulingConflictWithDBFunctions() {
        // Create a test movie
        int movieId = MovieDBFunctions.addMovie("Test Movie Conflict", "Test", "PG", "Desc", 90, "", "", false);
        Assertions.assertTrue(movieId > 0, "Failed to create test movie");
        createdMovieIds.add(movieId);

        // Find a showroom ID
        List<Map<String, Object>> showrooms = ShowtimeDBFunctions.getAllShowrooms();
        Assertions.assertFalse(showrooms.isEmpty(), "No showrooms found in DB");
        String showroomId = (String) showrooms.get(0).get("showroomId");

        // Add initial showtime
        int showtimeId = ShowtimeDBFunctions.addShowtime(movieId, showroomId, "19:00:00");
        Assertions.assertTrue(showtimeId > 0, "Failed to add showtime");
        createdShowtimeIds.add(showtimeId);

        // Check conflict for same showroom/time
        boolean conflict = ShowtimeDBFunctions.checkConflict(showroomId, "19:00:00");
        Assertions.assertTrue(conflict, "Expected a scheduling conflict but none found");
    }

    @Test
    public void testShowtimeVisibilityWithDBFunctions() {
        // Create a test movie
        int movieId = MovieDBFunctions.addMovie("Test Movie Visibility", "Test", "PG", "Desc", 90, "", "", false);
        Assertions.assertTrue(movieId > 0, "Failed to create test movie");
        createdMovieIds.add(movieId);

        // Use an existing showroom
        List<Map<String, Object>> showrooms = ShowtimeDBFunctions.getAllShowrooms();
        Assertions.assertFalse(showrooms.isEmpty(), "No showrooms found in DB");
        String showroomId = (String) showrooms.get(0).get("showroomId");

        // Add a new showtime for visibility
        int showtimeId = ShowtimeDBFunctions.addShowtime(movieId, showroomId, "20:30:00");
        Assertions.assertTrue(showtimeId > 0, "Failed to add showtime");
        createdShowtimeIds.add(showtimeId);

        // Get showtimes by movie
        List<Map<String, Object>> showtimes = ShowtimeDBFunctions.getShowtimesByMovie(movieId);
        Assertions.assertFalse(showtimes.isEmpty(), "No showtimes returned for the test movie");

        boolean found = showtimes.stream().anyMatch(s -> "20:30:00".equals(s.get("showtime")));
        Assertions.assertTrue(found, "Created showtime not visible in getShowtimesByMovie");
    }

    @Test
    public void testSeatAvailability() {
        // Create a test movie and showtime
        int movieId = MovieDBFunctions.addMovie("Test Movie Booking", "Test", "PG", "Desc", 90, "", "", false);
        Assertions.assertTrue(movieId > 0, "Failed to create test movie");
        createdMovieIds.add(movieId);

        List<Map<String, Object>> showrooms = ShowtimeDBFunctions.getAllShowrooms();
        Assertions.assertFalse(showrooms.isEmpty(), "No showrooms found in DB");
        String showroomId = (String) showrooms.get(0).get("showroomId");

        int showtimeId = ShowtimeDBFunctions.addShowtime(movieId, showroomId, "18:45:00");
        Assertions.assertTrue(showtimeId > 0, "Failed to add showtime");
        createdShowtimeIds.add(showtimeId);

        // Ensure seat exists in the Seats table to satisfy FK constraint before booking
        java.sql.Connection conn = DatabaseConnectSingleton.getInstance().getConn();
        try (java.sql.PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO Seats (seat_id, row_label, seat_number, showroom_id) VALUES (?, ?, ?, ?)")) {
            pstmt.setString(1, "A1");
            pstmt.setString(2, "A");
            pstmt.setInt(3, 1);
            pstmt.setString(4, showroomId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Book a seat using BookingDBFunctions directly (customer id '1' exists in seed DB)
        BookingDBFunctions bookingDB = new BookingDBFunctions();
        List<Map<String, String>> tickets = List.of(Map.of("seatId", "A1", "type", "adult"));

        String bookingId = bookingDB.createBooking("1", showtimeId, 12.00, null, tickets);
        Assertions.assertNotNull(bookingId, "Booking creation failed");
        createdBookingIds.add(bookingId);

        // Check seat availability via BookingFunctions.getBookedSeats (converts PM -> 24hr)
        BookingFunctions bf2 = new BookingFunctions();
        List<String> booked = bf2.getBookedSeats(Integer.toString(movieId), "6:45 PM");
        Assertions.assertTrue(booked.contains("A1"), "Seat A1 should be booked but is not listed as booked");

        // Ensure B2 exists in Seats table
        try (java.sql.PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO Seats (seat_id, row_label, seat_number, showroom_id) VALUES (?, ?, ?, ?)")) {
            pstmt.setString(1, "B2");
            pstmt.setString(2, "B");
            pstmt.setInt(3, 2);
            pstmt.setString(4, showroomId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testBookingFlow() {
        // Create a test movie and showtime
        int movieId = MovieDBFunctions.addMovie("Test Movie Booking Flow", "Test", "PG", "Desc", 90, "", "", false);
        Assertions.assertTrue(movieId > 0, "Failed to create test movie");
        createdMovieIds.add(movieId);

        List<Map<String, Object>> showrooms = ShowtimeDBFunctions.getAllShowrooms();
        Assertions.assertFalse(showrooms.isEmpty(), "No showrooms found in DB");
        String showroomId = (String) showrooms.get(0).get("showroomId");

        int showtimeId = ShowtimeDBFunctions.addShowtime(movieId, showroomId, "21:00:00");
        Assertions.assertTrue(showtimeId > 0, "Failed to add showtime");
        createdShowtimeIds.add(showtimeId);

        // Ensure seats exist
        java.sql.Connection conn = DatabaseConnectSingleton.getInstance().getConn();
        try (java.sql.PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO Seats (seat_id, row_label, seat_number, showroom_id) VALUES (?, ?, ?, ?)")) {
            pstmt.setString(1, "C3");
            pstmt.setString(2, "C");
            pstmt.setInt(3, 3);
            pstmt.setString(4, showroomId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Booking via service layer
        BookingFunctions bf = new BookingFunctions();
        List<Map<String, String>> tickets = List.of(Map.of("seatId", "C3", "type", "adult"));
        String bookingId = bf.createBooking("1", movieId, showtimeId, tickets, null);
        Assertions.assertNotNull(bookingId, "Booking creation failed in service layer");
        createdBookingIds.add(bookingId);

        // Verify booking stored and tickets are present
        BookingDBFunctions bookingDB = new BookingDBFunctions();
        Map<String, Object> booking = bookingDB.getBookingById(bookingId, "1");
        Assertions.assertNotNull(booking, "Stored booking not found");
        List<Map<String, Object>> storedTickets = (List<Map<String, Object>>) booking.get("tickets");
        Assertions.assertNotNull(storedTickets);
        Assertions.assertTrue(storedTickets.stream().anyMatch(t -> "C3".equals(t.get("seatId"))), "Ticket for C3 not found in stored booking");
    }

    @AfterEach
    public void tearDown() {
        // Allow skipping cleanup for debugging by setting SKIP_TEST_CLEANUP to '1'
        String skipCleanup = System.getenv("SKIP_TEST_CLEANUP");
        if (skipCleanup != null && (skipCleanup.equals("1") || skipCleanup.equalsIgnoreCase("true"))) {
            System.out.println("SKIP_TEST_CLEANUP set: not cleaning test data (use cleanup-test-data scripts to clean later)");
            return;
        }

        java.sql.Connection conn = DatabaseConnectSingleton.getInstance().getConn();
        java.sql.PreparedStatement pstmt = null;
        try {
            // Delete tickets and bookings first
            for (String bookingId : createdBookingIds) {
                pstmt = conn.prepareStatement("DELETE FROM Tickets WHERE booking_id = ?");
                pstmt.setString(1, bookingId);
                pstmt.executeUpdate();
                pstmt.close();

                pstmt = conn.prepareStatement("DELETE FROM Bookings WHERE booking_id = ?");
                pstmt.setString(1, bookingId);
                pstmt.executeUpdate();
                pstmt.close();
            }

            // Delete showtimes
            for (Integer showtimeId : createdShowtimeIds) {
                pstmt = conn.prepareStatement("DELETE FROM Showtimes WHERE showtime_id = ?");
                pstmt.setInt(1, showtimeId);
                pstmt.executeUpdate();
                pstmt.close();
            }

            // Delete movies
            for (Integer movieId : createdMovieIds) {
                pstmt = conn.prepareStatement("DELETE FROM Movies WHERE movie_id = ?");
                pstmt.setInt(1, movieId);
                pstmt.executeUpdate();
                pstmt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception e) { /* ignore */ }
            createdBookingIds.clear();
            createdShowtimeIds.clear();
            createdMovieIds.clear();
        }
    }
}
