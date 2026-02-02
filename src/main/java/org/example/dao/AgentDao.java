package org.example.dao;

import org.example.db.Db;
import org.example.model.Agent;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AgentDao {

    public List<Agent> findAll() throws SQLException {
        String sql = """
            SELECT person_id, salary, hire_date
            FROM agent
            ORDER BY person_id
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Agent> out = new ArrayList<>();
            while (rs.next()) {
                Date d = rs.getDate("hire_date");
                out.add(new Agent(
                        rs.getLong("person_id"),
                        rs.getBigDecimal("salary"),
                        d == null ? null : d.toLocalDate()
                ));
            }
            return out;
        }
    }

    public Agent findById(long personId) throws SQLException {
        String sql = """
            SELECT person_id, salary, hire_date
            FROM agent
            WHERE person_id = ?
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, personId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Date d = rs.getDate("hire_date");
                return new Agent(
                        rs.getLong("person_id"),
                        rs.getBigDecimal("salary"),
                        d == null ? null : d.toLocalDate()
                );
            }
        }
    }

    /** Note: person_id must already exist in person table. */
    public void insert(Agent agent) throws SQLException {
        String sql = """
            INSERT INTO agent (person_id, salary, hire_date)
            VALUES (?, ?, ?)
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, agent.personId());
            setBigDecimalOrNull(ps, 2, agent.salary());
            setLocalDateOrNull(ps, 3, agent.hireDate());
            ps.executeUpdate();
        }
    }

    public void update(Agent agent) throws SQLException {
        String sql = """
            UPDATE agent
            SET salary = ?, hire_date = ?
            WHERE person_id = ?
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            setBigDecimalOrNull(ps, 1, agent.salary());
            setLocalDateOrNull(ps, 2, agent.hireDate());
            ps.setLong(3, agent.personId());
            ps.executeUpdate();
        }
    }

    public void delete(long personId) throws SQLException {
        String sql = "DELETE FROM agent WHERE person_id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, personId);
            ps.executeUpdate();
        }
    }

    private static void setBigDecimalOrNull(PreparedStatement ps, int idx, BigDecimal v) throws SQLException {
        if (v == null) ps.setNull(idx, Types.NUMERIC);
        else ps.setBigDecimal(idx, v);
    }

    private static void setLocalDateOrNull(PreparedStatement ps, int idx, LocalDate d) throws SQLException {
        if (d == null) ps.setNull(idx, Types.DATE);
        else ps.setDate(idx, Date.valueOf(d));
    }
}
