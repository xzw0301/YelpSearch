package edu.scu.xzw;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Hello world!
 *
 */
public class Populate {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int BATCH_SIZE = 100;
    private Connection getConnection() throws SQLException {
        //Class.forName("oracle.jdbc.OracleDriver");
        return ProcessorFactory.getConnection();
    }

    private void populateBusiness(String businessFilePath) throws IOException, SQLException {
        populate(businessFilePath, "business");
    }

    private void populate(String filePath, String type) throws IOException, SQLException {
        Connection connection = getConnection();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        List<JsonNode> batch = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(line);
            batch.add(jsonNode);
            if (batch.size() == BATCH_SIZE) {
                Populator populator = ProcessorFactory.getInstance(type, connection);

                populator.populate(batch);
                batch.clear();
            }
        }
        if (batch.size() > 0) {
            Populator populator = ProcessorFactory.getInstance(type, connection);
            populator.populate(batch);
        }
    }

    private void unpopulateBusiness() throws SQLException {
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement("DELETE FROM Category");
        ResultSet rs = statement.executeQuery();
        statement.close();
        rs.close();

        statement = connection.prepareStatement("DELETE FROM Attribute");
        rs = statement.executeQuery();
        statement.close();
        rs.close();

        statement = connection.prepareStatement("DELETE FROM Business");
        rs = statement.executeQuery();
        statement.close();
        rs.close();
    }

    private void unpopulateUser() throws SQLException {
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement("DELETE FROM YelpUser");
        ResultSet rs = statement.executeQuery();
        statement.close();
        rs.close();
    }

    private void populateUser(String userFilePath) throws IOException, SQLException {
        populate(userFilePath, "users");
    }

    private void unpopulateReview() throws SQLException {
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement("DELETE FROM Review");
        ResultSet rs = statement.executeQuery();
        statement.close();
        rs.close();
    }

    private void populateReview(String reviewFilePath) throws IOException, SQLException {
        populate(reviewFilePath, "reviews");
    }

    public static void main(String[] args) throws IOException, SQLException {
        Populate populate = new Populate();
        populate.unpopulateBusiness();
        populate.populateBusiness(args[0]);
        populate.unpopulateUser();
        populate.populateUser(args[1]);
        populate.unpopulateReview();
        populate.populateReview(args[2]);
    }
}
