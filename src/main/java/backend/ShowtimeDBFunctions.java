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
     * @return List of showroom maps with id, name, and seat_count
     */
    public static List<Map<String, Object>> getAllShowrooms() {
        DatabaseConnectSingleton dcs = DatabaseConnectSingleton.getInstance();
        Connection conn = dcs.getConn();
        Statement stmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> showrooms = new ArrayList<>();

        try {
            stmt = conn.createStatement();
            
            String sql = "SELECT showroom_id, name, seat_count FROM Showrooms ORDER BY showroom_id";
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Map<String, Object> showroom = new HashMap<>();
                showroom.put("showroomId", rs.getString("showroom_id"));
                showroom.put("name", rs.getString("name"));
                showroom.put("seatCount", rs.getInt("seat_count"));
                
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
     * Check if there is a scheduling conflict for a showroom at a specific time.
     * A conflict exists if the same showroom is already booked at the same time.
     * 
     * @param showroomId ID of the showroom (VARCHAR)
     * @param showtime Time of the showing (HH:MM:SS format)
     * @return true if conflict exists, false otherwise
     */
    public static boolean checkConflict(String showroomId, String showtime) {
        DatabaseConnectSingleton dcs = DatabaseConnectSingleton.getInstance();
        Connection conn = dcs.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean hasConflict = false;

        try {
            
            String sql = "SELECT COUNT(*) as count FROM Showtimes " +
                        "WHERE showroom_id = ? AND showtime = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, showroomId);
            pstmt.setString(2, showtime);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                hasConflict = rs.getInt("count") > 0;
            }
            
            if (hasConflict) {
                System.out.println("Conflict detected: Showroom " + showroomId + 
                                 " is already booked at " + showtime);
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
     * @param showroomId ID of the showroom (VARCHAR)
     * @param showtime Time of the showing (HH:MM:SS format)
     * @return The generated showtimeId, or -1 if insert failed
     */
    public static int addShowtime(int movieId, String showroomId, String showtime) {
        DatabaseConnectSingleton dcs = DatabaseConnectSingleton.getInstance();
        Connection conn = dcs.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int showtimeId = -1;

        try {
            String sql = "INSERT INTO Showtimes (movie_id, showroom_id, showtime) " +
                        "VALUES (?, ?, ?)";
            
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, movieId);
            pstmt.setString(2, showroomId);
            pstmt.setString(3, showtime);
            
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
        DatabaseConnectSingleton dcs = DatabaseConnectSingleton.getInstance();
        Connection conn = dcs.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> showtimes = new ArrayList<>();

        try {
            String sql = "SELECT s.showtime_id, s.movie_id, s.showroom_id, s.showtime, " +
                        "sr.name, sr.seat_count " +
                        "FROM Showtimes s " +
                        "JOIN Showrooms sr ON s.showroom_id = sr.showroom_id " +
                        "WHERE s.movie_id = ? " +
                        "ORDER BY s.showtime";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, movieId);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> showtime = new HashMap<>();
                showtime.put("showtimeId", rs.getInt("showtime_id"));
                showtime.put("movieId", rs.getInt("movie_id"));
                showtime.put("showroomId", rs.getString("showroom_id"));
                showtime.put("showtime", rs.getString("showtime"));
                showtime.put("showroomName", rs.getString("name"));
                showtime.put("seatCount", rs.getInt("seat_count"));
                
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
