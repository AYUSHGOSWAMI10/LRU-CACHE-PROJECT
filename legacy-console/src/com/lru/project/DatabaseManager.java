package com.lru.project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
	// Apna MySQL username/password yahan badlo
	private static final String URL = "jdbc:mysql://localhost:3306/lrudb" + "?createDatabaseIfNotExist=true"
			+ "&useSSL=false" + "&allowPublicKeyRetrieval=true" + "&serverTimezone=UTC" ;
	private static final String USER = "ayush";   // demo name
	private static final String PASSWORD = "1234";  //demo password

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}

	/**
	 * App start par ek baar call karo: database/table nahi hai to bana deta hai.
	 */
	public static void init() {
		String sql = """
				CREATE TABLE IF NOT EXISTS simulation_runs (
				    id INT AUTO_INCREMENT PRIMARY KEY,
				    frames INT NOT NULL,
				    reference_string VARCHAR(500) NOT NULL,
				    hits INT NOT NULL,
				    faults INT NOT NULL,
				    hit_ratio DOUBLE NOT NULL,
				    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
				)""";
		try (Connection c = getConnection(); Statement st = c.createStatement()) {
			st.execute(sql);
		} catch (SQLException e) {
			throw new RuntimeException("Database init failed: " + e.getMessage(), e);
		}
	}
}