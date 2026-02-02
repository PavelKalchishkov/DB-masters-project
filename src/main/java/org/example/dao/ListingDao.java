package org.example.dao;

import org.example.db.Db;
import org.example.model.Listing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ListingDao {

    public List<Listing> findAll() throws SQLException {
        String sql = """
            SELECT listing_id, type_of_listing, description, notes
            FROM listing
            ORDER BY listing_id
        """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Listing> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Listing(
                        rs.getLong("listing_id"),
                        rs.getString("type_of_listing"),
                        rs.getString("description"),
                        rs.getString("notes")
                ));
            }
            return out;
        }
    }

    public long insert(Listing l) throws SQLException {
        String sql = """
            INSERT INTO listing (type_of_listing, description, notes)
            VALUES (?, ?, ?)
            RETURNING listing_id
        """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, l.typeOfListing());
            ps.setString(2, l.description());
            ps.setString(3, l.notes());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void update(Listing l) throws SQLException {
        String sql = """
            UPDATE listing
            SET type_of_listing = ?, description = ?, notes = ?
            WHERE listing_id = ?
        """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, l.typeOfListing());
            ps.setString(2, l.description());
            ps.setString(3, l.notes());
            ps.setLong(4, l.listingId());

            ps.executeUpdate();
        }
    }

    public void delete(long listingId) throws SQLException {
        String sql = "DELETE FROM listing WHERE listing_id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, listingId);
            ps.executeUpdate();
        }
    }
}

