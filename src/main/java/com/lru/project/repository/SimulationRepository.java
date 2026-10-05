package com.lru.project.repository;

import com.lru.project.model.SimulationModels.HistoryRow;
import com.lru.project.model.SimulationModels.SimulationOutcome;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** JDBC access to the existing simulation_runs table; no schema generation or migration. */
@Repository
public class SimulationRepository {
    private final JdbcTemplate jdbc;

    public SimulationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int insert(SimulationOutcome outcome) {
        String sql = "INSERT INTO simulation_runs(frames, reference_string, hits, faults, hit_ratio) VALUES (?,?,?,?,?)";
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, outcome.frames());
            statement.setString(2, outcome.referenceString());
            statement.setInt(3, outcome.hits());
            statement.setInt(4, outcome.faults());
            statement.setDouble(5, outcome.hitRatio());
            return statement;
        }, keys);
        Number id = keys.getKey();
        if (id == null) throw new IllegalStateException("MySQL did not return an ID for the saved simulation.");
        return id.intValue();
    }

    public List<HistoryRow> findAll() {
        String sql = "SELECT id, frames, reference_string, hits, faults, hit_ratio, created_at FROM simulation_runs ORDER BY id DESC";
        return jdbc.query(sql, (rs, row) -> {
            Timestamp created = rs.getTimestamp("created_at");
            LocalDateTime local = created == null ? null : created.toLocalDateTime();
            return new HistoryRow(rs.getInt("id"), rs.getInt("frames"), rs.getString("reference_string"),
                    rs.getInt("hits"), rs.getInt("faults"), rs.getDouble("hit_ratio"),
                    local == null ? null : local.toString());
        });
    }

    public int deleteAll() {
        return jdbc.update("DELETE FROM simulation_runs");
    }
}
