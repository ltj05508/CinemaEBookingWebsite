package backend;

import java.sql.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Handles movie database operations including search and filter functionality
 */
public class MovieSearchandFilter {
    private static final String URL = "jdbc:mysql://localhost:3306/cinema_eBooking_system";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Booboorex";
    
    private Gson gson;
    
    public MovieSearchandFilter() {
        this.gson = new GsonBuilder().create();
    }
    
    // Helper method to get database connection
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }
    
    /**
     * Get all movies with their showtimes
     */
    public List<Movie> getAllMovies() throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String query = """
            SELECT m.movie_id, m.title, m.genre, m.rating, m.description, 
                   m.duration_minutes, m.currently_showing, m.poster_url, m.trailer_url,
                   GROUP_CONCAT(TIME_FORMAT(s.showtime, '%h:%i %p') ORDER BY s.showtime SEPARATOR ', ') as showtimes
            FROM Movies m
            LEFT JOIN Showtimes s ON m.movie_id = s.movie_id
            GROUP BY m.movie_id, m.title, m.genre, m.rating, m.description, 
                     m.duration_minutes, m.currently_showing, m.poster_url, m.trailer_url
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Movie movie = new Movie();
                movie.setMovieId(rs.getInt("movie_id"));
                movie.setTitle(rs.getString("title"));
                movie.setGenre(rs.getString("genre"));
                movie.setRating(rs.getString("rating"));
                movie.setMovieDescription(rs.getString("description"));
                movie.setDuration(rs.getInt("duration_minutes") + "");
                movie.setCurrentlyShowing(rs.getBoolean("currently_showing"));
                movie.setPosterUrl(rs.getString("poster_url"));
                movie.setTrailerUrl(rs.getString("trailer_url"));
                
                String showtimes = rs.getString("showtimes");
                movie.setShowtimes(showtimes != null ? showtimes : "TBA");
                
                movies.add(movie);
            }
        }
        
        return movies;
    }
    
    /**
     * Return all movies as JSON
     */
    public String getAllMoviesJson() {
        try {
            return gson.toJson(getAllMovies());
        } catch (SQLException e) {
            System.err.println("Error getting all movies: " + e.getMessage());
            return "[]";
        }
    }
    
    /**
     * Search movies by title using prepared statements for security
     */
    public List<Movie> searchByTitle(String title) throws SQLException {
        List<Movie> movies = new ArrayList<>();
        
        if (title == null || title.trim().isEmpty()) {
            return getAllMovies();
        }
        
        String query = """
            SELECT m.movie_id, m.title, m.genre, m.rating, m.description, 
                   m.duration, m.currently_showing, m.poster_url, m.trailer_url,
                   GROUP_CONCAT(TIME_FORMAT(s.showtime, '%h:%i %p') ORDER BY s.showtime SEPARATOR ', ') as showtimes
            FROM Movies m
            LEFT JOIN Showtimes s ON m.movie_id = s.movie_id
            WHERE m.title LIKE ?
            GROUP BY m.movie_id, m.title, m.genre, m.rating, m.description, 
                     m.duration, m.currently_showing, m.poster_url, m.trailer_url
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, "%" + title + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Movie movie = new Movie();
                    movie.setMovieId(rs.getInt("movie_id"));
                    movie.setTitle(rs.getString("title"));
                    movie.setGenre(rs.getString("genre"));
                    movie.setRating(rs.getString("rating"));
                    movie.setMovieDescription(rs.getString("description"));
                    movie.setDuration(rs.getString("duration"));
                    movie.setCurrentlyShowing(rs.getBoolean("currently_showing"));
                    movie.setPosterUrl(rs.getString("poster_url"));
                    movie.setTrailerUrl(rs.getString("trailer_url"));
                    
                    String showtimes = rs.getString("showtimes");
                    movie.setShowtimes(showtimes != null ? showtimes : "TBA");
                    
                    movies.add(movie);
                }
            }
        }
        
        return movies;
    }
    
    /**
     * Search for movies and return as JSON
     */
    public String searchByTitleJson(String title) {
        try {
            List<Movie> results = searchByTitle(title);
            return gson.toJson(results);
        } catch (SQLException e) {
            System.err.println("Error searching movies by title: " + e.getMessage());
            return "[]";
        }
    }
    
    /**
     * Filter movies by genre using prepared statements for security
     */
    public List<Movie> filterByGenre(String genre) throws SQLException {
        List<Movie> movies = new ArrayList<>();
        
        if (genre == null || genre.trim().isEmpty()) {
            return getAllMovies();
        }
        
        String query = """
            SELECT m.movie_id, m.title, m.genre, m.rating, m.description, 
                   m.duration, m.currently_showing, m.poster_url, m.trailer_url,
                   GROUP_CONCAT(TIME_FORMAT(s.showtime, '%h:%i %p') ORDER BY s.showtime SEPARATOR ', ') as showtimes
            FROM Movies m
            LEFT JOIN Showtimes s ON m.movie_id = s.movie_id
            WHERE m.genre = ?
            GROUP BY m.movie_id, m.title, m.genre, m.rating, m.description, 
                     m.duration, m.currently_showing, m.poster_url, m.trailer_url
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, genre);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Movie movie = new Movie();
                    movie.setMovieId(rs.getInt("movie_id"));
                    movie.setTitle(rs.getString("title"));
                    movie.setGenre(rs.getString("genre"));
                    movie.setRating(rs.getString("rating"));
                    movie.setMovieDescription(rs.getString("description"));
                    movie.setDuration(rs.getString("duration"));
                    movie.setCurrentlyShowing(rs.getBoolean("currently_showing"));
                    movie.setPosterUrl(rs.getString("poster_url"));
                    movie.setTrailerUrl(rs.getString("trailer_url"));
                    
                    String showtimes = rs.getString("showtimes");
                    movie.setShowtimes(showtimes != null ? showtimes : "TBA");
                    
                    movies.add(movie);
                }
            }
        }
        
        return movies;
    }
    
    /**
     * Filter movies and return as JSON
     */
    public String filterByGenreJson(String genre) {
        try {
            List<Movie> results = filterByGenre(genre);
            return gson.toJson(results);
        } catch (SQLException e) {
            System.err.println("Error filtering movies by genre: " + e.getMessage());
            return "[]";
        }
    }
    
    /**
     * Get movie by ID
     */
    public Movie getMovieById(int movieId) throws SQLException {
        String query = """
            SELECT m.movie_id, m.title, m.genre, m.rating, m.description, 
                   m.duration, m.currently_showing, m.poster_url, m.trailer_url,
                   GROUP_CONCAT(TIME_FORMAT(s.showtime, '%h:%i %p') ORDER BY s.showtime SEPARATOR ', ') as showtimes
            FROM Movies m
            LEFT JOIN Showtimes s ON m.movie_id = s.movie_id
            WHERE m.movie_id = ?
            GROUP BY m.movie_id, m.title, m.genre, m.rating, m.description, 
                     m.duration, m.currently_showing, m.poster_url, m.trailer_url
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, movieId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Movie movie = new Movie();
                    movie.setMovieId(rs.getInt("movie_id"));
                    movie.setTitle(rs.getString("title"));
                    movie.setGenre(rs.getString("genre"));
                    movie.setRating(rs.getString("rating"));
                    movie.setMovieDescription(rs.getString("description"));
                    movie.setDuration(rs.getString("duration"));
                    movie.setCurrentlyShowing(rs.getBoolean("currently_showing"));
                    movie.setPosterUrl(rs.getString("poster_url"));
                    movie.setTrailerUrl(rs.getString("trailer_url"));
                    
                    String showtimes = rs.getString("showtimes");
                    movie.setShowtimes(showtimes != null ? showtimes : "TBA");
                    
                    return movie;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get all distinct genres
     */
    public Set<String> getAllGenres() throws SQLException {
        Set<String> genres = new HashSet<>();
        String query = "SELECT DISTINCT genre FROM Movies WHERE genre IS NOT NULL";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                genres.add(rs.getString("genre"));
            }
        }
        
        return genres;
    }
}