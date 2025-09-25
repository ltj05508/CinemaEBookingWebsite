package backend;

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
        return new ArrayList<>(movies);
    }
    
    /**
     * Return all movies as JSON
     */
    public String getAllMoviesJson() {
        return gson.toJson(movies);
    }
    
    /**
     * Search movies by title 
     */
    public List<Movie> searchByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return getAllMovies();
        }
        
        String searchTerm = title.toLowerCase().trim();
        return movies.stream()
                .filter(movie -> movie.getTitle().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
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
        return movies.stream()
                .filter(movie -> movie.getGenre().toLowerCase().equals(filterGenre))
                .collect(Collectors.toList());
    }
    
    /**
     * Filter movies and return as JSON
     */
    public String filterByGenreJson(String genre) {
        List<Movie> results = filterByGenre(genre);
        return gson.toJson(results);
    }
    
}