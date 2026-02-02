package org.example.dao;

import org.example.db.Db;
import org.example.model.Person;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonDao {

    public List<Person> findAll() throws SQLException {
        String sql = "SELECT person_id, first_name, last_name, email, phone_number FROM person ORDER BY person_id";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Person> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Person(
                        rs.getLong("person_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone_number")
                ));
            }
            return out;
        }
    }

    public long insert(Person p) throws SQLException {
        String sql = """
            INSERT INTO person (first_name, last_name, email, phone_number)
            VALUES (?, ?, ?, ?)
            RETURNING person_id
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.firstName());
            ps.setString(2, p.lastName());
            ps.setString(3, p.email());
            ps.setString(4, p.phoneNumber());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void update(Person p) throws SQLException {
        String sql = """
            UPDATE person
            SET first_name=?, last_name=?, email=?, phone_number=?
            WHERE person_id=?
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.firstName());
            ps.setString(2, p.lastName());
            ps.setString(3, p.email());
            ps.setString(4, p.phoneNumber());
            ps.setLong(5, p.personId());
            ps.executeUpdate();
        }
    }

    public void delete(long personId) throws SQLException {
        String sql = "DELETE FROM person WHERE person_id=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, personId);
            ps.executeUpdate();
        }
    }

    public String getDeleteBlockers(long personId) throws SQLException {
        StringBuilder sb = new StringBuilder();

        try (Connection c = Db.getConnection()) {

            if (exists(c, "SELECT 1 FROM client WHERE person_id = ?", personId))
                sb.append("client, ");

            if (exists(c, "SELECT 1 FROM agent WHERE person_id = ?", personId))
                sb.append("agent, ");

            if (exists(c, "SELECT 1 FROM property WHERE owner_id = ?", personId))
                sb.append("property.owner_id, ");

            if (exists(c, "SELECT 1 FROM successful_deals WHERE agent_id = ?", personId))
                sb.append("successful_deals.agent_id, ");

            if (exists(c, "SELECT 1 FROM successful_deals WHERE client_id = ?", personId))
                sb.append("successful_deals.client_id, ");

            if (exists(c, "SELECT 1 FROM person_roles WHERE person_id = ?", personId))
                sb.append("person_roles, ");

            if (exists(c, "SELECT 1 FROM property_owner WHERE person_id = ?", personId))
                sb.append("property_owner, ");
        }

        // remove trailing ", "
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }

        return sb.toString(); // empty = safe to delete
    }

    private boolean exists(Connection c, String sql, long personId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, personId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
