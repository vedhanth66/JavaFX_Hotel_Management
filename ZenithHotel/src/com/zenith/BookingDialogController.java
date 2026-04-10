package com.zenith;

import com.zenith.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BookingDialogController {

    @FXML private TextField guestNameField;
    @FXML private ComboBox<String> roomTypeCombo;
    @FXML private ComboBox<String> roomSelectCombo;
    @FXML private ComboBox<String> guestSelectCombo;
    @FXML private DatePicker checkInDate;
    @FXML private DatePicker checkOutDate;
    @FXML private Label priceLabel;
    @FXML private Label nightsLabel;
    @FXML private Label errorLabel;
    @FXML private TextArea notesArea;

    private HotelDataStore dataStore;
    private boolean confirmed = false;

    @FXML
    public void initialize() {
        dataStore = HotelDataStore.getInstance();

        // Room type filter
        if (roomTypeCombo != null) {
            roomTypeCombo.getItems().addAll("All Types", "Standard", "Deluxe", "Suite", "Penthouse");
            roomTypeCombo.getSelectionModel().selectFirst();
            roomTypeCombo.setOnAction(e -> refreshAvailableRooms());
        }

        // Guest selection
        if (guestSelectCombo != null) {
            for (Guest g : dataStore.getGuests()) {
                guestSelectCombo.getItems().add(g.getFullName() + " (" + g.getEmail() + ")");
            }
            guestSelectCombo.setOnAction(e -> {
                int idx = guestSelectCombo.getSelectionModel().getSelectedIndex();
                if (idx >= 0 && idx < dataStore.getGuests().size() && guestNameField != null) {
                    guestNameField.setText(dataStore.getGuests().get(idx).getFullName());
                }
            });
        }

        // Dates
        if (checkInDate != null) {
            checkInDate.setValue(LocalDate.now());
            checkInDate.setOnAction(e -> updatePricing());
        }
        if (checkOutDate != null) {
            checkOutDate.setValue(LocalDate.now().plusDays(3));
            checkOutDate.setOnAction(e -> updatePricing());
        }

        refreshAvailableRooms();
        updatePricing();
    }

    private void refreshAvailableRooms() {
        if (roomSelectCombo == null) return;
        roomSelectCombo.getItems().clear();

        String typeFilter = roomTypeCombo.getValue();

        for (Room r : dataStore.getRooms()) {
            if (r.getStatus() != Room.RoomStatus.AVAILABLE) continue;

            if (typeFilter != null && !typeFilter.equals("All Types")) {
                if (!r.getTypeDisplay().equals(typeFilter)) continue;
            }

            roomSelectCombo.getItems().add(
                "Room " + r.getRoomNumber() + " — " + r.getTypeDisplay() + " ($" + String.format("%.0f", r.getPricePerNight()) + "/night)"
            );
        }

        if (!roomSelectCombo.getItems().isEmpty()) {
            roomSelectCombo.getSelectionModel().selectFirst();
        }
        
        if (roomSelectCombo.getOnAction() == null) {
            roomSelectCombo.setOnAction(e -> updatePricing());
        }
        
        updatePricing();
    }

    private void updatePricing() {
        if (priceLabel == null || nightsLabel == null) return;
        
        Room selectedRoom = getSelectedRoom();
        if (selectedRoom == null || checkInDate.getValue() == null || checkOutDate.getValue() == null) {
            priceLabel.setText("$0");
            nightsLabel.setText("0 nights");
            return;
        }

        long nights = ChronoUnit.DAYS.between(checkInDate.getValue(), checkOutDate.getValue());
        if (nights < 1) nights = 1;
        double total = nights * selectedRoom.getPricePerNight();

        nightsLabel.setText(nights + " night" + (nights > 1 ? "s" : ""));
        priceLabel.setText(HotelDataStore.getInstance().formatCurrency(total));
    }

    private Room getSelectedRoom() {
        if (roomSelectCombo == null || roomSelectCombo.getValue() == null) return null;
        String value = roomSelectCombo.getValue();
        // Extract room number from "Room 102 — ..."
        try {
            String num = value.split(" — ")[0].replace("Room ", "").trim();
            for (Room r : dataStore.getRooms()) {
                if (r.getRoomNumber().equals(num)) return r;
            }
        } catch (Exception e) { /* ignore */ }
        return null;
    }

    private Guest getSelectedGuest() {
        // First try guest select combo
        if (guestSelectCombo != null) {
            int idx = guestSelectCombo.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < dataStore.getGuests().size()) {
                return dataStore.getGuests().get(idx);
            }
        }
        // Otherwise create from name field
        if (guestNameField != null && !guestNameField.getText().trim().isEmpty()) {
            String name = guestNameField.getText().trim();
            // Check if guest exists
            for (Guest g : dataStore.getGuests()) {
                if (g.getFullName().equalsIgnoreCase(name)) return g;
            }
            // Create new guest
            return dataStore.addGuest(name, name.toLowerCase().replace(" ", ".") + "@guest.com", "", "", false);
        }
        return null;
    }

    @FXML
    private void handleConfirm() {
        if (errorLabel != null) errorLabel.setText("");

        Guest guest = getSelectedGuest();
        Room room = getSelectedRoom();

        if (guest == null) {
            if (errorLabel != null) errorLabel.setText("Please select or enter a guest name");
            return;
        }
        if (room == null) {
            if (errorLabel != null) errorLabel.setText("Please select an available room");
            return;
        }
        if (checkInDate.getValue() == null || checkOutDate.getValue() == null) {
            if (errorLabel != null) errorLabel.setText("Please select check-in and check-out dates");
            return;
        }
        if (!checkOutDate.getValue().isAfter(checkInDate.getValue())) {
            if (errorLabel != null) errorLabel.setText("Check-out must be after check-in");
            return;
        }

        String notes = (notesArea != null) ? notesArea.getText() : "";
        dataStore.createBooking(guest, room, checkInDate.getValue(), checkOutDate.getValue(), notes);
        confirmed = true;
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) checkInDate.getScene().getWindow();
        stage.close();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
