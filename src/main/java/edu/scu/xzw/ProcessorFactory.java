package edu.scu.xzw;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.JsonNode;

class ProcessorFactory {
    public static Connection connection;

    public synchronized static Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe","system","oracle123");
        }
        return connection;
    }

    public static Populator getInstance(String type, Connection connection) {
        if (type.equals("business")) {
            return BusinessProcessor.getInstance(connection);
        }
        if (type.equals("users")) {
            return UserProcessor.getInstance(connection);
        }
        if (type.equals("reviews")) {
            return ReviewProcessor.getInstance(connection);
        }
        return null;
    }
}
