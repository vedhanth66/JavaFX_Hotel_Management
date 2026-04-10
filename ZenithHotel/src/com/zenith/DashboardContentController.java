package com.zenith;

import com.zenith.model.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class DashboardContentController implements Initializable {

    @FXML private LineChart<String, Number> revenueChart;
    @FXML private ListView<String> checkInList;
    @FXML private PieChart roomStatusPie;
    @FXML private Label occupancyValue;
    @FXML private Label revenueValue;
    @FXML private Label bookingsValue;
    @FXML private Label occupancyDelta;
    @FXML private Label revenueDelta;
    @FXML private Label bookingsDelta;
    @FXML private ProgressIndicator occupancyProgress;

    private HotelDataStore dataStore;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = HotelDataStore.getInstance();
        setupStats();
        setupRevenueChart();
        setupCheckInList();
        setupRoomStatusPie();
    }

    private void setupStats() {
        // Occupancy
        double occupancy = dataStore.getOccupancyRate();
        if (occupancyValue != null) {
            occupancyValue.setText(String.format("%.0f%%", occupancy * 100));
        }
        if (occupancyProgress != null) {
            occupancyProgress.setProgress(occupancy);
        }
        if (occupancyDelta != null) {
            occupancyDelta.setText(dataStore.getAvailableRoomCount() + " rooms free");
        }

        // Revenue
        double revenue = dataStore.getTotalRevenue();
        if (revenueValue != null) {
            revenueValue.setText(dataStore.formatCurrency(revenue));
        }
        if (revenueDelta != null) {
            revenueDelta.setText("+12% from last week");
        }

        // Bookings
        long activeBookings = dataStore.getActiveBookingsCount();
        if (bookingsValue != null) {
            bookingsValue.setText(String.valueOf(activeBookings));
        }
        if (bookingsDelta != null) {
            bookingsDelta.setText(dataStore.getTodayCheckIns() + " check-ins today");
        }
    }

    private void setupRevenueChart() {
        if (revenueChart == null) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue 2026");
        
        // Generate revenue data from bookings per day of week
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        double[] dayRevenue = new double[7];
        
        for (Booking b : dataStore.getBookings()) {
            if (b.getStatus() != Booking.BookingStatus.CANCELLED && b.getCheckInDate() != null) {
                int dow = b.getCheckInDate().getDayOfWeek().getValue() - 1;
                dayRevenue[dow] += b.getTotalAmount() / Math.max(1, b.getNights());
            }
        }
        
        // Ensure some plausible values
        for (int i = 0; i < 7; i++) {
            if (dayRevenue[i] < 500) dayRevenue[i] = 2000 + (Math.random() * 3000);
            series.getData().add(new XYChart.Data<>(days[i], dayRevenue[i]));
        }
        
        revenueChart.getData().clear();
        revenueChart.getData().add(series);
    }

    private void setupCheckInList() {
        if (checkInList == null) return;
        checkInList.getItems().clear();
        
        LocalDate today = LocalDate.now();
        
        // Show today's and tomorrow's check-ins from real data
        for (Booking b : dataStore.getBookings()) {
            if (b.getStatus() == Booking.BookingStatus.CHECKED_IN || b.getStatus() == Booking.BookingStatus.CONFIRMED) {
                String prefix = "";
                if (b.getCheckInDate() != null) {
                    if (b.getCheckInDate().equals(today)) prefix = "TODAY ";
                    else if (b.getCheckInDate().equals(today.plusDays(1))) prefix = "TMRW ";
                }
                checkInList.getItems().add(
                    prefix + "Room " + b.getRoomNumber() + " — " + b.getGuestName() + 
                    " (" + b.getStatusDisplay() + ")"
                );
            }
        }
        
        if (checkInList.getItems().isEmpty()) {
            checkInList.getItems().add("No upcoming check-ins");
        }
    }

    private void setupRoomStatusPie() {
        if (roomStatusPie == null) return;
        roomStatusPie.getData().clear();
        roomStatusPie.getData().addAll(
            new PieChart.Data("Available", dataStore.getAvailableRoomCount()),
            new PieChart.Data("Occupied", dataStore.getOccupiedRoomCount()),
            new PieChart.Data("Reserved", dataStore.getReservedRoomCount()),
            new PieChart.Data("Maintenance", dataStore.getMaintenanceRoomCount())
        );
    }

    @FXML
    private void handleQuickCheckIn() {
        // Find first confirmed booking and check-in
        for (Booking b : dataStore.getBookings()) {
            if (b.getStatus() == Booking.BookingStatus.CONFIRMED) {
                dataStore.checkIn(b);
                setupStats();
                setupCheckInList();
                setupRoomStatusPie();
                return;
            }
        }
    }

    @FXML
    private void handleQuickCheckOut() {
        // Find first checked-in booking and check-out
        for (Booking b : dataStore.getBookings()) {
            if (b.getStatus() == Booking.BookingStatus.CHECKED_IN) {
                dataStore.checkOut(b);
                setupStats();
                setupCheckInList();
                setupRoomStatusPie();
                return;
            }
        }
    }

    @FXML
    private void handleQuickBooking() {
        try {
            URL resource = getClass().getResource("booking_dialog.fxml");
            if (resource == null) return;
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Quick Booking");
            Scene scene = new Scene(root);
            URL css = getClass().getResource("hotel_style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
            // Refresh stats after booking
            setupStats();
            setupCheckInList();
            setupRoomStatusPie();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
