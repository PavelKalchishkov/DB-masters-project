package org.example.dao;

import org.example.db.Db;
import org.example.model.SuccessfulDeal;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SuccessfulDealDao {

    public List<SuccessfulDeal> findAll() throws SQLException {
        String sql = """
            SELECT deal_id, property_id, final_price, agent_id, client_id
            FROM successful_deals
            ORDER BY deal_id
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<SuccessfulDeal> out = new ArrayList<>();
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        }
    }

    public SuccessfulDeal findById(long dealId) throws SQLException {
        String sql = """
            SELECT deal_id, property_id, final_price, agent_id, client_id
            FROM successful_deals
            WHERE deal_id = ?
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, dealId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public long insert(SuccessfulDeal d) throws SQLException {
        String sql = """
            INSERT INTO successful_deals (property_id, final_price, agent_id, client_id)
            VALUES (?, ?, ?, ?)
            RETURNING deal_id
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, d.propertyId());
            setBigDecimalOrNull(ps, 2, d.finalPrice());
            ps.setLong(3, d.agentId());
            ps.setLong(4, d.clientId());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void update(SuccessfulDeal d) throws SQLException {
        String sql = """
            UPDATE successful_deals
            SET property_id=?, final_price=?, agent_id=?, client_id=?
            WHERE deal_id=?
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, d.propertyId());
            setBigDecimalOrNull(ps, 2, d.finalPrice());
            ps.setLong(3, d.agentId());
            ps.setLong(4, d.clientId());
            ps.setLong(5, d.dealId());

            ps.executeUpdate();
        }
    }

    public void delete(long dealId) throws SQLException {
        String sql = "DELETE FROM successful_deals WHERE deal_id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, dealId);
            ps.executeUpdate();
        }
    }

    private static SuccessfulDeal map(ResultSet rs) throws SQLException {
        return new SuccessfulDeal(
                rs.getLong("deal_id"),
                rs.getLong("property_id"),
                rs.getBigDecimal("final_price"),
                rs.getLong("agent_id"),
                rs.getLong("client_id")
        );
    }

    private static void setBigDecimalOrNull(PreparedStatement ps, int idx, BigDecimal v) throws SQLException {
        if (v == null) ps.setNull(idx, Types.NUMERIC);
        else ps.setBigDecimal(idx, v);
    }
}

