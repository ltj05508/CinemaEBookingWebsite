package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database functions for showtime management.
 * Handles scheduling movies, conflict detection, and showroom management.
 */
public class ShowtimeDBFunctions {

    /**
     * Get all available showrooms.
     * 
     * @return List of showroom maps with id, name, and capacity
     */
    public static List<Map<String, Object>> getAllShowrooms() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> showrooms = new ArrayList<>();

        try {
            conn = ConnectToDatabase.getConnection();
            stmt = conn.createStatement();
            
            String sql = "SELECT showroomId, showroomName, capacity FROM Showrooms ORDER BY showroomId";
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Map<String, Object> showroom = new HashMap<>();
                showroom.put("showroomId", rs.getInt("showroomId"));
                showroom.put("showroomName", rs.getString("showroomName"));
                showroom.put("capacity", rs.getInt("capacity"));
                
                showrooms.add(showroom);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting showrooms: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return showrooms;
    }

    /**
     * Check if there is a scheduling conflict for a showroom at a specific date and time.
     * A conflict exists if the same showroom is already booked at the same date/time.
     * 
     * @param showroomId ID of the showroom
     * @param showDate Date of the showing (YYYY-MM-DD)
     * @param showTime Time of the showing (HH:MM)
     * @return true if conflict exists, false otherwise
     */
    public static boolean checkConflict(int showroomId, String showDate, String showTime) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean hasConflict = false;

        try {
            conn = ConnectToDatabase.getConnection();
            
            String sql = "SELECT COUNT(*) as count FROM Showtimes " +
                        "WHERE showroomId = ? AND showDate = ? AND showTime = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, showroomId);
            pstmt.setString(2, showDate);
            pstmt.setString(3, showTime);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                hasConflict = rs.getInt("count") > 0;
            }
            
            if (hasConflict) {
                System.out.println("Conflict detected: Showroom " + showroomId + 
                                 " is already booked for " + showDate + " at " + showTime);
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking showtime conflict: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return hasConflict;
    }

    /**
     * Add a new showtime for a movie.
     * 
     * @param movieId ID of the movie
     * @param showroomId ID of the showroom
     * @param showDate Date of the showing (YYYY-MM-DD)
     * @param showTime Time of the showing (HH:MM)
     * @return The generated showtimeId, or -1 if insert failed
     */
    public static int addShowtime(int movieId, int showroomId, String showDate, String showTime) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int showtimeId = -1;

        try {
            conn = ConnectToDatabase.getConnection();
            
            String sql = "INSERT INTO Showtimes (movieId, showroomId, showDate, showTime) " +
                        "VALUES (?, ?, ?, ?)";
            
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, movieId);
            pstmt.setInt(2, showroomId);
            pstmt.setString(3, showDate);
            pstmt.setString(4, showTime);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    showtimeId = rs.getInt(1);
                }
            }
            
            System.out.println("Showtime added successfully with ID: " + showtimeId);
            
        } catch (SQLException e) {
            System.err.println("Error adding showtime: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return showtimeId;
    }

    /**
     * Get all showtimes for a specific movie.
     * 
     * @param movieId ID of the movie
     * @return List of showtime maps with details
     */
    public static List<Map<String, Object>> getShowtimesByMovie(int movieId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> showtimes = new ArrayList<>();

        try {
            conn = ConnectToDatabase.getConnection();
            
            String sql = "SELECT s.showtimeId, s.movieId, s.showroomId, s.showDate, s.showTime, " +
                        "sr.showroomName, sr.capacity " +
                        "FROM Showtimes s " +
                        "JOIN Showrooms sr ON s.showroomId = sr.showroomId " +
                        "WHERE s.movieId = ? " +
                        "ORDER BY s.showDate, s.showTime";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, movieId);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> showtime = new HashMap<>();
                showtime.put("showtimeId", rs.getInt("showtimeId"));
                showtime.put("movieId", rs.getInt("movieId"));
                showtime.put("showroomId", rs.getInt("showroomId"));
                showtime.put("showDate", rs.getString("showDate"));
                showtime.put("showTime", rs.getString("showTime"));
                showtime.put("showroomName", rs.getString("showroomName"));
                showtime.put("capacity", rs.getInt("capacity"));
                
                showtimes.add(showtime);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting showtimes: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return showtimes;
    }
}
