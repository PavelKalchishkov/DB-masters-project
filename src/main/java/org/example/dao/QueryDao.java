package org.example.dao;

import org.example.db.Db;
import org.example.model.AgentSalesRow;
import org.example.model.ClientBudgetMatchRow;
import org.example.model.PropertyOwnerRow;
import org.example.model.AvgSaleByTypeRow;
import org.example.model.ClientDealsRow;
import org.example.model.UnsoldPropertyRow;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QueryDao {

    public List<PropertyOwnerRow> propertiesWithOptionalOwner() throws SQLException {
        String sql = """
            SELECT
                p.property_id,
                p.property_type,
                p.city,
                p.owner_id,
                (pe.first_name || ' ' || pe.last_name) AS owner_name
            FROM property p
            LEFT JOIN person pe ON pe.person_id = p.owner_id
            ORDER BY p.property_id
        """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<PropertyOwnerRow> out = new ArrayList<>();
            while (rs.next()) {
                Long ownerId = rs.getObject("owner_id", Long.class);
                out.add(new PropertyOwnerRow(
                        rs.getLong("property_id"),
                        rs.getString("property_type"),
                        rs.getString("city"),
                        ownerId,
                        rs.getString("owner_name")
                ));
            }
            return out;
        }
    }

    public List<AgentSalesRow> topAgentsBySales() throws SQLException {
        String sql = """
            SELECT
                a.person_id AS agent_id,
                pe.first_name,
                pe.last_name,
                COUNT(*) AS deals,
                COALESCE(SUM(d.final_price), 0) AS total_sales
            FROM successful_deals d
            INNER JOIN agent a ON a.person_id = d.agent_id
            INNER JOIN person pe ON pe.person_id = a.person_id
            GROUP BY a.person_id, pe.first_name, pe.last_name
            ORDER BY total_sales DESC, deals DESC
        """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<AgentSalesRow> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new AgentSalesRow(
                        rs.getLong("agent_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getLong("deals"),
                        rs.getBigDecimal("total_sales")
                ));
            }
            return out;
        }
    }

    public List<ClientBudgetMatchRow> propertiesUnderClientBudget(long clientId) throws SQLException {
        String sql = """
            SELECT
                c.person_id AS client_id,
                (pe.first_name || ' ' || pe.last_name) AS client_name,
                p.property_id,
                p.city,
                p.property_type,
                p.price
            FROM client c
            INNER JOIN person pe ON pe.person_id = c.person_id
            INNER JOIN property p ON p.price <= c.budget
            WHERE c.person_id = ?
            ORDER BY p.price ASC
        """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, clientId);

            try (ResultSet rs = ps.executeQuery()) {
                List<ClientBudgetMatchRow> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new ClientBudgetMatchRow(
                            rs.getLong("client_id"),
                            rs.getString("client_name"),
                            rs.getLong("property_id"),
                            rs.getString("city"),
                            rs.getString("property_type"),
                            rs.getBigDecimal("price")
                    ));
                }
                return out;
            }
        }
    }

    public List<AvgSaleByTypeRow> avgSalePriceByPropertyType() throws SQLException {
        String sql = """
        SELECT
            p.property_type,
            COUNT(*) AS deals,
            AVG(d.final_price) AS avg_final_price
        FROM successful_deals d
        INNER JOIN property p ON p.property_id = d.property_id
        GROUP BY p.property_type
        ORDER BY avg_final_price DESC
    """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<AvgSaleByTypeRow> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new AvgSaleByTypeRow(
                        rs.getString("property_type"),
                        rs.getLong("deals"),
                        rs.getBigDecimal("avg_final_price")
                ));
            }
            return out;
        }
    }

    public List<ClientDealsRow> topClientsByDeals() throws SQLException {
        String sql = """
        SELECT
            c.person_id AS client_id,
            pe.first_name,
            pe.last_name,
            COUNT(*) AS deals,
            COALESCE(SUM(d.final_price), 0) AS total_spent
        FROM successful_deals d
        INNER JOIN client c ON c.person_id = d.client_id
        INNER JOIN person pe ON pe.person_id = c.person_id
        GROUP BY c.person_id, pe.first_name, pe.last_name
        ORDER BY deals DESC, total_spent DESC
    """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<ClientDealsRow> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new ClientDealsRow(
                        rs.getLong("client_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getLong("deals"),
                        rs.getBigDecimal("total_spent")
                ));
            }
            return out;
        }
    }

    public List<UnsoldPropertyRow> unsoldProperties() throws SQLException {
        String sql = """
        SELECT
            p.property_id,
            p.property_type,
            p.city,
            p.price
        FROM property p
        LEFT JOIN successful_deals d ON d.property_id = p.property_id
        WHERE d.deal_id IS NULL
        ORDER BY p.property_id
    """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<UnsoldPropertyRow> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new UnsoldPropertyRow(
                        rs.getLong("property_id"),
                        rs.getString("property_type"),
                        rs.getString("city"),
                        rs.getBigDecimal("price")
                ));
            }
            return out;
        }
    }
}