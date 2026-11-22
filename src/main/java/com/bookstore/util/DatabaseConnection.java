package com.bookstore.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/bookstore_db?connectTimeout=5000&socketTimeout=15000&useUnicode=true&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "123456";

    public static Connection getConnection() {
        String url = resolveNonBlank("bookstore.db.url", "BOOKSTORE_DB_URL", DEFAULT_URL);
        String user = resolveNonBlank("bookstore.db.user", "BOOKSTORE_DB_USER", DEFAULT_USER);
        String password = resolveNullable("bookstore.db.password", "BOOKSTORE_DB_PASSWORD", DEFAULT_PASSWORD);
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể kết nối cơ sở dữ liệu.", e);
        }
    }

    public static void closeConnection(Connection c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String resolveNonBlank(String propertyName, String envName, String fallback) {
        String prop = System.getProperty(propertyName);
        if (prop != null && !prop.isBlank()) {
            return prop.trim();
        }
        String altProp = System.getProperty(envName);
        if (altProp != null && !altProp.isBlank()) {
            return altProp.trim();
        }
        String env = System.getenv(envName);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return fallback;
    }

    private static String resolveNullable(String propertyName, String envName, String fallback) {
        String prop = System.getProperty(propertyName);
        if (prop != null) {
            return prop;
        }
        String altProp = System.getProperty(envName);
        if (altProp != null) {
            return altProp;
        }
        String env = System.getenv(envName);
        if (env != null) {
            return env;
        }
        return fallback;
    }
}
