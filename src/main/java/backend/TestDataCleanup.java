package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * TestDataCleanup helper - deletes movies and related rows that match a given title pattern.
 *
 * Usage: mvn -DskipTests exec:java -Dexec.mainClass=backend.TestDataCleanup -Dexec.args="'Test Movie%'"
 */
public class TestDataCleanup {
    public static void main(String[] args) {
        String pattern = "Test Movie%";
        if (args != null && args.length > 0) {
            pattern = args[0];
        } else {
            String envPattern = System.getenv("CLEANUP_PATTERN");
            if (envPattern != null && !envPattern.trim().isEmpty()) {
                pattern = envPattern;
            }
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseConnectSingleton.getInstance().getConn();
            if (conn == null) {
                System.err.println("DB connection not available");
                System.exit(1);
            }

            String sql1 = "DELETE t FROM Tickets t JOIN Showtimes s ON t.showtime_id = s.showtime_id JOIN Movies m ON s.movie_id = m.movie_id WHERE m.title LIKE ?";
            pstmt = conn.prepareStatement(sql1);
            pstmt.setString(1, pattern);
            int r1 = pstmt.executeUpdate();
            pstmt.close();
            System.out.println("Deleted Tickets: " + r1);

            // Ensure booking_id is treated as an INT in cleanup queries
            String sql2 = "DELETE b FROM Bookings b JOIN Tickets t2 ON b.booking_id = t2.booking_id JOIN Showtimes s2 ON t2.showtime_id = s2.showtime_id JOIN Movies m2 ON s2.movie_id = m2.movie_id WHERE m2.title LIKE ?";
            pstmt = conn.prepareStatement(sql2);
            pstmt.setString(1, pattern);
            int r2 = pstmt.executeUpdate();
            pstmt.close();
            System.out.println("Deleted Bookings: " + r2);

            String sql3 = "DELETE s3 FROM Showtimes s3 JOIN Movies m3 ON s3.movie_id = m3.movie_id WHERE m3.title LIKE ?";
            pstmt = conn.prepareStatement(sql3);
            pstmt.setString(1, pattern);
            int r3 = pstmt.executeUpdate();
            pstmt.close();
            System.out.println("Deleted Showtimes: " + r3);

            String sql4 = "DELETE m3 FROM Movies m3 WHERE m3.title LIKE ?";
            pstmt = conn.prepareStatement(sql4);
            pstmt.setString(1, pattern);
            int r4 = pstmt.executeUpdate();
            pstmt.close();
            System.out.println("Deleted Movies: " + r4);

            System.out.println("Test data cleanup finished for pattern: " + pattern);
        } catch (SQLException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) { /* ignore */ }
        }
    }
}
