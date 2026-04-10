package com.zenith;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Label clockLabel;
    
    // Sidebar Buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnRooms;
    @FXML private Button btnGuests;
    @FXML private Button btnAnalytics;
    @FXML private Button btnSettings;
    @FXML private Button btnAdmin;

    private List<Button> navButtons;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        navButtons = Arrays.asList(btnDashboard, btnRooms, btnGuests, btnAnalytics, btnSettings, btnAdmin);
        
        // Update clock
        if (clockLabel != null) {
            updateClock();
            javafx.animation.Timeline clock = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.seconds(30), e -> updateClock())
            );
            clock.setCycleCount(javafx.animation.Animation.INDEFINITE);
            clock.play();
        }
        
        // Load dashboard by default
        showDashboard();
    }

    private void updateClock() {
        if (clockLabel != null) {
            clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, MMM d · HH:mm")));
        }
    }

    @FXML
    public void showDashboard() {
        loadView("dashboard_content.fxml");
        setActiveButton(btnDashboard);
    }

    @FXML
    public void showRooms() {
        loadView("rooms.fxml");
        setActiveButton(btnRooms);
    }

    @FXML
    public void showGuests() {
        loadView("guests.fxml");
        setActiveButton(btnGuests);
    }

    @FXML
    public void showAnalytics() {
        loadView("analytics.fxml");
        setActiveButton(btnAnalytics);
    }

    @FXML
    public void showSettings() {
        loadView("settings.fxml");
        setActiveButton(btnSettings);
    }

    @FXML
    public void showAdmin() {
        loadView("admin.fxml");
        setActiveButton(btnAdmin);
    }

    private void loadView(String fxml) {
        try {
            URL resource = getClass().getResource(fxml);
            if (resource == null) {
                System.err.println("ERROR: Resource not found: " + fxml);
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            
            // Fade transition
            FadeTransition fade = new FadeTransition(Duration.millis(200), view);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            
            contentArea.getChildren().setAll(view);
            fade.play();
            
        } catch (Exception e) {
            System.err.println("FAILURE loading: " + fxml);
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeBtn) {
        if (navButtons == null) return;
        
        for (Button btn : navButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("nav-button-active");
            }
        }
        
        if (activeBtn != null && !activeBtn.getStyleClass().contains("nav-button-active")) {
            activeBtn.getStyleClass().add("nav-button-active");
        }
    }
}
