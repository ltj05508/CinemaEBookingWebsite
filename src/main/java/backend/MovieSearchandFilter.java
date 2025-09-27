package backend;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * 
 * Searches for movies by title and filters by genre
 */

public class MovieSearchandFilter {
    private List<Movie> movies;
    private Gson gson;
    public static Connection conn;
    
    public MovieSearchandFilter() {
        this.movies = new ArrayList<>();
        this.gson = new GsonBuilder().create();
    }
    
    /**
     * Constructor 
     */
    public MovieSearchandFilter(List<Movie> movieList) {
        if(this.movies != null) {
            this.movies = new ArrayList<>(movieList);
        } else {
            this.movies = new ArrayList<>();
        }
        this.gson = new GsonBuilder().create();
        setUpConnection("127.0.0.1", "CinemaEBooking", "root", "Booboorex");
        System.out.println("MovieSearchAndFilter successfully created!");
    }
    
    /**
     * Set the movie list for search and filter operations
     */
    public void setMovies(List<Movie> movieList) {
        if(this.movies != null) {
            this.movies = new ArrayList<>(movieList);
        } else {
            this.movies = new ArrayList<>();
        }
    }
    
    
    /**
     * Get all movies
     */
    public List<Movie> getAllMovies() {
        try {
            Statement state = conn.createStatement();
            ResultSet resultSet = state.executeQuery("select * from Movies"); //cinema_eBooking_system
            System.out.println("Movies in Database");

            while(resultSet.next()) {
                Movie newMovie = new Movie(resultSet.getInt("movie_id"), resultSet.getString("title"), resultSet.getString("genre"), resultSet.getString("rating"),
                        resultSet.getString("description"), "placeholder", resultSet.getString("duration"), resultSet.getString("poster_url"),
                        resultSet.getString("trailer_url"), resultSet.getBoolean("currently_showing"));

                movies.add(newMovie);
            }
        } catch(Exception e) {
            System.out.println("Problem in getAllMovies!");
            e.printStackTrace();
        }
        return new ArrayList<>(movies);
    }
    
    /**
     * Return all movies as JSON
     */
    public String getAllMoviesJson() {
        return gson.toJson(getAllMovies());
    }
    
    /**
     * Search movies by title 
     */
    public List<Movie> searchByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return getAllMovies();
        }
        
        String searchTerm = title.toLowerCase().trim();
        try {
            Statement state = conn.createStatement();
            ResultSet resultSet = state.executeQuery("select * from Movies where title like '%" +searchTerm+ "%'"); //cinema_eBooking_system
            System.out.println("Movies in Database Searched by " +searchTerm);



            while(resultSet.next()) {
                //ResultSet showtimeSet = state.executeQuery("select * from Showtimes where movie_id = " +resultSet.getInt("movie_id"));
                Movie newMovie = new Movie(resultSet.getInt("movie_id"), resultSet.getString("title"), resultSet.getString("genre"), resultSet.getString("rating"),
                        resultSet.getString("description"), "placeholder", resultSet.getString("duration"), resultSet.getString("poster_url"),
                        resultSet.getString("trailer_url"), resultSet.getBoolean("currently_showing"));

                movies.add(newMovie);
            }

        } catch(Exception e) {
            System.out.println("Exception in searchByTitle!");
            e.printStackTrace();
        }
        return movies;
        /*
        return movies.stream()
                .filter(movie -> movie.getTitle().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
         */
    }
    
    /**
     * Search for movies and return as JSON
     */
    public String searchByTitleJson(String title) {
        List<Movie> results = searchByTitle(title);
        return gson.toJson(results);
    }
    
    /**
     * Filter movies by genre 
     */
    public List<Movie> filterByGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            return getAllMovies();
        }
        
        String filterGenre = genre.toLowerCase().trim();

        try {
            Statement state = conn.createStatement();
            ResultSet resultSet = state.executeQuery("select * from Movies where genre like '" +filterGenre+ "'"); //cinema_eBooking_system
            System.out.println("Movies in Database with Genre:  " +filterGenre);

            while(resultSet.next()) {
                Movie newMovie = new Movie(resultSet.getInt("movie_id"), resultSet.getString("title"), resultSet.getString("genre"), resultSet.getString("rating"),
                        resultSet.getString("description"), "placeholder", resultSet.getString("duration"), resultSet.getString("poster_url"),
                        resultSet.getString("trailer_url"), resultSet.getBoolean("currently_showing"));

                movies.add(newMovie);
            }

        } catch(Exception e) {
            System.out.println("Exception in searchByTitle!");
            e.printStackTrace();
        }

        return movies;
        /*
        return movies.stream()
                .filter(movie -> movie.getGenre().toLowerCase().equals(filterGenre))
                .collect(Collectors.toList());

         */
    }
    
    /**
     * Filter movies and return as JSON
     */
    public String filterByGenreJson(String genre) {
        List<Movie> results = filterByGenre(genre);
        return gson.toJson(results);
    }


    public static void setUpConnection(String hostURL, String databaseName, String username, String password) {

        try {                                     //"jdbc:mysql://151.101.1.69:3306/databasename?useUnicode=true&characterEncoding=utf8"
            conn = DriverManager.getConnection("jdbc:mysql://" +hostURL+ ":3306/" +databaseName+ "?enabledTLSProtocols=TLSv1.2", username, password); //Current: jdbc:mysql://192.168.1.185:3306/CinemaEBooking?useUnicode=true&characterEncoding=utf8
            //"jdbc:mysql://" +hostURL+ ":3306/" +databaseName+ "?useUnicode=true&characterEncoding=utf8");
        }
        catch(Exception e) {
            System.out.println("Error in setUpConnection!");
            e.printStackTrace();
        }
    }
}