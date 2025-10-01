package edu.scu.xzw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang3.StringUtils;

public class AttributeProcessor implements Populator {
    private static AttributeProcessor instance;
    private Connection connection;

    synchronized public static AttributeProcessor getInstance(Connection connection) {
        if (instance == null) {
            instance = new AttributeProcessor(connection);
        }
        return instance;
    }

    private AttributeProcessor(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getPreparedStatement() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
            "INSERT INTO Attribute(ID, BusinessID, AttributeName)" + 
            "VALUES(?, ?, ?)");
        return preparedStatement;
    }

    @Override
    public void populate(List<JsonNode> businessNodes) throws SQLException {
        List<Attribute> attributes = new ArrayList<>();
        for (JsonNode bn : businessNodes) {
            attributes.addAll(Attribute.from(bn));
        }
        PreparedStatement preparedStatement = getPreparedStatement();
        for (Attribute attribute : attributes) {
            attribute.addInsertStatement(preparedStatement);
        }
        preparedStatement.executeBatch();
        preparedStatement.close();
    }

    private String getPlaceholder(Set<String> set) {
        List<String> placeholders = new ArrayList<>();
        set.forEach(c -> placeholders.add("?"));
        return StringUtils.join(placeholders, ",");
    }

    private static String buildSqlForAndAttributes(Set<String> attributes, String placeHolder) {
        List<String> sqls = new ArrayList<>();
        for (String c : attributes) {
            sqls.add(String.format("(SELECT BusinessID FROM Attribute WHERE AttributeName=%s)", placeHolder));
        }
        return StringUtils.join(sqls, " INTERSECT ");
    }

    private static String buildSqlForOrAttributes(Set<String> attributes, String placeHolder) {
        if (attributes.isEmpty()) {
            return "";
        }
        String sql = "SELECT BusinessID FROM Attribute";
        if (attributes.size() > 0) {
            sql += String.format(" WHERE AttributeName IN (%s)", Category.getPlaceholder(attributes, placeHolder));
        }
        return sql;
    }

    public static String buildSqlForAttributes(Set<String> attributes, boolean andAttributes, String placeHolder) {
        String sql = "";
        if (!andAttributes) {
            sql = buildSqlForOrAttributes(attributes, placeHolder);
        } else {
            sql = buildSqlForAndAttributes(attributes, placeHolder);
        }
        return sql;
    }

    private static String buildSqlForAttributesFromCategoriesSubCategories(Set<String> categories, boolean andCategories, Set<String> subcategories, boolean andSubCategories, String placeHolder) {
        String cSql = CategoryProcessor.buildSqlForCategories(categories, andCategories, placeHolder);
        String scSql = CategoryProcessor.buildSqlForSubCategories(subcategories, andSubCategories, placeHolder);
        List<String> conditions = new ArrayList<>();
        for (String s : Arrays.asList(cSql, scSql)) {
            if (s.length() == 0) {
                continue;
            } 
            conditions.add(String.format("(%s)", s));
        }

        return String.format("SELECT UNIQUE(AttributeName) FROM Attribute WHERE BusinessID IN(%s)", 
            StringUtils.join(conditions,  " INTERSECT "));
    }

    public List<Attribute> fetch(Set<String> categories, boolean andCategories, Set<String> subcategories, boolean andSubCategories) throws SQLException {
        String sql = buildSqlForAttributesFromCategoriesSubCategories(categories, andCategories, subcategories, andSubCategories, "?");
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        int idx = 1;
        for (String c : categories) {
            preparedStatement.setString(idx++, c);
        }
        for (String c : subcategories) {
            preparedStatement.setString(idx++, c);
        }
        ResultSet rs = preparedStatement.executeQuery();
        List<Attribute> attributes = new ArrayList<>();
        while (rs.next()) {
            String attributeName = rs.getString("AttributeName");
            attributes.add(new Attribute.AttributeBuilder().attributeName(attributeName).build());
        }
        preparedStatement.close();
        rs.close();
        return attributes;
    }
}
