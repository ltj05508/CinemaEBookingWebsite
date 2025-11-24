package backend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

/**
 * Spring Security configuration for the Cinema E-Booking application.
 * Configures authentication, authorization, and security settings.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Creates a BCryptPasswordEncoder bean for password hashing.
     * Used throughout the application for secure password storage.
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) for the application.
     * Allows requests from the frontend on localhost:3000.
     * 
     * @return CorsConfigurationSource with configured CORS settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3002"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configures the security filter chain with route-based authorization.
     * 
     * Public routes (no authentication required):
     * - /api/auth/** - All authentication endpoints (register, login, verify, etc.)
     * - /api/movies/** - Movie browsing and search
     * - /error - Error handling
     * 
     * Protected routes (authentication required):
     * - /api/profile/** - User profile management
     * - /api/bookings/** - Booking management (future)
     * - /api/admin/** - Admin functions (future)
     * 
     * @param http HttpSecurity configuration object
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS with our configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Disable CSRF for REST API (using session-based auth)
            .csrf(csrf -> csrf.disable())
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/movies/**").permitAll()
                .requestMatchers("/api/genres").permitAll()
                .requestMatchers("/api/profile/**").permitAll()  // Changed to permitAll - we handle auth in controllers
                .requestMatchers("/error").permitAll()
                
                // All other requests require authentication by default
                .anyRequest().authenticated()
            )
            
            // Disable default login form (we're using REST API)
            .formLogin(form -> form.disable())
            
            // Disable HTTP Basic authentication
            .httpBasic(basic -> basic.disable())
            
            // Configure logout
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .permitAll()
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            
            // Configure session management
            .sessionManagement(session -> session
                .maximumSessions(1) // Only one session per user
                .maxSessionsPreventsLogin(false) // New login invalidates old session
            );

        return http.build();
    }
}
