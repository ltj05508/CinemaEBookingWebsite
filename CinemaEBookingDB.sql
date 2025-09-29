DROP DATABASE IF EXISTS cinema_eBooking_system;
CREATE DATABASE cinema_eBooking_system;
USE cinema_eBooking_system;

CREATE TABLE Movies (
    movie_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(50),
    rating VARCHAR(10),            
    description TEXT,
    duration VARCHAR(10),
    currently_showing BOOL,
    poster_url VARCHAR(255),       
    trailer_url VARCHAR(255)       
);

CREATE TABLE Showtimes (
    showtime_id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT,
    showtime TIME,
    FOREIGN KEY (movie_id) REFERENCES Movies(movie_id)
);

-- Insert movies with proper currently_showing values
INSERT INTO Movies (title, genre, rating, description, duration, currently_showing, poster_url, trailer_url)
VALUES
('Inception', 'Sci-Fi', 'PG-13', 'A thief who steals corporate secrets through dream-sharing technology is given a chance to erase his past crimes.', '2:28', false, 'https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg', 'https://www.youtube.com/watch?v=YoHD9XEInc0'),
('The Matrix', 'Sci-Fi', 'R', 'A computer hacker learns about the true nature of reality and his role in the war against its controllers.', '2:16', false, 'https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg', 'https://www.youtube.com/watch?v=vKQi3bBA1y8'),
('The Godfather', 'Crime', 'R', 'The aging patriarch of an organized crime dynasty transfers control of his empire to his reluctant son.', '2:57', true, 'https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja1.jpg', 'https://www.youtube.com/watch?v=sY1S34973zA'),
('Interstellar', 'Sci-Fi', 'PG-13', 'A team of explorers travels through a wormhole in space in an attempt to ensure humanity survival.', '2:49', true, 'https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg', 'https://www.youtube.com/watch?v=zSWdZVtXT7E'),
('The Dark Knight', 'Action', 'PG-13', 'Batman sets out to dismantle organized crime in Gotham but finds himself facing the Joker.', '2:32', true, 'https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg', 'https://www.youtube.com/watch?v=EXeTwQWrcwY'),
('Pulp Fiction', 'Crime', 'R', 'The lives of two mob hitmen, a boxer, and a pair of diner bandits intertwine in four tales.', '2:29', false, 'https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg', 'https://www.youtube.com/watch?v=s7EdQ4FqbhY');

-- Insert showtimes (only once per movie-time combination)
INSERT INTO Showtimes (movie_id, showtime) VALUES
(1, '14:00:00'), (1, '17:00:00'), (1, '20:00:00'),
(2, '13:30:00'), (2, '16:30:00'), (2, '19:30:00'),
(3, '15:00:00'), (3, '18:00:00'), (3, '21:00:00'),
(4, '16:30:00'), (4, '19:30:00'), (4, '22:30:00'),
(5, '15:30:00'), (5, '18:30:00'), (5, '21:30:00'),
(6, '12:30:00'), (6, '15:30:00'), (6, '18:30:00');