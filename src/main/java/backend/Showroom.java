package backend;
import com.google.gson.Gson;
import java.sql.Time;

public class Showroom{
    private int showroomId;
    private String name;
    private int seatCount;
    private int numOfRows;
    private int numOfCols;
    private String theatreId;
    private int[][] showroomMap;


    //Each showtime has their own showroom (add a foreign key showtimeId). Then add a 2d array to the list of variables to keep track of
    //the sold/unsold seats
    //How to compensate for lack of 2d array support in sql?

    public Showroom() {}

    public Showroom(int showroomId, String name, int seatCount, int numOfRows, int numOfCols, String theatreId) {
        this.showroomId = showroomId;
        this.name = name;
        this.seatCount = seatCount;
        this.numOfRows = numOfRows;
        this.numOfCols = numOfCols;
        this.theatreId = theatreId;
    }

    public Showroom(int showroomId, String name, int seatCount, int numOfRows, int numOfCols, String theatreId, int[][] showroomMap) {
        this.showroomId = showroomId;
        this.name = name;
        this.seatCount = seatCount;
        this.numOfRows = numOfRows;
        this.numOfCols = numOfCols;
        this.theatreId = theatreId;
        this.showroomMap = showroomMap;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public int getShowroomId() {return showroomId;}
    public String getName() {return name;}
    public int getSeatCount() {return seatCount;}
    public int getNumOfRows() {return numOfRows;}
    public int getNumOfCols() {return numOfCols;}
    public String getTheatreId() {return theatreId;}
    public int[][] getShowroomMap() {return showroomMap;};

    public void setShowroomId(int showroomId) {this.showroomId = showroomId;}
    public void setName(String name) {this.name = name;}
    public void setSeatCount(int seatCount) {this.seatCount = seatCount;}
    public void setNumOfRows(int numOfRows) {this.numOfRows = numOfRows;}
    public void setNumOfCols(int numOfCols) {this.numOfCols = numOfCols;}
    public void setTheatreId(String theatreId) {this.theatreId = theatreId;}
    public void setShowroomMap(int[][] showroomMap) {this.showroomMap = showroomMap;}

    @Override
    public String toString() {
        return "Showroom{" +
                "showroomId=" + showroomId +
                ", name='" + name + '\'' +
                ", seatCount='" + seatCount + '\'' +
                ", numOfRows='" + numOfRows + '\'' +
                ", numOfCols='" + numOfCols + '\'' +
                ", theatreId='" + theatreId + '\'' +
                '}';
    }
}
