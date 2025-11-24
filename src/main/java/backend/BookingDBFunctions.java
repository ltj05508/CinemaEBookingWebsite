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

        try {
            // Start transaction
            dcs.getConn().setAutoCommit(false);

            // Insert into Bookings table and get generated key
            String bookingSql = "INSERT INTO Bookings (customer_id, status, total_price, promo_id) " +
                               "VALUES (?, ?, ?, ?)";
            pstmt = dcs.getConn().prepareStatement(bookingSql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, userId);
            pstmt.setString(2, "Confirmed");
            pstmt.setDouble(3, totalPrice);
            pstmt.setString(4, promoId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                dcs.getConn().rollback();
                return null;
            }
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                bookingId = rs.getInt(1);
            } else {
                dcs.getConn().rollback();
                return null;
            }
            pstmt.close();

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
            booking.put("bookingDate", rs.getTimestamp("booking_date"));
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