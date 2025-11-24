import backend.UserDBFunctions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;

import backend.MovieDBFunctions;
import backend.ShowtimeDBFunctions;
import backend.BookingDBFunctions;
import backend.BookingFunctions;
import backend.DatabaseConnectSingleton;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShowtimeFunctionalTests {
    @Autowired
    private TestRestTemplate restTemplate; // kept if we want to extend tests to use REST later

    // Track created entities so each test can clean up after itself
    private final java.util.List<Integer> createdMovieIds = new java.util.ArrayList<>();
    private final java.util.List<Integer> createdShowtimeIds = new java.util.ArrayList<>();
    private final java.util.List<Integer> createdBookingIds = new java.util.ArrayList<>();
    private final java.util.List<String> createdUserIds = new java.util.ArrayList<>();

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

        // Ensure booking_id is treated as an INT in tests
        Integer bookingId = bookingDB.createBooking("1", showtimeId, 12.00, null, tickets);
        Assertions.assertTrue(bookingId != null && bookingId > 0, "Booking creation failed");
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
        Integer bookingId = bf.createBooking("1", movieId, showtimeId, tickets, null);
        Assertions.assertTrue(bookingId != null && bookingId > 0, "Booking creation failed in service layer");
        createdBookingIds.add(bookingId);

        // Verify booking stored and tickets are present
        BookingDBFunctions bookingDB = new BookingDBFunctions();
        Map<String, Object> booking = bookingDB.getBookingById(bookingId, "1");
        Assertions.assertNotNull(booking, "Stored booking not found");
        List<Map<String, Object>> storedTickets = (List<Map<String, Object>>) booking.get("tickets");
        Assertions.assertNotNull(storedTickets);
        Assertions.assertTrue(storedTickets.stream().anyMatch(t -> "C3".equals(t.get("seatId"))), "Ticket for C3 not found in stored booking");
    }

    @Test
    public void testAdminAddShowtimeRejectsConflict() {
        // Create a test movie via DB functions
        int movieId = MovieDBFunctions.addMovie("Test Movie Conflict Admin", "Test", "PG", "Desc", 90, "", "", false);
        Assertions.assertTrue(movieId > 0, "Failed to create test movie");
        createdMovieIds.add(movieId);

        // Find a showroom ID
        List<Map<String, Object>> showrooms = ShowtimeDBFunctions.getAllShowrooms();
        Assertions.assertFalse(showrooms.isEmpty(), "No showrooms found in DB");
        String showroomId = (String) showrooms.get(0).get("showroomId");

        // Create admin user (hashed password) and set active + admin
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String adminEmail = "test-admin-" + System.currentTimeMillis() + "@example.com";
        String adminPassword = "AdminPass123!";
        String hashed = encoder.encode(adminPassword);
        String adminId = UserDBFunctions.createUser("Test", "Admin", adminEmail, hashed, false);
        Assertions.assertNotNull(adminId, "Failed to create admin user");
        createdUserIds.add(adminId);

        // Activate admin user
        boolean activated = UserDBFunctions.activateCustomer(adminEmail);
        Assertions.assertTrue(activated, "Failed to activate admin user");

        // Add admin role
        try (java.sql.Connection conn = DatabaseConnectSingleton.getInstance().getConn();
             java.sql.PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO Admins (admin_id) VALUES (?)")) {
            pstmt.setString(1, adminId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Failed to add admin role");
        }

        // Login as admin via REST to obtain session cookie
        Map<String, String> loginReq = new java.util.HashMap<>();
        loginReq.put("email", adminEmail);
        loginReq.put("password", adminPassword);

        org.springframework.http.ResponseEntity<java.util.Map> loginResp = restTemplate.postForEntity("/api/auth/login", loginReq, java.util.Map.class);
        Assertions.assertEquals(org.springframework.http.HttpStatus.OK, loginResp.getStatusCode(), "Admin login failed");
        String setCookie = loginResp.getHeaders().getFirst("Set-Cookie");
        Assertions.assertNotNull(setCookie, "No Set-Cookie header returned from login");
        String cookie = setCookie.split(";")[0]; // JSESSIONID=...

        // Add initial showtime via Admin API
        Map<String, Object> showtimeReq = new java.util.HashMap<>();
        showtimeReq.put("movieId", movieId);
        showtimeReq.put("showroomId", showroomId);
        showtimeReq.put("showtime", "19:00:00");

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Cookie", cookie);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(showtimeReq, headers);

        org.springframework.http.ResponseEntity<java.util.Map> addResp = restTemplate.exchange("/api/admin/showtimes", org.springframework.http.HttpMethod.POST, entity, java.util.Map.class);
        Assertions.assertEquals(org.springframework.http.HttpStatus.OK, addResp.getStatusCode(), "Failed to add initial showtime");
        // Extract showtimeId created and track it
        java.util.Map body = addResp.getBody();
        if (body != null && body.get("showtimeId") != null) {
            createdShowtimeIds.add(((Number) body.get("showtimeId")).intValue());
        }

        // Now attempt to add conflicting showtime via Admin API - same showroom/time
        org.springframework.http.ResponseEntity<java.util.Map> conflictResp = restTemplate.exchange("/api/admin/showtimes", org.springframework.http.HttpMethod.POST, entity, java.util.Map.class);
        Assertions.assertEquals(org.springframework.http.HttpStatus.CONFLICT, conflictResp.getStatusCode(), "Expected 409 Conflict when adding a conflicting showtime");

        // Verify via Admin GET showtimes that only the original showtime is present
        org.springframework.http.HttpEntity<Void> getEntity = new org.springframework.http.HttpEntity<>(headers);
        org.springframework.http.ResponseEntity<java.util.Map> getResp = restTemplate.exchange("/api/admin/showtimes/" + movieId, org.springframework.http.HttpMethod.GET, getEntity, java.util.Map.class);
        Assertions.assertEquals(org.springframework.http.HttpStatus.OK, getResp.getStatusCode(), "Failed to fetch showtimes via admin API");
        java.util.Map getBody = getResp.getBody();
        Assertions.assertNotNull(getBody);
        java.util.List javaList = (java.util.List) getBody.get("showtimes");
        Assertions.assertNotNull(javaList);
        // Expect no duplicate showtime entries at 19:00:00
        long count = javaList.stream().filter(i -> java.util.Objects.equals(((java.util.Map) i).get("showtime"), "19:00:00")).count();
        Assertions.assertEquals(1, count, "Expected only one showtime at 19:00:00");
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
            for (Integer bookingId : createdBookingIds) {
                pstmt = conn.prepareStatement("DELETE FROM Tickets WHERE booking_id = ?");
                pstmt.setInt(1, bookingId);
                pstmt.executeUpdate();
                pstmt.close();

                pstmt = conn.prepareStatement("DELETE FROM Bookings WHERE booking_id = ?");
                pstmt.setInt(1, bookingId);
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

            // Delete created admin/users
            for (String userId : createdUserIds) {
                pstmt = conn.prepareStatement("DELETE FROM Admins WHERE admin_id = ?");
                pstmt.setString(1, userId);
                pstmt.executeUpdate();
                pstmt.close();

                pstmt = conn.prepareStatement("DELETE FROM Customers WHERE customer_id = ?");
                pstmt.setString(1, userId);
                pstmt.executeUpdate();
                pstmt.close();

                pstmt = conn.prepareStatement("DELETE FROM Users WHERE user_id = ?");
                pstmt.setString(1, userId);
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
            createdUserIds.clear();
        }
    }
}
