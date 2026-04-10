package com.zenith;

import com.zenith.model.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML private TextField hotelNameField;
    @FXML private ComboBox<String> currencyCombo;
    @FXML private TextField contactEmailField;
    @FXML private TextField contactPhoneField;
    @FXML private TextField rateStandard;
    @FXML private TextField rateDeluxe;
    @FXML private TextField rateSuite;
    @FXML private TextField ratePenthouse;
    @FXML private CheckBox chkEmailNotif;
    @FXML private CheckBox chkSoundNotif;
    @FXML private CheckBox chkAutoCheckout;
    @FXML private CheckBox chkDarkMode;
    @FXML private CheckBox chkAnimations;
    @FXML private CheckBox chkCompactView;
    @FXML private ComboBox<String> maintenanceRoomCombo;
    @FXML private Label maintenanceStatus;
    @FXML private Label settingsStatus;

    private HotelDataStore dataStore;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = HotelDataStore.getInstance();

        // Currency
        currencyCombo.getItems().addAll("INR (₹)");
        currencyCombo.getSelectionModel().select(dataStore.getCurrency());
        if (currencyCombo.getSelectionModel().getSelectedIndex() == -1) {
            currencyCombo.getSelectionModel().selectFirst();
        }

        // Load Hotel Info
        hotelNameField.setText(dataStore.getHotelName());
        contactEmailField.setText(dataStore.getContactEmail());
        contactPhoneField.setText(dataStore.getContactPhone());

        // Load Preferences
        chkEmailNotif.setSelected(dataStore.isEmailNotifications());
        chkSoundNotif.setSelected(dataStore.isSoundAlerts());
        chkAutoCheckout.setSelected(dataStore.isAutoCheckoutReminders());
        chkDarkMode.setSelected(dataStore.isDarkMode());
        chkAnimations.setSelected(dataStore.isUiAnimations());
        chkCompactView.setSelected(dataStore.isCompactRoomView());

        // Load current rates
        loadCurrentRates();

        // Populate maintenance room combo
        refreshMaintenanceCombo();
    }

    private void loadCurrentRates() {
        // Find current rates from room data
        for (Room r : dataStore.getRooms()) {
            switch (r.getType()) {
                case STANDARD: rateStandard.setText(String.format("%.2f", r.getPricePerNight())); break;
                case DELUXE: rateDeluxe.setText(String.format("%.2f", r.getPricePerNight())); break;
                case SUITE: rateSuite.setText(String.format("%.2f", r.getPricePerNight())); break;
                case PENTHOUSE: ratePenthouse.setText(String.format("%.2f", r.getPricePerNight())); break;
            }
        }
    }

    private void refreshMaintenanceCombo() {
        maintenanceRoomCombo.getItems().clear();
        for (Room r : dataStore.getRooms()) {
            String label = "Room " + r.getRoomNumber() + " (" + r.getStatusDisplay() + ")";
            maintenanceRoomCombo.getItems().add(label);
        }
    }

    @FXML
    private void handleSaveSettings() {
        dataStore.setHotelName(hotelNameField.getText());
        dataStore.setCurrency(currencyCombo.getSelectionModel().getSelectedItem());
        dataStore.setContactEmail(contactEmailField.getText());
        dataStore.setContactPhone(contactPhoneField.getText());

        dataStore.setEmailNotifications(chkEmailNotif.isSelected());
        dataStore.setSoundAlerts(chkSoundNotif.isSelected());
        dataStore.setAutoCheckoutReminders(chkAutoCheckout.isSelected());
        dataStore.setDarkMode(chkDarkMode.isSelected());
        dataStore.setUiAnimations(chkAnimations.isSelected());
        dataStore.setCompactRoomView(chkCompactView.isSelected());

        settingsStatus.setText("Hotel info & preferences saved!");
        settingsStatus.setStyle("-fx-text-fill: #10b981;");
        ActivityLog.getInstance().log("System settings and preferences updated.");
    }

    @FXML
    private void handleApplyRates() {
        try {
            double std = Double.parseDouble(rateStandard.getText().trim());
            double dlx = Double.parseDouble(rateDeluxe.getText().trim());
            double ste = Double.parseDouble(rateSuite.getText().trim());
            double pnt = Double.parseDouble(ratePenthouse.getText().trim());

            for (Room r : dataStore.getRooms()) {
                switch (r.getType()) {
                    case STANDARD: r.setPricePerNight(std); break;
                    case DELUXE: r.setPricePerNight(dlx); break;
                    case SUITE: r.setPricePerNight(ste); break;
                    case PENTHOUSE: r.setPricePerNight(pnt); break;
                }
            }
            settingsStatus.setText("Room rates updated successfully!");
            settingsStatus.setStyle("-fx-text-fill: #10b981;");
            ActivityLog.getInstance().log("Room rates updated: Std=$" + std + " Dlx=$" + dlx + " Ste=$" + ste + " Pnt=$" + pnt);
        } catch (NumberFormatException e) {
            settingsStatus.setText("Invalid rate values. Please enter valid numbers.");
            settingsStatus.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    @FXML
    private void handleSetMaintenance() {
        int idx = maintenanceRoomCombo.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < dataStore.getRooms().size()) {
            Room room = dataStore.getRooms().get(idx);
            if (room.getStatus() == Room.RoomStatus.OCCUPIED || room.getStatus() == Room.RoomStatus.RESERVED) {
                maintenanceStatus.setText("Cannot set maintenance: Room " + room.getRoomNumber() + " is " + room.getStatusDisplay());
                maintenanceStatus.setStyle("-fx-text-fill: #ef4444;");
            } else {
                dataStore.setRoomMaintenance(room, true);
                maintenanceStatus.setText("Room " + room.getRoomNumber() + " set to Maintenance");
                maintenanceStatus.setStyle("-fx-text-fill: #f59e0b;");
                refreshMaintenanceCombo();
                maintenanceRoomCombo.getSelectionModel().select(idx);
            }
        } else {
            maintenanceStatus.setText("Please select a room first");
            maintenanceStatus.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    @FXML
    private void handleClearMaintenance() {
        int idx = maintenanceRoomCombo.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < dataStore.getRooms().size()) {
            Room room = dataStore.getRooms().get(idx);
            if (room.getStatus() == Room.RoomStatus.MAINTENANCE) {
                dataStore.setRoomMaintenance(room, false);
                maintenanceStatus.setText("Room " + room.getRoomNumber() + " restored to Available");
                maintenanceStatus.setStyle("-fx-text-fill: #10b981;");
                refreshMaintenanceCombo();
                maintenanceRoomCombo.getSelectionModel().select(idx);
            } else {
                maintenanceStatus.setText("Room " + room.getRoomNumber() + " is not in maintenance");
                maintenanceStatus.setStyle("-fx-text-fill: #64748b;");
            }
        }
    }
}
