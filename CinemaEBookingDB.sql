DROP DATABASE IF EXISTS cinema_eBooking_system;
CREATE DATABASE cinema_eBooking_system;
USE cinema_eBooking_system;

CREATE TABLE IF NOT EXISTS Users (
    user_id        VARCHAR(50) PRIMARY KEY,
    first_name     VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password      VARCHAR(255) NOT NULL,
    login_status   BOOLEAN DEFAULT FALSE,
    marketing_opt_in BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS Admins (
    admin_id   VARCHAR(50) PRIMARY KEY,
    FOREIGN KEY (admin_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Customers (
    customer_id   VARCHAR(50) PRIMARY KEY,
    state        ENUM('Active', 'Inactive', 'Suspended') NOT NULL DEFAULT 'Active',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Addresses (
    address_id    VARCHAR(50) PRIMARY KEY,
    street       VARCHAR(255),
    city         VARCHAR(100),
    state        VARCHAR(50),
    postal_code   VARCHAR(20),
    country      VARCHAR(50),
    customer_id   VARCHAR(50) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS PaymentCards (
    card_id         VARCHAR(50) PRIMARY KEY,
    card_number     VARCHAR(50) NOT NULL,
    expiration_date DATE NOT NULL,
    customer_id     VARCHAR(50) NOT NULL,
    billing_address_id VARCHAR(50),
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
        ON DELETE CASCADE,
    FOREIGN KEY (billing_address_id) REFERENCES Addresses(address_id)
        ON DELETE CASCADE
);

CREATE TABLE Movies (
    movie_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(50),
    rating VARCHAR(10),            
    description TEXT,
    duration_minutes INT,
    currently_showing BOOL,
    poster_url VARCHAR(255),       
    trailer_url VARCHAR(255)       
);

CREATE TABLE IF NOT EXISTS Promotions (
    promo_id       VARCHAR(50) PRIMARY KEY,
    code          VARCHAR(50) UNIQUE NOT NULL,
    description   TEXT,
    discount_percent DECIMAL(5,2),
    valid_from     DATE,
    valid_to       DATE
);

CREATE TABLE IF NOT EXISTS Theatres (
    theatre_id     VARCHAR(50) PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    address       VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS Showrooms (
    showroom_id    VARCHAR(50) PRIMARY KEY,
    name          VARCHAR(255),
    seat_count     	INT,
    num_of_rows		INT,
    num_of_cols 	INT,
    theatre_id     VARCHAR(50) NOT NULL,
    FOREIGN KEY (theatre_id) REFERENCES Theatres(theatre_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Seats (
    seat_id        VARCHAR(50) PRIMARY KEY,
    row_label      VARCHAR(10),
    seat_number    INT,
    showroom_id    VARCHAR(50) NOT NULL,
    FOREIGN KEY (showroom_id) REFERENCES Showrooms(showroom_id) #replace with showtime_id?
        ON DELETE CASCADE
);

CREATE TABLE Showtimes (
    showtime_id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT NOT NULL,
    showroom_id VARCHAR(50) NOT NULL,
    showtime TIME,
    FOREIGN KEY (movie_id) REFERENCES Movies(movie_id),
    FOREIGN KEY (showroom_id) REFERENCES Showrooms(showroom_id)
);

CREATE TABLE IF NOT EXISTS Bookings (
    booking_id     CHAR(50) PRIMARY KEY,
    booking_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status        VARCHAR(50),
    total_price    DECIMAL(10,2),
    customer_id    VARCHAR(50) NOT NULL,
    promo_id       VARCHAR(50),
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
        ON DELETE CASCADE,
    FOREIGN KEY (promo_id) REFERENCES Promotions(promo_id)
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS Payments (
    payment_id     VARCHAR(50) PRIMARY KEY,
    booking_id     INT UNIQUE NOT NULL,
    payment_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount        DECIMAL(10,2) NOT NULL,
    card_id        VARCHAR(50) NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id)
        ON DELETE CASCADE,
    FOREIGN KEY (card_id) REFERENCES PaymentCards(card_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Tickets (
    ticket_id      VARCHAR(50) PRIMARY KEY,
    seat_id        VARCHAR(50) NOT NULL, #Ax2, Cx7
    showtime_id        INT NOT NULL,
    booking_id     INT NOT NULL,
    price         DECIMAL(10,2),
    type          ENUM('adult','senior','child'),
    #FOREIGN KEY (seat_id) REFERENCES Seats(seat_id)
     #   ON DELETE CASCADE,
    FOREIGN KEY (showtime_id) REFERENCES Showtimes(showtime_id)
        ON DELETE CASCADE,
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id)
        ON DELETE CASCADE
    #UNIQUE (seat_id, showtime_id) -- ensures logical seat per show
);

-- Insert movies with proper currently_showing values
INSERT INTO Movies (title, genre, rating, description, duration_minutes, currently_showing, poster_url, trailer_url)
VALUES
('Inception', 'Sci-Fi', 'PG-13', 'A thief who steals corporate secrets through dream-sharing technology is given a chance to erase his past crimes.', 148, false, 'https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg', 'https://www.youtube.com/watch?v=YoHD9XEInc0'),
('The Matrix', 'Sci-Fi', 'R', 'A computer hacker learns about the true nature of reality and his role in the war against its controllers.', 136, false, 'https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg', 'https://www.youtube.com/watch?v=vKQi3bBA1y8'),
('The Godfather', 'Crime', 'R', 'The aging patriarch of an organized crime dynasty transfers control of his empire to his reluctant son.', 177, true, 'https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja1.jpg', 'https://www.youtube.com/watch?v=sY1S34973zA'),
('Interstellar', 'Sci-Fi', 'PG-13', 'A team of explorers travels through a wormhole in space in an attempt to ensure humanity survival.', 169, true, 'https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg', 'https://www.youtube.com/watch?v=zSWdZVtXT7E'),
('The Dark Knight', 'Action', 'PG-13', 'Batman sets out to dismantle organized crime in Gotham but finds himself facing the Joker.', 152, true, 'https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg', 'https://www.youtube.com/watch?v=EXeTwQWrcwY'),
('Pulp Fiction', 'Crime', 'R', 'The lives of two mob hitmen, a boxer, and a pair of diner bandits intertwine in four tales.', 149, false, 'https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg', 'https://www.youtube.com/watch?v=s7EdQ4FqbhY');

INSERT INTO Theatres (theatre_id, name, address)
VALUES
(1, 'Theatre1', '12 Earnhart Lane'),
(2, 'Theatre2', '213 Cricket Drive');

INSERT INTO Showrooms (showroom_id, name, seat_count, num_of_rows, num_of_cols, theatre_id)
VALUES
('1', 'Showroom1', 75, 5, 15, 1),
('2', 'Showroom2', 105, 7, 15, 1),
('3', 'RoyalShowroom', 150, 10, 15, 2);

-- Insert showtimes (only once per movie-time combination)
INSERT INTO Showtimes (movie_id, showroom_id, showtime) VALUES
(1, '1', '14:00:00'), (1, '1', '17:00:00'), (1, '1', '20:00:00'),
(2, '1', '13:30:00'), (2, '1', '16:30:00'), (2, '1', '19:30:00'),
(3, '2', '15:00:00'), (3, '2', '18:00:00'), (3, '2', '21:00:00'),
(4, '2', '16:30:00'), (4, '2', '19:30:00'), (4, '2', '22:30:00'),
(5, '3', '15:30:00'), (5, '3', '18:30:00'), (5, '3', '21:30:00'),
(6, '3', '12:30:00'), (6, '3', '15:30:00'), (6, '3', '18:30:00');

INSERT INTO Users (user_id, first_name, last_name, email, password, login_status)
VALUES
('1', 'John', 'Smith', 'johnsmith22@gmail.com', 'password123', false),
('2', 'Leo', 'Jahn', 'leojahn@gmail.com', 'goodPassword!', false),
('3', 'Ahdmin', 'Jones', 'ahdminjones123@gmail.com', 'strongPassword', false);

INSERT INTO Customers (customer_id, state)
VALUES
('1', 'Active'),
('2', 'Inactive');

INSERT INTO Addresses (address_id, street, city, state, postal_code, country, customer_id)
VALUES
('1', '45 Brickroad Street', 'Atlanta', 'Georgia', '30033', 'United States', '1');

INSERT INTO PaymentCards(card_id, card_number, expiration_date, customer_id, billing_address_id)
VALUES
('1', '12345678', '2030-10-11', '1', '1'),
('2', '98765432', '2025-09-01', '1', '1');

INSERT INTO Admins (admin_id)
VALUES
('3');

INSERT INTO Promotions(promo_id, code, description, discount_percent, valid_from, valid_to)
VALUES
('1', 'promo1234', 'Test promotion for 15% off!', 15.00, '2025-10-01', '2025-12-25');

INSERT INTO Bookings(booking_id, booking_date, status, total_price, customer_id, promo_id)
VALUES
('1', CURRENT_TIMESTAMP, 'active', 24, 1, '1'),
('2', CURRENT_TIMESTAMP, 'active', 12, 2, '1');

INSERT INTO Tickets(ticket_id, seat_id, showtime_id, booking_id, price, type)
VALUES
('1', 'Ax5', 4, 1, 10, 'adult'),
('2', 'Cx5', 9, 1, 10, 'adult'),
('3', 'Dx6', 4, 2, 10, 'adult'),
('4', 'Dx7', 4, 2, 10, 'adult'),
('5', 'Ax6', 4, 1, 10, 'adult');

