package org.example.dao;

import org.example.db.Db;
import org.example.model.Property;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyDao {

    public List<Property> findAll() throws SQLException {
        String sql = """
            SELECT property_id, price, square_meters, latitude, longitude, city,
                   property_type, owner_id
            FROM property
            ORDER BY property_id
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Property> out = new ArrayList<>();
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        }
    }

    public Property findById(long propertyId) throws SQLException {
        String sql = """
            SELECT property_id, price, square_meters, latitude, longitude, city,
                   property_type, owner_id
            FROM property
            WHERE property_id = ?
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, propertyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public long insert(Property p) throws SQLException {
        String sql = """
            INSERT INTO property (
                price, square_meters, latitude, longitude, city, property_type, owner_id
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING property_id
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            setBigDecimalOrNull(ps, 1, p.price());
            setBigDecimalOrNull(ps, 2, p.squareMeters());
            ps.setString(3, p.latitude());
            ps.setString(4, p.longitude());
            ps.setString(5, p.city());
            ps.setString(6, p.propertyType());
            setLongOrNull(ps, 7, p.ownerId());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void update(Property p) throws SQLException {
        String sql = """
            UPDATE property
            SET price=?, square_meters=?, latitude=?, longitude=?, city=?,
                property_type=?, owner_id=?
            WHERE property_id=?
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            setBigDecimalOrNull(ps, 1, p.price());
            setBigDecimalOrNull(ps, 2, p.squareMeters());
            ps.setString(3, p.latitude());
            ps.setString(4, p.longitude());
            ps.setString(5, p.city());
            ps.setString(6, p.propertyType());
            setLongOrNull(ps, 7, p.ownerId());
            ps.setLong(8, p.propertyId());

            ps.executeUpdate();
        }
    }

    public void delete(long propertyId) throws SQLException {
        String sql = "DELETE FROM property WHERE property_id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, propertyId);
            ps.executeUpdate();
        }
    }

    private static Property map(ResultSet rs) throws SQLException {
        Long ownerId = rs.getObject("owner_id", Long.class);
        return new Property(
                rs.getLong("property_id"),
                rs.getBigDecimal("price"),
                rs.getBigDecimal("square_meters"),
                rs.getString("latitude"),
                rs.getString("longitude"),
                rs.getString("city"),
                rs.getString("property_type"),
                ownerId
        );
    }

    private static void setBigDecimalOrNull(PreparedStatement ps, int idx, BigDecimal v) throws SQLException {
        if (v == null) ps.setNull(idx, Types.NUMERIC);
        else ps.setBigDecimal(idx, v);
    }

    private static void setLongOrNull(PreparedStatement ps, int idx, Long v) throws SQLException {
        if (v == null) ps.setNull(idx, Types.BIGINT);
        else ps.setLong(idx, v);
    }
}
