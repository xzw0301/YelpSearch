package edu.scu.xzw;

import java.sql.SQLException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class BusinessDetailController {
    
    @FXML
    private Label businessNameLabel;

    @FXML
    private Label cityLabel;

    @FXML
    private Label stateLabel;

    @FXML
    private Label starLabel;

    @FXML
    private TableView businessReviewTable;

    public void populate(Business business) throws SQLException {
        businessNameLabel.setText(business.getName());
        cityLabel.setText(business.getCity());
        stateLabel.setText(business.getState());
        starLabel.setText(String.valueOf(business.getStars()));

        List<String> businessProperies = Review.getProperties();
        for (String p : businessProperies) {
            TableColumn<Review, Object> tableColumn = new TableColumn<>(p);
            tableColumn.setCellValueFactory(new PropertyValueFactory<Review, Object>(p));
            businessReviewTable.getColumns().add(tableColumn);
        }
        List<Review> reviews = ReviewProcessor.getInstance(ProcessorFactory.getConnection()).fetchWithUserName("BusinessID", business.getBusinessId());
        businessReviewTable.getItems().addAll(reviews);      
    }
}


