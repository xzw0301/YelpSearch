package edu.scu.xzw;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class UserSearchController {
    @FXML
    private DatePicker memberSince;

    @FXML
    private ChoiceBox<String> reviewCountChoice;

    @FXML
    private TextField reviewCountValue;

    @FXML
    private ChoiceBox<String> friendsNumberChoice;

    @FXML
    private TextField friendsNumberValue;

    @FXML
    private ChoiceBox<String> averageStarsChoice;

    @FXML
    private TextField averageStarsValue;

    @FXML
    private ChoiceBox<String> votesNumerChoice;

    @FXML
    private TextField votesNumberValue;

    @FXML
    private ChoiceBox<String> attributesOpChoice;

    @FXML
    private TableView userResults;

    @FXML
    private TextField maxUsersNumber;

    @FXML
    private void searchUser() throws SQLException {
        userResults.getItems().clear();
        userResults.getColumns().clear();
        User.Criteria criteria =  new User.Criteria(
            Optional.ofNullable(memberSince.getValue()),
            Optional.ofNullable(reviewCountChoice.getValue()), 
            Optional.ofNullable(friendsNumberChoice.getValue()), 
            Optional.ofNullable(averageStarsChoice.getValue()),
            Optional.ofNullable(votesNumerChoice.getValue()),
            attributesOpChoice.getValue(),
            Integer.valueOf(reviewCountValue.getText()),
            Integer.valueOf(friendsNumberValue.getText()),
            Double.valueOf(averageStarsValue.getText()),
            Integer.valueOf(votesNumberValue.getText()));
        List<User> users = UserProcessor.getInstance(ProcessorFactory.getConnection()).fetch(criteria, Integer.valueOf(maxUsersNumber.getText()));
        List<String> userProperties = User.getProperties();
        for (String p : userProperties) {
            TableColumn<User, Object> tableColumn = new TableColumn<>(p);
            tableColumn.setCellValueFactory(new PropertyValueFactory<User, Object>(p));
            userResults.getColumns().add(tableColumn);
        }
        userResults.getItems().addAll(users);
    }

    @FXML
    private void viewUser() throws IOException, SQLException {
        User user = (User) userResults.getSelectionModel().getSelectedItem();

        FXMLLoader loader = CommonUtil.loadFile("UserDetail.fxml", "User Reviews");

        UserDetailController userDetailController = (UserDetailController) loader.getController();

        userDetailController.populate(user);
    }
}
