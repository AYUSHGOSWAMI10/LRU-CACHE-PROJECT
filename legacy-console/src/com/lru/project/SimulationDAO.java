package com.lru.project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SimulationDAO {
	/** Ek simulation run ka result. */
	public record SimulationResult(int frames, String referenceString, int hits, int faults, double hitRatio,
			String createdAt) {
	}
	public record HistoryEntry(int id, SimulationResult result) { }

	public void save(SimulationResult r) {
		try {
			saveChecked(r);
		} catch (RuntimeException e) {
			System.out.println("Save failed: " + e.getMessage());
		}
	}

	public void saveChecked(SimulationResult r) {
		String sql = "INSERT INTO simulation_runs(frames, reference_string, hits, faults, hit_ratio) VALUES (?,?,?,?,?)";
		try (Connection c = DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setInt(1, r.frames());
			ps.setString(2, r.referenceString());
			ps.setInt(3, r.hits());
			ps.setInt(4, r.faults());
			ps.setDouble(5, r.hitRatio());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException("Could not save simulation history: " + e.getMessage(), e);
		}
	}

	public List<SimulationResult> findAll() {
		List<SimulationResult> list = new ArrayList<>();
		String sql = "SELECT frames, reference_string, hits, faults, hit_ratio, created_at FROM simulation_runs ORDER BY id DESC";
		try (Connection c = DatabaseManager.getConnection();
				Statement st = c.createStatement();
				ResultSet rs = st.executeQuery(sql)) {
			while (rs.next()) {
				list.add(new SimulationResult(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4),
						rs.getDouble(5), rs.getString(6)));
			}
		} catch (SQLException e) {
			System.out.println("Read failed: " + e.getMessage());
		}
		return list;
	}

	public List<HistoryEntry> findHistoryChecked() {
		List<HistoryEntry> list = new ArrayList<>();
		String sql = "SELECT id, frames, reference_string, hits, faults, hit_ratio, created_at FROM simulation_runs ORDER BY id DESC";
		try (Connection c = DatabaseManager.getConnection();
				Statement st = c.createStatement();
				ResultSet rs = st.executeQuery(sql)) {
			while (rs.next()) {
				SimulationResult result = new SimulationResult(rs.getInt(2), rs.getString(3), rs.getInt(4),
						rs.getInt(5), rs.getDouble(6), rs.getString(7));
				list.add(new HistoryEntry(rs.getInt(1), result));
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Could not load simulation history: " + e.getMessage(), e);
		}
		return list;
	}

	public void deleteAll() {
		try (Connection c = DatabaseManager.getConnection(); Statement st = c.createStatement()) {
			st.executeUpdate("DELETE FROM simulation_runs");
		} catch (SQLException e) {
			System.out.println("Delete failed: " + e.getMessage());
		}
	}

	public void deleteAllChecked() {
		try (Connection c = DatabaseManager.getConnection(); Statement st = c.createStatement()) {
			st.executeUpdate("DELETE FROM simulation_runs");
		} catch (SQLException e) {
			throw new IllegalStateException("Could not clear simulation history: " + e.getMessage(), e);
		}
	}
}
