package edu.scu.xzw;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang3.ArchUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class BusinessProcessor implements Populator {
    private static BusinessProcessor instance;
    private Connection connection;

    synchronized public static BusinessProcessor getInstance(Connection connection) {
        if (instance == null) {
            instance = new BusinessProcessor(connection);
        }
        return instance;
    }

    private BusinessProcessor(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getPreparedStatement() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
            "INSERT INTO Business(BusinessID, FullAddress, Hours, Open, Categories," + 
                "City, ReviewCount, Name, State, Stars, Attributes, Longitude, Latitude, Type)" + 
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        return preparedStatement;
    }

    @Override
    public void populate(List<JsonNode> businessNodes) throws SQLException {
        List<Business> businesses = new ArrayList<>();
        for (JsonNode bn : businessNodes) {
            businesses.add(Business.from(bn));
        }
        PreparedStatement preparedStatement = getPreparedStatement();
        for (Business b : businesses) {
            b.addInsertStament(preparedStatement);
        }
        preparedStatement.executeBatch();
        preparedStatement.close();
        CategoryProcessor.getInstance(connection).populate(businessNodes);
        AttributeProcessor.getInstance(connection).populate(businessNodes);
    }

    public String buildSqlWithPlaceHolder(Set<String> categories, boolean andCategories, 
        Set<String> subcategories, boolean andSubCategories,
        Set<String> attributes, boolean andAttributes, Review.Criteria criteria, String placeHolder) {

        String cSql = CategoryProcessor.buildSqlForCategories(categories, andCategories, placeHolder);
        String scSql = CategoryProcessor.buildSqlForSubCategories(subcategories, andSubCategories, placeHolder);
        String aSql = AttributeProcessor.buildSqlForAttributes(attributes, andAttributes, placeHolder);
        List<String> nSqls = new ArrayList<>();
        for (String s : new String[]{cSql, scSql, aSql}) {
            if (s.length() > 0) {
                nSqls.add(String.format("(%s)", s));
            }
        }
        
        if (criteria.hasCriteria()) {
            nSqls.add(String.format("(%s)", ReviewProcessor.buildSql(criteria, placeHolder)));
        }
        String sql = String.format("SELECT * FROM Business WHERE BusinessID IN (%s)", StringUtils.join(nSqls, " INTERSECT "));
        return sql;
    }

    public String getQuerySql(Set<String> categories, boolean andCategories, 
        Set<String> subcategories, boolean andSubCategories,
        Set<String> attributes, boolean andAttributes, Review.Criteria criteria) {

        String sql = buildSqlWithPlaceHolder(categories, andCategories, subcategories, andSubCategories, attributes, andAttributes, criteria, "%s");
        List<Object> arguments = new ArrayList<>();
        arguments.addAll(categories);
        arguments.addAll(subcategories);
        arguments.addAll(attributes);
        arguments.addAll(criteria.getArguments());
        return String.format(sql, arguments.toArray());
    }

    public List<Business> fetch(Set<String> categories, boolean andCategories, 
        Set<String> subcategories, boolean andSubCategories,
        Set<String> attributes, boolean andAttributes, Review.Criteria criteria) throws SQLException {

        String sql = buildSqlWithPlaceHolder(categories, andCategories, subcategories, andSubCategories, attributes, andAttributes, criteria, "?");
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        Category.fillInPlaceHolder(preparedStatement, categories, 1);
        Category.fillInPlaceHolder(preparedStatement, subcategories, categories.size() + 1);
        Category.fillInPlaceHolder(preparedStatement, attributes, categories.size() + subcategories.size() + 1);
        if (criteria.hasCriteria()) {
            criteria.fillInPlaceHolder(preparedStatement, categories.size() + subcategories.size() + attributes.size() + 1);
        }
        
        ResultSet rs = preparedStatement.executeQuery();
        List<Business> businesses = new ArrayList<>();
        while (rs.next()) {
            businesses.add(Business.from(rs));
        }
        rs.close();
        preparedStatement.close();
        return businesses;
    }
}
