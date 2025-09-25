package backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CinemaAPIController {
    
    private MovieSearchandFilter movieService;
    private List<Movie> movies;
    
    public CinemaAPIController() {
        initializeMovies();
        this.movieService = new MovieSearchandFilter(movies);
    }
    
    private void initializeMovies() {
        movies = new ArrayList<>();
        
    }
    
    @GetMapping("/movies")
    public ResponseEntity<String> getAllMovies() {
        try {
            String moviesJson = movieService.getAllMoviesJson();
            return ResponseEntity.ok(moviesJson);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\":\"Failed to retrieve movies\"}");
        }
    }
    
    @GetMapping("/movies/search")
    public ResponseEntity<String> searchMovies(@RequestParam String title) {
        try {
            String searchResults = movieService.searchByTitleJson(title);
            return ResponseEntity.ok(searchResults);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\":\"Search failed\"}");
        }
    }
    
    @GetMapping("/movies/filter")
    public ResponseEntity<String> filterMovies(@RequestParam String genre) {
        try {
            String filteredResults = movieService.filterByGenreJson(genre);
            return ResponseEntity.ok(filteredResults);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\":\"Filter failed\"}");
        }
    }
    
    @GetMapping("/genres")
    public ResponseEntity<String> getGenres() {
        try {
            Set<String> genres = new HashSet<>();
            for (Movie movie : movies) {
                genres.add(movie.getGenre());
            }
            
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
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\":\"Failed to retrieve genres\"}");
        }
    }
    
    @GetMapping("/movies/{id}")
    public ResponseEntity<String> getMovieById(@PathVariable int id) {
        try {
            Optional<Movie> movie = movies.stream()
                .filter(m -> m.getMovieId() == id)
                .findFirst();
                
            if (movie.isPresent()) {
                return ResponseEntity.ok(movie.get().toJson());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\":\"Failed to retrieve movie\"}");
        }
    }
}
