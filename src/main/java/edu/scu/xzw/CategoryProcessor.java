package edu.scu.xzw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang3.StringUtils;

public class CategoryProcessor implements Populator {

    private static CategoryProcessor instance;
    private Connection connection;

    synchronized public static CategoryProcessor getInstance(Connection connection) {
        if (instance == null) {
            instance = new CategoryProcessor(connection);
        }
        return instance;
    }

    private CategoryProcessor(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getPreparedStatement() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
            "INSERT INTO Category(ID, BusinessID, MainCategory, SubCategory)" + 
            "VALUES(?, ?, ?, ?)");
        return preparedStatement;
    }
    
    @Override
    public void populate(List<JsonNode> businessNodes) throws SQLException {
        List<Category> categories = new ArrayList<>();
        for (JsonNode bn : businessNodes) {
            categories.addAll(Category.from(bn));
        }
        PreparedStatement preparedStatement = getPreparedStatement();
        for (Category c : categories) {
            c.addInsertStatement(preparedStatement);
        }
        preparedStatement.executeBatch();
        preparedStatement.close();
    }

    private List<Category> fetchFromStatement(PreparedStatement preparedStatement) throws SQLException {
        ResultSet rs = preparedStatement.executeQuery();
        List<Category> results = new ArrayList<>();
        while (rs.next()) {
            // String id = rs.getString("ID");
            // String businessId = rs.getString("BusinessID");
            // String mainCategory = rs.getString("MainCategory");
            String subCategory = rs.getString("SubCategory");
            results.add(new Category.CategoryBuilder().subCategory(subCategory).build());
        }
        preparedStatement.close();
        rs.close();
        return results;
    }

    public static String buildSqlForAndCategories(Set<String> categories, String placeHolder) {
        List<String> sqls = new ArrayList<>();
        for (String c : categories) {
            sqls.add(String.format("(SELECT BusinessID FROM Category WHERE MainCategory=%s)", placeHolder));
        }
        return StringUtils.join(sqls, " INTERSECT ");
    }

    public static String buildSqlForAndSubCategories(Set<String> subcategories, String placeHolder) {
        List<String> sqls = new ArrayList<>();
        for (String c : subcategories) {
            sqls.add(String.format("(SELECT BusinessID FROM Category WHERE SubCategory=%s)", placeHolder));
        }
        return StringUtils.join(sqls, " INTERSECT ");
    }

    public static String buildSqlForOrCategories(Set<String> categories, String placeHolder) {
        String sql = "SELECT BusinessID FROM Category";
        if (categories.size() > 0) {
            sql += String.format(" WHERE MainCategory IN (%s)", Category.getPlaceholder(categories, placeHolder));
        }
        return sql;
    }

    public static String buildSqlForOrSubCategories(Set<String> subcategories, String placeHolder) {
        if (subcategories.isEmpty()) {
            return "";
        }
        String sql = "SELECT BusinessID from Category";
        if (subcategories.size() > 0) {
            sql += String.format(" WHERE SubCategory IN (%s)", Category.getPlaceholder(subcategories, placeHolder));
        }
        return sql;
    }

    public static String buildSqlForCategories(Set<String> categories, boolean andCategories, String placeHolder) {
        String sql = "";
        if (!andCategories) {
            sql = buildSqlForOrCategories(categories, placeHolder);
        } else {
            sql = buildSqlForAndCategories(categories, placeHolder);
        }
        return sql;
    }

    public static String buildSqlForSubCategories(Set<String> subcategories, boolean andSubCategories, String placeHolder) {
        String sql = "";
        if (!andSubCategories) {
            sql = buildSqlForOrSubCategories(subcategories, placeHolder);
        } else {
            sql = buildSqlForAndSubCategories(subcategories, placeHolder);
        }
        return sql;
    }

    public static String buildSqlForSubCategoriesFromCategories(Set<String> categories, boolean andCategories, String placeHolder) {
        return String.format("SELECT UNIQUE(SubCategory) FROM Category WHERE BusinessID IN(%s)", 
            buildSqlForCategories(categories, andCategories, placeHolder));
    }

    public List<Category> fetch(Set<String> categories, boolean andCategories) throws SQLException {
        String sql = buildSqlForSubCategoriesFromCategories(categories, andCategories, "?");
        
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        int idx = 1;
        for (String c : categories) {
            preparedStatement.setString(idx++, c);
        }
        return fetchFromStatement(preparedStatement);
    }

    // public List<Category> fetch(Set<String> categories, boolean andCategories, Set<String> subcategories, boolean andSubCategories) throws SQLException {
    //     String sql = buildSqlForAttributesFromCategoriesSubCategories(categories, andCategories, subcategories, andSubCategories);
    //     PreparedStatement preparedStatement = connection.prepareStatement(sql);
    //     int idx = 1;
    //     for (String c : categories) {
    //         preparedStatement.setString(idx++, c);
    //     }
    //     for (String c : subcategories) {
    //         preparedStatement.setString(idx++, c);
    //     }
    //     return fetchFromStatement(preparedStatement);
    // }
    
}
