package com.zenith;

import com.zenith.model.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML private Label uptimeLabel;
    @FXML private Label lastActionLabel;
    @FXML private Label totalRoomsLabel;
    @FXML private Label totalGuestsLabel;
    @FXML private Label totalBookingsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private ListView<String> activityLogList;
    @FXML private TextArea reportArea;

    private HotelDataStore dataStore;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = HotelDataStore.getInstance();
        refreshStats();
        
        // Bind activity log
        activityLogList.setItems(ActivityLog.getInstance().getEntries());
        
        uptimeLabel.setText("Active since " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private void refreshStats() {
        totalRoomsLabel.setText(String.valueOf(dataStore.getRooms().size()));
        totalGuestsLabel.setText(String.valueOf(dataStore.getGuests().size()));
        totalBookingsLabel.setText(String.valueOf(dataStore.getBookings().size()));
        totalRevenueLabel.setText(dataStore.formatCurrency(dataStore.getTotalRevenue()));
        lastActionLabel.setText("Last refresh: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    @FXML
    private void handleGenerateReport() {
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════\n");
        report.append("   ZENITH HOTEL — SYSTEM REPORT\n");
        report.append("   Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        report.append("═══════════════════════════════════\n\n");

        report.append("ROOM SUMMARY\n");
        report.append("─────────────────────────────\n");
        report.append("Total Rooms:      ").append(dataStore.getRooms().size()).append("\n");
        report.append("Available:        ").append(dataStore.getAvailableRoomCount()).append("\n");
        report.append("Occupied:         ").append(dataStore.getOccupiedRoomCount()).append("\n");
        report.append("Reserved:         ").append(dataStore.getReservedRoomCount()).append("\n");
        report.append("Maintenance:      ").append(dataStore.getMaintenanceRoomCount()).append("\n");
        report.append("Occupancy Rate:   ").append(String.format("%.1f%%", dataStore.getOccupancyRate() * 100)).append("\n\n");

        report.append("GUEST SUMMARY\n");
        report.append("─────────────────────────────\n");
        report.append("Total Guests:     ").append(dataStore.getGuests().size()).append("\n");
        long vipCount = dataStore.getGuests().stream().filter(Guest::isVip).count();
        report.append("VIP Guests:       ").append(vipCount).append("\n");
        report.append("Regular Guests:   ").append(dataStore.getGuests().size() - vipCount).append("\n\n");

        report.append("BOOKING SUMMARY\n");
        report.append("─────────────────────────────\n");
        report.append("Total Bookings:   ").append(dataStore.getBookings().size()).append("\n");
        long active = dataStore.getActiveBookingsCount();
        report.append("Active:           ").append(active).append("\n");
        long checkedOut = dataStore.getBookings().stream().filter(b -> b.getStatus() == Booking.BookingStatus.CHECKED_OUT).count();
        report.append("Checked Out:      ").append(checkedOut).append("\n");
        long cancelled = dataStore.getBookings().stream().filter(b -> b.getStatus() == Booking.BookingStatus.CANCELLED).count();
        report.append("Cancelled:        ").append(cancelled).append("\n\n");

        report.append("FINANCIAL SUMMARY\n");
        report.append("─────────────────────────────\n");
        report.append("Total Revenue:    ").append(dataStore.formatCurrency(dataStore.getTotalRevenue())).append("\n");
        report.append("Avg Stay Duration: ").append(String.format("%.1f nights", dataStore.getAverageStayDuration())).append("\n\n");

        report.append("REVENUE BY ROOM TYPE\n");
        report.append("─────────────────────────────\n");
        report.append("Standard:    ").append(dataStore.formatCurrency(dataStore.getRevenueByRoomType(Room.RoomType.STANDARD))).append("\n");
        report.append("Deluxe:      ").append(dataStore.formatCurrency(dataStore.getRevenueByRoomType(Room.RoomType.DELUXE))).append("\n");
        report.append("Suite:       ").append(dataStore.formatCurrency(dataStore.getRevenueByRoomType(Room.RoomType.SUITE))).append("\n");
        report.append("Penthouse:   ").append(dataStore.formatCurrency(dataStore.getRevenueByRoomType(Room.RoomType.PENTHOUSE))).append("\n\n");

        report.append("═══════════════════════════════════\n");
        report.append("        END OF REPORT\n");
        report.append("═══════════════════════════════════\n");

        reportArea.setText(report.toString());
        ActivityLog.getInstance().log("System report generated");
    }

    @FXML
    private void handleExportSummary() {
        if (reportArea.getText() == null || reportArea.getText().isEmpty()) {
            handleGenerateReport();
        }
        System.out.println("\n" + reportArea.getText());
        ActivityLog.getInstance().log("Report exported to console");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Complete");
        alert.setHeaderText("Report Exported");
        alert.setContentText("The summary report has been exported to the console output.");
        alert.showAndWait();
    }

    @FXML
    private void handleRefreshStats() {
        refreshStats();
        ActivityLog.getInstance().log("Admin stats refreshed");
    }

    @FXML
    private void handleResetData() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset All Data");
        confirm.setHeaderText("Are you sure you want to reset all data?");
        confirm.setContentText("This will restore all rooms, guests, and bookings to their default sample values. This cannot be undone.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            dataStore.resetAllData();
            refreshStats();
            reportArea.clear();
        }
    }

    @FXML
    private void handleClearLog() {
        ActivityLog.getInstance().clear();
    }
}
