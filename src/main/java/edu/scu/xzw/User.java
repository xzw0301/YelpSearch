package edu.scu.xzw;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class User {

    @AllArgsConstructor
    @Data
    static class Criteria {
        private Optional<LocalDate> memberSince;
        private Optional<String> reviewCountOp;
        private Optional<String> friendsNumberOp;
        private Optional<String> averageStarsOp;
        private Optional<String> votesNumberOp;
        private String attributeOp;
        private int reviewCount;
        private int friendsNumber;
        private double averageStars;
        private int votesNumber;

        public boolean hasCriteria() {
            return memberSince.isPresent() || reviewCountOp.isPresent() || 
                friendsNumberOp.isPresent() || averageStarsOp.isPresent() || votesNumberOp.isPresent();
        }

        public static String toOperator(String op) {
            return op.equals("greater than") ? ">" : op.equals("equal to") ? "=" : "<";
        }

        public void fillInPlaceHolder(PreparedStatement preparedStatement, int idx) throws SQLException {
            if (memberSince.isPresent()) {
                preparedStatement.setDate(idx++, java.sql.Date.valueOf(memberSince.get()));
            }
            if (reviewCountOp.isPresent()) {
                preparedStatement.setInt(idx++, reviewCount);
            }
            if (friendsNumberOp.isPresent()) {
                preparedStatement.setInt(idx++, friendsNumber);
            }
            if (averageStarsOp.isPresent()) {
                preparedStatement.setDouble(idx++, averageStars);
            }
            if (votesNumberOp.isPresent()) {
                preparedStatement.setInt(idx++, votesNumber);
            }
        }
    }

    private String userId;
    private Date since;
    private String votes;
    private int votesNumber;
    private int reviewCount;
    private String name;
    private String friends;
    private int friendsNumber;
    private int fans;
    private double averageStars;
    private String type;
    private String compliments;
    private String elite;

    public static User from(JsonNode user) {
        String userId = user.get("user_id").asText();
        String since = user.get("yelping_since").asText();
        DateFormat format = new SimpleDateFormat("yyyy-MM", Locale.ENGLISH);
        Date date;
        try {
            date = format.parse(since);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        String votes = user.get("votes").toString();
        JsonNode votesNode = user.get("votes");
        int votesNumber = votesNode.get("funny").asInt() + votesNode.get("cool").asInt() + votesNode.get("useful").asInt();
        int reviewCount = user.get("review_count").asInt();
        String name = user.get("name").asText();
        String friends = user.get("friends").asText();
        int friendsNumber = user.get("friends").size();
        int fans = user.get("fans").asInt();
        double averageStars = user.get("average_stars").asDouble();
        String type = user.get("type").asText();
        String compliments = user.get("compliments").toString();
        String elite = user.get("elite").toString();
        return new User(userId, date, votes, votesNumber, reviewCount, name, friends, friendsNumber, fans, averageStars, type, compliments, elite);
    }

    public void addInsertStament(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, userId);
        preparedStatement.setDate(2, new java.sql.Date(since.getTime()));
        preparedStatement.setString(3, votes);
        preparedStatement.setInt(4, votesNumber);
        preparedStatement.setInt(5, reviewCount);
        preparedStatement.setString(6, name);
        preparedStatement.setString(7, friends);
        preparedStatement.setInt(8, friendsNumber);
        preparedStatement.setInt(9, fans);
        preparedStatement.setDouble(10, averageStars);
        preparedStatement.setString(11, type);
        preparedStatement.setString(12, compliments);
        preparedStatement.setString(13, elite);
        preparedStatement.addBatch();
    }

    public static List<User> from(ResultSet rs) throws SQLException {
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            String userId = rs.getString("UserID");
            Date since = rs.getDate("Since");
            String votes = rs.getString("Votes");
            int votesNumber = rs.getInt("VotesNumber");
            int reviewCount = rs.getInt("ReviewCount");
            String name = rs.getString("Name");
            String friends = rs.getString("Friends");
            int friendsNumber = rs.getInt("FriendsNumber");
            int fans = rs.getInt("Fans");
            double averageStars = rs.getDouble("AverageStars");
            String type = rs.getString("Type");
            String compliments = rs.getString("Compliments");
            String elite = rs.getString("Elite");
            users.add(new User(userId, since, votes, votesNumber, reviewCount, name, friends, friendsNumber, fans, averageStars, type, compliments, elite));
        }
        rs.close();
        return users;
    }

    public static List<String> getProperties() {
        return List.of("userId", "since", "votes",  "votesNumber", "reviewCount", "name", "friends", "friendsNumber",
            "fans", "averageStars", "type", "compliments", "elite");
    }
}
