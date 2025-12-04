package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// import java.util.UUID; // No longer needed for booking_id

/**
 * Database functions for booking management.
 */
public class BookingDBFunctions {
    public BookingDBFunctions() { }

    // Keeps the last error message for caller diagnostics
    private String lastError = null;

    public String getLastError() {
        return lastError;
    }

    // Generates the next booking ID in case the DB does not auto-generate it
    private Integer getNextBookingId(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT IFNULL(MAX(booking_id), 0) + 1 AS nextId FROM Bookings");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("nextId");
            }
        }
        return null;
    }

    private String getShowroomIdForShowtime(Integer showtimeId, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT showroom_id FROM Showtimes WHERE showtime_id = ?")) {
            ps.setInt(1, showtimeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("showroom_id");
                }
            }
        }
        return null;
    }

    private void ensureSeatsExist(List<Map<String, String>> tickets, String showroomId, Connection conn) throws SQLException {
        if (tickets == null || tickets.isEmpty() || showroomId == null) return;

        for (Map<String, String> ticket : tickets) {
            String seatId = ticket.get("seatId");
            if (seatId == null || seatId.isBlank()) continue;

            // Parse row label (letters) and seat number (digits)
            int split = 0;
            while (split < seatId.length() && Character.isLetter(seatId.charAt(split))) {
                split++;
            }
            String rowLabel = seatId.substring(0, Math.max(split, 1));
            String seatNumStr = seatId.substring(Math.max(split, 1));
            int seatNumber = 0;
            try {
                seatNumber = Integer.parseInt(seatNumStr);
            } catch (NumberFormatException ignored) {
                // leave seatNumber as 0
            }

            // Insert seat if missing
            try (PreparedStatement check = conn.prepareStatement("SELECT seat_id FROM Seats WHERE seat_id = ?")) {
                check.setString(1, seatId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        continue; // already exists
                    }
                }
            }

            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO Seats (seat_id, row_label, seat_number, showroom_id) VALUES (?, ?, ?, ?)")) {
                insert.setString(1, seatId);
                insert.setString(2, rowLabel);
                insert.setInt(3, seatNumber);
                insert.setString(4, showroomId);
                insert.executeUpdate();
            }
        }
    }

    public Showroom getSeatsForShow(String movieId, String showtime) {
        DatabaseConnectSingleton dcs = DatabaseConnectSingleton.getInstance();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String showroomId = null;
        int showtimeId = -1;
        Showroom activeShowroom = null;

        try {
            // Get both showroom_id and showtime_id
            String sql = "SELECT showroom_id, showtime_id FROM Showtimes WHERE movie_id = ? AND showtime = CAST(? AS TIME)";

            pstmt = dcs.getConn().prepareStatement(sql);
            pstmt.setString(1, movieId);
            pstmt.setString(2, showtime);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                showroomId = rs.getString("showroom_id");
                showtimeId = rs.getInt("showtime_id");
            }

            sql = "SELECT * FROM Showrooms WHERE showroom_id = ?";
            pstmt = dcs.getConn().prepareStatement(sql);
            pstmt.setString(1, showroomId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                activeShowroom = new Showroom();
                activeShowroom.setShowroomId(rs.getInt("showroom_id"));
                activeShowroom.setName(rs.getString("name"));
                activeShowroom.setSeatCount(rs.getInt("seat_count"));
                activeShowroom.setNumOfRows(rs.getInt("num_of_rows"));
                activeShowroom.setNumOfCols(rs.getInt("num_of_cols"));
                activeShowroom.setTheatreId(rs.getString("theatre_id"));
                activeShowroom.setShowtimeId(showtimeId);  // Set the showtimeId
            }

            return activeShowroom;

        } catch (SQLException e) {
            System.err.println("Error getting promotion by code: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Get list of seat IDs that are already booked for a specific showtime.
     * 
     * @param movieId The showtime ID
     * @return List of booked seat IDs
     */
    public List<String> getBookedSeats(String movieId, String showtime) {
        DatabaseConnectSingleton dcs = DatabaseConnectSingleton.getInstance();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String> bookedSeats = new ArrayList<>();

        try {
            String sql = "SELECT seat_id FROM Tickets AS t INNER JOIN Showtimes AS sh ON t.showtime_id = sh.showtime_id WHERE sh.movie_id = ? AND sh.showtime = CAST(? AS TIME)";
            pstmt = dcs.getConn().prepareStatement(sql);
            pstmt.setString(1, movieId);
            pstmt.setString(2, showtime);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                bookedSeats.add(rs.getString("seat_id"));
            }

            System.out.println("Found " + bookedSeats.size() + " booked seats for showing of Movie #" +movieId+ " at " +showtime+ ".");
            return bookedSeats;

        } catch (SQLException e) {
            System.err.println("Error getting booked seats: " + e.getMessage());
            e.printStackTrace();
            return bookedSeats;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create a new booking with tickets.
     * 
     * @param userId Customer ID
     * @param showtimeId Showtime ID
     * @param totalPrice Total booking price
     * @param promoId Promotion ID (optional, can be null)
     * @param tickets List of tickets with seat and type information
     * @return Generated booking ID, or null if failed
     */
    public Integer createBooking(String userId, Integer showtimeId, double totalPrice, 
                               String promoId, List<Map<String, String>> tickets) {
        DatabaseConnectSingleton dcs = DatabaseConnectSingleton.getInstance();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Integer bookingId = null;
        lastError = null;

        try {
            // Quick check: verify none of the requested seats are already booked for this showtime
            if (tickets != null && !tickets.isEmpty()) {
                StringBuilder placeholders = new StringBuilder();
                for (int i = 0; i < tickets.size(); i++) {
                    if (i > 0) placeholders.append(",");
                    placeholders.append("?");
                }
                String dupSql = "SELECT seat_id FROM Tickets WHERE showtime_id = ? AND seat_id IN (" + placeholders + ")";
                pstmt = dcs.getConn().prepareStatement(dupSql);
                pstmt.setInt(1, showtimeId);
                for (int i = 0; i < tickets.size(); i++) {
                    pstmt.setString(i + 2, tickets.get(i).get("seatId"));
                }
                rs = pstmt.executeQuery();
                List<String> alreadyBooked = new ArrayList<>();
                while (rs.next()) {
                    alreadyBooked.add(rs.getString("seat_id"));
                }
                rs.close();
                pstmt.close();

                if (!alreadyBooked.isEmpty()) {
                    lastError = "Seats already booked: " + String.join(", ", alreadyBooked);
                    return null;
                }
            }

            // Start transaction
            dcs.getConn().setAutoCommit(false);

            // Generate booking ID manually to avoid "no default value" issues on some schemas
            bookingId = getNextBookingId(dcs.getConn());
            if (bookingId == null) {
                lastError = "Could not generate booking id";
                dcs.getConn().rollback();
                return null;
            }

            // Insert into Bookings table using explicit booking_id
            String bookingSql = "INSERT INTO Bookings (booking_id, customer_id, status, total_price, promo_id) " +
                               "VALUES (?, ?, ?, ?, ?)";
            pstmt = dcs.getConn().prepareStatement(bookingSql);
            pstmt.setInt(1, bookingId);
            pstmt.setString(2, userId);
            pstmt.setString(3, "Confirmed");
            pstmt.setDouble(4, totalPrice);
            pstmt.setString(5, promoId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                dcs.getConn().rollback();
                return null;
            }
            pstmt.close();

            // Ensure seat rows exist for this showroom (FK on Tickets -> Seats)
            String showroomId = getShowroomIdForShowtime(showtimeId, dcs.getConn());
            if (showroomId == null) {
                lastError = "Showroom not found for showtime " + showtimeId;
                dcs.getConn().rollback();
                return null;
            }
            ensureSeatsExist(tickets, showroomId, dcs.getConn());

            // Insert tickets
            String ticketSql = "INSERT INTO Tickets (ticket_id, seat_id, showtime_id, booking_id, price, type) " +
                              "VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = dcs.getConn().prepareStatement(ticketSql);
            for (Map<String, String> ticket : tickets) {
                String ticketId = java.util.UUID.randomUUID().toString(); // ticket_id can remain UUID
                String seatId = ticket.get("seatId");
                String type = ticket.get("type");
                // Get price based on type
                double price = 12.00; // default adult
                if ("senior".equals(type)) price = 10.00;
                else if ("child".equals(type)) price = 8.00;
                pstmt.setString(1, ticketId);
                pstmt.setString(2, seatId);
                pstmt.setInt(3, showtimeId);
                pstmt.setInt(4, bookingId);
                pstmt.setDouble(5, price);
                pstmt.setString(6, type);
                pstmt.addBatch();
            }

            int[] batchResults = pstmt.executeBatch();
            
            // Check if all tickets were inserted
            for (int result : batchResults) {
                if (result == Statement.EXECUTE_FAILED) {
                    dcs.getConn().rollback();
                    System.err.println("Failed to insert some tickets - rolling back");
                    return null;
                }
            }

            // Commit transaction
            dcs.getConn().commit();
            System.out.println("Booking created successfully with ID: " + bookingId);
            return bookingId;

        } catch (SQLException e) {
            System.err.println("Error creating booking: " + e.getMessage());
            e.printStackTrace();
            lastError = e.getMessage();
            try {
                if (dcs.getConn() != null) {
                    dcs.getConn().rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (dcs.getConn() != null) {
                    dcs.getConn().setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get booking details by ID, verifying user ownership.
     * 
     * @param bookingId Booking ID
     * @param userId User ID making the request
     * @return Booking details with tickets, or null if not found/unauthorized
     */
    public Map<String, Object> getBookingById(int bookingId, String userId) {
        DatabaseConnectSingleton dcs = DatabaseConnectSingleton.getInstance();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Get booking info
            String sql = "SELECT b.booking_id, b.booking_date, b.status, b.total_price, b.customer_id, b.promo_id FROM Bookings b WHERE b.booking_id = ? AND b.customer_id = ?";
            
            pstmt = dcs.getConn().prepareStatement(sql);
            pstmt.setInt(1, bookingId);
            pstmt.setString(2, userId);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                return null; // Not found or unauthorized
            }

            Map<String, Object> booking = new HashMap<>();
            booking.put("bookingId", rs.getInt("booking_id"));
            
            // Get timestamp as local time and format for JavaScript
            // Use Calendar to read timestamp in local timezone, not UTC
            java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getDefault());
            java.sql.Timestamp timestamp = rs.getTimestamp("booking_date", cal);
            if (timestamp != null) {
                // Format as ISO string with timezone offset (e.g., 2025-12-04T11:15:30-05:00)
                java.time.ZonedDateTime zdt = timestamp.toLocalDateTime().atZone(java.time.ZoneId.systemDefault());
                booking.put("bookingDate", zdt.format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            } else {
                booking.put("bookingDate", null);
            }
            
            booking.put("status", rs.getString("status"));
            booking.put("totalPrice", rs.getDouble("total_price"));
            booking.put("customerId", rs.getString("customer_id"));
            booking.put("promoId", rs.getString("promo_id"));

            rs.close();
            pstmt.close();

            // Get tickets for this booking
            sql = "SELECT t.ticket_id, t.seat_id, t.showtime_id, t.price, t.type " +
                  "FROM Tickets t " +
                  "WHERE t.booking_id = ?";
            
            pstmt = dcs.getConn().prepareStatement(sql);
            pstmt.setInt(1, bookingId);
            rs = pstmt.executeQuery();

            List<Map<String, Object>> tickets = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> ticket = new HashMap<>();
                ticket.put("ticketId", rs.getString("ticket_id"));
                ticket.put("seatId", rs.getString("seat_id"));
                ticket.put("showtimeId", rs.getInt("showtime_id"));
                ticket.put("price", rs.getDouble("price"));
                ticket.put("type", rs.getString("type"));
                tickets.add(ticket);
            }

            booking.put("tickets", tickets);

            return booking;

        } catch (SQLException e) {
            System.err.println("Error getting booking: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    }
