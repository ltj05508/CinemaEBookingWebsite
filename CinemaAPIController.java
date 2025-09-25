import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class CinemaAPIController {
    
    private MovieSearchandFilter movieService;
    
    public CinemaAPIController() {
        this.movieService = new MovieSearchandFilter();
        System.out.println("🎬 CinemaAPIController initialized with " + movieService.getAllMovies().size() + " movies");
    }
    
    @GetMapping("/movies")
    public ResponseEntity<String> getAllMovies() {
        try {
            String moviesJson = movieService.getAllMoviesJson();
            return ResponseEntity.ok(moviesJson);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Failed to get movies\"}");
        }
    }
    
    @GetMapping("/movies/search")
    public ResponseEntity<String> searchMovies(@RequestParam(defaultValue = "") String title) {
        try {
            String searchResults = movieService.searchByTitleJson(title);
            return ResponseEntity.ok(searchResults);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Search failed\"}");
        }
    }
    
    @GetMapping("/movies/filter")
    public ResponseEntity<String> filterMovies(@RequestParam(defaultValue = "") String genre) {
        try {
            String filterResults = movieService.filterByGenreJson(genre);
            return ResponseEntity.ok(filterResults);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Filter failed\"}");
        }
    }
    
    @GetMapping("/genres")
    public ResponseEntity<String> getGenres() {
        try {
            String genresJson = movieService.getAvailableGenresJson();
            return ResponseEntity.ok(genresJson);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Failed to get genres\"}");
        }
    }
    
}