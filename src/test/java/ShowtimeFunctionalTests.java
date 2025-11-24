package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShowtimeFunctionalTests {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testSchedulingConflict() {
        // Arrange: Create a showtime in a showroom at a specific time
        Map<String, Object> showtime1 = new HashMap<>();
        showtime1.put("showroomId", 1);
        showtime1.put("movieId", 1);
        showtime1.put("startTime", "2025-11-23T18:00:00");
        showtime1.put("endTime", "2025-11-23T20:00:00");
        ResponseEntity<String> response1 = restTemplate.postForEntity("/api/showtimes", showtime1, String.class);
        Assertions.assertEquals(HttpStatus.OK, response1.getStatusCode());

        // Act: Try to create a conflicting showtime in the same showroom
        Map<String, Object> showtime2 = new HashMap<>();
        showtime2.put("showroomId", 1);
        showtime2.put("movieId", 2);
        showtime2.put("startTime", "2025-11-23T19:00:00"); // Overlaps with previous
        showtime2.put("endTime", "2025-11-23T21:00:00");
        ResponseEntity<String> response2 = restTemplate.postForEntity("/api/showtimes", showtime2, String.class);
        // Assert: Should fail due to conflict
        Assertions.assertTrue(response2.getStatusCode().is4xxClientError() || response2.getStatusCode().is5xxServerError());
    }

    @Test
    public void testShowtimeVisibility() {
        // Arrange: Create a showtime in the future
        Map<String, Object> showtime = new HashMap<>();
        showtime.put("showroomId", 2);
        showtime.put("movieId", 3);
        showtime.put("startTime", "2025-12-01T18:00:00");
        showtime.put("endTime", "2025-12-01T20:00:00");
        restTemplate.postForEntity("/api/showtimes", showtime, String.class);

        // Act: Get visible showtimes
        ResponseEntity<String> response = restTemplate.getForEntity("/api/showtimes/visible", String.class);
        // Assert: The created showtime should be present in the response
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("2025-12-01T18:00:00"));
    }
}
