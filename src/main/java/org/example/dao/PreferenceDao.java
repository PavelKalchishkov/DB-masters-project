package org.example.dao;

import org.example.db.Db;
import org.example.model.Preference;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PreferenceDao {

    public List<Preference> findAll() throws SQLException {
        String sql = """
            SELECT preference_id, client_id, preference_type
            FROM preferences
            ORDER BY preference_id
        """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Preference> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Preference(
                        rs.getLong("preference_id"),
                        rs.getLong("client_id"),
                        rs.getString("preference_type")
                ));
            }
            return out;
        }
    }

    public long insert(Preference p) throws SQLException {
        String sql = """
            INSERT INTO preferences (client_id, preference_type)
            VALUES (?, ?)
            RETURNING preference_id
        """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, p.clientId());
            ps.setString(2, p.preferenceType());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void update(Preference p) throws SQLException {
        String sql = """
            UPDATE preferences
            SET client_id = ?, preference_type = ?
            WHERE preference_id = ?
        """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, p.clientId());
            ps.setString(2, p.preferenceType());
            ps.setLong(3, p.preferenceId());

            ps.executeUpdate();
        }
    }

    public void delete(long preferenceId) throws SQLException {
        String sql = "DELETE FROM preferences WHERE preference_id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, preferenceId);
            ps.executeUpdate();
        }
    }
}

