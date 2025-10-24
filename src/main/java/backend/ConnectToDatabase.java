package backend;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class ConnectToDatabase {
    public static Connection conn = null;
    public static List<Movie> movies;

    public static Properties props = new Properties();
    public static void main(String[] args) {
        movies = new ArrayList<>();
        String hostURL = "127.0.0.1";
        String databaseName = "CinemaEBooking";
        String username = "root";
        String password = "Booboorex"; //replace with your own password

        setUpConnection(hostURL, databaseName, username, password);


        //readMovies();
        //String[] movieData = retrieveMovieData(1);


        //sendEmail("ljahn724@gmail.com", "Testing", "Hello,\n\nThis is a test email.");


        //getAllMovies();

        //User myUser = new User("1", "Jeff", "Schortz", "jschortz@gmail.com", "goodpassword", false);
        //Customer myCust = new Customer("1", "Jeff", "Schortz", "jschortz@gmail.com", "goodpassword", false, "1", State.Active);
        //insertCustomer(myCust);

        Customer newCust = getCustomerInfo("1");
        System.out.println(newCust);
        System.out.println(newCust.getPaymentCards()[0]);

        if (conn != null) {
            try {
                conn.close();
            } catch(Exception e) {
                System.out.println("Did not close conn :'(");
            }
        }
    }

    public static Customer getCustomerInfo(String userId) {
        Customer cust = new Customer();
        try {
            //Connection conn = getConnection();
            PreparedStatement state = conn.prepareStatement("SELECT * FROM PaymentCards AS p INNER JOIN Customers AS c ON p.customer_id = c.customer_id INNER JOIN Users as u ON c.customer_id = u.user_id WHERE c.customer_id = ?");
            state.setString(1, userId);
            ResultSet rs = state.executeQuery();

            rs.next();
            cust.setUserId(rs.getString("user_id"));
            cust.setFirstName(rs.getString("first_name"));
            cust.setLastName(rs.getString("last_name"));
            cust.setEmail(rs.getString("email"));
            cust.setPassword(rs.getString("password"));
            cust.setLoginStatus(rs.getBoolean("login_status"));
            cust.setCustomerId(rs.getString("customer_id"));
            cust.setPaymentCard(new PaymentCard(rs.getString("card_id"), rs.getString("card_number"), rs.getString("billing_address"), rs.getDate("expiration_date"), rs.getString("customer_id")));
        } catch (SQLException se) {
            System.err.println("Error in getCustomerInfo: " + se);
            se.printStackTrace();
        }
        return cust;
    }

    /**
     * Sends out an email containing the given subject line and body text to the given email address
     * Called for both forget password and registration confirmation?
     */

    public static void sendEmail(String toAddress, String subjectLine, String bodyText) {
        try {
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
            props.setProperty("mail.smtp.starttls.enable", "true");
            props.setProperty("mail.smtp.host", "smtp.gmail.com");

            Authenticator auth = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("noreplycinemaebooking@gmail.com", "eptp qpwv yhtm rfgc");
                }
            };

            Session session = Session.getDefaultInstance(props, auth);
            String fromAddress = "noreplycinemaebooking@gmail.com";

            javax.mail.Message msg = new javax.mail.internet.MimeMessage(session);
            msg.setFrom(new InternetAddress(fromAddress));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            msg.setSubject(subjectLine);
            msg.setText(bodyText);
            Transport.send(msg);

        } catch (MessagingException me) {
            System.err.println("Error in SendMessage!: " +me.getMessage());
            me.printStackTrace();
        }
    }


    public static void insertCustomer(Customer cust) {
        try {
            insertUser(cust);

            PreparedStatement state = conn.prepareStatement("INSERT INTO Customers (customer_id, state) VALUES (?, ?)");
            state.setString(1, cust.getUserId());
            state.setObject(2, State.Active.toString());

            //state = conn.prepareStatement("INSERT INTO Customers ()");
           // state.setString("");

            state.executeUpdate();
        } catch(SQLException se) {
            System.err.println("Error in insertCustomer! " + se);
            se.printStackTrace();
        }
    }


    public static void insertUser(User user) {
        try {
            PreparedStatement state = conn.prepareStatement("INSERT INTO Users (user_id, first_name, last_name, email, password, login_status) VALUES (?, ?, ?, ?, ?, ?)");
            state.setString(1, user.getUserId());
            state.setString(2, user.getFirstName());
            state.setString(3, user.getLastName());
            state.setString(4, user.getEmail());
            state.setString(5, user.getPassword());
            state.setBoolean(6, user.getLoginStatus());

            state.executeUpdate();
        } catch(SQLException se) {
            System.err.println("Error in insertUser! " + se);
            se.printStackTrace();
        }
    }



    public static List<Movie> getAllMovies() {
        try {
            Statement state = conn.createStatement();
            ResultSet resultSet = state.executeQuery("select * from Movies"); //cinema_eBooking_system
            //ResultSet showtimeSet = state.executeQuery("select * from Showtimes");
            System.out.println("Movies in Database");

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                System.out.println("Column " + i + ": " + metaData.getColumnName(i));
            }

            while(resultSet.next()) {
                Movie newMovie = new Movie(resultSet.getInt("movie_id"), resultSet.getString("title"), resultSet.getString("genre"), resultSet.getString("rating"),
                        resultSet.getString("description"), resultSet.getString("showtimes"), resultSet.getString("duration"), resultSet.getString("poster_url"),
                        resultSet.getString("trailer_url"), resultSet.getBoolean("currently_showing"));

                movies.add(newMovie);
                //showtimeSet.next();
            }
        } catch(Exception e) {
            System.out.println("Problem in getAllMovies!");
            e.printStackTrace();
        }
        return new ArrayList<>(movies);
    }

    /*
     * Creates a connection to the sql server using DriverManager.getConnection()
     */
    public static void setUpConnection(String hostURL, String databaseName, String username, String password) {
        try {                                     //"jdbc:mysql://151.101.1.69:3306/databasename?useUnicode=true&characterEncoding=utf8"
            conn = DriverManager.getConnection("jdbc:mysql://" +hostURL+ ":3306/" +databaseName+ "?enabledTLSProtocols=TLSv1.2", username, password); //Current: jdbc:mysql://192.168.1.185:3306/CinemaEBooking?useUnicode=true&characterEncoding=utf8
                    //"jdbc:mysql://" +hostURL+ ":3306/" +databaseName+ "?useUnicode=true&characterEncoding=utf8");
        }
        catch(Exception e) {
            System.out.println("Error in setUpConnection!");
            e.printStackTrace();
        }
    }

    /*
     * Prints entire database table into console
     */
    public static void readMovies() {
        try {
            Statement state = conn.createStatement();
            ResultSet resultSet = state.executeQuery("select * from Movies"); //cinema_eBooking_system
             System.out.println("Movies in Database");

             while(resultSet.next()) {
                 System.out.println(resultSet.getString("movie_id") + ", " + resultSet.getString("title") + ", " + resultSet.getString("genre") + ", " + resultSet.getString("rating") + ", " + resultSet.getString("movie_description") + ", " + resultSet.getString("showtimes") + ", " + resultSet.getString("duration"));
             }
             System.out.println();

             if (resultSet != null) {
                 resultSet.close();
             }

             if (state != null) {
                 state.close();
             }
        }
        catch(Exception e) {
            System.out.println("Error in readMovies!");
            e.printStackTrace();
        }
    }

    /*
     * Returns all the data for one of the movie entries based on its movie_id number
     */
    public static String[] retrieveMovieData(int movie_id) {
        String[] movieInfo = new String[10];
        try {
            Statement state = conn.createStatement();
            ResultSet resultSet = state.executeQuery("select * from Movies where movie_id=" +movie_id); //cinema_eBooking_system
            System.out.println("Movie " +movie_id+ " in Database");

            resultSet.next();
            movieInfo[0] = resultSet.getString("movie_id");
            movieInfo[1] = resultSet.getString("title");
            movieInfo[2] = resultSet.getString("genre");
            movieInfo[3] = resultSet.getString("rating");
            movieInfo[4] = resultSet.getString("movie_description");
            movieInfo[5] = resultSet.getString("showtimes");
            movieInfo[6] = resultSet.getString("duration");
            //movieInfo[7] = resultSet.getString("");

            if (resultSet != null) {
                resultSet.close();
            }

            if (state != null) {
                state.close();
            }

        }
        catch(Exception e) {
            System.out.println("Error in readMovies!");
            e.printStackTrace();
        }
        return movieInfo;
    }
}