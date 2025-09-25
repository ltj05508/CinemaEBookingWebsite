package backend;

import com.google.gson.Gson;

public class Movie {
    
    private int movieId;
    private String title;
    private String genre;
    private String rating; 
    private String movieDescription;
    private String showtimes; 
    private String duration; 
    private String posterUrl;
    private String trailerUrl;
    private boolean isCurrentlyShowing;
    
    public Movie() {
    }
    
    public Movie(int movieId, String title, String genre, String rating, 
                 String movieDescription, String showtimes, String duration,
                 String posterUrl, String trailerUrl, boolean isCurrentlyShowing) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.rating = rating;
        this.movieDescription = movieDescription;
        this.showtimes = showtimes;
        this.duration = duration;
        this.posterUrl = posterUrl;
        this.trailerUrl = trailerUrl;
        this.isCurrentlyShowing = isCurrentlyShowing;
    }
    
    public int getMovieId() {
        return movieId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public String getRating() {
        return rating;
    }
    
    public String getMovieDescription() {
        return movieDescription;
    }
    
    public String getShowtimes() {
        return showtimes;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public String getPosterUrl() {
        return posterUrl;
    }
    
    public String getTrailerUrl() {
        return trailerUrl;
    }
    
    public boolean isCurrentlyShowing() {
        return isCurrentlyShowing;
    }
    
    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public void setRating(String rating) {
        this.rating = rating;
    }
    
    public void setMovieDescription(String movieDescription) {
        this.movieDescription = movieDescription;
    }
    
    public void setShowtimes(String showtimes) {
        this.showtimes = showtimes;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }
    
    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }
      
    public void setCurrentlyShowing(boolean currentlyShowing) {
        this.isCurrentlyShowing = currentlyShowing;
    }
      
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    
    @Override
    public String toString() {
        return "Movie{" +
                "movieId=" + movieId +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", rating='" + rating + '\'' +
                ", duration='" + duration + '\'' +
                ", isShowing=" + isCurrentlyShowing +
                '}';
    }
}
