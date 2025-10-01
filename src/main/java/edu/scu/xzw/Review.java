package edu.scu.xzw;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.gluonhq.charm.glisten.control.settings.Option;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Review {

    @AllArgsConstructor
    @Data
    static class Criteria {
        private Optional<LocalDate> from;
        private Optional<LocalDate> to;
        private Optional<String> starsOperator;
        private Optional<String> votesOperator;
        private int stars;
        private int votesNumber;

        public boolean hasCriteria() {
            return from.isPresent() || to.isPresent() || starsOperator.isPresent() || votesOperator.isPresent();
        }

        public static String toOperator(String op) {
            return op.equals("greater than") ? ">" : op.equals("equal to") ? "=" : "<";
        }

        public void fillInPlaceHolder(PreparedStatement preparedStatement, int idx) throws SQLException {
            if (from.isPresent()) {
                preparedStatement.setDate(idx++, java.sql.Date.valueOf(from.get()));
            }
            if (to.isPresent()) {
                preparedStatement.setDate(idx++, java.sql.Date.valueOf(to.get()));
            }
            if (starsOperator.isPresent()) {
                preparedStatement.setInt(idx++, stars);
            }
            if (votesOperator.isPresent()) {
                preparedStatement.setInt(idx++, votesNumber);
            }
        }

        public List<Object> getArguments() {
            List<Object> arguments = new ArrayList<>();
            if (from.isPresent()) {
                arguments.add(from.get());
            }
            if (to.isPresent()) {
                arguments.add(to.get());
            }
            if (starsOperator.isPresent()) {
                arguments.add(stars);
            }
            if (votesOperator.isPresent()) {
                arguments.add(votesNumber);
            }
            return arguments;
        }
    }

    private String reviewId;
    private String businessId;
    private String userId;
    private String votes;
    private int votesNumber;
    private int stars;
    private Date reviewDate;
    private String text;
    private String type;
    private String user;

    public static Review from(JsonNode review) {
        String reviewId = review.get("review_id").asText();
        String businessId = review.get("business_id").asText();
        String userId = review.get("user_id").asText();
        String votes = review.get("votes").toString();
        JsonNode votesNode = review.get("votes");
        int votesNumber = votesNode.get("funny").asInt() + votesNode.get("cool").asInt() + votesNode.get("useful").asInt();
        int stars = review.get("stars").asInt();
        String date = review.get("date").asText();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date reviewDate;
        try {
            reviewDate = format.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        String text = review.get("text").asText();
        String type = review.get("type").asText();
        return new Review.ReviewBuilder().reviewId(reviewId)
            .businessId(businessId)
            .userId(userId)
            .votes(votes)
            .votesNumber(votesNumber)
            .stars(stars)
            .reviewDate(reviewDate)
            .text(text)
            .type(type)
            .build();
    }

    public void addInsertStament(PreparedStatement preparedStatement) throws SQLException {
        
        preparedStatement.setString(1, reviewId);
        preparedStatement.setString(2, businessId);
        preparedStatement.setString(3, userId);
        preparedStatement.setString(4, votes);
        preparedStatement.setInt(5, votesNumber);
        preparedStatement.setInt(6, stars);
        preparedStatement.setDate(7, new java.sql.Date(reviewDate.getTime()));
        preparedStatement.setString(8, text.substring(0, Math.min(2999, text.length())));
        preparedStatement.setString(9, type);
        preparedStatement.addBatch();
    }

    public static List<Review> from(ResultSet rs) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        while (rs.next()) {
            String reviewId = rs.getString("ReviewID");
            String userId = rs.getString("UserID");
            String businessId = rs.getString("BusinessID");
            String votes = rs.getString("Votes");
            int votesNumber = rs.getInt("VotesNumber");
            int stars = rs.getInt("Stars");
            Date date = rs.getDate("ReviewDate");
            String text = rs.getString("Text");
            String type = rs.getString("Type");
            String user = rs.getString("Name");
            
            reviews.add(new Review(reviewId, businessId, userId, votes, votesNumber, stars, date, text, type, user));
        }
        rs.close();
        return reviews;
    }

    public static List<String> getProperties() {
        return List.of("reviewId", "userId", "user", "businessId", "votes", "votesNumber", "stars", "reviewDate", "text", "type");
    }
}
