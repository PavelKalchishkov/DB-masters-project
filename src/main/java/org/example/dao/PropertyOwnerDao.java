package org.example.dao;

import org.example.db.Db;
import org.example.model.PropertyOwner;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyOwnerDao {

    public List<PropertyOwner> findAll() throws SQLException {
        String sql = """
            SELECT person_id, property_id
            FROM property_owner
            ORDER BY person_id, property_id
        """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<PropertyOwner> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new PropertyOwner(
                        rs.getLong("person_id"),
                        rs.getLong("property_id")
                ));
            }
            return out;
        }
    }

    public void insert(PropertyOwner po) throws SQLException {
        String sql = """
            INSERT INTO property_owner (person_id, property_id)
            VALUES (?, ?)
        """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, po.personId());
            ps.setLong(2, po.propertyId());
            ps.executeUpdate();
        }
    }

    /**
     * Because the "row id" is the (person_id, property_id) pair,
     * updating means: change to a new pair.
     */
    public void update(long oldPersonId, long oldPropertyId, PropertyOwner newPair) throws SQLException {
        String sql = """
            UPDATE property_owner
            SET person_id = ?, property_id = ?
            WHERE person_id = ? AND property_id = ?
        """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, newPair.personId());
            ps.setLong(2, newPair.propertyId());
            ps.setLong(3, oldPersonId);
            ps.setLong(4, oldPropertyId);

            ps.executeUpdate();
        }
    }

    public void delete(long personId, long propertyId) throws SQLException {
        String sql = """
            DELETE FROM property_owner
            WHERE person_id = ? AND property_id = ?
        """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, personId);
            ps.setLong(2, propertyId);
            ps.executeUpdate();
        }
    }
}
