CREATE DATABASE cinema_eBooking_system;
USE cinema_eBooking_system;

CREATE TABLE Movies (
    movie_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(50),
    rating VARCHAR(10),            
    description TEXT,
    poster_url VARCHAR(255),       
    trailer_url VARCHAR(255)       
);

CREATE TABLE Showtimes (
    showtime_id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT,
    showtime TIME,
    FOREIGN KEY (movie_id) REFERENCES Movies(movie_id)
);

INSERT INTO Movies (title, genre, rating, description, poster_url, trailer_url)
VALUES
('Inception', 'Sci-Fi', 'PG-13', 'A thief who steals corporate secrets through dream-sharing technology is given a chance to erase his past crimes.', 'inception.jpg', 'https://www.youtube.com/watch?v=YoHD9XEInc0&pp=ygURaW5jZXB0aW9uIHRyYWlsZXI%3D'),
('The Matrix', 'Sci-Fi', 'R', 'A computer hacker learns about the true nature of reality and his role in the war against its controllers.', 'matrix.jpg', 'https://www.youtube.com/watch?v=vKQi3bBA1y8&pp=ygUObWF0cml4IHRyYWlsZXI%3D'),
('The Godfather', 'Crime', 'R', 'The aging patriarch of an organized crime dynasty transfers control of his empire to his reluctant son.', 'godfather.jpg', 'https://www.youtube.com/watch?v=sY1S34973zA&pp=ygURZ29kZmF0aGVyIHRyYWlsZXI%3D');

INSERT INTO Showtimes (movie_id, showtime) VALUES
(1, '14:00:00'), (1, '17:00:00'), (1, '20:00:00'),
(2, '13:30:00'), (2, '16:30:00'), (2, '19:30:00'),
(3, '15:00:00'), (3, '18:00:00'), (3, '21:00:00');

-- Useful Queries (for backend/frontend)
-- Get all movies
SELECT * FROM Movies;

-- Search by title (example: Inception)
SELECT * FROM Movies
WHERE title LIKE '%Inception%';

-- Filter by genre (example: Sci-Fi)
SELECT * FROM Movies
WHERE genre = 'Sci-Fi';

-- Get movie details with showtimes (example: movie_id = 1)
SELECT m.title, m.rating, m.description, m.poster_url, m.trailer_url, s.showtime
FROM Movies m
JOIN Showtimes s ON m.movie_id = s.movie_id
WHERE m.movie_id = 1;
