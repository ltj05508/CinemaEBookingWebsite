CREATE SCHEMA `CinemaEBooking` ;
use CinemaEBooking;
CREATE TABLE IF NOT EXISTS cinemaebooking.movies (
	movie_id INT AUTO_INCREMENT PRIMARY KEY,
	title VARCHAR(255),
	genre VARCHAR(50),
    rating DOUBLE,
    movie_description VARCHAR(1000),
	showtimes VARCHAR(255),
	duration INT
);




INSERT INTO cinemaebooking.movies (title, genre, rating, movie_description, showtimes, duration)
VALUES
('Inception', 'Sci-Fi', 8.7, 'The tale as old as time', '1:30,3:00,4:30', 148),
('The Matrix', 'Sci-Fi', 9.0, 'Blah blah blah', '11:00,1:00,5:00', 136),
('Interstellar', 'Sci-Fi', 4.7, 'Heard good things', '10:30,12:00', 169),
('The Godfather', 'Crime', 10, 'Movie', '1:00,2:00,5:00,6:00', 175);