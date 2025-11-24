TESTING and CLEANUP

This file includes instructions for running the functional tests and cleaning up test data.

Running tests
----------------
Run the functional tests:

```powershell
cd C:\Users\natha\CinemaEbook\CinemaEBookingWebsite
mvn -Dtest=ShowtimeFunctionalTests test
```

If you prefer to run the entire test suite:

```powershell
mvn test
```

About cleanup
----------------
The functional tests previously modified the database by creating movies, showtimes and bookings. To ensure your DB remains clean, these tests now track created IDs and attempt to delete them in an `@AfterEach` teardown. However, as an additional safety or for cleaning up leftover test data from earlier runs, there are helpers in the repo:

- `cleanup-test-data.ps1` (PowerShell):
  - Uses the `mysql` CLI if available.
  - If `mysql` CLI is not installed, falls back to invoking a Java helper via `mvn exec:java`.
  - Default pattern: `Test Movie%`.

- `cleanup-test-data.sh` (Bash):
  - Cross-platform shell script for Linux/macOS/WSL.
  - Requires the `mysql` CLI to be installed and in your PATH.

- Java helper: `backend.TestDataCleanup`
  - You can run it with Maven: 
    ```powershell
    mvn -DskipTests exec:java -Dexec.mainClass=backend.TestDataCleanup -Dexec.args="'Test Movie%'"
    ```

Usage examples:

PowerShell:
```powershell
cd C:\Users\natha\CinemaEbook\CinemaEBookingWebsite
.\cleanup-test-data.ps1 -Pattern "Test Movie%"
```

Bash:
```bash
cd /path/to/CinemaEBookingWebsite
./cleanup-test-data.sh "Test Movie%"
```

Notes and recommendations:
* If you need to delete only the rows created during one test run, consider adding unique titles (e.g. `Test Movie 2025-11-24_0935`) and then pass that full pattern as `-Pattern` to the script.
* For a highly repeatable CI setup, consider using Testcontainers instead of operating on a shared local DB.
