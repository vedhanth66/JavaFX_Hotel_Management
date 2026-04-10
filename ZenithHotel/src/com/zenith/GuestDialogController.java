package com.zenith;

import com.zenith.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class GuestDialogController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField nationalityField;
    @FXML private CheckBox vipCheckBox;
    @FXML private Label errorLabel;

    private Guest editingGuest;
    private Guest result;
    private boolean saved = false;

    public void setGuest(Guest guest) {
        this.editingGuest = guest;
        nameField.setText(guest.getFullName());
        emailField.setText(guest.getEmail());
        phoneField.setText(guest.getPhone());
        nationalityField.setText(guest.getNationality());
        vipCheckBox.setSelected(guest.isVip());
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty()) {
            errorLabel.setText("Name is required");
            return;
        }
        if (email.isEmpty() || !email.contains("@")) {
            errorLabel.setText("Valid email is required");
            return;
        }

        if (editingGuest != null) {
            // Update existing
            editingGuest.setFullName(name);
            editingGuest.setEmail(email);
            editingGuest.setPhone(phoneField.getText().trim());
            editingGuest.setNationality(nationalityField.getText().trim());
            editingGuest.setVip(vipCheckBox.isSelected());
            result = editingGuest;
            ActivityLog.getInstance().log("Guest updated: " + name);
        } else {
            // Create new
            result = HotelDataStore.getInstance().addGuest(
                name, email,
                phoneField.getText().trim(),
                nationalityField.getText().trim(),
                vipCheckBox.isSelected()
            );
        }
        saved = true;
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        saved = false;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    public boolean isSaved() { return saved; }
    public Guest getResult() { return result; }
}
