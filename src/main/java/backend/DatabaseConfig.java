package backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * Configuration class to initialize the database connection on Spring Boot startup.
 * This ensures ConnectToDatabase.conn is initialized before any database operations.
 */
/*
@Configuration
public class DatabaseConfig {
    
    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;
    
    /**
     * Initialize database connection after Spring context is loaded.
     * Extracts host and database name from the JDBC URL and calls ConnectToDatabase.setUpConnection().
     */
/*
    @PostConstruct
    public void initializeDatabaseConnection() {
        try {
            System.out.println("=== Initializing Database Connection ===");
            System.out.println("JDBC URL: " + datasourceUrl);
            
            // Parse JDBC URL: jdbc:mysql://host:port/database
            String[] parts = datasourceUrl.split("//")[1].split("/");
            String hostURL = parts[0]; // e.g., "localhost:3306" or "127.0.0.1:3306"
            String databaseName = parts[1].split("\\?")[0]; // e.g., "cinema_eBooking_system"
            
            // Extract just the host without port if present
            if (hostURL.contains(":")) {
                hostURL = hostURL.split(":")[0];
            }
            
            System.out.println("Host: " + hostURL);
            System.out.println("Database: " + databaseName);
            System.out.println("Username: " + username);
            
            // Initialize the connection
            ConnectToDatabase.setUpConnection(hostURL, databaseName, username, password);
            
            if (ConnectToDatabase.conn != null && !ConnectToDatabase.conn.isClosed()) {
                System.out.println("✅ Database connection initialized successfully!");
            } else {
                System.err.println("❌ Database connection is null or closed!");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
*/
