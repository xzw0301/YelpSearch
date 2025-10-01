package edu.scu.xzw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Business {
    private String businessId;
    private String fullAddress;
    private String hours;
    private String open;
    private String categories;
    private String city;
    private int reviewCount;
    private String name;
    private String neighborHoods;
    private String state;
    private double stars;
    private String attributes;
    private double longitude;
    private double latitude;
    private String type;

    public static Business from(JsonNode business) {
        String businessId = business.get("business_id").asText();
        String fullAddress = business.get("full_address").asText();
        String hours = business.get("hours").toString();
        String open = business.get("open").asBoolean() ? "Y" : "N";
        String categories = business.get("categories").toString();
        String city = business.get("city").asText();
        int reviewCount = business.get("review_count").asInt();
        String name = business.get("name").asText();
        String neighborHoods = business.get("neighborhoods").toString();
        double longitude = business.get("longitude").asDouble();
        double latitude = business.get("latitude").asDouble();
        String state = business.get("state").asText();
        double stars = business.get("stars").asDouble();
        String attributes = business.get("attributes").toString();
        String type = business.get("type").asText();
        return new Business(
            businessId,
            fullAddress,
            hours,
            open,
            categories,
            city,
            reviewCount,
            name,
            neighborHoods,
            state,
            stars,
            attributes,
            longitude,
            latitude,
            type
        );
    }

    public static Business from(ResultSet rs) throws SQLException {
        String businessId = rs.getString("BusinessID");
        String fullAddress = rs.getString("FullAddress");
        String hours = rs.getString("Hours");
        String open = rs.getString("Open");
        String categories = rs.getString("Categories");
        String city = rs.getString("City");
        int reviewCount = rs.getInt("ReviewCount");
        String name = rs.getString("Name");
        String neighborHoods = rs.getString("NeighborHoods");
        double longitude = rs.getDouble("Longitude");
        double latitude = rs.getDouble("Latitude");
        String state = rs.getString("State");
        double stars = rs.getDouble("Stars");
        String attributes = rs.getString("Attributes");
        String type = rs.getString("Type");
        return new Business(
            businessId,
            fullAddress,
            hours,
            open,
            categories,
            city,
            reviewCount,
            name,
            neighborHoods,
            state,
            stars,
            attributes,
            longitude,
            latitude,
            type
        );
    }

    public void addInsertStament(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, businessId);
        preparedStatement.setString(2, fullAddress);
        preparedStatement.setString(3, hours);
        preparedStatement.setString(4, open);
        preparedStatement.setString(5, categories);
        preparedStatement.setString(6, city);
        preparedStatement.setInt(7, reviewCount);
        preparedStatement.setString(8, name);
        preparedStatement.setString(9, state);
        preparedStatement.setDouble(10, stars);
        preparedStatement.setString(11, attributes);
        preparedStatement.setDouble(12, longitude);
        preparedStatement.setDouble(13, latitude);
        preparedStatement.setString(14, type);
        preparedStatement.addBatch();
    }

    public static List<String> getProperties() {
        return List.of("businessId", "fullAddress", "hours", "open", "categories", "city", "reviewCount", "name", "state",
            "stars", "attributes", "longitude", "latitude", "type");
    }
}
