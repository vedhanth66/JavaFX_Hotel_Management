package com.zenith;

import com.zenith.model.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class RoomController implements Initializable {

    @FXML private TilePane roomGrid;
    @FXML private Button btnAll;
    @FXML private Button btnAvailable;
    @FXML private Button btnOccupied;
    @FXML private Button btnMaintenance;
    @FXML private Label roomCountLabel;
    @FXML private ComboBox<String> floorFilter;

    private HotelDataStore dataStore;
    private String currentFilter = "ALL";
    private int currentFloor = 0; // 0 = all floors

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = HotelDataStore.getInstance();
        
        // Setup floor filter
        if (floorFilter != null) {
            floorFilter.getItems().addAll("All Floors", "Floor 1", "Floor 2", "Floor 3");
            floorFilter.getSelectionModel().selectFirst();
            floorFilter.setOnAction(e -> {
                int idx = floorFilter.getSelectionModel().getSelectedIndex();
                currentFloor = idx; // 0=all, 1=floor1, 2=floor2, 3=floor3
                refreshRoomGrid();
            });
        }
        
        refreshRoomGrid();
    }

    private void refreshRoomGrid() {
        roomGrid.getChildren().clear();
        int count = 0;
        
        for (Room room : dataStore.getRooms()) {
            // Floor filter
            if (currentFloor > 0 && room.getFloor() != currentFloor) continue;
            
            // Status filter
            if (!currentFilter.equals("ALL")) {
                if (currentFilter.equals("AVAILABLE") && room.getStatus() != Room.RoomStatus.AVAILABLE) continue;
                if (currentFilter.equals("OCCUPIED") && room.getStatus() != Room.RoomStatus.OCCUPIED && room.getStatus() != Room.RoomStatus.RESERVED) continue;
                if (currentFilter.equals("MAINTENANCE") && room.getStatus() != Room.RoomStatus.MAINTENANCE) continue;
            }
            
            roomGrid.getChildren().add(createRoomCard(room));
            count++;
        }
        
        if (roomCountLabel != null) {
            roomCountLabel.setText(count + " rooms");
        }
    }

    private VBox createRoomCard(Room room) {
        VBox card = new VBox();
        card.getStyleClass().addAll("glass-card", "room-card");
        card.setSpacing(6);

        Label numLabel = new Label("Room " + room.getRoomNumber());
        numLabel.getStyleClass().add("room-number");

        Label typeLabel = new Label(room.getTypeDisplay());
        typeLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        Label priceLabel = new Label("$" + String.format("%.0f", room.getPricePerNight()) + "/night");
        priceLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        Label statusLabel = new Label(room.getStatusDisplay());
        switch (room.getStatus()) {
            case AVAILABLE:
                statusLabel.getStyleClass().add("status-badge-available");
                break;
            case OCCUPIED:
                statusLabel.getStyleClass().add("status-badge-occupied");
                break;
            case RESERVED:
                statusLabel.getStyleClass().add("status-badge-reserved");
                break;
            case MAINTENANCE:
                statusLabel.getStyleClass().add("status-badge-maintenance");
                break;
        }

        card.getChildren().addAll(numLabel, typeLabel, priceLabel, statusLabel);

        // Show guest name if occupied/reserved
        if ((room.getStatus() == Room.RoomStatus.OCCUPIED || room.getStatus() == Room.RoomStatus.RESERVED)
                && room.getCurrentGuestName() != null && !room.getCurrentGuestName().isEmpty()) {
            Label guestLabel = new Label(room.getCurrentGuestName());
            guestLabel.setStyle("-fx-text-fill: #00f2ff; -fx-font-size: 10px;");
            guestLabel.setWrapText(true);
            card.getChildren().add(guestLabel);
        }

        // Click for room actions
        card.setOnMouseClicked(e -> showRoomActions(room));

        return card;
    }

    private void showRoomActions(Room room) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Room " + room.getRoomNumber());
        dialog.setHeaderText(room.getTypeDisplay() + " — " + room.getStatusDisplay());

        StringBuilder content = new StringBuilder();
        content.append("Price: $").append(String.format("%.0f", room.getPricePerNight())).append("/night\n");
        content.append("Floor: ").append(room.getFloor()).append("\n");
        if (room.getCurrentGuestName() != null && !room.getCurrentGuestName().isEmpty()) {
            content.append("Guest: ").append(room.getCurrentGuestName()).append("\n");
        }

        dialog.setContentText(content.toString());

        ButtonType checkInBtn = new ButtonType("Check In");
        ButtonType checkOutBtn = new ButtonType("Check Out");
        ButtonType maintenanceBtn = new ButtonType("Toggle Maintenance");
        ButtonType cancelBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getButtonTypes().setAll(checkInBtn, checkOutBtn, maintenanceBtn, cancelBtn);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (result.get() == checkInBtn) {
                // Find confirmed booking for this room
                for (Booking b : dataStore.getBookings()) {
                    if (b.getRoom() == room && b.getStatus() == Booking.BookingStatus.CONFIRMED) {
                        dataStore.checkIn(b);
                        break;
                    }
                }
            } else if (result.get() == checkOutBtn) {
                for (Booking b : dataStore.getBookings()) {
                    if (b.getRoom() == room && b.getStatus() == Booking.BookingStatus.CHECKED_IN) {
                        dataStore.checkOut(b);
                        break;
                    }
                }
            } else if (result.get() == maintenanceBtn) {
                if (room.getStatus() == Room.RoomStatus.MAINTENANCE) {
                    dataStore.setRoomMaintenance(room, false);
                } else if (room.getStatus() == Room.RoomStatus.AVAILABLE) {
                    dataStore.setRoomMaintenance(room, true);
                }
            }
            refreshRoomGrid();
        }
    }

    @FXML
    public void handleFilterAll() {
        currentFilter = "ALL";
        setFilterActive(btnAll);
        refreshRoomGrid();
    }

    @FXML
    public void handleFilterAvailable() {
        currentFilter = "AVAILABLE";
        setFilterActive(btnAvailable);
        refreshRoomGrid();
    }

    @FXML
    public void handleFilterOccupied() {
        currentFilter = "OCCUPIED";
        setFilterActive(btnOccupied);
        refreshRoomGrid();
    }

    @FXML
    public void handleFilterMaintenance() {
        currentFilter = "MAINTENANCE";
        setFilterActive(btnMaintenance);
        refreshRoomGrid();
    }

    private void setFilterActive(Button active) {
        Button[] filterBtns = {btnAll, btnAvailable, btnOccupied, btnMaintenance};
        for (Button b : filterBtns) {
            if (b != null) {
                b.getStyleClass().remove("filter-active");
            }
        }
        if (active != null) {
            active.getStyleClass().add("filter-active");
        }
    }

    @FXML
    public void handleAddBooking() {
        try {
            URL resource = getClass().getResource("booking_dialog.fxml");
            if (resource == null) return;

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Create New Booking");
            Scene scene = new Scene(root);
            URL css = getClass().getResource("hotel_style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
            
            // Refresh after booking
            refreshRoomGrid();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
