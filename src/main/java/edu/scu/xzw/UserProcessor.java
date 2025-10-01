package edu.scu.xzw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang3.StringUtils;

public class UserProcessor implements Populator {
    private static UserProcessor instance;
    private Connection connection;

    synchronized public static UserProcessor getInstance(Connection connection) {
        if (instance == null) {
            instance = new UserProcessor(connection);
        }
        return instance;
    }

    private UserProcessor(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getPreparedStatement() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
            "INSERT INTO YelpUser(UserID, Since, Votes, VotesNumber, ReviewCount, Name," + 
                "Friends, FriendsNumber, Fans, AverageStars, Type, Compliments, Elite)" + 
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        return preparedStatement;
    }

    @Override
    public void populate(List<JsonNode> userNodes) throws SQLException {
        List<User> users = new ArrayList<>();
        for (JsonNode user : userNodes) {
            users.add(User.from(user));
        }
        PreparedStatement preparedStatement = getPreparedStatement();
        for (User user : users) {
            user.addInsertStament(preparedStatement);
        }
        preparedStatement.executeBatch();
        preparedStatement.close();
    }

    public static String buildSql(User.Criteria criteria, String attributeOperator, int maxUsersNumber) {
        String sql = "SELECT * FROM YelpUser WHERE ";
        List<String> conditions = new ArrayList<>();
        if (criteria.getMemberSince().isPresent()) {
            conditions.add("Since >= ?");
        } 
        if (criteria.getReviewCountOp().isPresent()) {
            conditions.add(String.format("ReviewCount %s ?", User.Criteria.toOperator(criteria.getReviewCountOp().get())));
        } 
        if (criteria.getFriendsNumberOp().isPresent()) {
            conditions.add(String.format("FriendsNumber %s ?", User.Criteria.toOperator(criteria.getFriendsNumberOp().get())));
        }

        if (criteria.getAverageStarsOp().isPresent()) {
            conditions.add(String.format("AverageStars %s ?", User.Criteria.toOperator(criteria.getAverageStarsOp().get())));
        }

        if (criteria.getVotesNumberOp().isPresent()) {
            conditions.add(String.format("VotesNumber %s ?", User.Criteria.toOperator(criteria.getVotesNumberOp().get())));
        }
        String nsql = String.format("(%s) AND ROWNUM <= %s", StringUtils.join(conditions, String.format(" %s ", attributeOperator)), maxUsersNumber);
        return sql + nsql;
    }

    public List<User> fetch(User.Criteria criteria, int maxUsersNumber) throws SQLException {
        String sql = String.format("SELECT * FROM YelpUser WHERE ROWNUM <= %s", maxUsersNumber);
        if (criteria.hasCriteria()) {
            sql = buildSql(criteria, criteria.getAttributeOp(), maxUsersNumber);
        }
        
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if (criteria.hasCriteria()) {
            criteria.fillInPlaceHolder(preparedStatement, 1);
        }
        ResultSet rs = preparedStatement.executeQuery();
        List<User> users = User.from(rs);
        preparedStatement.close();
        return users;
    }
}
