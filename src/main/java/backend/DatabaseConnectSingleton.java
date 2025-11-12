package backend;

import java.sql.*;

public class DatabaseConnectSingleton {
        private Connection conn = null;
        private static DatabaseConnectSingleton instance = null;

        private DatabaseConnectSingleton() {
            try {
                System.out.println("=== Initializing Database Connection ===");
                String hostURL = "127.0.0.1";
                String databaseName = "cinema_eBooking_system";
                String username = "root";
                String password = "Booboorex";

                conn = DriverManager.getConnection("jdbc:mysql://" +hostURL+ ":3306/" +databaseName+ "?enabledTLSProtocols=TLSv1.2", username, password); //Current: jdbc:mysql://192.168.1.185:3306/CinemaEBooking?useUnicode=true&characterEncoding=utf8

                if (conn != null && !conn.isClosed()) {
                    System.out.println("✅ Database connection initialized successfully!");
                } else {
                    System.err.println("❌ Database connection is null or closed!");
                }

            } catch (Exception e) {
                System.err.println("❌ Failed to initialize database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }

        public static DatabaseConnectSingleton getInstance() {
            if (instance == null) {
                instance = new DatabaseConnectSingleton();
            }
            return instance;
        }

        public Connection getConn() {
            return conn;
        }
    }


