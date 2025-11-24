#!/usr/bin/env bash
# cleanup-test-data.sh
# Run this script on Linux/macOS to remove test movies and related entries

DB_HOST=${DB_HOST:-127.0.0.1}
DB_PORT=${DB_PORT:-3306}
DB_USER=${DB_USER:-root}
DB_PASS=${DB_PASS:-Booboorex}
DB_NAME=${DB_NAME:-cinema_eBooking_system}
PATTERN=${1:-"Test Movie%"}

SQL=$(cat <<EOF
DELETE t
FROM Tickets t
JOIN Showtimes s ON t.showtime_id = s.showtime_id
JOIN Movies m ON s.movie_id = m.movie_id
WHERE m.title LIKE '${PATTERN}';

DELETE b
FROM Bookings b
JOIN Tickets t2 ON b.booking_id = t2.booking_id
JOIN Showtimes s2 ON t2.showtime_id = s2.showtime_id
JOIN Movies m2 ON s2.movie_id = m2.movie_id
WHERE m2.title LIKE '${PATTERN}';

DELETE s3
FROM Showtimes s3
JOIN Movies m3 ON s3.movie_id = m3.movie_id
WHERE m3.title LIKE '${PATTERN}';

DELETE m3
FROM Movies m3
WHERE m3.title LIKE '${PATTERN}';
EOF
)

echo "Running cleanup for pattern: ${PATTERN}"
mysql --host=$DB_HOST --port=$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME -e "$SQL"

echo "Done"
