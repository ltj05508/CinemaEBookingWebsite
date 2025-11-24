package backend;
import com.google.gson.Gson;

public class Seat {
    private String seatId;
    private String rowLabel;
    private int seatNumber;
    private String showroomId;

    public Seat() {}
    public Seat(String seatId, String rowLabel, int seatNumber, String showroomId) {
        this.seatId = seatId;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.showroomId = showroomId;
    }

    public String getSeatId() {return seatId;}
    public String getRowLabel() {return rowLabel;}
    public int getSeatNumber() {return seatNumber;}
    public String getShowroomId() {return showroomId;}

    public void setSeatId(String seatId) {this.seatId = seatId;}
    public void setRowLabel(String rowLabel) {this.rowLabel = rowLabel;}
    public void setSeatNumber(int seatNumber) {this.seatNumber = seatNumber;}
    public void setShowroomId(String showroomId) {this.showroomId = showroomId;}

    @Override
    public String toString() {
        return "Seat{" +
                "seatId=" + seatId +
                ", rowLabel='" + rowLabel + '\'' +
                ", seatNumber='" + seatNumber + '\'' +
                ", showroomId='" + showroomId + '\'' +
                '}';
    }
}
