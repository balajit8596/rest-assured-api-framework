package com.qa.framework.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Small JDBC helper for backend validation: after an API call, run SQL to confirm the data
 * was stored correctly. Works with any JDBC connection (MySQL, SQL Server, H2 ...).
 * Uses PreparedStatement so values are always bound as parameters, never concatenated.
 */
public class DbValidator {

    private final Connection connection;

    public DbValidator(Connection connection) {
        this.connection = connection;
    }

    /** Run a SELECT and return rows as column-name (lower case) to value maps. */
    public List<Map<String, Object>> query(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int c = 1; c <= meta.getColumnCount(); c++) {
                        row.put(meta.getColumnLabel(c).toLowerCase(), rs.getObject(c));
                    }
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    /** Return the single matching row, or fail if there are none or more than one. */
    public Map<String, Object> findOne(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> rows = query(sql, params);
        if (rows.size() != 1) {
            throw new AssertionError("Expected exactly 1 row but found " + rows.size() + " for: " + sql);
        }
        return rows.get(0);
    }

    /** Insert, update or delete. Returns the number of affected rows. */
    public int execute(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate();
        }
    }
}
