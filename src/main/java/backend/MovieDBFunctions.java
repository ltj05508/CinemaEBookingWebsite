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
     * @param duration Movie duration (e.g., "120 min")
     * @param posterUrl URL to movie poster image
     * @param trailerUrl URL to movie trailer
     * @param isCurrentlyShowing Whether movie is currently showing
     * @return The generated movieId, or -1 if insert failed
     */
    public static int addMovie(String title, String genre, String rating, String description,
                               String duration, String posterUrl, String trailerUrl, 
                               boolean isCurrentlyShowing) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int movieId = -1;

        try {
            conn = ConnectToDatabase.getConnection();
            
            String sql = "INSERT INTO Movies (title, genre, rating, movieDescription, duration, " +
                        "posterUrl, trailerUrl, isCurrentlyShowing) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, title);
            pstmt.setString(2, genre);
            pstmt.setString(3, rating);
            pstmt.setString(4, description);
            pstmt.setString(5, duration);
            pstmt.setString(6, posterUrl);
            pstmt.setString(7, trailerUrl);
            pstmt.setBoolean(8, isCurrentlyShowing);
            
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

    /**
     * Get a movie by its ID.
     * 
     * @param movieId ID of the movie
     * @return Movie object or null if not found
     */
    public static Movie getMovieById(int movieId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Movie movie = null;

        try {
            conn = ConnectToDatabase.getConnection();
            
            String sql = "SELECT * FROM Movies WHERE movieId = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, movieId);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                movie = new Movie();
                movie.setMovieId(rs.getInt("movieId"));
                movie.setTitle(rs.getString("title"));
                movie.setGenre(rs.getString("genre"));
                movie.setRating(rs.getString("rating"));
                movie.setMovieDescription(rs.getString("movieDescription"));
                movie.setDuration(rs.getString("duration"));
                movie.setPosterUrl(rs.getString("posterUrl"));
                movie.setTrailerUrl(rs.getString("trailerUrl"));
                movie.setCurrentlyShowing(rs.getBoolean("isCurrentlyShowing"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting movie: " + e.getMessage());
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
        
        return movie;
    }

    /**
     * Get all movies.
     * 
     * @return List of all movies
     */
    public static List<Movie> getAllMovies() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<Movie> movies = new ArrayList<>();

        try {
            conn = ConnectToDatabase.getConnection();
            stmt = conn.createStatement();
            
            String sql = "SELECT * FROM Movies ORDER BY isCurrentlyShowing DESC, title ASC";
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Movie movie = new Movie();
                movie.setMovieId(rs.getInt("movieId"));
                movie.setTitle(rs.getString("title"));
                movie.setGenre(rs.getString("genre"));
                movie.setRating(rs.getString("rating"));
                movie.setMovieDescription(rs.getString("movieDescription"));
                movie.setDuration(rs.getString("duration"));
                movie.setPosterUrl(rs.getString("posterUrl"));
                movie.setTrailerUrl(rs.getString("trailerUrl"));
                movie.setCurrentlyShowing(rs.getBoolean("isCurrentlyShowing"));
                
                movies.add(movie);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all movies: " + e.getMessage());
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
        
        return movies;
    }
}
