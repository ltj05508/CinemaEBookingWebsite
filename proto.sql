CREATE SCHEMA IF NOT EXISTS `CinemaEBooking` ;
use CinemaEBooking;

#CREATE TYPE customer_state AS ENUM('Active', 'Inactive', 'Suspended');
#CREATE TYPE ticket_type AS ENUM('adult', 'senior', 'child');

CREATE TABLE IF NOT EXISTS Users (
    user_id        VARCHAR(50) PRIMARY KEY,
    first_name     VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password      VARCHAR(255) NOT NULL,
    login_status   BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS Admins (
    admin_id   VARCHAR(50) PRIMARY KEY,
    FOREIGN KEY (admin_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Customers (
    customer_id   VARCHAR(50) PRIMARY KEY,
    state         ENUM('Active', 'Inactive', 'Suspended') NOT NULL DEFAULT 'Active',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS PaymentCards (
    card_id         VARCHAR(50) PRIMARY KEY,
    card_number     VARCHAR(20) NOT NULL,
    billing_address VARCHAR(255),
    expiration_date DATE NOT NULL,
    customer_id     VARCHAR(50) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Movies (
	movie_id INT AUTO_INCREMENT PRIMARY KEY,
	title VARCHAR(255) NOT NULL,
	genre VARCHAR(50),
    rating DOUBLE,
    movie_description VARCHAR(1000),
	showtimes VARCHAR(255),
	duration INT
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
    seat_count     INT,
    theatre_id     VARCHAR(50) NOT NULL,
    FOREIGN KEY (theatre_id) REFERENCES Theatres(theatre_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Shows (
    show_id        VARCHAR(50) PRIMARY KEY,
    show_time      TIMESTAMP NOT NULL,
    duration_minutes INT,
    movie_id       INT NOT NULL,
    showroom_id    VARCHAR(50) NOT NULL,
    FOREIGN KEY (movie_id) REFERENCES Movies(movie_id)
        ON DELETE CASCADE,
    FOREIGN KEY (showroom_id) REFERENCES Showrooms(showroom_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Seats (
    seat_id        VARCHAR(50) PRIMARY KEY,
    row_label      VARCHAR(10),
    seat_number    INT,
    showroom_id    VARCHAR(50) NOT NULL,
    FOREIGN KEY (showroom_id) REFERENCES Showrooms(showroom_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Bookings (
    booking_id     VARCHAR(50) PRIMARY KEY,
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

CREATE TABLE IF NOT EXISTS Tickets (
    ticket_id      VARCHAR(50) PRIMARY KEY,
    seat_number    VARCHAR(10),
    price         DECIMAL(10,2),
    type          ENUM('adult','senior','child'),
    show_id        VARCHAR(50) NOT NULL,
    booking_id     VARCHAR(50) NOT NULL,
    seat_id        VARCHAR(50),
    FOREIGN KEY (show_id) REFERENCES Shows(show_id)
        ON DELETE CASCADE,
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id)
        ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES Seats(seat_id)
        ON DELETE CASCADE
);

INSERT INTO Movies (title, genre, rating, movie_description, showtimes, duration)
VALUES
('Inception', 'Sci-Fi', 8.7, 'The tale as old as time', '1:30,3:00,4:30', 148),
('The Matrix', 'Sci-Fi', 9.0, 'Blah blah blah', '11:00,1:00,5:00', 136),
('Interstellar', 'Sci-Fi', 4.7, 'Heard good things', '10:30,12:00', 169),
('The Godfather', 'Crime', 10, 'Movie', '1:00,2:00,5:00,6:00', 175);

INSERT INTO Users (user_id, first_name, last_name, email, password, login_status)
VALUES
('1', 'John', 'Smith', 'johnsmith22@gmail.com', 'password123', false),
('2', 'Leo', 'Jahn', 'ljahn724@gmail.com', 'goodPassword!', false),
('3', 'Ahdmin', 'Jones', 'ahdminjones123@gmail.com', 'strongPassword', false);

INSERT INTO Customers (customer_id, state)
VALUES
('1', 'Active'),
('2', 'Inactive');

INSERT INTO PaymentCards(card_id, card_number, billing_address, expiration_date, customer_id)
VALUES
('1', '12345678', '123 Sycamore Lane', '2030-10-11', '1'),
('2', '98765432', '123 Sycamore Lane', '2025-09-01', '1');

INSERT INTO Admins (admin_id)
VALUES
('3');

INSERT INTO Promotions(promo_id, code, description, discount_percent, valid_from, valid_to)
VALUES
('1', 'promo1234', 'Test promotion for 15% off!', 15.00, '2025-10-01', '2025-12-25');


