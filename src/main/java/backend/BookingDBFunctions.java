package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 *
 *
 */
public class BookingDBFunctions {
    public BookingDBFunctions() { }

    public Showroom getSeatsForShow(String movieId, String showtime) {
        DatabaseConnectSingleton dcs = DatabaseConnectSingleton.getInstance();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Map<String, Object> showtimes = null;
        String showroomId = null;
        int seatCount = -1;
        Showroom activeShowroom = null;

        try {
            //String sql = "SELECT * FROM Seats AS s INNER JOIN Showrooms AS sh ON s.showroom_id = sh.showroom_id INNER JOIN Showtimes as showt ON sh.showroom_id = showt.showroom_id WHERE showt.showtime = STR_TO_DATE(?, '%H:%i') AND showt.movie_id = ?";
            //String sql = "SELECT * FROM Showtimes WHERE movie_id = ?";

            //For the love of god come up with a better method
            String[] split = showtime.split(":");
            int temp = Integer.parseInt(split[0]);
            if (temp < 8) {
                temp += 12;
                showtime = temp + ":" + split[1];
            }

            String sql = "SELECT showroom_id FROM Showtimes WHERE movie_id = ? AND showtime = CAST('14:00:00' AS TIME)";


            pstmt = dcs.getConn().prepareStatement(sql);
            pstmt.setString(1, movieId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                showroomId = rs.getString("showroom_id");
            }

            sql = "SELECT * FROM Showrooms WHERE showroom_id = ?";
            pstmt = dcs.getConn().prepareStatement(sql);
            pstmt.setString(1, showroomId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                activeShowroom = new Showroom();
                activeShowroom.setShowroomId(rs.getInt("showroom_id"));
                activeShowroom.setName(rs.getString("name"));
                activeShowroom.setSeatCount(rs.getInt("seat_count"));
                activeShowroom.setNumOfRows(rs.getInt("num_of_rows"));
                activeShowroom.setNumOfCols(rs.getInt("num_of_cols"));
                activeShowroom.setTheatreId(rs.getString("theatre_id"));
            }

            return activeShowroom;

        } catch (SQLException e) {
            System.err.println("Error getting promotion by code: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    }