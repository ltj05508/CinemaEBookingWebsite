<#
cleanup-test-data.ps1
PowerShell helper to remove test movies and related rows from the local MySQL database.

Usage:
  # Default pattern: 'Test Movie%'
  .\cleanup-test-data.ps1

  # Custom pattern
  .\cleanup-test-data.ps1 -Pattern "Test Movie%"

Notes:
  - The script will attempt to use the `mysql` CLI if available; otherwise it will fall back
    to running the Java helper class (requires `mvn` and the workspace built first).
  - Adjust DB credentials below if you use different settings or prefer to provide them as env vars.
#>

param(
    [string]$Pattern = "Test Movie%",
    [string]$DBHost = "127.0.0.1",
    [int]$DBPort = 3306,
    [string]$DBName = "cinema_eBooking_system",
    [string]$DBUser = "root",
    [string]$DBPass = "Booboorex"
)

function Has-MySqlCli {
    $p = Get-Command mysql -ErrorAction SilentlyContinue
    return $null -ne $p
}

$sql = @"
DELETE t
FROM Tickets t
JOIN Showtimes s ON t.showtime_id = s.showtime_id
JOIN Movies m ON s.movie_id = m.movie_id
WHERE m.title LIKE '${Pattern}';

DELETE b
FROM Bookings b
JOIN Tickets t2 ON b.booking_id = t2.booking_id
JOIN Showtimes s2 ON t2.showtime_id = s2.showtime_id
JOIN Movies m2 ON s2.movie_id = m2.movie_id
WHERE m2.title LIKE '${Pattern}';

DELETE s3
FROM Showtimes s3
JOIN Movies m3 ON s3.movie_id = m3.movie_id
WHERE m3.title LIKE '${Pattern}';

DELETE m3
FROM Movies m3
WHERE m3.title LIKE '${Pattern}';
"@

Try {
    if (Has-MySqlCli) {
        Write-Host "Using mysql CLI to run cleanup SQL..."
        $tmpFile = [System.IO.Path]::GetTempFileName()
        $tmpFileSql = "$tmpFile.sql"
        $sql | Out-File -FilePath $tmpFileSql -Encoding UTF8
        $mysqlCmd = "mysql --host=$DBHost --port=$DBPort --user=$DBUser --password=$DBPass $DBName < `"$tmpFileSql`""
        Write-Host "Executing: $mysqlCmd"
        cmd /c $mysqlCmd
        Remove-Item $tmpFileSql -Force -ErrorAction SilentlyContinue
        Write-Host "Cleanup (mysql CLI) completed for pattern: $Pattern"
    } else {
        Write-Host "mysql CLI not found. Attempting Java helper fallback using mvn library copy + java run..."
        # copy dependencies and run Java helper with dependency classpath to ensure JDBC driver is available
        $copyCmd = "mvn -DskipTests dependency:copy-dependencies"
        Write-Host "Executing: $copyCmd"
        cmd /c $copyCmd
        $cp = "target\classes;target\dependency\*"
        $javaCmd = "java -cp `"$cp`" backend.TestDataCleanup `"$Pattern`""
        Write-Host "Executing: $javaCmd"
        cmd /c $javaCmd
        Write-Host "Cleanup (Java helper) completed for pattern: $Pattern"
    }
} Catch {
    Write-Host "Cleanup failed: $($_.ToString())"
    exit 1
}

exit 0
