package org.example.dao;

import org.example.db.Db;
import org.example.model.Client;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDao {

    public List<Client> findAll() throws SQLException {
        String sql = """
            SELECT person_id, budget, area_interested_in
            FROM client
            ORDER BY person_id
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Client> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Client(
                        rs.getLong("person_id"),
                        rs.getBigDecimal("budget"),
                        rs.getString("area_interested_in")
                ));
            }
            return out;
        }
    }

    public Client findById(long personId) throws SQLException {
        String sql = """
            SELECT person_id, budget, area_interested_in
            FROM client
            WHERE person_id = ?
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, personId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Client(
                        rs.getLong("person_id"),
                        rs.getBigDecimal("budget"),
                        rs.getString("area_interested_in")
                );
            }
        }
    }

    /** Note: person_id must already exist in person table. */
    public void insert(Client client) throws SQLException {
        String sql = """
            INSERT INTO client (person_id, budget, area_interested_in)
            VALUES (?, ?, ?)
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, client.personId());
            setBigDecimalOrNull(ps, 2, client.budget());
            ps.setString(3, client.areaInterestedIn());
            ps.executeUpdate();
        }
    }

    public void update(Client client) throws SQLException {
        String sql = """
            UPDATE client
            SET budget = ?, area_interested_in = ?
            WHERE person_id = ?
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            setBigDecimalOrNull(ps, 1, client.budget());
            ps.setString(2, client.areaInterestedIn());
            ps.setLong(3, client.personId());
            ps.executeUpdate();
        }
    }

    public void delete(long personId) throws SQLException {
        String sql = "DELETE FROM client WHERE person_id = ?";
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
}
