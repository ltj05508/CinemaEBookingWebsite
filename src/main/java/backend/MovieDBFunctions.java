package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database functions for movie management.
 * Handles CRUD operations for movies in the database.
 */
public class MovieDBFunctions {

    /**
     * Add a new movie to the database.
     * 
     * @param title Movie title
     * @param genre Movie genre
     * @param rating Movie rating (e.g., PG-13, R)
     * @param description Movie description
     * @param durationMinutes Movie duration in minutes
     * @param posterUrl URL to movie poster image
     * @param trailerUrl URL to movie trailer
     * @param currentlyShowing Whether movie is currently showing
     * @return The generated movieId, or -1 if insert failed
     */
    public static int addMovie(String title, String genre, String rating, String description,
                               int durationMinutes, String posterUrl, String trailerUrl, 
                               boolean currentlyShowing) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int movieId = -1;

        try {
            conn = ConnectToDatabase.getConnection();
            
            String sql = "INSERT INTO Movies (title, genre, rating, description, duration_minutes, " +
                        "poster_url, trailer_url, currently_showing) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, title);
            pstmt.setString(2, genre);
            pstmt.setString(3, rating);
            pstmt.setString(4, description);
            pstmt.setInt(5, durationMinutes);
            pstmt.setString(6, posterUrl);
            pstmt.setString(7, trailerUrl);
            pstmt.setBoolean(8, currentlyShowing);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    movieId = rs.getInt(1);
                }
            }
            
            System.out.println("Movie added successfully with ID: " + movieId);
            
        } catch (SQLException e) {
            System.err.println("Error adding movie: " + e.getMessage());
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
        
        return movieId;
    }
}
