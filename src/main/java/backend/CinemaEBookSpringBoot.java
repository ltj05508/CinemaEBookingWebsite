package backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CinemaEBookSpringBoot {
    public static void main(String[] args) {
        // Initialize encryption key for payment card encryption
        // In production, this should come from environment variables or secure key management
        EncryptionUtil.setEncryptionKey("CinemaEBookKey16"); // Must be exactly 16 characters for AES-128
        
        SpringApplication.run(CinemaEBookSpringBoot.class, args);
        System.out.println("🎬 Cinema E-Booking API is running on http://localhost:8080");
        System.out.println("📚 API Endpoints:");
        System.out.println("   GET /api/movies");
        System.out.println("   GET /api/movies/search?title=batman");
        System.out.println("   GET /api/movies/filter?genre=action");
        System.out.println("   GET /api/genres");
    }
}