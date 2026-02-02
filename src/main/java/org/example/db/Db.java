package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Db {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/REALESTATEDB";
    private static final String USER = "postgres";
    private static final String PASS = "4321";

    private Db() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
