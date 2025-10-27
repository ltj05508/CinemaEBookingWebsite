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

    public Customer getCustomerInfo(String userId) {
        Customer cust = new Customer();
        try {
            Connection conn = getConnection();
            PreparedStatement state = conn.prepareStatement("SELECT * FROM PaymentCards AS p INNER JOIN Customers AS c ON p.customer_id = c.customer_id INNER JOIN Users as u ON c.customer_id = u.user_id WHERE c.customer_id = ?");
            state.setString(1, userId);
            ResultSet rs = state.executeQuery();

            if (rs.next()) {
                cust.setUserId(rs.getString("user_id"));
                cust.setFirstName(rs.getString("first_name"));
                cust.setLastName(rs.getString("last_name"));
                cust.setEmail(rs.getString("email"));
                cust.setPassword(rs.getString("password"));
                cust.setLoginStatus(rs.getBoolean("login_status"));
                cust.setCustomerId(rs.getString("customer_id"));
                cust.setPaymentCard(new PaymentCard(rs.getString("card_id"), rs.getString("card_number"), rs.getString("billing_address_id"), rs.getDate("expiration_date"), rs.getString("customer_id")));
            }
        } catch (SQLException se) {
            System.err.println("Error in getCustomerInfo: " + se);
            se.printStackTrace();
        }
        return cust;
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
