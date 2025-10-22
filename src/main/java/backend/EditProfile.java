package backend;

import java.sql.*;

public class EditProfile {
    private static final String URL = "jdbc:mysql://localhost:3306/cinema_eBooking_system";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Booboorex";

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }

    public void updateFirstName(User user, String newFirstName) {
        try {
            Connection conn = getConnection();
            PreparedStatement state = conn.prepareStatement("UPDATE Users SET first_name = ? WHERE user_id = ?");
            state.setString(1, newFirstName);
            state.setString(2, user.getUserId());
            state.executeUpdate();

            user.setFirstName(newFirstName);
        } catch (SQLException se) {
            System.err.println("Error in updateFirstName: " + se);
            se.printStackTrace();
        }
    }

    public void updateLastName(User user, String newLastName) {
        try {
            Connection conn = getConnection();
            PreparedStatement state = conn.prepareStatement("UPDATE Users SET last_name = ? WHERE user_id = ?");
            state.setString(1, newLastName);
            state.setString(2, user.getUserId());
            state.executeUpdate();

            user.setLastName(newLastName);
        } catch (SQLException se) {
            System.err.println("Error in updateLastName: " + se);
            se.printStackTrace();
        }
    }

    public void updatePassword(User user, String newPassword) {
        try {
            Connection conn = getConnection();
            PreparedStatement state = conn.prepareStatement("UPDATE Users SET password = ? WHERE user_id = ?");
            state.setString(1, newPassword);
            state.setString(2, user.getUserId());
            state.executeUpdate();

            user.setPassword(newPassword);
        } catch (SQLException se) {
            System.err.println("Error in updatePassword: " + se);
            se.printStackTrace();
        }
    }


}
