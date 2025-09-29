package backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CinemaAPIController {
    
    private MovieSearchandFilter movieService;
    
    public CinemaAPIController() {
        this.movieService = new MovieSearchandFilter();
    }
    
    @GetMapping("/movies")
    public ResponseEntity<String> getAllMovies() {
        try {
            String moviesJson = movieService.getAllMoviesJson();
            return ResponseEntity.ok(moviesJson);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\":\"Failed to retrieve movies: " + e.getMessage() + "\"}");
        }
    }
    
    @GetMapping("/movies/search")
    public ResponseEntity<String> searchMovies(@RequestParam String title) {
        try {
            String searchResults = movieService.searchByTitleJson(title);
            return ResponseEntity.ok(searchResults);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\":\"Search failed: " + e.getMessage() + "\"}");
        }
    }
    
    @GetMapping("/movies/filter")
    public ResponseEntity<String> filterMovies(@RequestParam String genre) {
        try {
            String filteredResults = movieService.filterByGenreJson(genre);
            return ResponseEntity.ok(filteredResults);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\":\"Filter failed: " + e.getMessage() + "\"}");
        }
    }
    
    @GetMapping("/genres")
    public ResponseEntity<String> getGenres() {
        try {
            Set<String> genres = movieService.getAllGenres();
            List<String> genreList = new ArrayList<>(genres);
            Collections.sort(genreList);
            
            // Convert to JSON manually
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < genreList.size(); i++) {
                json.append("\"").append(genreList.get(i)).append("\"");
                if (i < genreList.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            
            return ResponseEntity.ok(json.toString());
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body("{\"error\":\"Failed to retrieve genres: " + e.getMessage() + "\"}");
        }
    }
    
    @GetMapping("/movies/{id}")
    public ResponseEntity<String> getMovieById(@PathVariable int id) {
        try {
            Movie movie = movieService.getMovieById(id);
            if (movie != null) {
                return ResponseEntity.ok(movie.toJson());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body("{\"error\":\"Failed to retrieve movie: " + e.getMessage() + "\"}");
        }
    }
}
