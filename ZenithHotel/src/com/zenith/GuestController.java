package com.zenith;

import com.zenith.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class GuestController implements Initializable {

    @FXML private TableView<Guest> guestTable;
    @FXML private TableColumn<Guest, Integer> colId;
    @FXML private TableColumn<Guest, String> colName;
    @FXML private TableColumn<Guest, String> colEmail;
    @FXML private TableColumn<Guest, String> colPhone;
    @FXML private TableColumn<Guest, String> colNationality;
    @FXML private TableColumn<Guest, String> colVip;
    @FXML private TableColumn<Guest, Integer> colStays;
    @FXML private TableColumn<Guest, Double> colSpent;
    @FXML private TextField searchField;
    @FXML private Label totalGuestsLabel;
    @FXML private Label vipGuestsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label statusLabel;
    @FXML private Button btnEditGuest;
    @FXML private Button btnDeleteGuest;

    private HotelDataStore dataStore;
    private FilteredList<Guest> filteredGuests;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = HotelDataStore.getInstance();
        setupTable();
        setupSearch();
        updateStats();
        
        // Disable edit/delete when nothing selected
        btnEditGuest.setDisable(true);
        btnDeleteGuest.setDisable(true);
        guestTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            btnEditGuest.setDisable(selected == null);
            btnDeleteGuest.setDisable(selected == null);
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colNationality.setCellValueFactory(new PropertyValueFactory<>("nationality"));
        colStays.setCellValueFactory(new PropertyValueFactory<>("totalStays"));

        // VIP column with custom display
        colVip.setCellValueFactory(data -> {
            return new javafx.beans.property.SimpleStringProperty(data.getValue().getVipDisplay());
        });

        // Total Spent with formatting
        colSpent.setCellValueFactory(new PropertyValueFactory<>("totalSpent"));
        colSpent.setCellFactory(col -> new TableCell<Guest, Double>() {
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

        filteredGuests = new FilteredList<>(dataStore.getGuests(), p -> true);
        guestTable.setItems(filteredGuests);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredGuests.setPredicate(guest -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return guest.getFullName().toLowerCase().contains(lower) ||
                       guest.getEmail().toLowerCase().contains(lower) ||
                       guest.getPhone().toLowerCase().contains(lower) ||
                       guest.getNationality().toLowerCase().contains(lower);
            });
            updateStats();
        });
    }

    private void updateStats() {
        totalGuestsLabel.setText(String.valueOf(dataStore.getGuests().size()));
        long vipCount = dataStore.getGuests().stream().filter(Guest::isVip).count();
        vipGuestsLabel.setText(String.valueOf(vipCount));
        double totalSpent = dataStore.getGuests().stream().mapToDouble(Guest::getTotalSpent).sum();
        totalRevenueLabel.setText(dataStore.formatCurrency(totalSpent));
    }

    @FXML
    private void handleAddGuest() {
        Guest result = showGuestDialog(null);
        if (result != null) {
            updateStats();
            statusLabel.setText("Guest '" + result.getFullName() + "' added successfully");
        }
    }

    @FXML
    private void handleEditGuest() {
        Guest selected = guestTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Guest result = showGuestDialog(selected);
            if (result != null) {
                guestTable.refresh();
                updateStats();
                statusLabel.setText("Guest '" + result.getFullName() + "' updated");
            }
        }
    }

    @FXML
    private void handleDeleteGuest() {
        Guest selected = guestTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Guest");
            confirm.setHeaderText("Remove " + selected.getFullName() + "?");
            confirm.setContentText("This action cannot be undone.");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                dataStore.removeGuest(selected);
                updateStats();
                statusLabel.setText("Guest removed");
            }
        }
    }

    private Guest showGuestDialog(Guest existingGuest) {
        try {
            URL resource = getClass().getResource("guest_dialog.fxml");
            if (resource == null) return null;

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            GuestDialogController controller = loader.getController();
            if (existingGuest != null) {
                controller.setGuest(existingGuest);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(existingGuest != null ? "Edit Guest" : "Add New Guest");
            Scene scene = new Scene(root);
            URL css = getClass().getResource("hotel_style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

            if (controller.isSaved()) {
                return controller.getResult();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
