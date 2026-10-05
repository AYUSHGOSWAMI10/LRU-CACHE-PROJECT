# LRU Cache and Page Replacement Simulator

A small Java desktop application demonstrating a least recently used (LRU) cache and using the same cache behavior to simulate operating system page replacement. Page simulation results can be saved to and viewed from MySQL.

## Features

- Generic key-value LRU cache with `get`, `put`, `containsKey`, and `size` operations.
- Page replacement simulation that reports each page hit or fault and the current frame contents.
- MySQL history for simulation runs, including the frame count, reference string, hits, faults, hit percentage, and timestamp.
- A Swing dashboard with dedicated cache, simulator, and history screens.
- History refresh and confirmed clear actions.

## Requirements

- JDK 16 or newer (the project uses Java records and switch expressions).
- MySQL Server running locally, with a MySQL account the application can use.
- The MySQL Connector/J dependency included under `lib/`.

## Configure MySQL

Before launching the application, update the connection URL, username, and password in `src/com/lru/project/DatabaseManager.java` for your local MySQL setup. The URL targets `localhost:3306` and the database name `lrudb`; `createDatabaseIfNotExist=true` asks MySQL to create that database if it does not already exist. The application creates the `simulation_runs` table at startup.

Do not use real credentials in a shared commit. For anything beyond a local demo, load credentials from environment variables or another secret store instead of keeping them in source code.

## Run

From the `LRUDemo` directory:

```bash
./run.sh
```

The script compiles the Java sources into `out/` and starts `com.lru.project.AppMain` when compilation succeeds. You can also compile and launch manually:

```bash
javac -cp "lib/mysql-connector-j-26.7.0/*" -d out src/com/lru/project/*.java
java -cp "out:lib/mysql-connector-j-26.7.0/*" com.lru.project.AppMain
```

## Using the application

Use the dashboard cards to open the cache demo, page simulator, or history. The simulator accepts page numbers separated by spaces or commas and displays each hit/fault, memory state, and run totals. Successful runs are saved to MySQL. The history screen lists saved runs, newest first, and asks for confirmation before clearing them.

The cache prints entries from most recently used to least recently used. When full, inserting a new key evicts the least recently used entry.

## Project layout

```text
src/com/lru/project/
  AppMain.java                  Desktop application entry point
  DesktopApp.java               Swing dashboard and feature screens
  LRUCache.java                 Generic LRU cache implementation
  PageReplacementSimulator.java Page reference simulation
  DatabaseManager.java          MySQL connection and schema setup
  SimulationDAO.java            Save, list, and delete simulation history
lib/                            MySQL Connector/J dependency
run.sh                          Compile and launch script
```
