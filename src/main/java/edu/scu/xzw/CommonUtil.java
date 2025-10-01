package edu.scu.xzw;

import java.io.IOException;
import java.io.InputStream;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class CommonUtil {
    public static FXMLLoader loadFile(String filePath, String title, Stage stage) throws IOException {
        // Create the FXMLLoader 
        FXMLLoader loader = new FXMLLoader();
        ClassLoader classLoader = CommonUtil.class.getClassLoader();
        InputStream fxmlStream = classLoader.getResourceAsStream(filePath);
 
        // Create the Pane and all Details
        AnchorPane root = (AnchorPane) loader.load(fxmlStream);
 
        // Create the Scene
        Scene scene = new Scene(root);
        // Set the Scene to the Stage
        stage.setScene(scene);
        // Set the Title to the Stage
        stage.setTitle(title);
        // Display the Stage
        stage.show();
        return loader;
    }

    public static FXMLLoader loadFile(String filePath, String title) throws IOException {
        return loadFile(filePath, title, new Stage());
    }
}
