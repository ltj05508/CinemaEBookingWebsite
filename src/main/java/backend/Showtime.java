package backend;
import com.google.gson.Gson;
import java.sql.Time;

public class Showtime {
    private int showtimeId;
    private int movieId;
    private String showroomId;
    private Time showtime;

    public Showtime() {}

    public Showtime(int showtimeId, int movieId, String showroomId, Time showtime) {
        this.showtimeId = showtimeId;
        this.movieId = movieId;
        this.showroomId = showroomId;
        this.showtime = showtime;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public int getShowtimeId() {return showtimeId;}
    public int getMovieId() {return movieId;}
    public String getShowroomId() {return showroomId;}
    public Time getShowtime() {return showtime;}

    public void setShowtimeId(int showtimeId) {this.showtimeId = showtimeId;}
    public void setMovieId(int movieId) {this.movieId = movieId;}
    public void setShowroomId(String showroomId) {this.showroomId = showroomId;}
    public void setShowtime(Time showtime) {this.showtime = showtime;}

    @Override
    public String toString() {
        return "Showtime{" +
                "showtimeId=" + showtimeId +
                ", movieId='" + movieId + '\'' +
                ", showroomId='" + showroomId + '\'' +
                ", showtime='" + showtime + '\'' +
                '}';
    }
}
