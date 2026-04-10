package com.zenith.model;

import javafx.beans.property.*;

public class Guest {

    private static int nextId = 1000;

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty fullName = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty nationality = new SimpleStringProperty();
    private final BooleanProperty vip = new SimpleBooleanProperty(false);
    private final IntegerProperty totalStays = new SimpleIntegerProperty(0);
    private final DoubleProperty totalSpent = new SimpleDoubleProperty(0.0);

    public Guest(String fullName, String email, String phone, String nationality) {
        this.id.set(nextId++);
        this.fullName.set(fullName);
        this.email.set(email);
        this.phone.set(phone);
        this.nationality.set(nationality);
    }

    public Guest(String fullName, String email, String phone, String nationality, boolean vip, int totalStays, double totalSpent) {
        this(fullName, email, phone, nationality);
        this.vip.set(vip);
        this.totalStays.set(totalStays);
        this.totalSpent.set(totalSpent);
    }

    // ID
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    // Full Name
    public String getFullName() { return fullName.get(); }
    public void setFullName(String value) { fullName.set(value); }
    public StringProperty fullNameProperty() { return fullName; }

    // Email
    public String getEmail() { return email.get(); }
    public void setEmail(String value) { email.set(value); }
    public StringProperty emailProperty() { return email; }

    // Phone
    public String getPhone() { return phone.get(); }
    public void setPhone(String value) { phone.set(value); }
    public StringProperty phoneProperty() { return phone; }

    // Nationality
    public String getNationality() { return nationality.get(); }
    public void setNationality(String value) { nationality.set(value); }
    public StringProperty nationalityProperty() { return nationality; }

    // VIP
    public boolean isVip() { return vip.get(); }
    public void setVip(boolean value) { vip.set(value); }
    public BooleanProperty vipProperty() { return vip; }

    // Total Stays
    public int getTotalStays() { return totalStays.get(); }
    public void setTotalStays(int value) { totalStays.set(value); }
    public IntegerProperty totalStaysProperty() { return totalStays; }

    // Total Spent
    public double getTotalSpent() { return totalSpent.get(); }
    public void setTotalSpent(double value) { totalSpent.set(value); }
    public DoubleProperty totalSpentProperty() { return totalSpent; }

    public String getVipDisplay() { return vip.get() ? "⭐ VIP" : "Regular"; }

    @Override
    public String toString() {
        return fullName.get();
    }
}
