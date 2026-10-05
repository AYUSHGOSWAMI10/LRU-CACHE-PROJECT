# LRU Cache and Page Replacement Simulator

A small Spring Boot web application for visualizing the Least Recently Used (LRU) cache and using it for page replacement. The frontend is plain HTML, CSS, and JavaScript. Simulation history is stored in the existing MySQL lrudb.simulation_runs table.

## Requirements

- JDK 17 or newer; Spring Boot 4.1.1 supports Java 17 through 26.
- Apache Maven 3.6.3 or newer.
- MySQL Server available at localhost:3306 with access to the existing lrudb database and simulation_runs table.

## Configure MySQL

Connection settings are centralized in src/main/resources/application.properties:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/lrudb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
spring.datasource.username=${DB_USERNAME:ayush}
spring.datasource.password=${DB_PASSWORD:1234}
```

The values after the colon are local development defaults matching the current project configuration. Override them with DB_URL, DB_USERNAME, and DB_PASSWORD environment variables when needed. Credentials are only used by the backend and are never sent to the browser.

The application uses JDBC queries against the current table. It does not create, drop, or alter the database or table. Clear History deletes rows from simulation_runs only.

## Run

From this directory:

```bash
bash run.sh
```

Then open http://localhost:8080. You can also run mvn spring-boot:run directly. The launcher selects Java 26 on macOS when available and otherwise uses the default Java installation.

## Features

- **LRU Cache:** Put, update, and get keys; choose a capacity; view MRU-to-LRU order and any evicted key. Cache state is isolated per browser session.
- **Page Simulator:** Set frame count and enter page references separated by spaces or commas. The page-by-page table shows hits, faults, memory state, and evictions, along with totals and hit ratio.
- **Simulation History:** Read saved records from MySQL and refresh the table.
- **Clear History:** Confirm before deleting rows from the simulation history table.

## REST API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | /api/simulate | Run the existing LRU page replacement behavior and save its summary |
| GET | /api/history | Read saved simulations |
| DELETE | /api/history | Delete simulation rows only |
| POST | /api/cache | Perform a session-scoped cache PUT, GET, or RESET |
| GET | /api/health | Check that the web application is running |

The static browser UI calls these endpoints with fetch() and renders returned JSON.

## Layout

```text
src/main/java/com/lru/project/
  controller/                  REST endpoints and API error handling
  model/                       Request and response records
  repository/                  JDBC queries for simulation_runs
  service/                     LRU cache and simulation behavior
  LruWebApplication.java       Spring Boot entry point
src/main/resources/
  application.properties       MySQL and server configuration
  static/                      HTML, CSS, and vanilla JavaScript UI
legacy-console/                 Preserved pre-migration Java desktop sources
```

The original desktop implementation is retained under legacy-console/ as a source backup; Maven compiles only the Spring Boot application under src/main/java.
