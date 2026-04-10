package com.zenith.model;

import javafx.beans.property.*;

public class Room {

    public enum RoomType { STANDARD, DELUXE, SUITE, PENTHOUSE }
    public enum RoomStatus { AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE }

    private final StringProperty roomNumber = new SimpleStringProperty();
    private final ObjectProperty<RoomType> type = new SimpleObjectProperty<>();
    private final IntegerProperty floor = new SimpleIntegerProperty();
    private final DoubleProperty pricePerNight = new SimpleDoubleProperty();
    private final ObjectProperty<RoomStatus> status = new SimpleObjectProperty<>(RoomStatus.AVAILABLE);
    private final StringProperty currentGuestName = new SimpleStringProperty("");
    private final IntegerProperty capacity = new SimpleIntegerProperty(2);

    public Room(String roomNumber, RoomType type, int floor, double pricePerNight) {
        this.roomNumber.set(roomNumber);
        this.type.set(type);
        this.floor.set(floor);
        this.pricePerNight.set(pricePerNight);
    }

    // Room Number
    public String getRoomNumber() { return roomNumber.get(); }
    public void setRoomNumber(String value) { roomNumber.set(value); }
    public StringProperty roomNumberProperty() { return roomNumber; }

    // Type
    public RoomType getType() { return type.get(); }
    public void setType(RoomType value) { type.set(value); }
    public ObjectProperty<RoomType> typeProperty() { return type; }

    // Floor
    public int getFloor() { return floor.get(); }
    public void setFloor(int value) { floor.set(value); }
    public IntegerProperty floorProperty() { return floor; }

    // Price
    public double getPricePerNight() { return pricePerNight.get(); }
    public void setPricePerNight(double value) { pricePerNight.set(value); }
    public DoubleProperty pricePerNightProperty() { return pricePerNight; }

    // Status
    public RoomStatus getStatus() { return status.get(); }
    public void setStatus(RoomStatus value) { status.set(value); }
    public ObjectProperty<RoomStatus> statusProperty() { return status; }

    // Current Guest
    public String getCurrentGuestName() { return currentGuestName.get(); }
    public void setCurrentGuestName(String value) { currentGuestName.set(value); }
    public StringProperty currentGuestNameProperty() { return currentGuestName; }

    // Capacity
    public int getCapacity() { return capacity.get(); }
    public void setCapacity(int value) { capacity.set(value); }
    public IntegerProperty capacityProperty() { return capacity; }

    public String getTypeDisplay() {
        switch (type.get()) {
            case STANDARD: return "Standard";
            case DELUXE: return "Deluxe";
            case SUITE: return "Suite";
            case PENTHOUSE: return "Penthouse";
            default: return "Unknown";
        }
    }

    public String getStatusDisplay() {
        switch (status.get()) {
            case AVAILABLE: return "Available";
            case OCCUPIED: return "Occupied";
            case RESERVED: return "Reserved";
            case MAINTENANCE: return "Maintenance";
            default: return "Unknown";
        }
    }

    @Override
    public String toString() {
        return "Room " + roomNumber.get() + " (" + getTypeDisplay() + ")";
    }
}
