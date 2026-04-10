package com.zenith;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.net.URL;

public class ZenithHotelApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL resource = getClass().getResource("main_layout.fxml");
        if (resource == null) {
            System.err.println("CRITICAL ERROR: main_layout.fxml not found in package folder!");
            // Try loading with slash as fallback
            resource = getClass().getResource("/com/zenith/main_layout.fxml");
        }
        
        if (resource == null) {
            throw new RuntimeException("Could not find main_layout.fxml");
        }
        
        System.out.println("Loading FXML from: " + resource);
        
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        URL cssResource = getClass().getResource("hotel_style.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        }
        
        primaryStage.setTitle("Zenith Hotel Suite - Premium Management");
        primaryStage.setScene(scene);
        
        // Premium look: optional undecorated or silver border (keeping standard for compatibility but adding flair)
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
