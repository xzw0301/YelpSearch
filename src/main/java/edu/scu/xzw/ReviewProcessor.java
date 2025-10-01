package edu.scu.xzw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang3.StringUtils;

public class ReviewProcessor implements Populator{

    private static ReviewProcessor instance;
    private Connection connection;

    synchronized public static ReviewProcessor getInstance(Connection connection) {
        if (instance == null) {
            instance = new ReviewProcessor(connection);
        }
        return instance;
    }

    private ReviewProcessor(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getPreparedStatement() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
            "INSERT INTO Review(ReviewID, BusinessID, UserID, Votes, VotesNumber, Stars," + 
                "ReviewDate, Text, Type)" + 
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
        return preparedStatement;
    }

    public static String buildSql(Review.Criteria criteria, String placeHolder) {
        String sql = "SELECT BusinessID FROM Review WHERE ";
        List<String> conditions = new ArrayList<>();
        if (criteria.getFrom().isPresent()) {
            conditions.add(String.format("ReviewDate >= %s", placeHolder));
        } 
        if (criteria.getTo().isPresent()) {
            conditions.add(String.format("ReviewDate <= %s", placeHolder));
        }

        if (criteria.getStarsOperator().isPresent()) {
            conditions.add(String.format("stars %s %s", Review.Criteria.toOperator(criteria.getStarsOperator().get()), placeHolder));
        }

        if (criteria.getVotesOperator().isPresent()) {
            conditions.add(String.format("votesNumber %s %s", Review.Criteria.toOperator(criteria.getVotesOperator().get()), placeHolder));
        }
        return sql + StringUtils.join(conditions, " AND ");
    }

    @Override
    public void populate(List<JsonNode> reviewNodes) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        for (JsonNode review : reviewNodes) {
            reviews.add(Review.from(review));
        }
        PreparedStatement preparedStatement = getPreparedStatement();
        for (Review review : reviews) {
            review.addInsertStament(preparedStatement);
        }
        preparedStatement.executeBatch();
        preparedStatement.close();
    }

    public List<Review> fetchWithUserName(String key, String value) throws SQLException {
        String sql = String.format("SELECT * from Review LEFT JOIN YelpUser ON Review.UserID = YelpUser.UserID WHERE %s = ?", key);
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, value);
        ResultSet rs = preparedStatement.executeQuery();
        List<Review> result = Review.from(rs);
        preparedStatement.close();
        return result;
    }
}
