package edu.scu.xzw;

import java.sql.SQLException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class UserDetailController {
    @FXML
    private TableView userReviewTable;

    public void populate(User user) throws SQLException {

        List<String> businessProperies = Review.getProperties();
        for (String p : businessProperies) {
            TableColumn<Review, Object> tableColumn = new TableColumn<>(p);
            tableColumn.setCellValueFactory(new PropertyValueFactory<Review, Object>(p));
            userReviewTable.getColumns().add(tableColumn);
        }
        List<Review> reviews = ReviewProcessor.getInstance(ProcessorFactory.getConnection()).fetchWithUserName("YelpUser.UserID", user.getUserId());
        userReviewTable.getItems().addAll(reviews);      
    }
    
}
