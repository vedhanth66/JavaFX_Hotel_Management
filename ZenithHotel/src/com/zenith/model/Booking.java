package com.zenith.model;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Booking {

    public enum BookingStatus { CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED }

    private static int nextId = 5000;

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final ObjectProperty<Guest> guest = new SimpleObjectProperty<>();
    private final ObjectProperty<Room> room = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> checkInDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> checkOutDate = new SimpleObjectProperty<>();
    private final ObjectProperty<BookingStatus> status = new SimpleObjectProperty<>(BookingStatus.CONFIRMED);
    private final DoubleProperty totalAmount = new SimpleDoubleProperty();
    private final StringProperty notes = new SimpleStringProperty("");

    public Booking(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut) {
        this.id.set(nextId++);
        this.guest.set(guest);
        this.room.set(room);
        this.checkInDate.set(checkIn);
        this.checkOutDate.set(checkOut);
        calculateTotal();
    }

    public void calculateTotal() {
        if (room.get() != null && checkInDate.get() != null && checkOutDate.get() != null) {
            long nights = ChronoUnit.DAYS.between(checkInDate.get(), checkOutDate.get());
            if (nights < 1) nights = 1;
            totalAmount.set(nights * room.get().getPricePerNight());
        }
    }

    public long getNights() {
        if (checkInDate.get() != null && checkOutDate.get() != null) {
            long n = ChronoUnit.DAYS.between(checkInDate.get(), checkOutDate.get());
            return n < 1 ? 1 : n;
        }
        return 0;
    }

    // ID
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    // Guest
    public Guest getGuest() { return guest.get(); }
    public void setGuest(Guest value) { guest.set(value); }
    public ObjectProperty<Guest> guestProperty() { return guest; }

    // Room
    public Room getRoom() { return room.get(); }
    public void setRoom(Room value) { room.set(value); }
    public ObjectProperty<Room> roomProperty() { return room; }

    // Check-in
    public LocalDate getCheckInDate() { return checkInDate.get(); }
    public void setCheckInDate(LocalDate value) { checkInDate.set(value); }
    public ObjectProperty<LocalDate> checkInDateProperty() { return checkInDate; }

    // Check-out
    public LocalDate getCheckOutDate() { return checkOutDate.get(); }
    public void setCheckOutDate(LocalDate value) { checkOutDate.set(value); }
    public ObjectProperty<LocalDate> checkOutDateProperty() { return checkOutDate; }

    // Status
    public BookingStatus getStatus() { return status.get(); }
    public void setStatus(BookingStatus value) { status.set(value); }
    public ObjectProperty<BookingStatus> statusProperty() { return status; }

    // Total
    public double getTotalAmount() { return totalAmount.get(); }
    public void setTotalAmount(double value) { totalAmount.set(value); }
    public DoubleProperty totalAmountProperty() { return totalAmount; }

    // Notes
    public String getNotes() { return notes.get(); }
    public void setNotes(String value) { notes.set(value); }
    public StringProperty notesProperty() { return notes; }

    public String getStatusDisplay() {
        switch (status.get()) {
            case CONFIRMED: return "Confirmed";
            case CHECKED_IN: return "Checked In";
            case CHECKED_OUT: return "Checked Out";
            case CANCELLED: return "Cancelled";
            default: return "Unknown";
        }
    }

    public String getGuestName() {
        return guest.get() != null ? guest.get().getFullName() : "N/A";
    }

    public String getRoomNumber() {
        return room.get() != null ? room.get().getRoomNumber() : "N/A";
    }

    @Override
    public String toString() {
        return "Booking #" + id.get() + " - " + getGuestName() + " in Room " + getRoomNumber();
    }
}
