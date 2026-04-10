package com.zenith;

import com.zenith.model.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AnalyticsController implements Initializable {

    @FXML private Label totalRevenueLabel;
    @FXML private Label avgOccupancyLabel;
    @FXML private Label avgStayLabel;
    @FXML private Label totalBookingsLabel;
    @FXML private BarChart<String, Number> revenueByTypeChart;
    @FXML private PieChart roomTypeChart;
    @FXML private BarChart<String, Number> bookingVolumeChart;
    @FXML private PieChart statusChart;

    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, Integer> colBookingId;
    @FXML private TableColumn<Booking, String> colBookingGuest;
    @FXML private TableColumn<Booking, String> colBookingRoom;
    @FXML private TableColumn<Booking, LocalDate> colBookingCheckIn;
    @FXML private TableColumn<Booking, LocalDate> colBookingCheckOut;
    @FXML private TableColumn<Booking, String> colBookingStatus;
    @FXML private TableColumn<Booking, Double> colBookingAmount;

    private HotelDataStore dataStore;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = HotelDataStore.getInstance();
        populateStats();
        populateRevenueByTypeChart();
        populateRoomTypeChart();
        populateBookingVolumeChart();
        populateStatusChart();
        setupBookingTable();
    }

    private void populateStats() {
        double revenue = dataStore.getTotalRevenue();
        totalRevenueLabel.setText(dataStore.formatCurrency(revenue));

        double occupancy = dataStore.getOccupancyRate() * 100;
        avgOccupancyLabel.setText(String.format("%.0f%%", occupancy));

        double avgStay = dataStore.getAverageStayDuration();
        avgStayLabel.setText(String.format("%.1f nights", avgStay));

        long totalBookings = dataStore.getBookings().stream()
            .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED)
            .count();
        totalBookingsLabel.setText(String.valueOf(totalBookings));
    }

    private void populateRevenueByTypeChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");
        series.getData().add(new XYChart.Data<>("Standard", dataStore.getRevenueByRoomType(Room.RoomType.STANDARD)));
        series.getData().add(new XYChart.Data<>("Deluxe", dataStore.getRevenueByRoomType(Room.RoomType.DELUXE)));
        series.getData().add(new XYChart.Data<>("Suite", dataStore.getRevenueByRoomType(Room.RoomType.SUITE)));
        series.getData().add(new XYChart.Data<>("Penthouse", dataStore.getRevenueByRoomType(Room.RoomType.PENTHOUSE)));
        revenueByTypeChart.getData().clear();
        revenueByTypeChart.getData().add(series);
    }

    private void populateRoomTypeChart() {
        long std = dataStore.getRooms().stream().filter(r -> r.getType() == Room.RoomType.STANDARD).count();
        long dlx = dataStore.getRooms().stream().filter(r -> r.getType() == Room.RoomType.DELUXE).count();
        long ste = dataStore.getRooms().stream().filter(r -> r.getType() == Room.RoomType.SUITE).count();
        long pnt = dataStore.getRooms().stream().filter(r -> r.getType() == Room.RoomType.PENTHOUSE).count();

        roomTypeChart.getData().clear();
        roomTypeChart.getData().addAll(
            new PieChart.Data("Standard (" + std + ")", std),
            new PieChart.Data("Deluxe (" + dlx + ")", dlx),
            new PieChart.Data("Suite (" + ste + ")", ste),
            new PieChart.Data("Penthouse (" + pnt + ")", pnt)
        );
    }

    private void populateBookingVolumeChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Bookings");
        series.getData().add(new XYChart.Data<>("Standard", dataStore.getBookingCountByRoomType(Room.RoomType.STANDARD)));
        series.getData().add(new XYChart.Data<>("Deluxe", dataStore.getBookingCountByRoomType(Room.RoomType.DELUXE)));
        series.getData().add(new XYChart.Data<>("Suite", dataStore.getBookingCountByRoomType(Room.RoomType.SUITE)));
        series.getData().add(new XYChart.Data<>("Penthouse", dataStore.getBookingCountByRoomType(Room.RoomType.PENTHOUSE)));
        bookingVolumeChart.getData().clear();
        bookingVolumeChart.getData().add(series);
    }

    private void populateStatusChart() {
        statusChart.getData().clear();
        statusChart.getData().addAll(
            new PieChart.Data("Available (" + dataStore.getAvailableRoomCount() + ")", dataStore.getAvailableRoomCount()),
            new PieChart.Data("Occupied (" + dataStore.getOccupiedRoomCount() + ")", dataStore.getOccupiedRoomCount()),
            new PieChart.Data("Reserved (" + dataStore.getReservedRoomCount() + ")", dataStore.getReservedRoomCount()),
            new PieChart.Data("Maintenance (" + dataStore.getMaintenanceRoomCount() + ")", dataStore.getMaintenanceRoomCount())
        );
    }

    private void setupBookingTable() {
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBookingGuest.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getGuestName()));
        colBookingRoom.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getRoomNumber()));
        colBookingCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        colBookingCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        colBookingStatus.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatusDisplay()));
        colBookingAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colBookingAmount.setCellFactory(col -> new TableCell<Booking, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dataStore.formatCurrency(item));
                }
            }
        });
        bookingTable.setItems(dataStore.getBookings());
    }
}
