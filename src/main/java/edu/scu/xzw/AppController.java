package edu.scu.xzw;

import java.io.IOException;
import java.io.InputStream;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class AppController {

    @FXML //  fx:id="searchChoice"
    private ChoiceBox<String> searchChoice; // Value injected by FXMLLoader
    

    @FXML
    private void onSearchClicked() throws IOException {
        if (searchChoice.getValue().equals("Business Search")) {
            FXMLLoader loader = CommonUtil.loadFile("BusinessSearch.fxml", "Business Search");
            BusinessSearchController businessSearchController = (BusinessSearchController) loader.getController();
            businessSearchController.setup();
        } else {
            CommonUtil.loadFile("UserSearch.fxml", "User Search");
        }
    }
}
