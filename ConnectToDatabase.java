import java.sql.*;
public class ConnectToDatabase {
    public static Connection conn = null;
    public static void main(String[] args) {
        String hostURL = "127.0.0.1";
        String databaseName = "CinemaEBooking";
        String username = "root";
        String password = "Booboorex"; //replace with your own password

        setUpConnection(hostURL, databaseName, username, password);

        readMovies();
        String[] movieData = retrieveMovieData(1);

        if (conn != null) {
            try {
                conn.close();
            } catch(Exception e) {
                System.out.println("Did not close conn :'(");
            }
        }
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